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
 * Coordinate to Vector 节点，将整数坐标转换为浮点坐标/向量
 */
@NodeInfo(
    id = "data.conversion.coordinate_to_vector",
    displayName = "Coordinate to Vector",
    description = "Converts integer block coordinates to floating-point vector",
    category = "data.conversion"
)
public class CoordinateToVectorNode extends BaseNode {
    
    // --- 节点属性 ---
    private boolean addHalfBlock = false; // 是否添加0.5偏移（方块中心）
    private String description; // 存储节点描述
    
    // --- 输入/输出端口ID ---
    private static final String INPUT_COORDINATE_ID = "input_coordinate";
    private static final String OUTPUT_VECTOR_ID = "output_vector";
    
    /**
     * 构造一个新的坐标转向量节点
     */
    public CoordinateToVectorNode() {
        // 调用父类构造函数，使用UUID.randomUUID()生成新的ID
        super(UUID.randomUUID(), "data.conversion.coordinate_to_vector");
        
        // 设置节点描述
        this.description = "Converts integer block coordinates to floating-point vector";
        
        // 创建输入端口
        IPort coordinateInput = new BasePort(INPUT_COORDINATE_ID, "Coordinate", 
                "The block coordinates (integer)", NodeDataType.BLOCK_POS, this);
        addInputPort(coordinateInput);
        
        // 创建输出端口
        IPort vectorOutput = new BasePort(OUTPUT_VECTOR_ID, "Vector", 
                "The converted vector (float)", NodeDataType.VECTOR, this);
        addOutputPort(vectorOutput);
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
        Object coordinateObj = inputValues.get(INPUT_COORDINATE_ID);
        
        // 默认向量 (0,0,0)
        Vector3d vector = new Vector3d(0, 0, 0);
        
        // 检查输入是否为方块坐标
        if (coordinateObj instanceof BlockPos) {
            BlockPos pos = (BlockPos) coordinateObj;
            double offset = addHalfBlock ? 0.5 : 0.0;
            vector = new Vector3d(
                pos.getX() + offset,
                pos.getY() + offset,
                pos.getZ() + offset
            );
        }
        
        // 设置输出
        outputValues.put(OUTPUT_VECTOR_ID, vector);
    }
    
    // --- Getters/Setters for Properties ---
    
    public boolean isAddHalfBlock() {
        return addHalfBlock;
    }
    
    public void setAddHalfBlock(boolean addOffset) {
        this.addHalfBlock = addOffset;
        markDirty();
    }
    
    // --- 节点状态序列化 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("addHalfBlock", isAddHalfBlock());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("addHalfBlock")) {
                Object addOffsetObj = stateMap.get("addHalfBlock");
                if (addOffsetObj instanceof Boolean) {
                    setAddHalfBlock((Boolean) addOffsetObj);
                }
            }
        }
    }
} 