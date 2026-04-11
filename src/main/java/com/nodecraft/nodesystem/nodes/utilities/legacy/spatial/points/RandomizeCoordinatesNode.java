package com.nodecraft.nodesystem.nodes.utilities.legacy.spatial.points;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import com.nodecraft.nodesystem.util.BlockPosList;

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.Random;
import java.util.UUID;

/**
 * Randomize Coordinates 鑺傜偣: 闅忔満鍖栧潗鏍囧垪琛?
 */
@NodeInfo(
    id = "spatial.points.randomize_coordinates",
    displayName = "鍧愭爣闅忔満鍖?,
    description = "瀵瑰潗鏍囧垪琛ㄤ腑鐨勬瘡涓潗鏍囨坊鍔犻殢鏈哄亸绉?,
    category = "spatial.points"
)
public class RandomizeCoordinatesNode extends BaseNode {

    // --- 鑺傜偣灞炴€?---
    private boolean useUniformRange = true; // 榛樿浣跨敤缁熶竴鑼冨洿
    private boolean useSeed = false; // 榛樿涓嶄娇鐢ㄧ瀛?
    private long seed = 0; // 榛樿绉嶅瓙鍊?

    // --- 杈撳叆绔彛 IDs ---
    private static final String INPUT_COORDINATES_ID = "input_coordinates";
    private static final String INPUT_MIN_RANGE_ID = "input_min_range";
    private static final String INPUT_MAX_RANGE_ID = "input_max_range";
    private static final String INPUT_RANGE_VECTOR_ID = "input_range_vector";
    private static final String INPUT_SEED_ID = "input_seed";

    // --- 杈撳嚭绔彛 IDs ---
    private static final String OUTPUT_COORDINATES_ID = "output_coordinates";

    // --- 鏋勯€犲嚱鏁?---
    public RandomizeCoordinatesNode() {
        super(UUID.randomUUID(), "spatial.points.randomize_coordinates");
        
        // 鍒涘缓骞舵坊鍔犺緭鍏ョ鍙?
        addInputPort(new BasePort(INPUT_COORDINATES_ID, "Coordinates", 
                "The coordinates to randomize", NodeDataType.BLOCK_LIST, this));
        addInputPort(new BasePort(INPUT_MIN_RANGE_ID, "Min Range", 
                "Minimum random offset (uniform)", NodeDataType.DOUBLE, this));
        addInputPort(new BasePort(INPUT_MAX_RANGE_ID, "Max Range", 
                "Maximum random offset (uniform)", NodeDataType.DOUBLE, this));
        addInputPort(new BasePort(INPUT_RANGE_VECTOR_ID, "Range Vector", 
                "Maximum random offset vector (XYZ)", NodeDataType.VECTOR, this));
        addInputPort(new BasePort(INPUT_SEED_ID, "Seed", 
                "Random seed (optional)", NodeDataType.INTEGER, this));

        // 鍒涘缓骞舵坊鍔犺緭鍑虹鍙?
        addOutputPort(new BasePort(OUTPUT_COORDINATES_ID, "Coordinates", 
                "Randomized coordinates", NodeDataType.BLOCK_LIST, this));
    }

    // 娣诲姞 getDescription 鏂规硶
    @Override
    public String getDescription() {
        return "Applies random offset to a list of coordinates within a given range";
    }

    // 娣诲姞 getDisplayName 鏂规硶
    @Override
    public String getDisplayName() {
        return "Randomize Coordinates";
    }

    // --- 鏍稿績閫昏緫 ---
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 鑾峰彇杈撳叆鍊?
        Object coordinatesObj = inputValues.get(INPUT_COORDINATES_ID);
        Object minRangeObj = inputValues.get(INPUT_MIN_RANGE_ID);
        Object maxRangeObj = inputValues.get(INPUT_MAX_RANGE_ID);
        Object rangeVectorObj = inputValues.get(INPUT_RANGE_VECTOR_ID);
        Object seedObj = inputValues.get(INPUT_SEED_ID);
        
        // 榛樿绌虹殑鍧愭爣鍒楄〃
        BlockPosList result = new BlockPosList();
        
        // 妫€鏌ュ熀鏈緭鍏ユ槸鍚﹀悎娉?
        if (!(coordinatesObj instanceof BlockPosList)) {
            outputValues.put(OUTPUT_COORDINATES_ID, result);
            return;
        }
        
        BlockPosList coordinates = (BlockPosList) coordinatesObj;
        
        // 纭畾闅忔満鑼冨洿
        double minRange = 0.0;
        double maxRange = 1.0;
        Vector3d rangeVector = new Vector3d(1, 1, 1);
        
        // 纭畾闅忔満绉嶅瓙
        long randomSeed = this.seed;
        if (useSeed && seedObj instanceof Number) {
            randomSeed = ((Number) seedObj).longValue();
        } else if (!useSeed && seedObj instanceof Number) {
            // 濡傛灉鎻愪緵浜嗙瀛愪絾娌℃湁鍚敤绉嶅瓙璁剧疆锛屽垯鑷姩鍚敤
            randomSeed = ((Number) seedObj).longValue();
            useSeed = true;
        }
        
