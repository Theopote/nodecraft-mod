package com.nodecraft.nodesystem.nodes.deferred.out_of_scope;

import com.nodecraft.nodesystem.api.IPort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@NodeInfo(
    id = "deferred.out_of_scope.text_to_value",
    displayName = "Text To Value",
    description = "Parses text into number, integer, or boolean values.",
    category = "deferred.out_of_scope"
)
public class TextToValueNode extends BaseNode {

    public enum OutputType {
        NUMBER,
        BOOLEAN,
        INTEGER
    }

    private static final String INPUT_TEXT_ID = "input_text";
    private static final String OUTPUT_VALUE_ID = "output_value";
    private static final String OUTPUT_SUCCESS_ID = "output_success";

    private OutputType outputType = OutputType.NUMBER;

    public TextToValueNode() {
        super(UUID.randomUUID(), "deferred.out_of_scope.text_to_value");

        IPort textInput = new BasePort(INPUT_TEXT_ID, "Text",
            "Input text to parse", NodeDataType.STRING, this);
        addInputPort(textInput);

        IPort valueOutput = new BasePort(OUTPUT_VALUE_ID, "Value",
            "Parsed value", NodeDataType.ANY, this);
        addOutputPort(valueOutput);

        IPort successOutput = new BasePort(OUTPUT_SUCCESS_ID, "Success",
            "Whether parsing succeeded", NodeDataType.BOOLEAN, this);
        addOutputPort(successOutput);
    }

    @Override
    public String getDescription() {
        return "Parses text into number, integer, or boolean values.";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        Object textObj = inputValues.get(INPUT_TEXT_ID);
        Object result = null;
        boolean success = false;

        if (textObj instanceof String textValue) {
            String text = textValue.trim();
            try {
                switch (outputType) {
                    case NUMBER -> {
                        if (!text.isEmpty()) {
                            result = Double.parseDouble(text);
                            success = true;
                        }
                    }
                    case INTEGER -> {
                        if (!text.isEmpty()) {
                            result = Integer.parseInt(text);
                            success = true;
                        }
                    }
                    case BOOLEAN -> {
                        if (text.equalsIgnoreCase("true") || text.equals("1")) {
                            result = true;
                            success = true;
                        } else if (text.equalsIgnoreCase("false") || text.equals("0")) {
                            result = false;
                            success = true;
                        }
                    }
                }
            } catch (NumberFormatException ignored) {
                success = false;
            }
        }

        outputValues.put(OUTPUT_VALUE_ID, result);
        outputValues.put(OUTPUT_SUCCESS_ID, success);
    }

    public OutputType getOutputType() {
        return outputType;
    }

    public void setOutputType(OutputType outputType) {
        this.outputType = outputType == null ? OutputType.NUMBER : outputType;
        markDirty();
    }

    public void setOutputTypeString(String type) {
        if (type == null || type.isBlank()) {
            setOutputType(OutputType.NUMBER);
            return;
        }
        try {
            setOutputType(OutputType.valueOf(type.trim().toUpperCase()));
        } catch (IllegalArgumentException ignored) {
            setOutputType(OutputType.NUMBER);
        }
    }

    @Override
    public Object getNodeState() {
        Map<String, Object> state = new HashMap<>();
        state.put("outputType", outputType.name());
        return state;
    }

    @Override
    public void setNodeState(Object state) {
        if (state instanceof Map<?, ?> stateMap) {
            Object type = stateMap.get("outputType");
            if (type instanceof String typeString) {
                setOutputTypeString(typeString);
            }
        }
    }
}
