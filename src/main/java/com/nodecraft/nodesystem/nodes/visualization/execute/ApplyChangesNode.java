package com.nodecraft.nodesystem.nodes.visualization.execute;

import com.nodecraft.gui.editor.impl.BaseCustomUINode;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.api.NodeProperty;
import com.nodecraft.nodesystem.bake.BakePlacementService;
import com.nodecraft.nodesystem.bake.PlacementMode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import com.nodecraft.nodesystem.util.BlockPosList;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.flag.ImGuiCol;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Apply Changes 节点: 将预览/几何体结果实际应用到世界中。
 * 支持放置模式（覆盖/增量）、异步放置、记录撤销；可接方块列表与方块类型输入。
 */
@NodeInfo(
    id = "visualization.execute.apply_changes",
    displayName = "应用修改",
    description = "将预览或几何体结果实际应用到世界中，支持放置模式与异步",
    category = "visualization.execute"
)
public class ApplyChangesNode extends BaseCustomUINode {

    @NodeProperty(displayName = "显示进度条", category = "执行", order = 1)
    private boolean showProgressBar = true;

    @NodeProperty(displayName = "完成通知", category = "执行", order = 2)
    private boolean notifyOnComplete = true;

    @NodeProperty(displayName = "超时时间", category = "执行", order = 3)
    private int executionTimeout = 30;

    @NodeProperty(displayName = "放置模式", category = "放置", order = 4)
    private PlacementMode placementMode = PlacementMode.OVERWRITE;

    @NodeProperty(displayName = "异步放置", category = "放置", order = 5)
    private boolean useAsyncBake = true;

    @NodeProperty(displayName = "记录撤销", category = "放置", order = 6)
    private boolean recordUndo = true;

    private UUID executionId = UUID.randomUUID();
    private boolean isExecuting = false;
    private float progressPercentage = 0.0f;
    private String statusMessage = "就绪";

    private static final String INPUT_TRIGGER_ID = "input_trigger";
    private static final String INPUT_BLOCKS_ID = "input_blocks";
    private static final String INPUT_BLOCK_TYPE_ID = "input_block_type";
    private static final String INPUT_PREVIEW_IDS_ID = "input_preview_ids";
    private static final String INPUT_NOTIFY_ID = "input_notify";
    private static final String OUTPUT_SUCCESS_ID = "output_success";
    private static final String OUTPUT_OPERATION_COUNT_ID = "output_operation_count";
    private static final String OUTPUT_EXECUTION_TIME_ID = "output_execution_time";
    private static final String OUTPUT_STATUS_ID = "output_status";

    public ApplyChangesNode() {
        super(UUID.randomUUID(), "visualization.execute.apply_changes");
        addInputPort(new BasePort(INPUT_TRIGGER_ID, "Trigger", "触发执行", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_BLOCKS_ID, "Blocks", "要放置的方块坐标列表", NodeDataType.BLOCK_LIST, this));
        addInputPort(new BasePort(INPUT_BLOCK_TYPE_ID, "Block Type", "方块类型（如 minecraft:stone）", NodeDataType.STRING, this));
        addInputPort(new BasePort(INPUT_PREVIEW_IDS_ID, "Preview IDs", "要应用的预览ID列表（可选）", NodeDataType.LIST, this));
        addInputPort(new BasePort(INPUT_NOTIFY_ID, "Notify on Complete", "完成后是否通知", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_SUCCESS_ID, "Success", "是否成功", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_OPERATION_COUNT_ID, "Operation Count", "操作数量", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_EXECUTION_TIME_ID, "Execution Time", "执行时间(ms)", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_STATUS_ID, "Status", "状态信息", NodeDataType.STRING, this));
    }

    @Override
    public String getDescription() { return "将预览或几何体结果实际应用到世界中，支持放置模式与异步"; }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        boolean success = false;
        int operationCount = 0;
        int executionTime = 0;
        String status = "未执行操作";

        Object triggerObj = inputValues.get(INPUT_TRIGGER_ID);
        Object blocksObj = inputValues.get(INPUT_BLOCKS_ID);
        Object blockTypeObj = inputValues.get(INPUT_BLOCK_TYPE_ID);
        Object notifyObj = inputValues.get(INPUT_NOTIFY_ID);

        boolean notify = (notifyObj instanceof Boolean) ? (Boolean) notifyObj : this.notifyOnComplete;
        String blockType = (blockTypeObj instanceof String) ? (String) blockTypeObj : "minecraft:stone";

