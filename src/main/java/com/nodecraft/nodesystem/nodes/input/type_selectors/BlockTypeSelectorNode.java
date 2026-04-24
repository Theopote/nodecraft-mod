package com.nodecraft.nodesystem.nodes.input.type_selectors;

import com.nodecraft.core.NodeCraft;
import com.nodecraft.gui.editor.impl.BaseCustomUINode;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.api.NodeProperty;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@NodeInfo(
    id = "input.type_selectors.block_type_selector",
    displayName = "Block Type Selector",
    description = "Searches and selects a Minecraft block type.",
    category = "input.type_selectors",
    order = 0
)
public class BlockTypeSelectorNode extends BaseCustomUINode {

    private static final String[] QUICK_BLOCKS = {
            "minecraft:stone",
            "minecraft:cobblestone",
            "minecraft:stone_bricks",
            "minecraft:polished_andesite",
            "minecraft:smooth_stone",
            "minecraft:glass",
            "minecraft:oak_planks",
            "minecraft:quartz_block"
    };
    private static final int POPUP_PAGE_SIZE = 18;
    private static final float POPUP_WIDTH = 400.0f;
    private static final float POPUP_HEIGHT = 500.0f;
    private static final String BLOCK_PICKER_POPUP_KEY = "block_picker";

    @NodeProperty(
        displayName = "Selected Block",
        category = "Selection",
        order = 1,
        description = "The currently selected block ID."
    )
    private String selectedBlock = "minecraft:stone";

    @NodeProperty(
        displayName = "Allow Modded Blocks",
        category = "Filter",
        order = 2,
        description = "Whether block IDs outside the minecraft namespace should appear in search results."
    )
    private boolean allowModded = true;

    private static final String OUTPUT_BLOCK_ID = "output_block_id";
    private static final String OUTPUT_NAMESPACE = "output_namespace";
    private static final String OUTPUT_BLOCK_PATH = "output_block_path";
    private static final String OUTPUT_IS_MODDED = "output_is_modded";

    private transient ImString searchBuffer = new ImString(256);
    private transient volatile List<String> allBlocks = new ArrayList<>();
    private transient volatile List<String> filteredBlocks = new ArrayList<>();
    private transient volatile boolean minecraftOnly = false;
    private transient volatile int currentPage = 0;
    private transient volatile boolean blockRegistryReady = true;
    private transient volatile boolean registryErrorLogged = false;

    public BlockTypeSelectorNode() {
        super(UUID.randomUUID(), "input.type_selectors.block_type_selector");

        addOutputPort(new BasePort(OUTPUT_BLOCK_ID, "Block ID", "The selected block's full identifier", NodeDataType.STRING, this));
        addOutputPort(new BasePort(OUTPUT_NAMESPACE, "Namespace", "The namespace part of the selected block ID", NodeDataType.STRING, this));
        addOutputPort(new BasePort(OUTPUT_BLOCK_PATH, "Block Path", "The path part of the selected block ID", NodeDataType.STRING, this));
        addOutputPort(new BasePort(OUTPUT_IS_MODDED, "Is Modded", "Whether the selected block is outside the minecraft namespace", NodeDataType.BOOLEAN, this));

        updateOutputs();
    }

    @Override
    public String getDescription() {
        return "Searches and selects a Minecraft block type.";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        updateOutputs();
    }

    @Override
    protected float calculateUIHeight() {
        float height = getMediumPadding();
        height += ImGui.getTextLineHeight();
        height += getSmallPadding();
        height += ImGui.getFrameHeight();
        height += getMediumPadding();
        return height;
    }

    @Override
    protected float calculateMinUIWidth() {
        return 184f + getContentMargin();
    }

    @Override
    protected boolean renderCustomUIScaled(float width, float height, float zoom) {
        return layout(zoom, layout -> {
            boolean changed = false;

            try {
                float availableWidth = getAvailableContentWidth(width, zoom);
                layout.addVerticalSpacing(getMediumPadding());

                layout.pushFramePadding(4.0f, 3.0f);
                layout.setItemWidth(availableWidth / zoom);
                try {
                    String compactLabel = buildCompactLabel();
                    if (ImGui.button(compactLabel + "##open_block_picker", availableWidth / zoom, 0)) {
                        ensureBlockCatalogReady();
                        updateFilteredList(searchBuffer.get());
                        openScopedPopup(BLOCK_PICKER_POPUP_KEY);
                    }
                } finally {
                    layout.popItemWidth();
                    layout.popStyleVar();
                }

                if (renderBlockPickerPopup()) {
                    changed = true;
                }

                layout.addVerticalSpacing(getMediumPadding());
            } catch (Exception e) {
                System.err.println("BlockTypeSelectorNode UI render failed: " + e.getMessage());
            }

            return changed;
        });
    }

