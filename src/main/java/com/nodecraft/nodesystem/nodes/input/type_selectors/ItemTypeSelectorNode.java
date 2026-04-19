package com.nodecraft.nodesystem.nodes.input.type_selectors;

import com.nodecraft.gui.editor.impl.BaseCustomUINode;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.api.NodeProperty;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.type.ImString;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@NodeInfo(
    id = "input.type_selectors.item_type_selector",
    displayName = "Item Type Selector",
    description = "Searches and selects a Minecraft item type.",
    category = "input.type_selectors",
    order = 2
)
public class ItemTypeSelectorNode extends BaseCustomUINode {

    private static final String[] QUICK_ITEMS = {
        "minecraft:stone",
        "minecraft:oak_planks",
        "minecraft:glass",
        "minecraft:torch",
        "minecraft:bucket",
        "minecraft:water_bucket",
        "minecraft:minecart",
        "minecraft:armor_stand"
    };

    @NodeProperty(
        displayName = "Selected Item",
        category = "Selection",
        order = 1,
        description = "The currently selected item id."
    )
    private String selectedItem = "minecraft:stone";

    @NodeProperty(
        displayName = "Allow Modded Items",
        category = "Filter",
        order = 2,
        description = "Whether item ids outside the minecraft namespace should appear in search results."
    )
    private boolean allowModded = true;

    private static final String OUTPUT_ITEM_ID = "output_item_id";
    private static final String OUTPUT_NAMESPACE = "output_namespace";
    private static final String OUTPUT_ITEM_PATH = "output_item_path";
    private static final String OUTPUT_IS_MODDED = "output_is_modded";

    private transient ImString searchBuffer = new ImString(256);
    private transient volatile List<String> filteredItems = new ArrayList<>();
    private transient volatile boolean showDropdown = false;
    private transient volatile String lastSearchText = "";

    private static final int MAX_RESULTS = 20;

    public ItemTypeSelectorNode() {
        super(UUID.randomUUID(), "input.type_selectors.item_type_selector");

        addOutputPort(new BasePort(OUTPUT_ITEM_ID, "Item Type", "The selected item's full identifier", NodeDataType.ITEM_TYPE, this));
        addOutputPort(new BasePort(OUTPUT_NAMESPACE, "Namespace", "The namespace part of the selected item id", NodeDataType.STRING, this));
        addOutputPort(new BasePort(OUTPUT_ITEM_PATH, "Item Path", "The path part of the selected item id", NodeDataType.STRING, this));
        addOutputPort(new BasePort(OUTPUT_IS_MODDED, "Is Modded", "Whether the selected item is outside the minecraft namespace", NodeDataType.BOOLEAN, this));

        updateOutputs();
    }

    @Override
    public String getDescription() {
        return "Searches and selects a Minecraft item type.";
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
        height += getSmallPadding();
        if (showDropdown) {
            height += Math.min(filteredItems.size(), MAX_RESULTS) * ImGui.getTextLineHeightWithSpacing();
            height += getSmallPadding();
        }
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

                ImGui.text("Common:");
                for (int i = 0; i < QUICK_ITEMS.length; i++) {
                    String quickItem = QUICK_ITEMS[i];
                    String quickLabel = quickItem.split(":", 2)[1];
                    if (i > 0 && i % 2 != 0) {
                        ImGui.sameLine();
                    }
                    if (ImGui.smallButton(quickLabel + "##quick_item_" + i)) {
                        setSelectedItem(quickItem);
                        searchBuffer.set("");
                        filteredItems = new ArrayList<>();
                        showDropdown = false;
                        lastSearchText = "";
                        changed = true;
                    }
                }

                layout.addVerticalSpacing(getSmallPadding());

                layout.pushFramePadding(4.0f, 3.0f);
                layout.setItemWidth(availableWidth / zoom);

                if (ImGui.inputTextWithHint("##item_search", "Search items...", searchBuffer)) {
                    String searchText = searchBuffer.get().trim().toLowerCase();
                    if (!searchText.equals(lastSearchText)) {
                        lastSearchText = searchText;
                        updateFilteredList(searchText);
                        showDropdown = !searchText.isEmpty();
                    }
                }

                if (ImGui.isItemActivated() && !searchBuffer.get().isEmpty()) {
                    showDropdown = true;
                }

                layout.popItemWidth();
                layout.popStyleVar();

                layout.addVerticalSpacing(getSmallPadding());

                List<String> filteredSnapshot = filteredItems;
                if (showDropdown && !filteredSnapshot.isEmpty()) {
                    ImGui.pushStyleColor(ImGuiCol.ChildBg, 0.15f, 0.15f, 0.18f, 0.95f);

                    int displayCount = Math.min(filteredSnapshot.size(), MAX_RESULTS);
                    for (int i = 0; i < displayCount; i++) {
                        String itemId = filteredSnapshot.get(i);
                        String itemPath = itemId.contains(":") ? itemId.split(":", 2)[1] : itemId;

                        boolean isSelected = itemId.equals(selectedItem);
                        if (isSelected) {
                            ImGui.pushStyleColor(ImGuiCol.Text, 0.3f, 0.9f, 0.5f, 1.0f);
                        }

                        if (ImGui.selectable("  " + itemPath + "##item_" + i, isSelected)) {
                            setSelectedItem(itemId);
                            searchBuffer.set("");
                            showDropdown = false;
                            lastSearchText = "";
                            changed = true;
                        }

                        if (isSelected) {
                            ImGui.popStyleColor();
                        }

                        if (ImGui.isItemHovered()) {
                            ImGui.setTooltip(itemId);
                        }
                    }

                    if (filteredSnapshot.size() > MAX_RESULTS) {
                        ImGui.pushStyleColor(ImGuiCol.Text, 0.5f, 0.5f, 0.5f, 1.0f);
                        ImGui.text("  ... " + (filteredSnapshot.size() - MAX_RESULTS) + " more");
                        ImGui.popStyleColor();
                    }

                    ImGui.popStyleColor();
                }

                layout.addVerticalSpacing(getMediumPadding());
            } catch (Exception e) {
                System.err.println("ItemTypeSelectorNode UI render failed: " + e.getMessage());
            }

