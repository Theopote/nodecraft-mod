package com.nodecraft.nodesystem.nodes.inputs.minecraft;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.IPort;
import com.nodecraft.nodesystem.util.Vector3;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import com.nodecraft.nodesystem.minecraft.PlayerAccessor;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * 获取玩家选定区域（两个坐标点）的节点
 */
@NodeInfo(
    id = "inputs.minecraft.selected_region",
    displayName = "选定区域",
    description = "获取玩家选定的区域（两个坐标点）",
    category = "inputs.minecraft"
)
public class SelectedRegionNode extends BaseNode {
    
    // --- 节点属性 ---
    private boolean autoUpdate = true; // 自动更新选区
    
    // --- 输出端口 ---
    private static final String OUTPUT_POS1_ID = "output_pos1";
    private static final String OUTPUT_POS2_ID = "output_pos2";
    private static final String OUTPUT_POS1_X_ID = "output_pos1_x";
    private static final String OUTPUT_POS1_Y_ID = "output_pos1_y";
    private static final String OUTPUT_POS1_Z_ID = "output_pos1_z";
    private static final String OUTPUT_POS2_X_ID = "output_pos2_x";
    private static final String OUTPUT_POS2_Y_ID = "output_pos2_y";
    private static final String OUTPUT_POS2_Z_ID = "output_pos2_z";
    private static final String OUTPUT_MIN_POS_ID = "output_min_pos";
    private static final String OUTPUT_MAX_POS_ID = "output_max_pos";
    private static final String OUTPUT_SIZE_X_ID = "output_size_x";
    private static final String OUTPUT_SIZE_Y_ID = "output_size_y";
    private static final String OUTPUT_SIZE_Z_ID = "output_size_z";
    private static final String OUTPUT_VOLUME_ID = "output_volume";
    private static final String OUTPUT_HAS_SELECTION_ID = "output_has_selection";
    
    // 内部状态
    private Vector3 pos1 = null;
    private Vector3 pos2 = null;
    
    /**
     * 构造一个新的选定区域节点
     */
    public SelectedRegionNode() {
        // 使用新的分类命名 - inputs.minecraft.selected_region
        super(UUID.randomUUID(), "inputs.minecraft.selected_region");
        
        // 创建并添加输出端口
        IPort pos1Output = new BasePort(OUTPUT_POS1_ID, "Position 1", 
                "The first corner position", NodeDataType.VECTOR, this);
        addOutputPort(pos1Output);
        
        IPort pos2Output = new BasePort(OUTPUT_POS2_ID, "Position 2", 
                "The second corner position", NodeDataType.VECTOR, this);
        addOutputPort(pos2Output);
        
        IPort pos1XOutput = new BasePort(OUTPUT_POS1_X_ID, "Pos1 X", 
                "X coordinate of position 1", NodeDataType.INTEGER, this);
        addOutputPort(pos1XOutput);
        
        IPort pos1YOutput = new BasePort(OUTPUT_POS1_Y_ID, "Pos1 Y", 
                "Y coordinate of position 1", NodeDataType.INTEGER, this);
        addOutputPort(pos1YOutput);
        
        IPort pos1ZOutput = new BasePort(OUTPUT_POS1_Z_ID, "Pos1 Z", 
                "Z coordinate of position 1", NodeDataType.INTEGER, this);
        addOutputPort(pos1ZOutput);
        
        IPort pos2XOutput = new BasePort(OUTPUT_POS2_X_ID, "Pos2 X", 
                "X coordinate of position 2", NodeDataType.INTEGER, this);
        addOutputPort(pos2XOutput);
        
        IPort pos2YOutput = new BasePort(OUTPUT_POS2_Y_ID, "Pos2 Y", 
                "Y coordinate of position 2", NodeDataType.INTEGER, this);
        addOutputPort(pos2YOutput);
        
        IPort pos2ZOutput = new BasePort(OUTPUT_POS2_Z_ID, "Pos2 Z", 
                "Z coordinate of position 2", NodeDataType.INTEGER, this);
        addOutputPort(pos2ZOutput);
        
        IPort minPosOutput = new BasePort(OUTPUT_MIN_POS_ID, "Min Position", 
                "The minimum corner position", NodeDataType.VECTOR, this);
        addOutputPort(minPosOutput);
        
        IPort maxPosOutput = new BasePort(OUTPUT_MAX_POS_ID, "Max Position", 
                "The maximum corner position", NodeDataType.VECTOR, this);
        addOutputPort(maxPosOutput);
        
        IPort sizeXOutput = new BasePort(OUTPUT_SIZE_X_ID, "Size X", 
                "Width of the selection (X)", NodeDataType.INTEGER, this);
        addOutputPort(sizeXOutput);
        
        IPort sizeYOutput = new BasePort(OUTPUT_SIZE_Y_ID, "Size Y", 
                "Height of the selection (Y)", NodeDataType.INTEGER, this);
        addOutputPort(sizeYOutput);
        
        IPort sizeZOutput = new BasePort(OUTPUT_SIZE_Z_ID, "Size Z", 
                "Depth of the selection (Z)", NodeDataType.INTEGER, this);
        addOutputPort(sizeZOutput);
        
        IPort volumeOutput = new BasePort(OUTPUT_VOLUME_ID, "Volume", 
                "Total volume of the selection in blocks", NodeDataType.INTEGER, this);
        addOutputPort(volumeOutput);
        
        IPort hasSelectionOutput = new BasePort(OUTPUT_HAS_SELECTION_ID, "Has Selection", 
                "Whether a valid selection exists", NodeDataType.BOOLEAN, this);
        addOutputPort(hasSelectionOutput);
        
        // 设置默认输出值
        resetOutputs();
    }
    