    private boolean renderBlockPickerPopup() {
        boolean changed = false;
        ImGui.setNextWindowSize(POPUP_WIDTH, POPUP_HEIGHT, imgui.flag.ImGuiCond.FirstUseEver);
        if (!beginScopedPopup(BLOCK_PICKER_POPUP_KEY)) {
            return false;
        }

        try {
            ImGui.text("Select Block");
            ImGui.separator();

            if (ImGui.inputTextWithHint("##block_picker_search", "Search block id...", searchBuffer, ImGuiInputTextFlags.None)) {
                updateFilteredList(searchBuffer.get());
            }

            ImGui.sameLine();
            if (ImGui.smallButton("All##scope_all")) {
                minecraftOnly = false;
                updateFilteredList(searchBuffer.get());
            }
            ImGui.sameLine();
            if (ImGui.smallButton("Minecraft##scope_vanilla")) {
                minecraftOnly = true;
                updateFilteredList(searchBuffer.get());
            }

            ImGui.separator();
            for (int i = 0; i < QUICK_BLOCKS.length; i++) {
                String quickBlock = QUICK_BLOCKS[i];
                String quickLabel = quickBlock.split(":", 2)[1];
                if (i > 0 && i % 4 != 0) {
                    ImGui.sameLine();
                }
                if (ImGui.smallButton(quickLabel + "##quick_" + i)) {
                    setSelectedBlock(quickBlock);
                    changed = true;
                }
            }

            ImGui.separator();
            List<String> snapshot = filteredBlocks;
            int total = snapshot.size();
            int totalPages = Math.max(1, (int) Math.ceil(total / (float) POPUP_PAGE_SIZE));
            if (currentPage >= totalPages) {
                currentPage = totalPages - 1;
            }
            int start = currentPage * POPUP_PAGE_SIZE;
            int end = Math.min(start + POPUP_PAGE_SIZE, total);

            ImGui.text(String.format("Results: %d", total));
            if (ImGui.beginChild("##block_picker_list", 0, -ImGui.getFrameHeightWithSpacing() * 2, true, ImGuiWindowFlags.AlwaysVerticalScrollbar)) {
                if (snapshot.isEmpty()) {
                    if (!blockRegistryReady) {
                        ImGui.textDisabled("Block registry not ready");
                    } else {
                        ImGui.textDisabled("No blocks found");
                    }
                } else {
                    for (int i = start; i < end; i++) {
                        String blockId = snapshot.get(i);
                        boolean isSelected = blockId.equals(selectedBlock);
                        String selectableLabel = buildSelectableLabel(blockId);
                        if (ImGui.selectable(selectableLabel + "##block_" + i, isSelected)) {
                            setSelectedBlock(blockId);
                            changed = true;
                        }
                        if (ImGui.isItemHovered()) {
                            ImGui.setTooltip(blockId);
                        }
                    }
                }
                ImGui.endChild();
            }

            boolean canPrev = currentPage > 0;
            boolean canNext = currentPage + 1 < totalPages;
            if (!canPrev) ImGui.beginDisabled();
            if (ImGui.button("< Prev##block_page_prev")) {
                currentPage--;
            }
            if (!canPrev) ImGui.endDisabled();

            ImGui.sameLine();
            ImGui.text(String.format("Page %d / %d", currentPage + 1, totalPages));
            ImGui.sameLine();

            if (!canNext) ImGui.beginDisabled();
            if (ImGui.button("Next >##block_page_next")) {
                currentPage++;
            }
            if (!canNext) ImGui.endDisabled();

            ImGui.sameLine();
            if (ImGui.button("Close##close_block_picker")) {
                ImGui.closeCurrentPopup();
            }
        } finally {
            endScopedPopup();
        }
        return changed;
    }

    private String buildCompactLabel() {
        String display = selectedBlock;
        if (display.length() > 26) {
            display = display.substring(0, 23) + "...";
        }
        return "[" + display + " v]";
    }

    private String buildSelectableLabel(String blockId) {
        String[] parts = blockId.split(":", 2);
        if (parts.length < 2) {
            return blockId;
        }
        String prefix = "minecraft".equals(parts[0]) ? "[MC]" : "[MOD]";
        return prefix + " " + parts[1];
    }

    private void ensureBlockCatalogReady() {
        if (!allBlocks.isEmpty()) {
            blockRegistryReady = true;
            return;
        }
        List<String> collected = new ArrayList<>();
        try {
            for (Identifier id : Registries.BLOCK.getIds()) {
                collected.add(id.toString());
            }
            collected.sort(Comparator.naturalOrder());
            blockRegistryReady = !collected.isEmpty();
            if (blockRegistryReady) {
                registryErrorLogged = false;
            }
        } catch (Exception ignored) {
            blockRegistryReady = false;
            if (!registryErrorLogged) {
                NodeCraft.LOGGER.warn("Block registry is not ready for BlockTypeSelectorNode yet.");
                registryErrorLogged = true;
            }
        }
        allBlocks = collected;
    }

