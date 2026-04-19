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
    id = "input.type_selectors.entity_type_selector",
    displayName = "Entity Type Selector",
    description = "Searches and selects a Minecraft entity type.",
    category = "input.type_selectors",
    order = 1
)
public class EntityTypeSelectorNode extends BaseCustomUINode {

    private static final String[] QUICK_ENTITIES = {
        "minecraft:pig",
        "minecraft:villager",
        "minecraft:armor_stand",
        "minecraft:item_frame",
        "minecraft:boat",
        "minecraft:minecart",
        "minecraft:zombie",
        "minecraft:cow"
    };

    @NodeProperty(
        displayName = "Selected Entity",
        category = "Selection",
        order = 1,
        description = "The currently selected entity type id."
    )
    private String selectedEntity = "minecraft:pig";

    @NodeProperty(
        displayName = "Allow Modded Entities",
        category = "Filter",
        order = 2,
        description = "Whether entity ids outside the minecraft namespace should appear in search results."
    )
    private boolean allowModded = true;

    private static final String OUTPUT_ENTITY_ID = "output_entity_id";
    private static final String OUTPUT_NAMESPACE = "output_namespace";
    private static final String OUTPUT_ENTITY_PATH = "output_entity_path";
    private static final String OUTPUT_IS_MODDED = "output_is_modded";

    private transient ImString searchBuffer = new ImString(256);
    private transient volatile List<String> filteredEntities = new ArrayList<>();
    private transient volatile boolean showDropdown = false;
    private transient volatile String lastSearchText = "";

    private static final int MAX_RESULTS = 20;

    public EntityTypeSelectorNode() {
        super(UUID.randomUUID(), "input.type_selectors.entity_type_selector");

        addOutputPort(new BasePort(OUTPUT_ENTITY_ID, "Entity Type", "The selected entity's full identifier", NodeDataType.ENTITY_TYPE, this));
        addOutputPort(new BasePort(OUTPUT_NAMESPACE, "Namespace", "The namespace part of the selected entity id", NodeDataType.STRING, this));
        addOutputPort(new BasePort(OUTPUT_ENTITY_PATH, "Entity Path", "The path part of the selected entity id", NodeDataType.STRING, this));
        addOutputPort(new BasePort(OUTPUT_IS_MODDED, "Is Modded", "Whether the selected entity is outside the minecraft namespace", NodeDataType.BOOLEAN, this));

        updateOutputs();
    }

    @Override
    public String getDescription() {
        return "Searches and selects a Minecraft entity type.";
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
            height += Math.min(filteredEntities.size(), MAX_RESULTS) * ImGui.getTextLineHeightWithSpacing();
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
                for (int i = 0; i < QUICK_ENTITIES.length; i++) {
                    String quickEntity = QUICK_ENTITIES[i];
                    String quickLabel = quickEntity.split(":", 2)[1];
                    if (i > 0 && i % 2 != 0) {
                        ImGui.sameLine();
                    }
                    if (ImGui.smallButton(quickLabel + "##quick_entity_" + i)) {
                        setSelectedEntity(quickEntity);
                        searchBuffer.set("");
                        filteredEntities = new ArrayList<>();
                        showDropdown = false;
                        lastSearchText = "";
                        changed = true;
                    }
                }

                layout.addVerticalSpacing(getSmallPadding());

                layout.pushFramePadding(4.0f, 3.0f);
                layout.setItemWidth(availableWidth / zoom);

                if (ImGui.inputTextWithHint("##entity_search", "Search entities...", searchBuffer)) {
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

                List<String> filteredSnapshot = filteredEntities;
                if (showDropdown && !filteredSnapshot.isEmpty()) {
                    ImGui.pushStyleColor(ImGuiCol.ChildBg, 0.15f, 0.15f, 0.18f, 0.95f);

                    int displayCount = Math.min(filteredSnapshot.size(), MAX_RESULTS);
                    for (int i = 0; i < displayCount; i++) {
                        String entityId = filteredSnapshot.get(i);
                        String entityPath = entityId.contains(":") ? entityId.split(":", 2)[1] : entityId;

                        boolean isSelected = entityId.equals(selectedEntity);
                        if (isSelected) {
                            ImGui.pushStyleColor(ImGuiCol.Text, 0.3f, 0.9f, 0.5f, 1.0f);
                        }

                        if (ImGui.selectable("  " + entityPath + "##entity_" + i, isSelected)) {
                            setSelectedEntity(entityId);
                            searchBuffer.set("");
                            showDropdown = false;
                            lastSearchText = "";
                            changed = true;
                        }

                        if (isSelected) {
                            ImGui.popStyleColor();
                        }

                        if (ImGui.isItemHovered()) {
                            ImGui.setTooltip(entityId);
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
                System.err.println("EntityTypeSelectorNode UI render failed: " + e.getMessage());
            }

            return changed;
        });
    }

    private void updateFilteredList(String searchText) {
        List<String> nextFilteredEntities = new ArrayList<>();
        if (searchText.isEmpty()) {
            for (String quickEntity : QUICK_ENTITIES) {
                if (allowModded || quickEntity.startsWith("minecraft:")) {
                    nextFilteredEntities.add(quickEntity);
                }
            }
            filteredEntities = nextFilteredEntities;
            invalidateCache();
            return;
        }

        try {
            for (Identifier id : Registries.ENTITY_TYPE.getIds()) {
                String fullId = id.toString();
                if (!allowModded && !id.getNamespace().equals("minecraft")) {
                    continue;
                }

                if (fullId.contains(searchText) || id.getPath().contains(searchText)) {
                    nextFilteredEntities.add(fullId);
                    if (nextFilteredEntities.size() >= MAX_RESULTS * 2) {
                        break;
                    }
                }
            }
        } catch (Exception ignored) {
        }

        filteredEntities = nextFilteredEntities;
        invalidateCache();
    }

    public void setSelectedEntity(String entityId) {
        if (entityId == null || entityId.isEmpty()) {
            entityId = "minecraft:pig";
        }
        if (!this.selectedEntity.equals(entityId)) {
            this.selectedEntity = entityId;
            updateOutputs();
            markDirty();
        }
    }

    private void updateOutputs() {
        String namespace = "minecraft";
        String path = "pig";
        if (selectedEntity.contains(":")) {
            String[] parts = selectedEntity.split(":", 2);
            namespace = parts[0];
            path = parts[1];
        }
        outputValues.put(OUTPUT_ENTITY_ID, selectedEntity);
        outputValues.put(OUTPUT_NAMESPACE, namespace);
        outputValues.put(OUTPUT_ENTITY_PATH, path);
        outputValues.put(OUTPUT_IS_MODDED, !namespace.equals("minecraft"));
        syncOutputPorts();
    }

    public String getSelectedEntity() {
        return selectedEntity;
    }

    public boolean isAllowModded() {
        return allowModded;
    }

    public void setAllowModded(boolean allowModded) {
        this.allowModded = allowModded;
        if (!allowModded && !selectedEntity.startsWith("minecraft:")) {
            setSelectedEntity("minecraft:pig");
        }
    }

    @Override
    public Object getNodeState() {
        return Map.of(
            "selectedEntity", getSelectedEntity(),
            "allowModded", isAllowModded()
        );
    }

    @Override
    public void setNodeState(Object state) {
        if (state instanceof Map<?, ?> map) {
            if (map.get("allowModded") instanceof Boolean bool) {
                setAllowModded(bool);
            }
            if (map.get("selectedEntity") instanceof String value) {
                setSelectedEntity(value);
            }
        }
    }
}
