package com.nodecraft.nodesystem.nodes.inputs.minecraft;

import com.nodecraft.gui.editor.impl.BaseCustomUINode;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.datatypes.LineData;
import com.nodecraft.nodesystem.datatypes.PointData;
import com.nodecraft.nodesystem.datatypes.PolylineData;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import com.nodecraft.nodesystem.interaction.IBlockPickerCallback;
import com.nodecraft.nodesystem.interaction.NodeEditorInteractionManager;
import com.nodecraft.nodesystem.preview.PreviewManager;
import com.nodecraft.nodesystem.preview.PreviewOptions;
import com.nodecraft.nodesystem.util.Color;
import com.nodecraft.nodesystem.util.BlockPosList;
import com.nodecraft.nodesystem.util.Coordinate;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@NodeInfo(
    id = "inputs.minecraft.selected_block_sequence",
    displayName = "Selected Block Sequence",
    description = "Collects multiple picked blocks in click order and outputs an ordered block sequence",
    category = "inputs.minecraft"
)
public class SelectedBlockSequenceNode extends BaseCustomUINode implements IBlockPickerCallback {

    private static final String OUTPUT_COORDINATES_ID = "output_coordinates";
    private static final String OUTPUT_BLOCKS_ID = "output_blocks";
    private static final String OUTPUT_POINT_LIST_ID = "output_point_list";
    private static final String OUTPUT_LINE_ID = "output_line";
    private static final String OUTPUT_POLYLINE_ID = "output_polyline";
    private static final String OUTPUT_CENTERS_ID = "output_centers";
    private static final String OUTPUT_FIRST_ID = "output_first";
    private static final String OUTPUT_LAST_ID = "output_last";
    private static final String OUTPUT_COUNT_ID = "output_count";
    private static final String OUTPUT_SEGMENT_COUNT_ID = "output_segment_count";
    private static final String OUTPUT_IS_CLOSED_ID = "output_is_closed";
    private static final String OUTPUT_VALID_ID = "output_valid";

    private final List<Coordinate> pickedBlocks = new ArrayList<>();

    private boolean pickingActive = false;
    private boolean pendingRepick = false;
    private float maxDistance = 100.0f;
    private boolean includeFluids = false;
    private boolean allowDuplicates = false;
    private boolean autoPreviewPath = true;
    private boolean closePath = false;
    private String previewPathColor = "#FFD933";

    public SelectedBlockSequenceNode() {
        super(UUID.randomUUID(), "inputs.minecraft.selected_block_sequence");

        addOutputPort(new BasePort(OUTPUT_COORDINATES_ID, "Coordinates", "Ordered list of picked block coordinates", NodeDataType.COORDINATE_LIST, this));
        addOutputPort(new BasePort(OUTPUT_BLOCKS_ID, "Blocks", "Ordered list of picked block positions", NodeDataType.BLOCK_LIST, this));
        addOutputPort(new BasePort(OUTPUT_POINT_LIST_ID, "Point List", "Ordered geometric point list derived from the picked blocks", NodeDataType.LIST, this));
        addOutputPort(new BasePort(OUTPUT_LINE_ID, "Line", "Line built directly from the ordered point list when exactly 2 points exist", NodeDataType.LINE, this));
        addOutputPort(new BasePort(OUTPUT_POLYLINE_ID, "Polyline", "Polyline built directly from the ordered point list when at least 2 points exist", NodeDataType.POLYLINE, this));
        addOutputPort(new BasePort(OUTPUT_CENTERS_ID, "Centers", "Ordered list of block center points", NodeDataType.VECTOR_LIST, this));
        addOutputPort(new BasePort(OUTPUT_FIRST_ID, "First", "First picked block in the sequence", NodeDataType.COORDINATE, this));
        addOutputPort(new BasePort(OUTPUT_LAST_ID, "Last", "Last picked block in the sequence", NodeDataType.COORDINATE, this));
        addOutputPort(new BasePort(OUTPUT_COUNT_ID, "Count", "Number of picked blocks in the sequence", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_SEGMENT_COUNT_ID, "Segment Count", "Number of path segments implied by the current ordered sequence", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_IS_CLOSED_ID, "Is Closed", "Whether the ordered sequence is currently treated as a closed path", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_VALID_ID, "Valid", "Whether the sequence currently contains at least one picked block", NodeDataType.BOOLEAN, this));

        updateOutputs();
    }

    @Override
    public String getDescription() {
        return "Collects multiple picked blocks in click order and outputs an ordered block sequence";
    }