        // 鍒涘缓闅忔満鏁扮敓鎴愬櫒
        Random random;
        if (useSeed) {
            random = new Random(randomSeed);
        } else {
            random = new Random();
        }
        
        // 纭畾浣跨敤鐨勮寖鍥寸被鍨?
        if (useUniformRange) {
            // 浣跨敤缁熶竴鑼冨洿
            if (minRangeObj instanceof Number) {
                minRange = ((Number) minRangeObj).doubleValue();
            }
            if (maxRangeObj instanceof Number) {
                maxRange = ((Number) maxRangeObj).doubleValue();
            }
            // 纭繚鏈€灏忓€?=鏈€澶у€?
            if (minRange > maxRange) {
                double temp = minRange;
                minRange = maxRange;
                maxRange = temp;
            }
        } else if (rangeVectorObj instanceof Vector3d) {
            // 浣跨敤鍚戦噺鑼冨洿
            rangeVector = (Vector3d) rangeVectorObj;
            // 纭繚鑼冨洿鍚戦噺鍚勫垎閲忎负姝ｅ€?
            rangeVector.x = Math.abs(rangeVector.x);
            rangeVector.y = Math.abs(rangeVector.y);
            rangeVector.z = Math.abs(rangeVector.z);
        }
        
        // 搴旂敤闅忔満鍋忕Щ鍒版瘡涓潗鏍?
        for (BlockPos pos : coordinates) {
            // 璁＄畻闅忔満鍋忕Щ
            int offsetX, offsetY, offsetZ;
            
            if (useUniformRange) {
                // 鍦ㄦ渶灏忚寖鍥村拰鏈€澶ц寖鍥翠箣闂寸敓鎴愰殢鏈哄€?
                double range = maxRange - minRange;
                offsetX = (int) Math.round(minRange + random.nextDouble() * range);
                if (random.nextBoolean()) offsetX = -offsetX; // 50%姒傜巼涓鸿礋鍊?
                
                offsetY = (int) Math.round(minRange + random.nextDouble() * range);
                if (random.nextBoolean()) offsetY = -offsetY; // 50%姒傜巼涓鸿礋鍊?
                
                offsetZ = (int) Math.round(minRange + random.nextDouble() * range);
                if (random.nextBoolean()) offsetZ = -offsetZ; // 50%姒傜巼涓鸿礋鍊?
            } else {
                // 浣跨敤鍚戦噺鑼冨洿鐢熸垚闅忔満鍊?
                offsetX = (int) Math.round((random.nextDouble() * 2 - 1) * rangeVector.x);
                offsetY = (int) Math.round((random.nextDouble() * 2 - 1) * rangeVector.y);
                offsetZ = (int) Math.round((random.nextDouble() * 2 - 1) * rangeVector.z);
            }
            
            // 搴旂敤鍋忕Щ
            BlockPos randomizedPos = new BlockPos(
                pos.getX() + offsetX,
                pos.getY() + offsetY,
                pos.getZ() + offsetZ
            );
            
            // 娣诲姞鍒扮粨鏋滃垪琛?
            result.add(randomizedPos);
        }
        
        // 璁剧疆杈撳嚭鍊?
        outputValues.put(OUTPUT_COORDINATES_ID, result);
    }
    
    // --- Getters/Setters for Properties ---
    
    public boolean isUseUniformRange() {
        return useUniformRange;
    }
    
    public void setUseUniformRange(boolean useUniformRange) {
        this.useUniformRange = useUniformRange;
        markDirty();
    }
    
    public boolean isUseSeed() {
        return useSeed;
    }
    
    public void setUseSeed(boolean useSeed) {
        this.useSeed = useSeed;
        markDirty();
    }
    
    public long getSeed() {
        return seed;
    }
    
    public void setSeed(long seed) {
        this.seed = seed;
        markDirty();
    }
    
    // --- 鑺傜偣鐘舵€佸簭鍒楀寲 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("useUniformRange", useUniformRange);
        state.put("useSeed", useSeed);
        state.put("seed", seed);
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("useUniformRange")) {
                Object useUniformObj = stateMap.get("useUniformRange");
                if (useUniformObj instanceof Boolean) {
                    setUseUniformRange((Boolean) useUniformObj);
                }
            }
            
            if (stateMap.containsKey("useSeed")) {
                Object useSeedObj = stateMap.get("useSeed");
                if (useSeedObj instanceof Boolean) {
                    setUseSeed((Boolean) useSeedObj);
                }
            }
            
            if (stateMap.containsKey("seed")) {
                Object seedObj = stateMap.get("seed");
                if (seedObj instanceof Number) {
                    setSeed(((Number) seedObj).longValue());
                }
            }
        }
    }
} 