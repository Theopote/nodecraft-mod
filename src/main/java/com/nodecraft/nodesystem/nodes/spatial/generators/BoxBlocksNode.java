package com.nodecraft.nodesystem.nodes.spatial.generators;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.datatypes.RegionData;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import com.nodecraft.nodesystem.util.BlockPosList;

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Box / Cuboid (Blocks) 节点: 生成一个三维立方体区域的Coordinate列表或Region
 */
@NodeInfo(
    id = "spatial.generators.box_blocks",
    displayName = "立方体生成器",
    description = "生成三维立方体区域的坐标列表或区域",
    category = "spatial.generators"
)
public class BoxBlocksNode extends BaseNode {

    // --- 节点属性 ---
    private boolean fillBox = true; // 默认填充立方体
    private boolean outputAsRegion = false; // 默认输出为坐标列表

    // --- 输入端口 IDs ---
    private static final String INPUT_CENTER_ID = "input_center";
    private static final String INPUT_SIZE_X_ID = "input_size_x";
    private static final String INPUT_SIZE_Y_ID = "input_size_y";
    private static final String INPUT_SIZE_Z_ID = "input_size_z";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_BOX_BLOCKS_ID = "output_box_blocks";
    private static final String OUTPUT_REGION_ID = "output_region";

    // --- 构造函数 ---
    public BoxBlocksNode() {
        super(UUID.randomUUID(), "spatial.generators.box_blocks");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_CENTER_ID, "Center", 
                "The center point of the box", NodeDataType.BLOCK_POS, this));
        addInputPort(new BasePort(INPUT_SIZE_X_ID, "Size X", 
                "The width of the box (X axis)", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_SIZE_Y_ID, "Size Y", 
                "The height of the box (Y axis)", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_SIZE_Z_ID, "Size Z", 
                "The depth of the box (Z axis)", NodeDataType.INTEGER, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_BOX_BLOCKS_ID, "Box Blocks", 
                "The blocks forming the box", NodeDataType.BLOCK_LIST, this));
        addOutputPort(new BasePort(OUTPUT_REGION_ID, "Region", 
                "The box as a region", NodeDataType.REGION, this));
    }

    // 添加 getDescription 方法
    @Override
    public String getDescription() {
        return "Generates a 3D box/cuboid of blocks";
    }

    // 添加 getDisplayName 方法
    @Override
    public String getDisplayName() {
        return "Box / Cuboid (Blocks)";
    }

    // --- 核心逻辑 ---
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Object centerObj = inputValues.get(INPUT_CENTER_ID);
        Object sizeXObj = inputValues.get(INPUT_SIZE_X_ID);
        Object sizeYObj = inputValues.get(INPUT_SIZE_Y_ID);
        Object sizeZObj = inputValues.get(INPUT_SIZE_Z_ID);
        
        // 默认空的坐标列表和区域
        BlockPosList blocksList = new BlockPosList();
        RegionData region = null;
        
        // 检查输入是否合法
        if (centerObj instanceof BlockPos && 
            sizeXObj instanceof Number && 
            sizeYObj instanceof Number && 
            sizeZObj instanceof Number) {
            
            BlockPos center = (BlockPos) centerObj;
            int sizeX = ((Number) sizeXObj).intValue();
            int sizeY = ((Number) sizeYObj).intValue();
            int sizeZ = ((Number) sizeZObj).intValue();
            
            // 确保尺寸为正数
            sizeX = Math.max(1, sizeX);
            sizeY = Math.max(1, sizeY);
            sizeZ = Math.max(1, sizeZ);
            
            // 计算立方体的边界
            int halfSizeX = sizeX / 2;
            int halfSizeY = sizeY / 2;
            int halfSizeZ = sizeZ / 2;
            
            // 计算立方体的两个角点
            BlockPos corner1 = new BlockPos(
                center.getX() - halfSizeX,
                center.getY() - halfSizeY,
                center.getZ() - halfSizeZ
            );
            
            BlockPos corner2 = new BlockPos(
                center.getX() + halfSizeX,
                center.getY() + halfSizeY,
                center.getZ() + halfSizeZ
            );
            
            // 创建区域数据
            region = new RegionData(corner1, corner2);
            
            // 如果需要，生成方块列表
            if (!outputAsRegion) {
                if (fillBox) {
                    // 填充立方体
                    for (int x = corner1.getX(); x <= corner2.getX(); x++) {
                        for (int y = corner1.getY(); y <= corner2.getY(); y++) {
                            for (int z = corner1.getZ(); z <= corner2.getZ(); z++) {
                                blocksList.add(new BlockPos(x, y, z));
                            }
                        }
                    }
                } else {
                    // 只生成立方体外壳
                    for (int x = corner1.getX(); x <= corner2.getX(); x++) {
                        for (int y = corner1.getY(); y <= corner2.getY(); y++) {
                            for (int z = corner1.getZ(); z <= corner2.getZ(); z++) {
                                // 只添加表面方块
                                if (x == corner1.getX() || x == corner2.getX() ||
                                    y == corner1.getY() || y == corner2.getY() ||
                                    z == corner1.getZ() || z == corner2.getZ()) {
                                    blocksList.add(new BlockPos(x, y, z));
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_BOX_BLOCKS_ID, blocksList);
        outputValues.put(OUTPUT_REGION_ID, region);
    }
    
    // --- Getters/Setters for Properties ---
    
    public boolean isFillBox() {
        return fillBox;
    }
    
    public void setFillBox(boolean fillBox) {
        this.fillBox = fillBox;
        markDirty();
    }
    
    public boolean isOutputAsRegion() {
        return outputAsRegion;
    }
    
    public void setOutputAsRegion(boolean outputAsRegion) {
        this.outputAsRegion = outputAsRegion;
        markDirty();
    }
    
    // --- 节点状态序列化 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("fillBox", fillBox);
        state.put("outputAsRegion", outputAsRegion);
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("fillBox")) {
                Object fillObj = stateMap.get("fillBox");
                if (fillObj instanceof Boolean) {
                    setFillBox((Boolean) fillObj);
                }
            }
            
            if (stateMap.containsKey("outputAsRegion")) {
                Object outputObj = stateMap.get("outputAsRegion");
                if (outputObj instanceof Boolean) {
                    setOutputAsRegion((Boolean) outputObj);
                }
            }
        }
    }
} 