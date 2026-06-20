package com.nodecraft.nodesystem.preview;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PreviewRendererRegistryTest {

    @Test
    void defaultPreviewTypesAreRegistered() throws Exception {
        Map<?, ?> registry = elementRegistry();
        Set<String> expectedTypes = Set.of(
                "block_highlight",
                "wireframe",
                "ghost_block",
                "semi_transparent_block",
                "region_box",
                "spatial_shape",
                "plane_grid",
                "frame_axes",
                "points",
                "vectors",
                "arrows",
                "lines",
                "paths",
                "geometry_surface",
                "transformation_gizmo",
                "field_visualization",
                "text_labels"
        );

        assertAll(expectedTypes.stream()
                .map(type -> () -> assertTrue(registry.containsKey(type), "missing preview type: " + type)));
        assertAll(registry.entrySet().stream()
                .map(entry -> () -> assertNotNull(entry.getValue(), "missing preview factory for: " + entry.getKey())));
    }

    private static Map<?, ?> elementRegistry() throws Exception {
        Field field = PreviewRenderer.class.getDeclaredField("elementRegistry");
        field.setAccessible(true);
        return (Map<?, ?>) field.get(PreviewRenderer.getInstance());
    }
}