            return changed;
        });
    }

    private void updateFilteredList(String searchText) {
        List<String> nextFilteredItems = new ArrayList<>();
        if (searchText.isEmpty()) {
            for (String quickItem : QUICK_ITEMS) {
                if (allowModded || quickItem.startsWith("minecraft:")) {
                    nextFilteredItems.add(quickItem);
                }
            }
            filteredItems = nextFilteredItems;
            invalidateCache();
            return;
        }

        try {
            for (Identifier id : Registries.ITEM.getIds()) {
                String fullId = id.toString();
                if (!allowModded && !id.getNamespace().equals("minecraft")) {
                    continue;
                }

                if (fullId.contains(searchText) || id.getPath().contains(searchText)) {
                    nextFilteredItems.add(fullId);
                    if (nextFilteredItems.size() >= MAX_RESULTS * 2) {
                        break;
                    }
                }
            }
        } catch (Exception ignored) {
        }

        filteredItems = nextFilteredItems;
        invalidateCache();
    }

    public void setSelectedItem(String itemId) {
        if (itemId == null || itemId.isEmpty()) {
            itemId = "minecraft:stone";
        }
        if (!this.selectedItem.equals(itemId)) {
            this.selectedItem = itemId;
            updateOutputs();
            markDirty();
        }
    }

    private void updateOutputs() {
        String namespace = "minecraft";
        String path = "stone";
        if (selectedItem.contains(":")) {
            String[] parts = selectedItem.split(":", 2);
            namespace = parts[0];
            path = parts[1];
        }
        outputValues.put(OUTPUT_ITEM_ID, selectedItem);
        outputValues.put(OUTPUT_NAMESPACE, namespace);
        outputValues.put(OUTPUT_ITEM_PATH, path);
        outputValues.put(OUTPUT_IS_MODDED, !namespace.equals("minecraft"));
        syncOutputPorts();
    }

    public String getSelectedItem() {
        return selectedItem;
    }

    public boolean isAllowModded() {
        return allowModded;
    }

    public void setAllowModded(boolean allowModded) {
        this.allowModded = allowModded;
        if (!allowModded && !selectedItem.startsWith("minecraft:")) {
            setSelectedItem("minecraft:stone");
        }
    }

    @Override
    public Object getNodeState() {
        return Map.of(
            "selectedItem", getSelectedItem(),
            "allowModded", isAllowModded()
        );
    }

    @Override
    public void setNodeState(Object state) {
        if (state instanceof Map<?, ?> map) {
            if (map.get("allowModded") instanceof Boolean bool) {
                setAllowModded(bool);
            }
            if (map.get("selectedItem") instanceof String value) {
                setSelectedItem(value);
            }
        }
    }
}
