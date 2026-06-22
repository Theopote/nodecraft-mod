package com.nodecraft.nodesystem.preview.gizmo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maps preview owner node ids to live gizmo transform callbacks.
 */
public final class GizmoBindingRegistry {

    private static final Map<String, GizmoTransformCallback> BINDINGS = new ConcurrentHashMap<>();

    private GizmoBindingRegistry() {
    }

    public static void register(String ownerNodeId, GizmoTransformCallback callback) {
        if (ownerNodeId == null || ownerNodeId.isBlank() || callback == null) {
            return;
        }
        BINDINGS.put(ownerNodeId, callback);
    }

    public static void unregister(String ownerNodeId) {
        if (ownerNodeId == null || ownerNodeId.isBlank()) {
            return;
        }
        BINDINGS.remove(ownerNodeId);
    }

    public static GizmoTransformCallback get(String ownerNodeId) {
        if (ownerNodeId == null || ownerNodeId.isBlank()) {
            return null;
        }
        return BINDINGS.get(ownerNodeId);
    }
}