    @Override
    public String getDescription() {
        return "Gets the region (two coordinates) selected by the player.";
    }
    
    @Override
    public String getDisplayName() {
        return "Selected Region";
    }
    
    /**
     * 节点的计算逻辑
     * @param context 执行上下文
     */
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        if (context == null) {
            if (pos1 == null || pos2 == null) {
                resetOutputs();
            } else {
                updateOutputsFromPositions();
            }
            return;
        }
        
        // 如果设置为自动更新，则从PlayerAccessor获取选区
        if (autoUpdate) {
            PlayerAccessor playerAccessor = context.getPlayerAccessor();
            if (playerAccessor == null) {
                if (pos1 == null || pos2 == null) {
                    resetOutputs();
                }
                return;
            }
            
            // 在实际实现中，这里应该从PlayerAccessor获取玩家当前的选区
            // 此处为演示目的，假设有一个选区
            fetchSelectionFromPlayer(playerAccessor);
        }
        
        // 更新输出
        updateOutputsFromPositions();
    }
    
    /**
     * 从玩家获取选区
     */
    private void fetchSelectionFromPlayer(PlayerAccessor playerAccessor) {
        // 在实际实现中，这里应该通过某种机制获取玩家的选区
        // 例如，通过世界编辑工具、命令或特定物品交互等
        
        // 为演示目的，这里假设玩家有一个选区
        // 在实际使用时，需要根据Minecraft API实现具体获取逻辑
        Vector3 playerPos = playerAccessor.getPlayerPosition();
        
        // 假设选区是以玩家为中心的5x5x5区域
        pos1 = new Vector3(
            playerPos.getX() - 2,
            playerPos.getY() - 2,
            playerPos.getZ() - 2
        );
        
        pos2 = new Vector3(
            playerPos.getX() + 2,
            playerPos.getY() + 2,
            playerPos.getZ() + 2
        );
    }
    
    /**
     * 手动设置选区的第一个点
     * @param x X坐标
     * @param y Y坐标
     * @param z Z坐标
     */
    public void setPos1(int x, int y, int z) {
        pos1 = new Vector3(x, y, z);
        if (pos2 != null) {
            updateOutputsFromPositions();
        } else {
            // 如果pos2还未设置，输出仅更新pos1相关的值
            outputValues.put(OUTPUT_POS1_ID, pos1);
            outputValues.put(OUTPUT_POS1_X_ID, (int)pos1.getX());
            outputValues.put(OUTPUT_POS1_Y_ID, (int)pos1.getY());
            outputValues.put(OUTPUT_POS1_Z_ID, (int)pos1.getZ());
            outputValues.put(OUTPUT_HAS_SELECTION_ID, false);
        }
        markDirty();
    }
    
    /**
     * 手动设置选区的第二个点
     * @param x X坐标
     * @param y Y坐标
     * @param z Z坐标
     */
    public void setPos2(int x, int y, int z) {
        pos2 = new Vector3(x, y, z);
        if (pos1 != null) {
            updateOutputsFromPositions();
        } else {
            // 如果pos1还未设置，输出仅更新pos2相关的值
            outputValues.put(OUTPUT_POS2_ID, pos2);
            outputValues.put(OUTPUT_POS2_X_ID, (int)pos2.getX());
            outputValues.put(OUTPUT_POS2_Y_ID, (int)pos2.getY());
            outputValues.put(OUTPUT_POS2_Z_ID, (int)pos2.getZ());
            outputValues.put(OUTPUT_HAS_SELECTION_ID, false);
        }
        markDirty();
    }
    
    /**
     * 清除选区
     */
    public void clearSelection() {
        pos1 = null;
        pos2 = null;
        resetOutputs();
        markDirty();
    }
    
    /**
     * 根据两个位置点更新所有输出
     */
    private void updateOutputsFromPositions() {
        if (pos1 == null || pos2 == null) {
            resetOutputs();
            return;
        }
        
        // 更新基本位置输出
        outputValues.put(OUTPUT_POS1_ID, pos1);
        outputValues.put(OUTPUT_POS1_X_ID, (int)pos1.getX());
        outputValues.put(OUTPUT_POS1_Y_ID, (int)pos1.getY());
        outputValues.put(OUTPUT_POS1_Z_ID, (int)pos1.getZ());
        
        outputValues.put(OUTPUT_POS2_ID, pos2);
        outputValues.put(OUTPUT_POS2_X_ID, (int)pos2.getX());
        outputValues.put(OUTPUT_POS2_Y_ID, (int)pos2.getY());
        outputValues.put(OUTPUT_POS2_Z_ID, (int)pos2.getZ());
        
        // 计算最小和最大点
        float minX = Math.min(pos1.getX(), pos2.getX());
        float minY = Math.min(pos1.getY(), pos2.getY());
        float minZ = Math.min(pos1.getZ(), pos2.getZ());
        
        float maxX = Math.max(pos1.getX(), pos2.getX());
        float maxY = Math.max(pos1.getY(), pos2.getY());
        float maxZ = Math.max(pos1.getZ(), pos2.getZ());
        
        Vector3 minPos = new Vector3(minX, minY, minZ);
        Vector3 maxPos = new Vector3(maxX, maxY, maxZ);
        
        outputValues.put(OUTPUT_MIN_POS_ID, minPos);
        outputValues.put(OUTPUT_MAX_POS_ID, maxPos);
        
        // 计算尺寸和体积
        int sizeX = (int)(maxX - minX) + 1; // +1因为包含两端点
        int sizeY = (int)(maxY - minY) + 1;
        int sizeZ = (int)(maxZ - minZ) + 1;
        int volume = sizeX * sizeY * sizeZ;
        
        outputValues.put(OUTPUT_SIZE_X_ID, sizeX);
        outputValues.put(OUTPUT_SIZE_Y_ID, sizeY);
        outputValues.put(OUTPUT_SIZE_Z_ID, sizeZ);
        outputValues.put(OUTPUT_VOLUME_ID, volume);
        outputValues.put(OUTPUT_HAS_SELECTION_ID, true);
    }
    
    /**
     * 重置输出端口的值为默认值
     */
    private void resetOutputs() {
        Vector3 zeroVec = new Vector3(0, 0, 0);
        outputValues.put(OUTPUT_POS1_ID, zeroVec);
        outputValues.put(OUTPUT_POS2_ID, zeroVec);
        outputValues.put(OUTPUT_POS1_X_ID, 0);
        outputValues.put(OUTPUT_POS1_Y_ID, 0);
        outputValues.put(OUTPUT_POS1_Z_ID, 0);
        outputValues.put(OUTPUT_POS2_X_ID, 0);
        outputValues.put(OUTPUT_POS2_Y_ID, 0);
        outputValues.put(OUTPUT_POS2_Z_ID, 0);
        outputValues.put(OUTPUT_MIN_POS_ID, zeroVec);
        outputValues.put(OUTPUT_MAX_POS_ID, zeroVec);
        outputValues.put(OUTPUT_SIZE_X_ID, 0);
        outputValues.put(OUTPUT_SIZE_Y_ID, 0);
        outputValues.put(OUTPUT_SIZE_Z_ID, 0);
        outputValues.put(OUTPUT_VOLUME_ID, 0);
        outputValues.put(OUTPUT_HAS_SELECTION_ID, false);
    }
    
    // --- Getters/Setters for Properties ---
    
    public boolean isAutoUpdate() {
        return autoUpdate;
    }
    
    public void setAutoUpdate(boolean autoUpdate) {
        if (this.autoUpdate != autoUpdate) {
            this.autoUpdate = autoUpdate;
            markDirty();
        }
    }
    
    // --- 节点状态序列化 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("autoUpdate", isAutoUpdate());
        
        // 保存选区位置
        if (pos1 != null) {
            java.util.Map<String, Float> pos1Map = new java.util.HashMap<>();
            pos1Map.put("x", pos1.getX());
            pos1Map.put("y", pos1.getY());
            pos1Map.put("z", pos1.getZ());
            state.put("pos1", pos1Map);
        }
        
        if (pos2 != null) {
            java.util.Map<String, Float> pos2Map = new java.util.HashMap<>();
            pos2Map.put("x", pos2.getX());
            pos2Map.put("y", pos2.getY());
            pos2Map.put("z", pos2.getZ());
            state.put("pos2", pos2Map);
        }
        
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("autoUpdate")) {
                Object autoUpd = stateMap.get("autoUpdate");
                if (autoUpd instanceof Boolean) {
                    setAutoUpdate((Boolean) autoUpd);
                }
            }
            
            // 恢复选区位置
            if (stateMap.containsKey("pos1")) {
                Object pos1Obj = stateMap.get("pos1");
                if (pos1Obj instanceof java.util.Map) {
                    java.util.Map<?, ?> pos1Map = (java.util.Map<?, ?>) pos1Obj;
                    if (pos1Map.containsKey("x") && pos1Map.containsKey("y") && pos1Map.containsKey("z")) {
                        float x = ((Number) pos1Map.get("x")).floatValue();
                        float y = ((Number) pos1Map.get("y")).floatValue();
                        float z = ((Number) pos1Map.get("z")).floatValue();
                        pos1 = new Vector3(x, y, z);
                    }
                }
            } else {
                pos1 = null;
            }
            
            if (stateMap.containsKey("pos2")) {
                Object pos2Obj = stateMap.get("pos2");
                if (pos2Obj instanceof java.util.Map) {
                    java.util.Map<?, ?> pos2Map = (java.util.Map<?, ?>) pos2Obj;
                    if (pos2Map.containsKey("x") && pos2Map.containsKey("y") && pos2Map.containsKey("z")) {
                        float x = ((Number) pos2Map.get("x")).floatValue();
                        float y = ((Number) pos2Map.get("y")).floatValue();
                        float z = ((Number) pos2Map.get("z")).floatValue();
                        pos2 = new Vector3(x, y, z);
                    }
                }
            } else {
                pos2 = null;
            }
            
            // 更新输出
            if (pos1 != null && pos2 != null) {
                updateOutputsFromPositions();
            } else {
                resetOutputs();
            }
        }
    }
} 