        if (triggerObj != null && !isExecuting && context != null && context.getWorld() != null
                && blocksObj instanceof BlockPosList) {
            BlockPosList blocks = (BlockPosList) blocksObj;
            if (blocks.isEmpty()) {
                status = "方块列表为空";
                outputValues.put(OUTPUT_SUCCESS_ID, false);
                outputValues.put(OUTPUT_OPERATION_COUNT_ID, 0);
                outputValues.put(OUTPUT_EXECUTION_TIME_ID, 0);
                outputValues.put(OUTPUT_STATUS_ID, status);
                return;
            }

            BlockState targetState = resolveBlockState(blockType);
            if (targetState == null) {
                status = "无效方块类型: " + blockType;
                outputValues.put(OUTPUT_SUCCESS_ID, false);
                outputValues.put(OUTPUT_OPERATION_COUNT_ID, 0);
                outputValues.put(OUTPUT_EXECUTION_TIME_ID, 0);
                outputValues.put(OUTPUT_STATUS_ID, status);
                return;
            }

            isExecuting = true;
            long startTime = System.currentTimeMillis();
            try {
                progressPercentage = 0.2f;
                statusMessage = "正在应用...";

                if (useAsyncBake) {
                    BakePlacementService.getInstance().enqueue(
                            context.getWorld(),
                            new ArrayList<>(blocks.getPositions()),
                            targetState,
                            placementMode,
                            recordUndo,
                            1000,
                            null);
                    operationCount = blocks.size();
                    success = true;
                    status = "已提交 " + operationCount + " 方块（异步）";
                } else {
                    int count = 0;
                    for (BlockPos pos : blocks) {
                        if (placementMode == PlacementMode.INCREMENTAL && !context.getWorld().isAir(pos)) continue;
                        if (context.getWorld().setBlockState(pos.toImmutable(), targetState, Block.NOTIFY_ALL)) count++;
                    }
                    operationCount = count;
                    success = true;
                    status = "已放置 " + operationCount + "/" + blocks.size();
                }
                progressPercentage = 1.0f;
                statusMessage = success ? "完成" : "失败";
                executionTime = (int) (System.currentTimeMillis() - startTime);
                if (notify) System.out.println("应用修改: " + status + ", " + executionTime + "ms");
            } catch (Exception e) {
                status = "错误: " + e.getMessage();
                System.err.println("ApplyChangesNode: " + e.getMessage());
            } finally {
                isExecuting = false;
            }
        }
        outputValues.put(OUTPUT_SUCCESS_ID, success);
        outputValues.put(OUTPUT_OPERATION_COUNT_ID, operationCount);
        outputValues.put(OUTPUT_EXECUTION_TIME_ID, executionTime);
        outputValues.put(OUTPUT_STATUS_ID, status);
    }

    private BlockState resolveBlockState(String blockId) {
        if (blockId == null || blockId.isEmpty()) return null;
        try {
            var block = Registries.BLOCK.get(Identifier.of(blockId));
            return block != null ? block.getDefaultState() : null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected float calculateUIHeight() {
        float h = getMediumPadding();
        h += ImGui.getTextLineHeight();    // 状态行
        h += getSmallPadding();
        h += ImGui.getFrameHeight();       // 进度条
        h += getSmallPadding();
        h += ImGui.getFrameHeight();       // showProgressBar
        h += getSmallPadding();
        h += ImGui.getFrameHeight();       // notifyOnComplete
        h += getSmallPadding();
        h += ImGui.getFrameHeight();       // 放置模式
        h += getSmallPadding();
        h += ImGui.getFrameHeight() * 2;   // 异步、记录撤销
        h += getSmallPadding();
        h += ImGui.getFrameHeight();       // 超时滑条
        h += getMediumPadding();
        return h;
    }

    @Override
    protected float calculateMinUIWidth() {
        return 190f + getContentMargin();
    }

    @Override
    protected boolean renderCustomUIScaled(float width, float height, float zoom) {
        return layout(zoom, l -> {
            boolean changed = false;
            try {
                float aw = l.getAvailableContentWidth(width);
                l.addVerticalSpacing(getMediumPadding());

                // 状态行
                int statusColor = isExecuting ? 0xFF44AADD : (progressPercentage >= 1.0f ? 0xFF44DD44 : 0xFF888888);
                ImGui.pushStyleColor(ImGuiCol.Text, statusColor);
                ImGui.text(statusMessage);
                ImGui.popStyleColor();
                l.addVerticalSpacing(getSmallPadding());

                // 进度条
                ImGui.progressBar(progressPercentage, aw, ImGui.getFrameHeight(),
                    String.format("%.0f%%", progressPercentage * 100));
                l.addVerticalSpacing(getSmallPadding());

                // 显示进度条
                ImBoolean spBool = new ImBoolean(showProgressBar);
                if (ImGui.checkbox("显示进度条##sp", spBool)) { setShowProgressBar(spBool.get()); changed = true; }
                l.addVerticalSpacing(getSmallPadding());

                // 完成通知
                ImBoolean ncBool = new ImBoolean(notifyOnComplete);
                if (ImGui.checkbox("完成通知##nc", ncBool)) { setNotifyOnComplete(ncBool.get()); changed = true; }
                l.addVerticalSpacing(getSmallPadding());

                // 放置模式
                if (ImGui.beginCombo("放置模式##ac", placementMode == PlacementMode.OVERWRITE ? "覆盖" : "增量")) {
                    if (ImGui.selectable("覆盖", placementMode == PlacementMode.OVERWRITE)) { setPlacementMode(PlacementMode.OVERWRITE); changed = true; }
                    if (ImGui.selectable("增量", placementMode == PlacementMode.INCREMENTAL)) { setPlacementMode(PlacementMode.INCREMENTAL); changed = true; }
                    ImGui.endCombo();
                }
                l.addVerticalSpacing(getSmallPadding());
                ImBoolean asyncBool = new ImBoolean(useAsyncBake);
                if (ImGui.checkbox("异步放置##ac", asyncBool)) { setUseAsyncBake(asyncBool.get()); changed = true; }
                l.addVerticalSpacing(getSmallPadding());
                ImBoolean undoBool = new ImBoolean(recordUndo);
                if (ImGui.checkbox("记录撤销##ac", undoBool)) { setRecordUndo(undoBool.get()); changed = true; }
                l.addVerticalSpacing(getSmallPadding());

                // 超时滑条
                int[] timeout = {executionTimeout};
                l.pushFramePadding(4.0f, 3.0f);
                l.setItemWidth(aw / zoom);
                if (ImGui.sliderInt("##timeout", timeout, 5, 300, "超时: %d 秒")) {
                    setExecutionTimeout(timeout[0]); changed = true;
                }
                l.popItemWidth();
                l.popStyleVar();

                l.addVerticalSpacing(getMediumPadding());
            } catch (Exception e) {
                System.err.println("ApplyChangesNode UI渲染失败: " + e.getMessage());
            }
            return changed;
        });
    }

    public boolean isShowProgressBar() { return showProgressBar; }
    public void setShowProgressBar(boolean v) { if (this.showProgressBar != v) { this.showProgressBar = v; markDirty(); } }
    public boolean isNotifyOnComplete() { return notifyOnComplete; }
    public void setNotifyOnComplete(boolean v) { if (this.notifyOnComplete != v) { this.notifyOnComplete = v; markDirty(); } }
    public int getExecutionTimeout() { return executionTimeout; }
    public void setExecutionTimeout(int v) { v = Math.max(5, v); if (this.executionTimeout != v) { this.executionTimeout = v; markDirty(); } }
    public float getProgressPercentage() { return progressPercentage; }
    public String getStatusMessage() { return statusMessage; }
    public boolean isExecuting() { return isExecuting; }
    public void resetExecutionId() { executionId = UUID.randomUUID(); markDirty(); }

    public PlacementMode getPlacementMode() { return placementMode; }
    public void setPlacementMode(PlacementMode v) { if (placementMode != v) { placementMode = v; markDirty(); } }
    public boolean isUseAsyncBake() { return useAsyncBake; }
    public void setUseAsyncBake(boolean v) { if (useAsyncBake != v) { useAsyncBake = v; markDirty(); } }
    public boolean isRecordUndo() { return recordUndo; }
    public void setRecordUndo(boolean v) { if (recordUndo != v) { recordUndo = v; markDirty(); } }

    @Override
    public @Nullable Object getNodeState() {
        java.util.Map<String, Object> s = new java.util.HashMap<>();
        s.put("showProgressBar", showProgressBar); s.put("notifyOnComplete", notifyOnComplete);
        s.put("executionTimeout", executionTimeout); s.put("executionId", executionId.toString());
        s.put("placementMode", placementMode.name()); s.put("useAsyncBake", useAsyncBake); s.put("recordUndo", recordUndo);
        return s;
    }

    @Override
    public void setNodeState(@Nullable Object state) {
        if (!(state instanceof java.util.Map)) return;
        java.util.Map<?, ?> m = (java.util.Map<?, ?>) state;
        Object v;
        if ((v = m.get("showProgressBar")) instanceof Boolean) setShowProgressBar((Boolean) v);
        if ((v = m.get("notifyOnComplete")) instanceof Boolean) setNotifyOnComplete((Boolean) v);
        if ((v = m.get("executionTimeout")) instanceof Number) setExecutionTimeout(((Number) v).intValue());
        if ((v = m.get("executionId")) instanceof String) {
            try { executionId = UUID.fromString((String) v); } catch (IllegalArgumentException e) { resetExecutionId(); }
        }
        if ((v = m.get("placementMode")) instanceof String) {
            try { setPlacementMode(PlacementMode.valueOf((String) v)); } catch (Exception ignored) {}
        }
        if ((v = m.get("useAsyncBake")) instanceof Boolean) setUseAsyncBake((Boolean) v);
        if ((v = m.get("recordUndo")) instanceof Boolean) setRecordUndo((Boolean) v);
    }
}
