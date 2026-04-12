package com.nodecraft.gui.components;

import com.nodecraft.nodesystem.registry.NodeRegistry.NodeCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Maps canonical registry categories into node-library presentation categories.
 * The current mapping stays close to canonical taxonomy, but this layer keeps
 * UI grouping independent from the registry model.
 */
public final class NodeCategoryPresentationMapper {

    public enum PresentationMode {
        CANONICAL,
        TASK
    }

    public record CategoryPresentation(
        NodeCategory sourceCategory,
        String canonicalCategoryId,
        String displayCategoryId,
        String displayName,
        String parentDisplayId,
        String colorKey,
        boolean defaultExpanded
    ) {
    }

    private static final Set<String> DEFAULT_EXPANDED_DISPLAY_IDS = Set.of(
        "geometry", "input", "material", "math", "output", "pattern", "reference", "transform", "world", "utilities",
        "input.basic", "input.numeric", "input.context", "input.type_selectors",
        "reference.points", "reference.vectors", "reference.planes", "reference.frames",
        "world.selection", "world.read", "world.query", "world.write",
        "geometry.boolean", "geometry.curves", "geometry.primitives", "geometry.profiles", "geometry.solids", "geometry.architectural_primitives",
        "pattern.linear", "pattern.grid", "pattern.radial", "pattern.surface_volume_distribution",
        "transform.basic_transforms", "transform.deformations", "transform.orientation",
        "math.scalar_math", "math.compare", "math.logic", "math.random", "math.trigonometry", "math.list_sequence",
        "output.preview", "output.execute", "output.export", "output.debug",
        "utilities.organization", "utilities.assist",
        "material.basic_assignment", "material.gradient_mapping", "material.directional_mapping",
        "material.pattern_mapping", "material.block_state", "material.surface_aging"
    );

    private static final Set<String> DEFAULT_EXPANDED_TASK_DISPLAY_IDS = Set.of(
        "task.massing",
        "task.facade",
        "task.materials",
        "task.build",
        "task.world_tools",
        "task.advanced"
    );

    public List<CategoryPresentation> mapCategories(List<NodeCategory> canonicalCategories, PresentationMode mode) {
        if (mode == PresentationMode.TASK) {
            return mapTaskCategories(canonicalCategories);
        }
        return mapCanonicalCategories(canonicalCategories);
    }

    private List<CategoryPresentation> mapCanonicalCategories(List<NodeCategory> canonicalCategories) {
        List<CategoryPresentation> mapped = new ArrayList<>();
        if (canonicalCategories == null) {
            return mapped;
        }

        for (NodeCategory category : canonicalCategories) {
            if (category == null || category.getId() == null) {
                continue;
            }

            String canonicalId = category.getId().toLowerCase();
            String displayId = canonicalId;
            String parentDisplayId = canonicalId.contains(".")
                ? canonicalId.substring(0, canonicalId.lastIndexOf('.'))
                : null;

            mapped.add(new CategoryPresentation(
                category,
                canonicalId,
                displayId,
                category.getDisplayName(),
                parentDisplayId,
                canonicalId,
                DEFAULT_EXPANDED_DISPLAY_IDS.contains(displayId)
            ));
        }

        return List.copyOf(mapped);
    }

    private List<CategoryPresentation> mapTaskCategories(List<NodeCategory> canonicalCategories) {
        List<CategoryPresentation> mapped = new ArrayList<>();
        if (canonicalCategories == null) {
            return mapped;
        }

        mapped.add(createSyntheticGroup("task.massing", "Building Massing", "task.massing"));
        mapped.add(createSyntheticGroup("task.facade", "Facade / Windows", "task.facade"));
        mapped.add(createSyntheticGroup("task.materials", "Materials", "task.materials"));
        mapped.add(createSyntheticGroup("task.build", "Preview / Build", "task.build"));
        mapped.add(createSyntheticGroup("task.world_tools", "World Tools", "task.world_tools"));
        mapped.add(createSyntheticGroup("task.advanced", "Advanced / Utilities", "task.advanced"));

        for (NodeCategory category : canonicalCategories) {
            if (category == null || category.getId() == null) {
                continue;
            }

            String canonicalId = category.getId().toLowerCase();
            TaskGroupSpec group = resolveTaskGroup(canonicalId);
            String displayId = group.id() + "." + canonicalId;

            mapped.add(new CategoryPresentation(
                category,
                canonicalId,
                displayId,
                category.getDisplayName(),
                group.id(),
                group.colorKey(),
                false
            ));
        }

        return List.copyOf(mapped);
    }

    private CategoryPresentation createSyntheticGroup(String id, String displayName, String colorKey) {
        return new CategoryPresentation(
            new NodeCategory(id, displayName),
            id,
            id,
            displayName,
            null,
            colorKey,
            DEFAULT_EXPANDED_TASK_DISPLAY_IDS.contains(id)
        );
    }

    private TaskGroupSpec resolveTaskGroup(String canonicalId) {
        if (canonicalId.startsWith("geometry.")
            || canonicalId.startsWith("reference.")
            || canonicalId.startsWith("transform.")
            || canonicalId.startsWith("pattern.")) {
            if (canonicalId.startsWith("geometry.architectural_primitives")
                || canonicalId.startsWith("reference.points")
                || canonicalId.startsWith("geometry.boolean")
                || canonicalId.startsWith("geometry.solids")) {
                return new TaskGroupSpec("task.facade", "task.facade");
            }
            return new TaskGroupSpec("task.massing", "task.massing");
        }

        if (canonicalId.startsWith("material.")) {
            return new TaskGroupSpec("task.materials", "task.materials");
        }

        if (canonicalId.startsWith("output.")) {
            return new TaskGroupSpec("task.build", "task.build");
        }

        if (canonicalId.startsWith("world.")) {
            return new TaskGroupSpec("task.world_tools", "task.world_tools");
        }

        return new TaskGroupSpec("task.advanced", "task.advanced");
    }

    private record TaskGroupSpec(String id, String colorKey) {
    }
}
