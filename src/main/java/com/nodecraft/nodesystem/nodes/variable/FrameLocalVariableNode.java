package com.nodecraft.nodesystem.nodes.variable;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.api.NodeProperty;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@NodeInfo(
    id = "variable.frame_local",
    displayName = "Frame Local Variable",
    description = "Reads or writes variables in an isolated frame-local namespace.",
    category = "variable",
    order = 3
)
public class FrameLocalVariableNode extends BaseNode {

    private static final String LOCAL_SCOPE_ROOT_KEY = "__nodecraft.frame_local_scope";
    private static final Map<String, Map<String, Object>> FALLBACK_SCOPE = new ConcurrentHashMap<>();

    @NodeProperty(displayName = "Default Frame", category = "Frame Local", order = 1)
    private String defaultFrame = "default";

    @NodeProperty(displayName = "Default Name", category = "Frame Local", order = 2)
    private String defaultName = "";

    private static final String INPUT_FRAME_ID = "input_frame";
    private static final String INPUT_NAME_ID = "input_name";
    private static final String INPUT_VALUE_ID = "input_value";
    private static final String INPUT_DEFAULT_ID = "input_default";
    private static final String INPUT_WRITE_ID = "input_write";
    private static final String INPUT_CLEAR_FRAME_ID = "input_clear_frame";

    private static final String OUTPUT_VALUE_ID = "output_value";
    private static final String OUTPUT_PREVIOUS_ID = "output_previous";
    private static final String OUTPUT_EXISTS_ID = "output_exists";
    private static final String OUTPUT_FRAME_ID = "output_frame";
    private static final String OUTPUT_NAME_ID = "output_name";
    private static final String OUTPUT_SIZE_ID = "output_size";
    private static final String OUTPUT_VALID_ID = "output_valid";

    public FrameLocalVariableNode() {
        super(UUID.randomUUID(), "variable.frame_local");

        addInputPort(new BasePort(INPUT_FRAME_ID, "Frame", "Frame namespace", NodeDataType.STRING, this));
        addInputPort(new BasePort(INPUT_NAME_ID, "Name", "Variable name in this frame", NodeDataType.STRING, this));
        addInputPort(new BasePort(INPUT_VALUE_ID, "Value", "Value to write", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_DEFAULT_ID, "Default", "Fallback when key does not exist", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_WRITE_ID, "Write", "When true, writes Value into frame local map", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_CLEAR_FRAME_ID, "Clear Frame", "When true, clears all values under this frame", NodeDataType.BOOLEAN, this));

        addOutputPort(new BasePort(OUTPUT_VALUE_ID, "Value", "Read or written value", NodeDataType.ANY, this));
        addOutputPort(new BasePort(OUTPUT_PREVIOUS_ID, "Previous", "Previous value before write", NodeDataType.ANY, this));
        addOutputPort(new BasePort(OUTPUT_EXISTS_ID, "Exists", "Whether key exists in this frame", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_FRAME_ID, "Frame", "Resolved frame name", NodeDataType.STRING, this));
        addOutputPort(new BasePort(OUTPUT_NAME_ID, "Name", "Resolved variable name", NodeDataType.STRING, this));
        addOutputPort(new BasePort(OUTPUT_SIZE_ID, "Frame Size", "Number of entries in current frame", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_VALID_ID, "Valid", "Whether frame/name resolved correctly", NodeDataType.BOOLEAN, this));
    }

    @Override
    public String getDisplayName() {
        return "Frame Local Variable";
    }

    @Override
    public String getDescription() {
        return "Reads or writes variables in an isolated frame-local namespace.";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        String frame = resolveFrame(inputValues.get(INPUT_FRAME_ID));
        String name = resolveName(inputValues.get(INPUT_NAME_ID));
        boolean write = Boolean.TRUE.equals(inputValues.get(INPUT_WRITE_ID));
        boolean clearFrame = Boolean.TRUE.equals(inputValues.get(INPUT_CLEAR_FRAME_ID));

        if (frame == null || frame.isBlank() || name == null || name.isBlank()) {
            outputValues.put(OUTPUT_VALUE_ID, inputValues.get(INPUT_DEFAULT_ID));
            outputValues.put(OUTPUT_PREVIOUS_ID, null);
            outputValues.put(OUTPUT_EXISTS_ID, false);
            outputValues.put(OUTPUT_FRAME_ID, frame == null ? "" : frame);
            outputValues.put(OUTPUT_NAME_ID, name == null ? "" : name);
            outputValues.put(OUTPUT_SIZE_ID, 0);
            outputValues.put(OUTPUT_VALID_ID, false);
            return;
        }

        Map<String, Object> frameScope = getOrCreateFrameScope(context, frame);
        if (clearFrame) {
            frameScope.clear();
        }

        boolean exists = frameScope.containsKey(name);
        Object previous = exists ? frameScope.get(name) : null;
        Object fallback = inputValues.get(INPUT_DEFAULT_ID);

        Object value;
        if (write) {
            Object writeValue = inputValues.get(INPUT_VALUE_ID);
            frameScope.put(name, writeValue);
            value = writeValue;
            exists = true;
        } else {
            value = exists ? frameScope.get(name) : fallback;
        }

        outputValues.put(OUTPUT_VALUE_ID, value);
        outputValues.put(OUTPUT_PREVIOUS_ID, previous);
        outputValues.put(OUTPUT_EXISTS_ID, exists);
        outputValues.put(OUTPUT_FRAME_ID, frame);
        outputValues.put(OUTPUT_NAME_ID, name);
        outputValues.put(OUTPUT_SIZE_ID, frameScope.size());
        outputValues.put(OUTPUT_VALID_ID, true);
    }

    private String resolveFrame(Object inputFrame) {
        if (inputFrame instanceof String frame && !frame.isBlank()) {
            return frame.trim();
        }
        return defaultFrame == null ? null : defaultFrame.trim();
    }

    private String resolveName(Object inputName) {
        if (inputName instanceof String name && !name.isBlank()) {
            return name.trim();
        }
        return defaultName == null ? null : defaultName.trim();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getOrCreateFrameScope(@Nullable ExecutionContext context, String frame) {
        if (context != null) {
            Object rootObj = context.getVariable(LOCAL_SCOPE_ROOT_KEY);
            Map<String, Map<String, Object>> root;
            if (rootObj instanceof Map<?, ?> existing) {
                root = (Map<String, Map<String, Object>>) existing;
            } else {
                root = new ConcurrentHashMap<>();
                context.setVariable(LOCAL_SCOPE_ROOT_KEY, root);
            }
            return root.computeIfAbsent(frame, ignored -> new ConcurrentHashMap<>());
        }
        return FALLBACK_SCOPE.computeIfAbsent(frame, ignored -> new ConcurrentHashMap<>());
    }

    @Override
    public Object getNodeState() {
        Map<String, Object> state = new HashMap<>();
        state.put("defaultFrame", defaultFrame);
        state.put("defaultName", defaultName);
        return state;
    }

    @Override
    public void setNodeState(Object state) {
        if (!(state instanceof Map<?, ?> map)) {
            return;
        }
        Object frameObj = map.get("defaultFrame");
        if (frameObj instanceof String frame) {
            defaultFrame = frame;
        }
        Object nameObj = map.get("defaultName");
        if (nameObj instanceof String name) {
            defaultName = name;
        }
    }
}