    @Override
    public String getDisplayName() {
        return "Selected Block Sequence";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        if (pickingActive && pendingRepick) {
            NodeEditorInteractionManager interactionManager = NodeEditorInteractionManager.getInstance();
            if (!interactionManager.isPendingBlockPick(getId().toString())) {
                pendingRepick = false;
                requestNextPick();
            }
        }
        updateOutputs();
        updatePathPreview();
    }

    @Override
    public void onBlockPicked(Coordinate position, String blockId, com.nodecraft.nodesystem.util.BlockStateData blockStateData) {
        if (position == null) {
            return;
        }

        if (allowDuplicates || !pickedBlocks.contains(position)) {
            pickedBlocks.add(position);
        }

        updateOutputs();
        markDirty();

        if (pickingActive) {
            pendingRepick = true;
        }

        updatePathPreview();
    }

    @Override
    public void onPickingCancelled() {
        pickingActive = false;
        pendingRepick = false;
        updatePathPreview();
        markDirty();
    }

    @Override
    public BlockPickingConfig getPickingConfig() {
        BlockPickingConfig config = new BlockPickingConfig();
        config.setMaxDistance(maxDistance);
        config.setIncludeFluids(includeFluids);
        return config;
    }

    public void clearSequence() {
        pickedBlocks.clear();
        updateOutputs();
        updatePathPreview();
        markDirty();
    }

    public void removeLast() {
        if (pickedBlocks.isEmpty()) {
            return;
        }
        pickedBlocks.removeLast();
        updateOutputs();
        updatePathPreview();
        markDirty();
    }

    private void startPicking() {
        pickingActive = true;
        pendingRepick = false;
        requestNextPick();
        updatePathPreview();
        markDirty();
    }

    private void stopPicking() {
        pickingActive = false;
        pendingRepick = false;
        NodeEditorInteractionManager.getInstance().cancelBlockPick();
        updatePathPreview();
        markDirty();
    }

    private void requestNextPick() {
        NodeEditorInteractionManager.getInstance().requestBlockPick(getId().toString(), this);
    }

    private void updateOutputs() {
        List<Coordinate> coordinates = new ArrayList<>(pickedBlocks);
        BlockPosList blocks = new BlockPosList();
        List<PointData> pointList = new ArrayList<>();
        List<Vec3d> polylinePoints = new ArrayList<>();
        List<Vector3d> centers = new ArrayList<>();

        for (Coordinate coordinate : pickedBlocks) {
            appendOutputs(coordinate, blocks, pointList, polylinePoints, centers);
        }

        if (closePath && pickedBlocks.size() >= 2) {
            Coordinate first = pickedBlocks.getFirst();
            Coordinate last = pickedBlocks.getLast();
            if (!first.equals(last)) {
                coordinates.add(first);
                appendOutputs(first, blocks, pointList, polylinePoints, centers);
            }
        }

        LineData line = polylinePoints.size() == 2 ? new LineData(polylinePoints.get(0), polylinePoints.get(1)) : null;
        PolylineData polyline = polylinePoints.size() >= 2 ? new PolylineData(polylinePoints) : null;

        outputValues.put(OUTPUT_COORDINATES_ID, coordinates);
        outputValues.put(OUTPUT_BLOCKS_ID, blocks);
        outputValues.put(OUTPUT_POINT_LIST_ID, pointList);
        outputValues.put(OUTPUT_LINE_ID, line);
        outputValues.put(OUTPUT_POLYLINE_ID, polyline);
        outputValues.put(OUTPUT_CENTERS_ID, centers);
        outputValues.put(OUTPUT_FIRST_ID, pickedBlocks.isEmpty() ? null : pickedBlocks.getFirst());
        outputValues.put(OUTPUT_LAST_ID, pickedBlocks.isEmpty() ? null : pickedBlocks.getLast());
        outputValues.put(OUTPUT_COUNT_ID, pickedBlocks.size());
        outputValues.put(OUTPUT_SEGMENT_COUNT_ID, Math.max(0, coordinates.size() - 1));
        outputValues.put(OUTPUT_IS_CLOSED_ID, closePath && pickedBlocks.size() >= 2);
        outputValues.put(OUTPUT_VALID_ID, !pickedBlocks.isEmpty());
    }

