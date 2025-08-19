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

import java.util.List;
import java.util.UUID;

/**
 * Curve (Blocks) 节点: 生成平滑曲线并采样为Coordinate列表
 */
@NodeInfo(
    id = "spatial.generators.curve_blocks",
    displayName = "曲线生成器",
    description = "生成平滑曲线并采样为坐标列表",
    category = "spatial.generators"
)
public class CurveBlocksNode extends BaseNode {

    // --- 节点属性 ---
    public enum CurveType {
        BEZIER,
        CATMULL_ROM,
        B_SPLINE
    }
    
    private CurveType curveType = CurveType.BEZIER; // 默认使用贝塞尔曲线
    private int resolution = 20; // 默认分辨率

    // --- 输入端口 IDs ---
    private static final String INPUT_CONTROL_POINTS_ID = "input_control_points";
    private static final String INPUT_RESOLUTION_ID = "input_resolution";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_CURVE_BLOCKS_ID = "output_curve_blocks";

    // --- 构造函数 ---
    public CurveBlocksNode() {
        super(UUID.randomUUID(), "spatial.generators.curve_blocks");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_CONTROL_POINTS_ID, "Control Points", 
                "The control points defining the curve", NodeDataType.BLOCK_LIST, this));
        addInputPort(new BasePort(INPUT_RESOLUTION_ID, "Resolution", 
                "Number of segments to sample (higher = smoother)", NodeDataType.INTEGER, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_CURVE_BLOCKS_ID, "Curve Blocks", 
                "The blocks along the curve path", NodeDataType.BLOCK_LIST, this));
    }

    // 添加 getDescription 方法
    @Override
    public String getDescription() {
        return "Generates a smooth curve passing through or approximating control points";
    }

    // 添加 getDisplayName 方法
    @Override
    public String getDisplayName() {
        return "Curve (Blocks)";
    }

    // --- 核心逻辑 ---
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Object pointsObj = inputValues.get(INPUT_CONTROL_POINTS_ID);
        Object resolutionObj = inputValues.get(INPUT_RESOLUTION_ID);
        
        // 默认空的坐标列表
        BlockPosList result = new BlockPosList();
        
        // 检查输入是否为方块坐标列表
        if (pointsObj instanceof BlockPosList) {
            BlockPosList points = (BlockPosList) pointsObj;
            
            // 根据曲线类型所需的最小控制点数
            int minPoints = (curveType == CurveType.BEZIER) ? 2 : 
                           (curveType == CurveType.CATMULL_ROM) ? 3 : 4;
            
            // 确保有足够的控制点
            if (points.size() >= minPoints) {
                // 设置分辨率
                int curveResolution = this.resolution;
                if (resolutionObj instanceof Number) {
                    curveResolution = ((Number) resolutionObj).intValue();
                    curveResolution = Math.max(2, curveResolution); // 至少需要2个采样点
                }
                
                // 转换控制点为Vector3d列表，便于计算
                List<BlockPos> pointsList = points.getPositions();
                Vector3d[] controlPoints = new Vector3d[pointsList.size()];
                for (int i = 0; i < pointsList.size(); i++) {
                    BlockPos pos = pointsList.get(i);
                    controlPoints[i] = new Vector3d(pos.getX(), pos.getY(), pos.getZ());
                }
                
                // 计算曲线上的点
                switch (curveType) {
                    case BEZIER:
                        generateBezierCurve(controlPoints, curveResolution, result);
                        break;
                    case CATMULL_ROM:
                        generateCatmullRomCurve(controlPoints, curveResolution, result);
                        break;
                    case B_SPLINE:
                        generateBSplineCurve(controlPoints, curveResolution, result);
                        break;
                }
            }
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_CURVE_BLOCKS_ID, result);
    }
    
    /**
     * 生成贝塞尔曲线
     */
    private void generateBezierCurve(Vector3d[] controlPoints, int resolution, BlockPosList result) {
        // 分段计算采样点
        for (int i = 0; i <= resolution; i++) {
            double t = (double) i / resolution;
            
            // 贝塞尔曲线插值
            Vector3d point = bezierInterpolation(controlPoints, t);
            
            // 转换为方块坐标并添加到结果
            BlockPos blockPos = new BlockPos(
                (int) Math.round(point.x),
                (int) Math.round(point.y),
                (int) Math.round(point.z)
            );
            
            // 避免连续添加相同的方块
            if (i == 0 || result.size() == 0 || !blockPos.equals(result.getPositions().get(result.size() - 1))) {
                result.add(blockPos);
            }
        }
    }
    
    /**
     * 贝塞尔曲线插值（使用de Casteljau算法）
     */
    private Vector3d bezierInterpolation(Vector3d[] controlPoints, double t) {
        // 创建控制点的副本，进行原位修改
        Vector3d[] points = new Vector3d[controlPoints.length];
        for (int i = 0; i < controlPoints.length; i++) {
            points[i] = new Vector3d(controlPoints[i]);
        }
        
        // de Casteljau算法
        for (int r = 1; r < points.length; r++) {
            for (int i = 0; i < points.length - r; i++) {
                points[i].x = (1 - t) * points[i].x + t * points[i + 1].x;
                points[i].y = (1 - t) * points[i].y + t * points[i + 1].y;
                points[i].z = (1 - t) * points[i].z + t * points[i + 1].z;
            }
        }
        
        return points[0];
    }
    
    /**
     * 生成Catmull-Rom样条曲线（过控制点的平滑曲线）
     */
    private void generateCatmullRomCurve(Vector3d[] controlPoints, int resolution, BlockPosList result) {
        // 如果控制点太少，降级为折线
        if (controlPoints.length < 3) {
            BlockPos start = new BlockPos(
                (int) Math.round(controlPoints[0].x),
                (int) Math.round(controlPoints[0].y),
                (int) Math.round(controlPoints[0].z)
            );
            BlockPos end = new BlockPos(
                (int) Math.round(controlPoints[1].x),
                (int) Math.round(controlPoints[1].y),
                (int) Math.round(controlPoints[1].z)
            );
            
            generateParametricLine(start, end, result);
            return;
        }
        
        // 对每段曲线进行采样
        for (int i = 0; i < controlPoints.length - 3; i++) {
            Vector3d p0 = controlPoints[i];
            Vector3d p1 = controlPoints[i + 1];
            Vector3d p2 = controlPoints[i + 2];
            Vector3d p3 = controlPoints[i + 3];
            
            // 计算段内的采样点
            for (int j = 0; j <= resolution; j++) {
                double t = (double) j / resolution;
                
                // Catmull-Rom曲线插值
                Vector3d point = catmullRomInterpolation(p0, p1, p2, p3, t);
                
                // 转换为方块坐标并添加到结果
                BlockPos blockPos = new BlockPos(
                    (int) Math.round(point.x),
                    (int) Math.round(point.y),
                    (int) Math.round(point.z)
                );
                
                // 避免连续添加相同的方块
                if ((i == 0 && j == 0) || result.size() == 0 || !blockPos.equals(result.getPositions().get(result.size() - 1))) {
                    result.add(blockPos);
                }
            }
        }
    }
    
    /**
     * Catmull-Rom曲线插值
     */
    private Vector3d catmullRomInterpolation(Vector3d p0, Vector3d p1, Vector3d p2, Vector3d p3, double t) {
        double t2 = t * t;
        double t3 = t2 * t;
        
        // Catmull-Rom插值公式
        double x = 0.5 * ((2 * p1.x) + 
                      (-p0.x + p2.x) * t + 
                      (2 * p0.x - 5 * p1.x + 4 * p2.x - p3.x) * t2 + 
                      (-p0.x + 3 * p1.x - 3 * p2.x + p3.x) * t3);
        
        double y = 0.5 * ((2 * p1.y) + 
                      (-p0.y + p2.y) * t + 
                      (2 * p0.y - 5 * p1.y + 4 * p2.y - p3.y) * t2 + 
                      (-p0.y + 3 * p1.y - 3 * p2.y + p3.y) * t3);
        
        double z = 0.5 * ((2 * p1.z) + 
                      (-p0.z + p2.z) * t + 
                      (2 * p0.z - 5 * p1.z + 4 * p2.z - p3.z) * t2 + 
                      (-p0.z + 3 * p1.z - 3 * p2.z + p3.z) * t3);
        
        return new Vector3d(x, y, z);
    }
    
    /**
     * 生成B样条曲线（不经过控制点但平滑的曲线）
     */
    private void generateBSplineCurve(Vector3d[] controlPoints, int resolution, BlockPosList result) {
        // 如果控制点太少，降级为贝塞尔曲线
        if (controlPoints.length < 4) {
            generateBezierCurve(controlPoints, resolution, result);
            return;
        }
        
        // 均匀三次B样条需要至少4个控制点
        // 对每段曲线进行采样
        for (int i = 0; i <= controlPoints.length - 4; i++) {
            Vector3d p0 = controlPoints[i];
            Vector3d p1 = controlPoints[i + 1];
            Vector3d p2 = controlPoints[i + 2];
            Vector3d p3 = controlPoints[i + 3];
            
            // 计算段内的采样点
            for (int j = 0; j <= resolution; j++) {
                double t = (double) j / resolution;
                
                // 均匀三次B样条插值
                Vector3d point = bSplineInterpolation(p0, p1, p2, p3, t);
                
                // 转换为方块坐标并添加到结果
                BlockPos blockPos = new BlockPos(
                    (int) Math.round(point.x),
                    (int) Math.round(point.y),
                    (int) Math.round(point.z)
                );
                
                // 避免连续添加相同的方块
                if ((i == 0 && j == 0) || result.size() == 0 || !blockPos.equals(result.getPositions().get(result.size() - 1))) {
                    result.add(blockPos);
                }
            }
        }
    }
    
    /**
     * 均匀三次B样条插值
     */
    private Vector3d bSplineInterpolation(Vector3d p0, Vector3d p1, Vector3d p2, Vector3d p3, double t) {
        double t2 = t * t;
        double t3 = t2 * t;
        
        // 均匀三次B样条基函数
        double b0 = (1 - t) * (1 - t) * (1 - t) / 6.0;
        double b1 = (3 * t3 - 6 * t2 + 4) / 6.0;
        double b2 = (-3 * t3 + 3 * t2 + 3 * t + 1) / 6.0;
        double b3 = t3 / 6.0;
        
        // 计算曲线上的点
        double x = b0 * p0.x + b1 * p1.x + b2 * p2.x + b3 * p3.x;
        double y = b0 * p0.y + b1 * p1.y + b2 * p2.y + b3 * p3.y;
        double z = b0 * p0.z + b1 * p1.z + b2 * p2.z + b3 * p3.z;
        
        return new Vector3d(x, y, z);
    }
    
    /**
     * 使用参数化线段方程生成线段（用于降级处理）
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
            if (i == 0 || result.size() == 0 || !blockPos.equals(result.getPositions().get(result.size() - 1))) {
                result.add(blockPos);
            }
        }
    }
    
    // --- Getters/Setters for Properties ---
    
    public CurveType getCurveType() {
        return curveType;
    }
    
    public void setCurveType(CurveType curveType) {
        this.curveType = curveType;
        markDirty();
    }
    
    public int getResolution() {
        return resolution;
    }
    
    public void setResolution(int resolution) {
        this.resolution = Math.max(2, resolution); // 至少2个采样点
        markDirty();
    }
    
    // --- 节点状态序列化 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("curveType", curveType.name());
        state.put("resolution", resolution);
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("curveType")) {
                Object typeObj = stateMap.get("curveType");
                if (typeObj instanceof String) {
                    try {
                        setCurveType(CurveType.valueOf((String) typeObj));
                    } catch (IllegalArgumentException e) {
                        // 忽略无效的枚举值
                    }
                }
            }
            
            if (stateMap.containsKey("resolution")) {
                Object resObj = stateMap.get("resolution");
                if (resObj instanceof Number) {
                    setResolution(((Number) resObj).intValue());
                }
            }
        }
    }
} 