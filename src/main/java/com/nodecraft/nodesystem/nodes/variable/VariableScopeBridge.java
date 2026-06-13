package com.nodecraft.nodesystem.nodes.variable;

import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class VariableScopeBridge {

    static final String INTERNAL_PREFIX = "__nodecraft.";

    private static final Object NULL_VALUE = new Object();
    private static final Map<String, Object> FALLBACK_SCOPE = new ConcurrentHashMap<>();

    private VariableScopeBridge() {
    }

    static Object get(@Nullable ExecutionContext context, String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        if (context != null) {
            return context.getVariable(key);
        }
        return decodeFallbackValue(FALLBACK_SCOPE.get(key));
    }

    static Object put(@Nullable ExecutionContext context, String key, Object value) {
        if (key == null || key.isBlank()) {
            return null;
        }
        if (context != null) {
            Object previous = context.getVariable(key);
            context.setVariable(key, value);
            return previous;
        }
        return decodeFallbackValue(FALLBACK_SCOPE.put(key, encodeFallbackValue(value)));
    }

    static Object remove(@Nullable ExecutionContext context, String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        if (context != null) {
            return context.removeVariable(key);
        }
        return decodeFallbackValue(FALLBACK_SCOPE.remove(key));
    }

    static int clear(@Nullable ExecutionContext context, boolean includeInternalVariables) {
        Map<String, Object> snapshot = snapshot(context);
        int removed = 0;
        for (String key : snapshot.keySet()) {
            if (!includeInternalVariables && isInternalVariableName(key)) {
                continue;
            }
            if (containsKey(context, key)) {
                remove(context, key);
                removed++;
            }
        }
        return removed;
    }

    static boolean containsKey(@Nullable ExecutionContext context, String key) {
        if (key == null || key.isBlank()) {
            return false;
        }
        if (context != null) {
            return context.getAllVariables().containsKey(key);
        }
        return FALLBACK_SCOPE.containsKey(key);
    }

    static Map<String, Object> snapshot(@Nullable ExecutionContext context) {
        Map<String, Object> copy = new LinkedHashMap<>();
        if (context != null) {
            copy.putAll(context.getAllVariables());
            return copy;
        }
        for (Map.Entry<String, Object> entry : FALLBACK_SCOPE.entrySet()) {
            copy.put(entry.getKey(), decodeFallbackValue(entry.getValue()));
        }
        return copy;
    }

    static String resolveName(Object inputName, String defaultName) {
        if (inputName instanceof String name && !name.isBlank()) {
            return name.trim();
        }
        if (defaultName == null || defaultName.isBlank()) {
            return null;
        }
        return defaultName.trim();
    }

    static boolean isUserVariableNameValid(String name) {
        return validationError(name) == null;
    }

    static String validationError(String name) {
        if (name == null || name.isBlank()) {
            return "Variable name is required.";
        }
        if (isInternalVariableName(name)) {
            return "Variable names starting with " + INTERNAL_PREFIX + " are reserved.";
        }
        return null;
    }

    static boolean isInternalVariableName(String name) {
        return name != null && name.startsWith(INTERNAL_PREFIX);
    }

    private static Object encodeFallbackValue(Object value) {
        return value == null ? NULL_VALUE : value;
    }

    private static Object decodeFallbackValue(Object value) {
        return value == NULL_VALUE ? null : value;
    }
}
