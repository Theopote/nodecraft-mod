package com.nodecraft.nodesystem.nodes.spatial.generators;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import com.nodecraft.nodesystem.util.BlockPosList;

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import java.util.UUID;

/**
 * Line (Blocks) 节点: 生成两点之间的直线坐标列表
 */
@NodeInfo(
    id = "spatial.generators.line_blocks",
    displayName = "直线生成器",
    description = "生成两点之间的直线坐标列表",
    category = "spatial.generators"
)
public class LineBlocksNode extends BaseNode {

    // --- 节点属性 ---
    private boolean useBresenham = true; // 默认使用Bresenham算法

    // --- 输入端口 IDs ---
    private static final String INPUT_START_POINT_ID = "input_start_point";
    private static final String INPUT_END_POINT_ID = "input_end_point";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_LINE_BLOCKS_ID = "output_line_blocks";

    // --- 构造函数 ---
    public LineBlocksNode() {
        super(UUID.randomUUID(), "spatial.generators.line_blocks");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_START_POINT_ID, "Start Point", 
                "The start point of the line", NodeDataType.BLOCK_POS, this));
        addInputPort(new BasePort(INPUT_END_POINT_ID, "End Point", 
                "The end point of the line", NodeDataType.BLOCK_POS, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_LINE_BLOCKS_ID, "Line Blocks", 
                "The blocks along the line path", NodeDataType.BLOCK_LIST, this));
    }

    // 添加 getDescription 方法
    @Override
    public String getDescription() {
        return "Generates a path of blocks between two points";
    }

    // 添加 getDisplayName 方法
    @Override
    public String getDisplayName() {
        return "Line (Blocks)";
    }

    // --- 核心逻辑 ---
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Object startObj = inputValues.get(INPUT_START_POINT_ID);
        Object endObj = inputValues.get(INPUT_END_POINT_ID);
        
        // 默认空的坐标列表
        BlockPosList result = new BlockPosList();
        
        // 检查输入是否为方块坐标
        if (startObj instanceof BlockPos && endObj instanceof BlockPos) {
            BlockPos start = (BlockPos) startObj;
            BlockPos end = (BlockPos) endObj;
            
            // 根据选择的算法生成线段
            if (useBresenham) {
                generateBresenhamLine(start, end, result);
            } else {
                generateParametricLine(start, end, result);
            }
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_LINE_BLOCKS_ID, result);
    }
    
    /**
     * 使用Bresenham算法生成线段（适合于整数坐标）
     */
    private void generateBresenhamLine(BlockPos start, BlockPos end, BlockPosList result) {
        // Bresenham's 3D线段算法
        int x1 = start.getX(), y1 = start.getY(), z1 = start.getZ();
        int x2 = end.getX(), y2 = end.getY(), z2 = end.getZ();
        
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int dz = Math.abs(z2 - z1);
        
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int sz = z1 < z2 ? 1 : -1;
        
        int dm = Math.max(Math.max(dx, dy), dz);
        if (dm == 0) {
            // 起点和终点是同一个点
            result.add(new BlockPos(x1, y1, z1));
            return;
        }
        
        int x = x1, y = y1, z = z1;
        
        // 添加起点
        result.add(new BlockPos(x, y, z));
        
        // 遍历线段上的点
        for (int i = 0; i < dm; i++) {
            int err1 = (i + 1) * dx - dm;
            int err2 = (i + 1) * dy - dm;
            int err3 = (i + 1) * dz - dm;
            
            if (err1 > 0) x += sx;
            if (err2 > 0) y += sy;
            if (err3 > 0) z += sz;
            
            result.add(new BlockPos(x, y, z));
        }
    }
    
    /**
     * 使用参数化线段方程生成线段（适合于精确的线段）
     */
    private void generateParametricLine(BlockPos start, BlockPos end, BlockPosList result) {
        // 参数化线段方程: P(t) = P0 + t(P1 - P0), t∈[0,1]
        Vector3d startVec = new Vector3d(start.getX(), start.getY(), start.getZ());
        Vector3d endVec = new Vector3d(end.getX(), end.getY(), end.getZ());
        Vector3d dirVec = new Vector3d(endVec).sub(startVec);
        
        // 计算线段长度（曼哈顿距离）
        int distance = Math.abs(end.getX() - start.getX()) + 
                       Math.abs(end.getY() - start.getY()) + 
                       Math.abs(end.getZ() - start.getZ());
        distance = Math.max(distance, 1); // 确保至少有一个步长
        
        // 沿着线段均匀采样
        for (int i = 0; i <= distance; i++) {
            double t = (double) i / distance;
            Vector3d pos = new Vector3d(startVec).add(new Vector3d(dirVec).mul(t));
            
            // 转换为方块坐标（四舍五入到最接近的整数）
            BlockPos blockPos = new BlockPos(
                (int) Math.round(pos.x),
                (int) Math.round(pos.y),
                (int) Math.round(pos.z)
            );
            
            // 避免重复添加相同的方块坐标
            if (i == 0 || !blockPos.equals(result.getPositions().get(result.size() - 1))) {
                result.add(blockPos);
            }
        }
    }
    
    // --- Getters/Setters for Properties ---
    
    public boolean isUseBresenham() {
        return useBresenham;
    }
    
    public void setUseBresenham(boolean useBresenham) {
        this.useBresenham = useBresenham;
        markDirty();
    }
    
    // --- 节点状态序列化 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("useBresenham", useBresenham);
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("useBresenham")) {
                Object useBresObj = stateMap.get("useBresenham");
                if (useBresObj instanceof Boolean) {
                    setUseBresenham((Boolean) useBresObj);
                }
            }
        }
    }
} 