    private void updatePathPreview() {
        PreviewManager.hideNodePreviews(getId().toString());

        if (!autoPreviewPath || pickedBlocks.size() < 2) {
            return;
        }

        List<PointData> pointList = new ArrayList<>();
        for (Coordinate coordinate : pickedBlocks) {
            pointList.add(new PointData(
                coordinate.getX() + 0.5d,
                coordinate.getY() + 0.5d,
                coordinate.getZ() + 0.5d
            ));
        }
        if (closePath) {
            Coordinate first = pickedBlocks.getFirst();
            Coordinate last = pickedBlocks.getLast();
            if (!first.equals(last)) {
                pointList.add(new PointData(
                    first.getX() + 0.5d,
                    first.getY() + 0.5d,
                    first.getZ() + 0.5d
                ));
            }
        }

        Color color = Color.fromHex(previewPathColor);
        PreviewOptions options = new PreviewOptions()
            .setColor(color.getRed(), color.getGreen(), color.getBlue())
            .setLineWidth(1.5f)
            .setDuration(30);
        options.showArrows = true;
        options.arrowSize = 0.2f;

        PreviewManager.showPaths(getId().toString(), pointList, options);
    }

    private void appendOutputs(Coordinate coordinate, BlockPosList blocks, List<PointData> pointList, List<Vec3d> polylinePoints, List<Vector3d> centers) {
        blocks.add(new BlockPos(coordinate.getX(), coordinate.getY(), coordinate.getZ()));
        Vec3d centerPos = new Vec3d(
            coordinate.getX() + 0.5d,
            coordinate.getY() + 0.5d,
            coordinate.getZ() + 0.5d
        );
        pointList.add(new PointData(
            centerPos.x,
            centerPos.y,
            centerPos.z
        ));
        polylinePoints.add(centerPos);
        centers.add(new Vector3d(
            centerPos.x,
            centerPos.y,
            centerPos.z
        ));
    }

    @Override
    protected float calculateUIHeight() {
        float textLine = ImGui.getTextLineHeight();
        float frame = ImGui.getFrameHeight();
        float small = getSmallPadding();
        float medium = getMediumPadding();

        float height = medium;
        height += textLine * 4.0f;   // Ordered blocks / First / Last / color label
        height += frame * 8.0f;      // 4 checkboxes + slider + 2 color buttons row + 3 action buttons
        height += small * 11.0f;     // compact vertical spacing between groups
        height += medium;
        return height;
    }

    @Override
    protected float calculateMinUIWidth() {
        return 220.0f + getContentMargin();
    }

