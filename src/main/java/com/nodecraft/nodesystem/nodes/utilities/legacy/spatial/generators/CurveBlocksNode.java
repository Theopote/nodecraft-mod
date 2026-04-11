package com.nodecraft.nodesystem.nodes.utilities.legacy.spatial.generators;

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
 * Curve (Blocks) 鑺傜偣: 鐢熸垚骞虫粦鏇茬嚎骞堕噰鏍蜂负Coordinate鍒楄〃
 */
@NodeInfo(
    id = "spatial.generators.curve_blocks",
    displayName = "鏇茬嚎鐢熸垚鍣?,
    description = "鐢熸垚骞虫粦鏇茬嚎骞堕噰鏍蜂负鍧愭爣鍒楄〃",
    category = "spatial.generators"
)
public class CurveBlocksNode extends BaseNode {

    // --- 鑺傜偣灞炴€?---
    public enum CurveType {
        BEZIER,
        CATMULL_ROM,
        B_SPLINE
    }
    
    private CurveType curveType = CurveType.BEZIER; // 榛樿浣跨敤璐濆灏旀洸绾?
    private int resolution = 20; // 榛樿鍒嗚鲸鐜?

    // --- 杈撳叆绔彛 IDs ---
    private static final String INPUT_CONTROL_POINTS_ID = "input_control_points";
    private static final String INPUT_RESOLUTION_ID = "input_resolution";

    // --- 杈撳嚭绔彛 IDs ---
    private static final String OUTPUT_CURVE_BLOCKS_ID = "output_curve_blocks";

    // --- 鏋勯€犲嚱鏁?---
    public CurveBlocksNode() {
        super(UUID.randomUUID(), "spatial.generators.curve_blocks");
        
        // 鍒涘缓骞舵坊鍔犺緭鍏ョ鍙?
        addInputPort(new BasePort(INPUT_CONTROL_POINTS_ID, "Control Points", 
                "The control points defining the curve", NodeDataType.BLOCK_LIST, this));
        addInputPort(new BasePort(INPUT_RESOLUTION_ID, "Resolution", 
                "Number of segments to sample (higher = smoother)", NodeDataType.INTEGER, this));

        // 鍒涘缓骞舵坊鍔犺緭鍑虹鍙?
        addOutputPort(new BasePort(OUTPUT_CURVE_BLOCKS_ID, "Curve Blocks", 
                "The blocks along the curve path", NodeDataType.BLOCK_LIST, this));
    }

    // 娣诲姞 getDescription 鏂规硶
    @Override
    public String getDescription() {
        return "Generates a smooth curve passing through or approximating control points";
    }

    // 娣诲姞 getDisplayName 鏂规硶
    @Override
    public String getDisplayName() {
        return "Curve (Blocks)";
    }

    // --- 鏍稿績閫昏緫 ---
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 鑾峰彇杈撳叆鍊?
        Object pointsObj = inputValues.get(INPUT_CONTROL_POINTS_ID);
        Object resolutionObj = inputValues.get(INPUT_RESOLUTION_ID);
        
        // 榛樿绌虹殑鍧愭爣鍒楄〃
        BlockPosList result = new BlockPosList();
        