    private void updateFilteredList(String searchTextRaw) {
        ensureBlockCatalogReady();

        String searchText = searchTextRaw == null ? "" : searchTextRaw.trim().toLowerCase(Locale.ROOT);
        List<String> nextFilteredBlocks = new ArrayList<>();

        List<String> source = allBlocks;
        for (String fullId : source) {
            boolean isMinecraft = fullId.startsWith("minecraft:");
            if (!allowModded && !isMinecraft) {
                continue;
            }
            if (minecraftOnly && !isMinecraft) {
                continue;
            }
            if (searchText.isEmpty() || fullId.toLowerCase(Locale.ROOT).contains(searchText)) {
                nextFilteredBlocks.add(fullId);
            }
        }

        for (String quickBlock : QUICK_BLOCKS) {
            if (!nextFilteredBlocks.contains(quickBlock)) {
                boolean isMinecraft = quickBlock.startsWith("minecraft:");
                if (!allowModded && !isMinecraft) {
                    continue;
                }
                if (minecraftOnly && !isMinecraft) {
                    continue;
                }
                if (searchText.isEmpty() || quickBlock.toLowerCase(Locale.ROOT).contains(searchText)) {
                    nextFilteredBlocks.add(quickBlock);
                }
            }
        }

        currentPage = 0;
        filteredBlocks = nextFilteredBlocks;
    }

    public void setSelectedBlock(String blockId) {
        String nextBlockId = sanitizeBlockId(blockId);

        if (!allowModded && !nextBlockId.startsWith("minecraft:")) {
            nextBlockId = "minecraft:stone";
        }

        if (!isKnownBlockId(nextBlockId)) {
            nextBlockId = "minecraft:stone";
        }

        if (!this.selectedBlock.equals(nextBlockId)) {
            this.selectedBlock = nextBlockId;
            updateOutputs();
            markDirty();
        }
    }

    private String sanitizeBlockId(String blockId) {
        if (blockId == null) {
            return "minecraft:stone";
        }
        String normalized = blockId.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return "minecraft:stone";
        }
        if (!normalized.contains(":")) {
            normalized = "minecraft:" + normalized;
        }
        return normalized;
    }

    private boolean isKnownBlockId(String blockId) {
        if ("minecraft:stone".equals(blockId)) {
            return true;
        }

        ensureBlockCatalogReady();
        if (!allBlocks.isEmpty()) {
            return allBlocks.contains(blockId);
        }

        try {
            Identifier id = Identifier.tryParse(blockId);
            if (id == null) {
                return false;
            }

            // 注册表可能在极早期尚未完全可用，此时不强制判定为未知，避免误降级。
            if (Registries.BLOCK.getIds().isEmpty()) {
                return true;
            }
            return Registries.BLOCK.containsId(id);
        } catch (Exception ignored) {
            return false;
        }
    }

    private void updateOutputs() {
        String namespace = "minecraft";
        String path = "stone";
        if (selectedBlock.contains(":")) {
            String[] parts = selectedBlock.split(":", 2);
            namespace = parts[0];
            path = parts[1];
        }
        outputValues.put(OUTPUT_BLOCK_ID, selectedBlock);
        outputValues.put(OUTPUT_NAMESPACE, namespace);
        outputValues.put(OUTPUT_BLOCK_PATH, path);
        outputValues.put(OUTPUT_IS_MODDED, !namespace.equals("minecraft"));
        syncOutputPorts();
    }

    public String getSelectedBlock() {
        return selectedBlock;
    }

    public boolean isAllowModded() {
        return allowModded;
    }

    public void setAllowModded(boolean allowModded) {
        this.allowModded = allowModded;
        if (!allowModded && !selectedBlock.startsWith("minecraft:")) {
            setSelectedBlock("minecraft:stone");
        }
        updateFilteredList(searchBuffer.get());
    }

    @Override
    public Object getNodeState() {
        return Map.of(
            "selectedBlock", getSelectedBlock(),
            "allowModded", isAllowModded()
        );
    }

    @Override
    public void setNodeState(Object state) {
        if (state instanceof Map<?, ?> map) {
            if (map.get("allowModded") instanceof Boolean bool) {
                setAllowModded(bool);
            }
            if (map.get("selectedBlock") instanceof String value) {
                setSelectedBlock(value);
            }
        }
    }
}