    @Override
    protected boolean renderCustomUIScaled(float width, float height, float zoom) {
        return layout(zoom, layout -> {
            boolean changed = false;
            float availableWidth = layout.getAvailableContentWidth(width);

            layout.addVerticalSpacing(getMediumPadding());

            ImGui.pushStyleColor(ImGuiCol.Text, 0.80f, 0.80f, 0.85f, 1.0f);
            ImGui.text("Ordered blocks: " + pickedBlocks.size());
            if (!pickedBlocks.isEmpty()) {
                Coordinate first = pickedBlocks.getFirst();
                Coordinate last = pickedBlocks.getLast();
                ImGui.text(String.format("First: %d, %d, %d", first.getX(), first.getY(), first.getZ()));
                ImGui.text(String.format("Last: %d, %d, %d", last.getX(), last.getY(), last.getZ()));
            } else {
                ImGui.textDisabled("First: none");
                ImGui.textDisabled("Last: none");
            }
            ImGui.popStyleColor();

            layout.addVerticalSpacing(getSmallPadding());

            if (ImGui.checkbox("Allow Duplicates##allowDuplicates", allowDuplicates)) {
                allowDuplicates = !allowDuplicates;
                updatePathPreview();
                changed = true;
            }
            if (ImGui.checkbox("Include Fluids##includeFluids", includeFluids)) {
                includeFluids = !includeFluids;
                changed = true;
            }
            if (ImGui.checkbox("Auto Preview Path##autoPreviewPath", autoPreviewPath)) {
                autoPreviewPath = !autoPreviewPath;
                updatePathPreview();
                changed = true;
            }
            if (ImGui.checkbox("Close Path##closePath", closePath)) {
                closePath = !closePath;
                updateOutputs();
                updatePathPreview();
                changed = true;
            }

            layout.addVerticalSpacing(getSmallPadding());

            ImGui.text("Preview");
            ImGui.sameLine();
            ImGui.textDisabled(previewPathColor);
            float[] distance = {maxDistance};
            ImGui.text("Pick Distance: " + String.format("%.1f", maxDistance));
            if (ImGui.sliderFloat("##pickDistance", distance, 1.0f, 300.0f)) {
                maxDistance = distance[0];
                changed = true;
            }

            if (ImGui.button("Yellow##pathColorYellow", availableWidth / 2.0f - 2.0f, 0)) {
                previewPathColor = "#FFD933";
                updatePathPreview();
                changed = true;
            }
            ImGui.sameLine();
            if (ImGui.button("Cyan##pathColorCyan", availableWidth / 2.0f - 2.0f, 0)) {
                previewPathColor = "#4DE3FF";
                updatePathPreview();
                changed = true;
            }

            layout.addVerticalSpacing(getSmallPadding());

            if (!pickingActive) {
                if (ImGui.button("Start Picking##startPicking", availableWidth, 0)) {
                    startPicking();
                    changed = true;
                }
            } else {
                ImGui.pushStyleColor(ImGuiCol.Button, 0.80f, 0.25f, 0.25f, 1.0f);
                ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.90f, 0.30f, 0.30f, 1.0f);
                ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.70f, 0.15f, 0.15f, 1.0f);
                if (ImGui.button("Stop Picking##stopPicking", availableWidth, 0)) {
                    stopPicking();
                    changed = true;
                }
                ImGui.popStyleColor(3);
            }

            layout.addVerticalSpacing(getSmallPadding());

            boolean hasBlocks = !pickedBlocks.isEmpty();
            if (hasBlocks) {
                if (ImGui.button("Remove Last##removeLast", availableWidth, 0)) {
                    removeLast();
                    changed = true;
                }
            } else {
                ImGui.pushStyleColor(ImGuiCol.Button, 0.25f, 0.25f, 0.25f, 1.0f);
                ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.25f, 0.25f, 0.25f, 1.0f);
                ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.25f, 0.25f, 0.25f, 1.0f);
                ImGui.pushStyleColor(ImGuiCol.Text, 0.45f, 0.45f, 0.45f, 1.0f);
                ImGui.button("Remove Last##removeLastDisabled", availableWidth, 0);
                ImGui.popStyleColor(4);
            }

            layout.addVerticalSpacing(getSmallPadding());

            if (ImGui.button("Clear Sequence##clearSequence", availableWidth, 0)) {
                clearSequence();
                changed = true;
            }
            return changed;
        });
    }

    @Override
    public Object getNodeState() {
        Map<String, Object> state = new HashMap<>();
        state.put("pickingActive", false);
        state.put("maxDistance", maxDistance);
        state.put("includeFluids", includeFluids);
        state.put("allowDuplicates", allowDuplicates);
        state.put("autoPreviewPath", autoPreviewPath);
        state.put("closePath", closePath);
        state.put("previewPathColor", previewPathColor);

        List<Map<String, Integer>> blocks = new ArrayList<>(pickedBlocks.size());
        for (Coordinate coordinate : pickedBlocks) {
            Map<String, Integer> item = new HashMap<>();
            item.put("x", coordinate.getX());
            item.put("y", coordinate.getY());
            item.put("z", coordinate.getZ());
            blocks.add(item);
        }
        state.put("pickedBlocks", blocks);
        return state;
    }

    @Override
    public void setNodeState(Object state) {
        if (!(state instanceof Map<?, ?> map)) {
            return;
        }

        pickedBlocks.clear();

        if (map.get("maxDistance") instanceof Number number) {
            maxDistance = Math.max(1.0f, Math.min(300.0f, number.floatValue()));
        }
        if (map.get("includeFluids") instanceof Boolean bool) {
            includeFluids = bool;
        }
        if (map.get("allowDuplicates") instanceof Boolean bool) {
            allowDuplicates = bool;
        }
        if (map.get("autoPreviewPath") instanceof Boolean bool) {
            autoPreviewPath = bool;
        }
        if (map.get("closePath") instanceof Boolean bool) {
            closePath = bool;
        }
        if (map.get("previewPathColor") instanceof String value) {
            previewPathColor = value;
        }
        if (map.get("pickedBlocks") instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Map<?, ?> blockMap) {
                    Object x = blockMap.get("x");
                    Object y = blockMap.get("y");
                    Object z = blockMap.get("z");
                    if (x instanceof Number xNum && y instanceof Number yNum && z instanceof Number zNum) {
                        pickedBlocks.add(new Coordinate(xNum.intValue(), yNum.intValue(), zNum.intValue()));
                    }
                }
            }
        }

        pickingActive = false;
        pendingRepick = false;
        updateOutputs();
        updatePathPreview();
    }

    public void onNodeRemoved() {
        if (pickingActive) {
            NodeEditorInteractionManager.getInstance().cancelBlockPick();
        }
        PreviewManager.hideNodePreviews(getId().toString());
    }
}
