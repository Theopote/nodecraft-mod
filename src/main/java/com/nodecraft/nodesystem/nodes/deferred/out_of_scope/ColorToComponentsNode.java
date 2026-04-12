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
    id = "deferred.out_of_scope.color_to_components",
    displayName = "Color To Components",
    description = "Extracts RGBA components from a color value.",
    category = "deferred.out_of_scope"
)
public class ColorToComponentsNode extends BaseNode {

    private static final String INPUT_COLOR_ID = "input_color";
    private static final String OUTPUT_RED_ID = "output_red";
    private static final String OUTPUT_GREEN_ID = "output_green";
    private static final String OUTPUT_BLUE_ID = "output_blue";
    private static final String OUTPUT_ALPHA_ID = "output_alpha";

    private boolean outputFloatValues = true;

    public ColorToComponentsNode() {
        super(UUID.randomUUID(), "deferred.out_of_scope.color_to_components");

        IPort colorInput = new BasePort(INPUT_COLOR_ID, "Color",
            "Input color", NodeDataType.COLOR, this);
        addInputPort(colorInput);

        addOutputPort(new BasePort(OUTPUT_RED_ID, "Red", "Red component", NodeDataType.DOUBLE, this));
        addOutputPort(new BasePort(OUTPUT_GREEN_ID, "Green", "Green component", NodeDataType.DOUBLE, this));
        addOutputPort(new BasePort(OUTPUT_BLUE_ID, "Blue", "Blue component", NodeDataType.DOUBLE, this));
        addOutputPort(new BasePort(OUTPUT_ALPHA_ID, "Alpha", "Alpha component", NodeDataType.DOUBLE, this));
    }

    @Override
    public String getDescription() {
        return "Extracts RGBA components from a color value.";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        Object colorObj = inputValues.get(INPUT_COLOR_ID);
        float red = 0f;
        float green = 0f;
        float blue = 0f;
        float alpha = 1f;

        if (colorObj instanceof ColorData(float r, float g, float b, float a)) {
            red = r;
            green = g;
            blue = b;
            alpha = a;
        }

        if (outputFloatValues) {
            outputValues.put(OUTPUT_RED_ID, red);
            outputValues.put(OUTPUT_GREEN_ID, green);
            outputValues.put(OUTPUT_BLUE_ID, blue);
            outputValues.put(OUTPUT_ALPHA_ID, alpha);
        } else {
            outputValues.put(OUTPUT_RED_ID, Math.round(red * 255));
            outputValues.put(OUTPUT_GREEN_ID, Math.round(green * 255));
            outputValues.put(OUTPUT_BLUE_ID, Math.round(blue * 255));
            outputValues.put(OUTPUT_ALPHA_ID, Math.round(alpha * 255));
        }
    }

    public boolean isOutputFloatValues() {
        return outputFloatValues;
    }

    public void setOutputFloatValues(boolean outputFloatValues) {
        this.outputFloatValues = outputFloatValues;
        markDirty();
    }

    @Override
    public Object getNodeState() {
        Map<String, Object> state = new HashMap<>();
        state.put("outputFloatValues", outputFloatValues);
        return state;
    }

    @Override
    public void setNodeState(Object state) {
        if (state instanceof Map<?, ?> stateMap) {
            Object value = stateMap.get("outputFloatValues");
            if (value instanceof Boolean enabled) {
                setOutputFloatValues(enabled);
            }
        }
    }
}
