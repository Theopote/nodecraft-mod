package com.nodecraft.nodesystem.nodes.deferred.out_of_scope;

import com.nodecraft.nodesystem.api.IPort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.datatypes.ColorData;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@NodeInfo(
    id = "deferred.out_of_scope.components_to_color",
    displayName = "Components To Color",
    description = "Creates a color value from RGBA components.",
    category = "deferred.out_of_scope"
)
public class ComponentsToColorNode extends BaseNode {

    private static final String INPUT_RED_ID = "input_red";
    private static final String INPUT_GREEN_ID = "input_green";
    private static final String INPUT_BLUE_ID = "input_blue";
    private static final String INPUT_ALPHA_ID = "input_alpha";
    private static final String OUTPUT_COLOR_ID = "output_color";

    private boolean inputFloatValues = true;

    public ComponentsToColorNode() {
        super(UUID.randomUUID(), "deferred.out_of_scope.components_to_color");

        addInputPort(new BasePort(INPUT_RED_ID, "Red", "Red component", NodeDataType.DOUBLE, this));
        addInputPort(new BasePort(INPUT_GREEN_ID, "Green", "Green component", NodeDataType.DOUBLE, this));
        addInputPort(new BasePort(INPUT_BLUE_ID, "Blue", "Blue component", NodeDataType.DOUBLE, this));
        addInputPort(new BasePort(INPUT_ALPHA_ID, "Alpha", "Alpha component", NodeDataType.DOUBLE, this));
        addOutputPort(new BasePort(OUTPUT_COLOR_ID, "Color", "Resulting color", NodeDataType.COLOR, this));
    }

    @Override
    public String getDescription() {
        return "Creates a color value from RGBA components.";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        Object redObj = inputValues.get(INPUT_RED_ID);
        Object greenObj = inputValues.get(INPUT_GREEN_ID);
        Object blueObj = inputValues.get(INPUT_BLUE_ID);
        Object alphaObj = inputValues.get(INPUT_ALPHA_ID);

        float red = redObj instanceof Number number ? number.floatValue() : 0f;
        float green = greenObj instanceof Number number ? number.floatValue() : 0f;
        float blue = blueObj instanceof Number number ? number.floatValue() : 0f;
        float alpha = alphaObj instanceof Number number ? number.floatValue() : 1f;

        if (!inputFloatValues) {
            red /= 255f;
            green /= 255f;
            blue /= 255f;
            alpha /= 255f;
        }

        outputValues.put(OUTPUT_COLOR_ID, new ColorData(red, green, blue, alpha));
    }

    public boolean isInputFloatValues() {
        return inputFloatValues;
    }

    public void setInputFloatValues(boolean inputFloatValues) {
        this.inputFloatValues = inputFloatValues;
        markDirty();
    }

    @Override
    public Object getNodeState() {
        Map<String, Object> state = new HashMap<>();
        state.put("inputFloatValues", inputFloatValues);
        return state;
    }

    @Override
    public void setNodeState(Object state) {
        if (state instanceof Map<?, ?> stateMap) {
            Object value = stateMap.get("inputFloatValues");
            if (value instanceof Boolean enabled) {
                setInputFloatValues(enabled);
            }
        }
    }
}
