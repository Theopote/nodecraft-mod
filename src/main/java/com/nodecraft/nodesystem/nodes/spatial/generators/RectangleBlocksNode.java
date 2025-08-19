package com.nodecraft.nodesystem.nodes.spatial.generators;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import com.nodecraft.nodesystem.util.BlockPosList;

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Rectangle (Blocks) 节点: 生成一个平面矩形区域的Coordinate列表
 */
@NodeInfo(
    id = "spatial.generators.rectangle_blocks",
    displayName = "矩形生成器",
    description = "生成二维矩形区域的坐标列表",
    category = "spatial.generators"
)
public class RectangleBlocksNode extends BaseNode {

    // --- 节点属性 ---
    private boolean fillRectangle = true; // 默认填充矩形
    public enum Plane {
        XY, YZ, XZ
    }
    private Plane plane = Plane.XZ; // 默认在XZ平面（地面平面）

    // --- 输入端口 IDs ---
    private static final String INPUT_CENTER_ID = "input_center";
    private static final String INPUT_WIDTH_ID = "input_width";
    private static final String INPUT_HEIGHT_ID = "input_height";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_RECTANGLE_BLOCKS_ID = "output_rectangle_blocks";

    // --- 构造函数 ---
    public RectangleBlocksNode() {
        super(UUID.randomUUID(), "spatial.generators.rectangle_blocks");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_CENTER_ID, "Center", 
                "The center point of the rectangle", NodeDataType.BLOCK_POS, this));
        addInputPort(new BasePort(INPUT_WIDTH_ID, "Width", 
                "The width of the rectangle", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_HEIGHT_ID, "Height", 
                "The height of the rectangle", NodeDataType.INTEGER, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_RECTANGLE_BLOCKS_ID, "Rectangle Blocks", 
                "The blocks forming the rectangle", NodeDataType.BLOCK_LIST, this));
    }

    // 添加 getDescription 方法
    @Override
    public String getDescription() {
        return "Generates a planar rectangle of blocks";
    }

    // 添加 getDisplayName 方法
    @Override
    public String getDisplayName() {
        return "Rectangle (Blocks)";
    }

    // --- 核心逻辑 ---
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Object centerObj = inputValues.get(INPUT_CENTER_ID);
        Object widthObj = inputValues.get(INPUT_WIDTH_ID);
        Object heightObj = inputValues.get(INPUT_HEIGHT_ID);
        
        // 默认空的坐标列表
        BlockPosList result = new BlockPosList();
        
        // 检查输入是否合法
        if (centerObj instanceof BlockPos && 
            widthObj instanceof Number && 
            heightObj instanceof Number) {
            
            BlockPos center = (BlockPos) centerObj;
            int width = ((Number) widthObj).intValue();
            int height = ((Number) heightObj).intValue();
            
            // 确保宽度和高度为正数
            width = Math.max(1, width);
            height = Math.max(1, height);
            
            // 计算矩形的边界
            int halfWidth = width / 2;
            int halfHeight = height / 2;
            
            // 根据选择的平面生成矩形
            if (fillRectangle) {
                generateFilledRectangle(center, halfWidth, halfHeight, result);
            } else {
                generateOutlineRectangle(center, halfWidth, halfHeight, result);
            }
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_RECTANGLE_BLOCKS_ID, result);
    }
    
    /**
     * 生成填充的矩形
     */
    private void generateFilledRectangle(BlockPos center, int halfWidth, int halfHeight, BlockPosList result) {
        int x = center.getX();
        int y = center.getY();
        int z = center.getZ();
        
        // 根据选择的平面生成矩形
        switch (plane) {
            case XY: // XY平面 (固定Z坐标)
                for (int dx = -halfWidth; dx <= halfWidth; dx++) {
                    for (int dy = -halfHeight; dy <= halfHeight; dy++) {
                        result.add(new BlockPos(x + dx, y + dy, z));
                    }
                }
                break;
            case YZ: // YZ平面 (固定X坐标)
                for (int dy = -halfHeight; dy <= halfHeight; dy++) {
                    for (int dz = -halfWidth; dz <= halfWidth; dz++) {
                        result.add(new BlockPos(x, y + dy, z + dz));
                    }
                }
                break;
            case XZ: // XZ平面 (固定Y坐标)
            default:
                for (int dx = -halfWidth; dx <= halfWidth; dx++) {
                    for (int dz = -halfHeight; dz <= halfHeight; dz++) {
                        result.add(new BlockPos(x + dx, y, z + dz));
                    }
                }
                break;
        }
    }
    
    /**
     * 生成轮廓矩形
     */
    private void generateOutlineRectangle(BlockPos center, int halfWidth, int halfHeight, BlockPosList result) {
        int x = center.getX();
        int y = center.getY();
        int z = center.getZ();
        
        // 根据选择的平面生成矩形
        switch (plane) {
            case XY: // XY平面 (固定Z坐标)
                // 水平边
                for (int dx = -halfWidth; dx <= halfWidth; dx++) {
                    result.add(new BlockPos(x + dx, y - halfHeight, z)); // 底边
                    result.add(new BlockPos(x + dx, y + halfHeight, z)); // 顶边
                }
                // 垂直边 (避免重复角点)
                for (int dy = -halfHeight + 1; dy < halfHeight; dy++) {
                    result.add(new BlockPos(x - halfWidth, y + dy, z)); // 左边
                    result.add(new BlockPos(x + halfWidth, y + dy, z)); // 右边
                }
                break;
            case YZ: // YZ平面 (固定X坐标)
                // 水平边
                for (int dz = -halfWidth; dz <= halfWidth; dz++) {
                    result.add(new BlockPos(x, y - halfHeight, z + dz)); // 底边
                    result.add(new BlockPos(x, y + halfHeight, z + dz)); // 顶边
                }
                // 垂直边 (避免重复角点)
                for (int dy = -halfHeight + 1; dy < halfHeight; dy++) {
                    result.add(new BlockPos(x, y + dy, z - halfWidth)); // 左边
                    result.add(new BlockPos(x, y + dy, z + halfWidth)); // 右边
                }
                break;
            case XZ: // XZ平面 (固定Y坐标)
            default:
                // 水平边
                for (int dx = -halfWidth; dx <= halfWidth; dx++) {
                    result.add(new BlockPos(x + dx, y, z - halfHeight)); // 底边
                    result.add(new BlockPos(x + dx, y, z + halfHeight)); // 顶边
                }
                // 垂直边 (避免重复角点)
                for (int dz = -halfHeight + 1; dz < halfHeight; dz++) {
                    result.add(new BlockPos(x - halfWidth, y, z + dz)); // 左边
                    result.add(new BlockPos(x + halfWidth, y, z + dz)); // 右边
                }
                break;
        }
    }
    
    // --- Getters/Setters for Properties ---
    
    public boolean isFillRectangle() {
        return fillRectangle;
    }
    
    public void setFillRectangle(boolean fillRectangle) {
        this.fillRectangle = fillRectangle;
        markDirty();
    }
    
    public Plane getPlane() {
        return plane;
    }
    
    public void setPlane(Plane plane) {
        this.plane = plane;
        markDirty();
    }
    
    // --- 节点状态序列化 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("fillRectangle", fillRectangle);
        state.put("plane", plane.name());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("fillRectangle")) {
                Object fillObj = stateMap.get("fillRectangle");
                if (fillObj instanceof Boolean) {
                    setFillRectangle((Boolean) fillObj);
                }
            }
            
            if (stateMap.containsKey("plane")) {
                Object planeObj = stateMap.get("plane");
                if (planeObj instanceof String) {
                    try {
                        setPlane(Plane.valueOf((String) planeObj));
                    } catch (IllegalArgumentException e) {
                        // 忽略无效的枚举值
                    }
                }
            }
        }
    }
} 