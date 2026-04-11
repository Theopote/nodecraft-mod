package com.nodecraft.nodesystem.nodes.utilities.data_conversion;

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
 * Coordinate to Vector                                                  ?      ?
 */
@NodeInfo(
    id = "utilities.data_conversion.coordinate_to_vector",
    displayName = "Coordinate to Vector",
    description = "Converts integer block coordinates to floating-point vector",
    category = "utilities.data_conversion"
)
public class CoordinateToVectorNode extends BaseNode {
    
    // ---              ?---
    private boolean addHalfBlock = false; //              0.5                         ?
    private String description; //                    ?
    
    // ---       ?              D ---
    private static final String INPUT_COORDINATE_ID = "input_coordinate";
    private static final String OUTPUT_VECTOR_ID = "output_vector";
    
    /**
     *                                              
     */
    public CoordinateToVectorNode() {
        //                                        UID.randomUUID()              D
        super(UUID.randomUUID(), "utilities.data_conversion.coordinate_to_vector");
        
        //                    ?
        this.description = "Converts integer block coordinates to floating-point vector";
        
        //                    ?
        IPort coordinateInput = new BasePort(INPUT_COORDINATE_ID, "Coordinate", 
                "The block coordinates (integer)", NodeDataType.BLOCK_POS, this);
        addInputPort(coordinateInput);
        
        //                    ?
        IPort vectorOutput = new BasePort(OUTPUT_VECTOR_ID, "Vector", 
                "The converted vector (float)", NodeDataType.VECTOR, this);
        addOutputPort(vectorOutput);
    }
    
    /**
     *         ode            tDescription      ?
     * @return              ?
     */
    @Override
    public String getDescription() {
        return this.description;
    }
    
    /**
     *                         ?
     * @param context                ?
     */
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        //              ?
        Object coordinateObj = inputValues.get(INPUT_COORDINATE_ID);
        
        //              ?(0,0,0)
        Vector3d vector = new Vector3d(0, 0, 0);
        
        //                                     ?
        if (coordinateObj instanceof BlockPos) {
            BlockPos pos = (BlockPos) coordinateObj;
            double offset = addHalfBlock ? 0.5 : 0.0;
            vector = new Vector3d(
                pos.getX() + offset,
                pos.getY() + offset,
                pos.getZ() + offset
            );
        }
        
        //              ?
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
    
    // ---                         ?---
    
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