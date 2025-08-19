package com.nodecraft.nodesystem.nodes.data.conversion;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.api.IPort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import net.minecraft.util.math.BlockPos;
import org.joml.Vector3d;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Vector to Coordinate 节点，将浮点坐标/向量转换为整数坐标
 */
@NodeInfo(
    id = "data.conversion.vector_to_coordinate",
    displayName = "Vector to Coordinate",
    description = "Converts floating-point vector to integer block coordinates",
    category = "data.conversion"
)
public class VectorToCoordinateNode extends BaseNode {
    
    // --- 节点属性 ---
    public enum RoundingMode {
        FLOOR, ROUND, CEIL
    }
    
    private RoundingMode roundingMode = RoundingMode.FLOOR; // 默认为向下取整
    private String description; // 存储节点描述
    
    // --- 输入/输出端口ID ---
    private static final String INPUT_VECTOR_ID = "input_vector";
    private static final String OUTPUT_COORDINATE_ID = "output_coordinate";
    
    /**
     * 构造一个新的向量转坐标节点
     */
    public VectorToCoordinateNode() {
        // 调用父类构造函数，使用UUID.randomUUID()生成新的ID
        super(UUID.randomUUID(), "data.conversion.vector_to_coordinate");
        
        // 设置节点描述
        this.description = "Converts floating-point vector to integer block coordinates";
        
        // 创建输入端口
        IPort vectorInput = new BasePort(INPUT_VECTOR_ID, "Vector", 
                "The vector (float)", NodeDataType.VECTOR, this);
        addInputPort(vectorInput);
        
        // 创建输出端口
        IPort coordinateOutput = new BasePort(OUTPUT_COORDINATE_ID, "Coordinate", 
                "The converted block coordinates (integer)", NodeDataType.BLOCK_POS, this);
        addOutputPort(coordinateOutput);
    }
    
    /**
     * 实现INode接口的getDescription方法
     * @return 节点描述
     */
    @Override
    public String getDescription() {
        return this.description;
    }
    
    /**
     * 节点的计算逻辑
     * @param context 执行上下文
     */
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入
        Object vectorObj = inputValues.get(INPUT_VECTOR_ID);
        
        // 默认坐标 (0,0,0)
        BlockPos coordinate = new BlockPos(0, 0, 0);
        
        // 检查输入是否为向量
        if (vectorObj instanceof Vector3d) {
            Vector3d vec = (Vector3d) vectorObj;
            
            // 根据舍入模式转换为整数坐标
            int x, y, z;
            
            switch (roundingMode) {
                case FLOOR:
                    x = (int) Math.floor(vec.x);
                    y = (int) Math.floor(vec.y);
                    z = (int) Math.floor(vec.z);
                    break;
                case CEIL:
                    x = (int) Math.ceil(vec.x);
                    y = (int) Math.ceil(vec.y);
                    z = (int) Math.ceil(vec.z);
                    break;
                case ROUND:
                default:
                    x = (int) Math.round(vec.x);
                    y = (int) Math.round(vec.y);
                    z = (int) Math.round(vec.z);
                    break;
            }
            
            coordinate = new BlockPos(x, y, z);
        }
        
        // 设置输出
        outputValues.put(OUTPUT_COORDINATE_ID, coordinate);
    }
    
    // --- Getters/Setters for Properties ---
    
    public RoundingMode getRoundingMode() {
        return roundingMode;
    }
    
    public void setRoundingMode(RoundingMode mode) {
        this.roundingMode = mode;
        markDirty();
    }
    
    /**
     * 设置舍入模式（字符串形式，用于从UI或配置中设置）
     * @param modeStr 舍入模式字符串："FLOOR", "ROUND", "CEIL"
     */
    public void setRoundingModeString(String modeStr) {
        try {
            setRoundingMode(RoundingMode.valueOf(modeStr.toUpperCase()));
        } catch (IllegalArgumentException e) {
            // 如果字符串不匹配任何模式，使用默认的FLOOR
            setRoundingMode(RoundingMode.FLOOR);
        }
    }
    
    // --- 节点状态序列化 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("roundingMode", getRoundingMode().name());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("roundingMode")) {
                Object modeObj = stateMap.get("roundingMode");
                if (modeObj instanceof String) {
                    setRoundingModeString((String) modeObj);
                }
            }
        }
    }
} 