        // 妫€鏌ヨ緭鍏ユ槸鍚︿负鏂瑰潡鍧愭爣鍒楄〃
        if (pointsObj instanceof BlockPosList) {
            BlockPosList points = (BlockPosList) pointsObj;
            
            // 鏍规嵁鏇茬嚎绫诲瀷鎵€闇€鐨勬渶灏忔帶鍒剁偣鏁?
            int minPoints = (curveType == CurveType.BEZIER) ? 2 : 
                           (curveType == CurveType.CATMULL_ROM) ? 3 : 4;
            
            // 纭繚鏈夎冻澶熺殑鎺у埗鐐?
            if (points.size() >= minPoints) {
                // 璁剧疆鍒嗚鲸鐜?
                int curveResolution = this.resolution;
                if (resolutionObj instanceof Number) {
                    curveResolution = ((Number) resolutionObj).intValue();
                    curveResolution = Math.max(2, curveResolution); // 鑷冲皯闇€瑕?涓噰鏍风偣
                }
                
                // 杞崲鎺у埗鐐逛负Vector3d鍒楄〃锛屼究浜庤绠?
                List<BlockPos> pointsList = points.getPositions();
                Vector3d[] controlPoints = new Vector3d[pointsList.size()];
                for (int i = 0; i < pointsList.size(); i++) {
                    BlockPos pos = pointsList.get(i);
                    controlPoints[i] = new Vector3d(pos.getX(), pos.getY(), pos.getZ());
                }
                
                // 璁＄畻鏇茬嚎涓婄殑鐐?
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
        
        // 璁剧疆杈撳嚭鍊?
        outputValues.put(OUTPUT_CURVE_BLOCKS_ID, result);
    }
    
    /**
     * 鐢熸垚璐濆灏旀洸绾?
     */
    private void generateBezierCurve(Vector3d[] controlPoints, int resolution, BlockPosList result) {
        // 鍒嗘璁＄畻閲囨牱鐐?
        for (int i = 0; i <= resolution; i++) {
            double t = (double) i / resolution;
            
            // 璐濆灏旀洸绾挎彃鍊?
            Vector3d point = bezierInterpolation(controlPoints, t);
            
            // 杞崲涓烘柟鍧楀潗鏍囧苟娣诲姞鍒扮粨鏋?
            BlockPos blockPos = new BlockPos(
                (int) Math.round(point.x),
                (int) Math.round(point.y),
                (int) Math.round(point.z)
            );
            
            // 閬垮厤杩炵画娣诲姞鐩稿悓鐨勬柟鍧?
            if (i == 0 || result.size() == 0 || !blockPos.equals(result.getPositions().get(result.size() - 1))) {
                result.add(blockPos);
            }
        }
    }
    
    /**
     * 璐濆灏旀洸绾挎彃鍊硷紙浣跨敤de Casteljau绠楁硶锛?
     */
    private Vector3d bezierInterpolation(Vector3d[] controlPoints, double t) {
        // 鍒涘缓鎺у埗鐐圭殑鍓湰锛岃繘琛屽師浣嶄慨鏀?
        Vector3d[] points = new Vector3d[controlPoints.length];
        for (int i = 0; i < controlPoints.length; i++) {
            points[i] = new Vector3d(controlPoints[i]);
        }
        
        // de Casteljau绠楁硶
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
     * 鐢熸垚Catmull-Rom鏍锋潯鏇茬嚎锛堣繃鎺у埗鐐圭殑骞虫粦鏇茬嚎锛?
     */
    private void generateCatmullRomCurve(Vector3d[] controlPoints, int resolution, BlockPosList result) {
        // 濡傛灉鎺у埗鐐瑰お灏戯紝闄嶇骇涓烘姌绾?
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
        
        // 瀵规瘡娈垫洸绾胯繘琛岄噰鏍?
        for (int i = 0; i < controlPoints.length - 3; i++) {
            Vector3d p0 = controlPoints[i];
            Vector3d p1 = controlPoints[i + 1];
            Vector3d p2 = controlPoints[i + 2];
            Vector3d p3 = controlPoints[i + 3];
            
            // 璁＄畻娈靛唴鐨勯噰鏍风偣
            for (int j = 0; j <= resolution; j++) {
                double t = (double) j / resolution;
                
                // Catmull-Rom鏇茬嚎鎻掑€?
                Vector3d point = catmullRomInterpolation(p0, p1, p2, p3, t);
                
                // 杞崲涓烘柟鍧楀潗鏍囧苟娣诲姞鍒扮粨鏋?
                BlockPos blockPos = new BlockPos(
                    (int) Math.round(point.x),
                    (int) Math.round(point.y),
                    (int) Math.round(point.z)
                );
                
                // 閬垮厤杩炵画娣诲姞鐩稿悓鐨勬柟鍧?
                if ((i == 0 && j == 0) || result.size() == 0 || !blockPos.equals(result.getPositions().get(result.size() - 1))) {
                    result.add(blockPos);
                }
            }
        }
    }
    
    /**
     * Catmull-Rom鏇茬嚎鎻掑€?
     */
    private Vector3d catmullRomInterpolation(Vector3d p0, Vector3d p1, Vector3d p2, Vector3d p3, double t) {
        double t2 = t * t;
        double t3 = t2 * t;
        
        // Catmull-Rom鎻掑€煎叕寮?
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
     * 鐢熸垚B鏍锋潯鏇茬嚎锛堜笉缁忚繃鎺у埗鐐逛絾骞虫粦鐨勬洸绾匡級
     */
    private void generateBSplineCurve(Vector3d[] controlPoints, int resolution, BlockPosList result) {
        // 濡傛灉鎺у埗鐐瑰お灏戯紝闄嶇骇涓鸿礉濉炲皵鏇茬嚎
        if (controlPoints.length < 4) {
            generateBezierCurve(controlPoints, resolution, result);
            return;
        }
        
        // 鍧囧寑涓夋B鏍锋潯闇€瑕佽嚦灏?涓帶鍒剁偣
        // 瀵规瘡娈垫洸绾胯繘琛岄噰鏍?
        for (int i = 0; i <= controlPoints.length - 4; i++) {
            Vector3d p0 = controlPoints[i];
            Vector3d p1 = controlPoints[i + 1];
            Vector3d p2 = controlPoints[i + 2];
            Vector3d p3 = controlPoints[i + 3];
            
            // 璁＄畻娈靛唴鐨勯噰鏍风偣
            for (int j = 0; j <= resolution; j++) {
                double t = (double) j / resolution;
                
                // 鍧囧寑涓夋B鏍锋潯鎻掑€?
                Vector3d point = bSplineInterpolation(p0, p1, p2, p3, t);
                
                // 杞崲涓烘柟鍧楀潗鏍囧苟娣诲姞鍒扮粨鏋?
                BlockPos blockPos = new BlockPos(
                    (int) Math.round(point.x),
                    (int) Math.round(point.y),
                    (int) Math.round(point.z)
                );
                
                // 閬垮厤杩炵画娣诲姞鐩稿悓鐨勬柟鍧?
                if ((i == 0 && j == 0) || result.size() == 0 || !blockPos.equals(result.getPositions().get(result.size() - 1))) {
                    result.add(blockPos);
                }
            }
        }
    }
    
    /**
     * 鍧囧寑涓夋B鏍锋潯鎻掑€?
     */
    private Vector3d bSplineInterpolation(Vector3d p0, Vector3d p1, Vector3d p2, Vector3d p3, double t) {
        double t2 = t * t;
        double t3 = t2 * t;
        
        // 鍧囧寑涓夋B鏍锋潯鍩哄嚱鏁?
        double b0 = (1 - t) * (1 - t) * (1 - t) / 6.0;
        double b1 = (3 * t3 - 6 * t2 + 4) / 6.0;
        double b2 = (-3 * t3 + 3 * t2 + 3 * t + 1) / 6.0;
        double b3 = t3 / 6.0;
        
        // 璁＄畻鏇茬嚎涓婄殑鐐?
        double x = b0 * p0.x + b1 * p1.x + b2 * p2.x + b3 * p3.x;
        double y = b0 * p0.y + b1 * p1.y + b2 * p2.y + b3 * p3.y;
        double z = b0 * p0.z + b1 * p1.z + b2 * p2.z + b3 * p3.z;
        
        return new Vector3d(x, y, z);
    }
    
    /**
     * 浣跨敤鍙傛暟鍖栫嚎娈垫柟绋嬬敓鎴愮嚎娈碉紙鐢ㄤ簬闄嶇骇澶勭悊锛?
     */
    private void generateParametricLine(BlockPos start, BlockPos end, BlockPosList result) {
        // 鍙傛暟鍖栫嚎娈垫柟绋? P(t) = P0 + t(P1 - P0), t鈭圼0,1]
        Vector3d startVec = new Vector3d(start.getX(), start.getY(), start.getZ());
        Vector3d endVec = new Vector3d(end.getX(), end.getY(), end.getZ());
        Vector3d dirVec = new Vector3d(endVec).sub(startVec);
        
        // 璁＄畻绾挎闀垮害锛堟浖鍝堥】璺濈锛?
        int distance = Math.abs(end.getX() - start.getX()) + 
                       Math.abs(end.getY() - start.getY()) + 
                       Math.abs(end.getZ() - start.getZ());
        distance = Math.max(distance, 1); // 纭繚鑷冲皯鏈変竴涓闀?
        
        // 娌跨潃绾挎鍧囧寑閲囨牱
        for (int i = 0; i <= distance; i++) {
            double t = (double) i / distance;
            Vector3d pos = new Vector3d(startVec).add(new Vector3d(dirVec).mul(t));
            
            // 杞崲涓烘柟鍧楀潗鏍囷紙鍥涜垗浜斿叆鍒版渶鎺ヨ繎鐨勬暣鏁帮級
            BlockPos blockPos = new BlockPos(
                (int) Math.round(pos.x),
                (int) Math.round(pos.y),
                (int) Math.round(pos.z)
            );
            
            // 閬垮厤閲嶅娣诲姞鐩稿悓鐨勬柟鍧楀潗鏍?
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
        this.resolution = Math.max(2, resolution); // 鑷冲皯2涓噰鏍风偣
        markDirty();
    }
    
    // --- 鑺傜偣鐘舵€佸簭鍒楀寲 ---
    
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
                        // 蹇界暐鏃犳晥鐨勬灇涓惧€?
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