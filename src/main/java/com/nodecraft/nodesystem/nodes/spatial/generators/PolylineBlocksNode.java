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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Polyline (Blocks) 节点: 在多个点之间生成折线路径
 */
@NodeInfo(
    id = "spatial.generators.polyline_blocks",
    displayName = "折线生成器",
    description = "在多个点之间生成折线路径的坐标列表",
    category = "spatial.generators"
)
public class PolylineBlocksNode extends BaseNode {

    // --- 节点属性 ---
    private boolean useBresenham = true; // 默认使用Bresenham算法
    private boolean closedLoop = false; // 默认为开放折线

    // --- 输入端口 IDs ---
    private static final String INPUT_POINTS_LIST_ID = "input_points_list";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_POLYLINE_BLOCKS_ID = "output_polyline_blocks";

    // --- 构造函数 ---
    public PolylineBlocksNode() {
        super(UUID.randomUUID(), "spatial.generators.polyline_blocks");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_POINTS_LIST_ID, "Points", 
                "The list of points to connect", NodeDataType.BLOCK_LIST, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_POLYLINE_BLOCKS_ID, "Polyline Blocks", 
                "The blocks along the polyline path", NodeDataType.BLOCK_LIST, this));
    }

    // 添加 getDescription 方法
    @Override
    public String getDescription() {
        return "Generates a path of blocks connecting multiple points";
    }

    // 添加 getDisplayName 方法
    @Override
    public String getDisplayName() {
        return "Polyline (Blocks)";
    }

    // --- 核心逻辑 ---
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Object pointsObj = inputValues.get(INPUT_POINTS_LIST_ID);
        
        // 默认空的坐标列表
        BlockPosList result = new BlockPosList();
        
        // 检查输入是否为方块坐标列表
        if (pointsObj instanceof BlockPosList) {
            BlockPosList points = (BlockPosList) pointsObj;
            
            // 确保至少有两个点
            if (points.size() >= 2) {
                List<BlockPos> pointsList = points.getPositions();
                
                // 从第一个点到最后一个点，连接每相邻两点
                for (int i = 0; i < pointsList.size() - 1; i++) {
                    BlockPos start = pointsList.get(i);
                    BlockPos end = pointsList.get(i + 1);
                    
                    // 生成线段并添加到结果中
                    generateLineSegment(start, end, result);
                }
                
                // 如果是闭合折线，连接最后一个点和第一个点
                if (closedLoop && pointsList.size() > 2) {
                    BlockPos start = pointsList.get(pointsList.size() - 1);
                    BlockPos end = pointsList.get(0);
                    
                    // 生成线段并添加到结果中
                    generateLineSegment(start, end, result);
                }
            }
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_POLYLINE_BLOCKS_ID, result);
    }
    
    /**
     * 在两点间生成线段
     */
    private void generateLineSegment(BlockPos start, BlockPos end, BlockPosList result) {
        if (useBresenham) {
            generateBresenhamLine(start, end, result);
        } else {
            generateParametricLine(start, end, result);
        }
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
    
    public boolean isClosedLoop() {
        return closedLoop;
    }
    
    public void setClosedLoop(boolean closedLoop) {
        this.closedLoop = closedLoop;
        markDirty();
    }
    
    // --- 节点状态序列化 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("useBresenham", useBresenham);
        state.put("closedLoop", closedLoop);
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
            
            if (stateMap.containsKey("closedLoop")) {
                Object closedLoopObj = stateMap.get("closedLoop");
                if (closedLoopObj instanceof Boolean) {
                    setClosedLoop((Boolean) closedLoopObj);
                }
            }
        }
    }
} 