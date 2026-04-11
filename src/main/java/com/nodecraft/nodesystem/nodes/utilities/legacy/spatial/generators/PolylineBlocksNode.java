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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Polyline (Blocks) 鑺傜偣: 鍦ㄥ涓偣涔嬮棿鐢熸垚鎶樼嚎璺緞
 */
@NodeInfo(
    id = "spatial.generators.polyline_blocks",
    displayName = "鎶樼嚎鐢熸垚鍣?,
    description = "鍦ㄥ涓偣涔嬮棿鐢熸垚鎶樼嚎璺緞鐨勫潗鏍囧垪琛?,
    category = "spatial.generators"
)
public class PolylineBlocksNode extends BaseNode {

    // --- 鑺傜偣灞炴€?---
    private boolean useBresenham = true; // 榛樿浣跨敤Bresenham绠楁硶
    private boolean closedLoop = false; // 榛樿涓哄紑鏀炬姌绾?

    // --- 杈撳叆绔彛 IDs ---
    private static final String INPUT_POINTS_LIST_ID = "input_points_list";

    // --- 杈撳嚭绔彛 IDs ---
    private static final String OUTPUT_POLYLINE_BLOCKS_ID = "output_polyline_blocks";

    // --- 鏋勯€犲嚱鏁?---
    public PolylineBlocksNode() {
        super(UUID.randomUUID(), "spatial.generators.polyline_blocks");
        
        // 鍒涘缓骞舵坊鍔犺緭鍏ョ鍙?
        addInputPort(new BasePort(INPUT_POINTS_LIST_ID, "Points", 
                "The list of points to connect", NodeDataType.BLOCK_LIST, this));

        // 鍒涘缓骞舵坊鍔犺緭鍑虹鍙?
        addOutputPort(new BasePort(OUTPUT_POLYLINE_BLOCKS_ID, "Polyline Blocks", 
                "The blocks along the polyline path", NodeDataType.BLOCK_LIST, this));
    }

    // 娣诲姞 getDescription 鏂规硶
    @Override
    public String getDescription() {
        return "Generates a path of blocks connecting multiple points";
    }

    // 娣诲姞 getDisplayName 鏂规硶
    @Override
    public String getDisplayName() {
        return "Polyline (Blocks)";
    }

    // --- 鏍稿績閫昏緫 ---
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 鑾峰彇杈撳叆鍊?
        Object pointsObj = inputValues.get(INPUT_POINTS_LIST_ID);
        
        // 榛樿绌虹殑鍧愭爣鍒楄〃
        BlockPosList result = new BlockPosList();
        
        // 妫€鏌ヨ緭鍏ユ槸鍚︿负鏂瑰潡鍧愭爣鍒楄〃
        if (pointsObj instanceof BlockPosList) {
            BlockPosList points = (BlockPosList) pointsObj;
            
            // 纭繚鑷冲皯鏈変袱涓偣
            if (points.size() >= 2) {
                List<BlockPos> pointsList = points.getPositions();
                
                // 浠庣涓€涓偣鍒版渶鍚庝竴涓偣锛岃繛鎺ユ瘡鐩搁偦涓ょ偣
                for (int i = 0; i < pointsList.size() - 1; i++) {
                    BlockPos start = pointsList.get(i);
                    BlockPos end = pointsList.get(i + 1);
                    
                    // 鐢熸垚绾挎骞舵坊鍔犲埌缁撴灉涓?
                    generateLineSegment(start, end, result);
                }
                
                // 濡傛灉鏄棴鍚堟姌绾匡紝杩炴帴鏈€鍚庝竴涓偣鍜岀涓€涓偣
                if (closedLoop && pointsList.size() > 2) {
                    BlockPos start = pointsList.get(pointsList.size() - 1);
                    BlockPos end = pointsList.get(0);
                    
                    // 鐢熸垚绾挎骞舵坊鍔犲埌缁撴灉涓?
                    generateLineSegment(start, end, result);
                }
            }
        }
        
        // 璁剧疆杈撳嚭鍊?
        outputValues.put(OUTPUT_POLYLINE_BLOCKS_ID, result);
    }
    
    /**
     * 鍦ㄤ袱鐐归棿鐢熸垚绾挎
     */
    private void generateLineSegment(BlockPos start, BlockPos end, BlockPosList result) {
        if (useBresenham) {
            generateBresenhamLine(start, end, result);
        } else {
            generateParametricLine(start, end, result);
        }
    }
    
    /**
     * 浣跨敤Bresenham绠楁硶鐢熸垚绾挎锛堥€傚悎浜庢暣鏁板潗鏍囷級
     */
    private void generateBresenhamLine(BlockPos start, BlockPos end, BlockPosList result) {
        // Bresenham's 3D绾挎绠楁硶
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
            // 璧风偣鍜岀粓鐐规槸鍚屼竴涓偣
            result.add(new BlockPos(x1, y1, z1));
            return;
        }
        
        int x = x1, y = y1, z = z1;
        
        // 娣诲姞璧风偣
        result.add(new BlockPos(x, y, z));
        
        // 閬嶅巻绾挎涓婄殑鐐?
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
     * 浣跨敤鍙傛暟鍖栫嚎娈垫柟绋嬬敓鎴愮嚎娈碉紙閫傚悎浜庣簿纭殑绾挎锛?
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
    
    // --- 鑺傜偣鐘舵€佸簭鍒楀寲 ---
    
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