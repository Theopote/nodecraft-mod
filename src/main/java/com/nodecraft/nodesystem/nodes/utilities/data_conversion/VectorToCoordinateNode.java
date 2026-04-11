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
 * Vector to Coordinate                            /                            ?
 */
@NodeInfo(
    id = "utilities.data_conversion.vector_to_coordinate",
    displayName = "Vector to Coordinate",
    description = "Converts floating-point vector to integer block coordinates",
    category = "utilities.data_conversion"
)
public class VectorToCoordinateNode extends BaseNode {
    
    // ---              ?---
    public enum RoundingMode {
        FLOOR, ROUND, CEIL
    }
    
    private RoundingMode roundingMode = RoundingMode.FLOOR; //                       ?
    private String description; //                    ?
    
    // ---       ?              D ---
    private static final String INPUT_VECTOR_ID = "input_vector";
    private static final String OUTPUT_COORDINATE_ID = "output_coordinate";
    
    /**
     *                                             ?
     */
    public VectorToCoordinateNode() {
        //                                        UID.randomUUID()              D
        super(UUID.randomUUID(), "utilities.data_conversion.vector_to_coordinate");
        
        //                    ?
        this.description = "Converts floating-point vector to integer block coordinates";
        
        //                    ?
        IPort vectorInput = new BasePort(INPUT_VECTOR_ID, "Vector", 
                "The vector (float)", NodeDataType.VECTOR, this);
        addInputPort(vectorInput);
        
        //                    ?
        IPort coordinateOutput = new BasePort(OUTPUT_COORDINATE_ID, "Coordinate", 
                "The converted block coordinates (integer)", NodeDataType.BLOCK_POS, this);
        addOutputPort(coordinateOutput);
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
        Object vectorObj = inputValues.get(INPUT_VECTOR_ID);
        
        //              ?(0,0,0)
        BlockPos coordinate = new BlockPos(0, 0, 0);
        
        //                                ?
        if (vectorObj instanceof Vector3d) {
            Vector3d vec = (Vector3d) vectorObj;
            
            //                                             ?
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
        
        //              ?
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
     *                                                         I                      ?
     * @param modeStr                           "FLOOR", "ROUND", "CEIL"
     */
    public void setRoundingModeString(String modeStr) {
        try {
            setRoundingMode(RoundingMode.valueOf(modeStr.toUpperCase()));
        } catch (IllegalArgumentException e) {
            //                                                                  OOR
            setRoundingMode(RoundingMode.FLOOR);
        }
    }
    
    // ---                         ?---
    
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