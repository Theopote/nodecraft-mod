package com.nodecraft.nodesystem.preview;

import com.nodecraft.core.NodeCraft;
import com.nodecraft.nodesystem.bake.PlacementMode;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import com.nodecraft.nodesystem.preview.protocol.PreviewBlock;
import com.nodecraft.nodesystem.preview.protocol.PreviewBlocksPayload;
import com.nodecraft.nodesystem.preview.protocol.PreviewKind;
import com.nodecraft.nodesystem.preview.protocol.PreviewPayload;
import com.nodecraft.nodesystem.preview.protocol.PreviewPayloadAdapters;
import com.nodecraft.nodesystem.preview.protocol.PreviewRequest;
import com.nodecraft.nodesystem.preview.protocol.PreviewStyle;
import com.nodecraft.nodesystem.util.Coordinate;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * High-level helper API for preview rendering.
 */
public final class PreviewManager {

    private static final PreviewRenderer RENDERER = PreviewRenderer.getInstance();

    private PreviewManager() {
    }

    // Block highlight

    public static String highlightBlock(String nodeId, Coordinate position) {
        return highlightBlock(nodeId, position, PreviewOptions.createBlockHighlight());
    }

    public static String highlightBlock(String nodeId, Coordinate position, PreviewOptions options) {
        try {
            String previewId = RENDERER.showPreview(nodeId, "block_highlight", position, options);
            if (previewId == null) {
                NodeCraft.LOGGER.error("PreviewManager.highlightBlock failed: renderer.showPreview returned null");
            }
            return previewId;
        } catch (Exception e) {
            NodeCraft.LOGGER.error("PreviewManager.highlightBlock failed: nodeId={}, position={}", nodeId, position, e);
            return null;
        }
    }

    public static String highlightBlocks(String nodeId, List<Coordinate> positions) {
        return highlightBlocks(nodeId, positions, PreviewOptions.createBlockHighlight());
    }

    public static String highlightBlocks(String nodeId, List<Coordinate> positions, PreviewOptions options) {
        return RENDERER.showPreview(nodeId, "block_highlight", positions, options);
    }

    // Ghost blocks

    public static String showGhostBlocks(String nodeId, List<Coordinate> positions) {
        return showGhostBlocks(nodeId, positions, new PreviewOptions().ghostBlockMode().setOpacity(0.5f));
    }

    public static String showGhostBlocks(String nodeId, List<Coordinate> positions, PreviewOptions options) {
        return RENDERER.showPreview(nodeId, "ghost_block", positions, options);
    }

    public static String showGhostBlockPlacements(
            String nodeId,
            List<GhostBlockPlacement> placements,
            PreviewOptions options
    ) {
        if (placements == null || placements.isEmpty()) {
            hideNodePreviews(nodeId);
            return null;
        }
        PreviewBlocksPayload payload = PreviewPayloadAdapters.fromGhostBlockPlacements(placements);
        float fallbackOpacity = placements.get(0).opacity();
        PreviewStyle style = PreviewStyle.fromLegacyGhostOptions(options, fallbackOpacity);
        return showPreview(new PreviewRequest(nodeId, payload, style, PreviewBackend.GHOST, null));
    }

    /**
     * v1 unified entry: dispatches on {@link PreviewKind} and {@link PreviewBackend}.
     * Currently implements {@link PreviewKind#BLOCKS} for GHOST and TRACKED_WORLD.
     */
    @Nullable
    public static String showPreview(PreviewRequest request) {
        try {
            String nodeId = request.ownerNodeId();
            PreviewPayload payload = request.payload();
            PreviewOptions opts = request.style().toPreviewOptions();

            if (payload.getKind() != PreviewKind.BLOCKS || !(payload instanceof PreviewBlocksPayload blocksPayload)) {
                NodeCraft.LOGGER.warn(
                    "PreviewManager.showPreview: unsupported payload kind={} type={}",
                    payload.getKind(),
                    payload.getClass().getName()
                );
                return null;
            }

            hideNodePreviews(nodeId);
            ExecutionContext ctx = request.executionContext();

            if (request.backend() == PreviewBackend.GHOST) {
                if (ctx != null && ctx.getWorld() != null) {
                    TrackedPreviewPlacementService.getInstance().clearTrackedPreview(ctx.getWorld(), nodeId);
                }
                return RENDERER.showPreview(nodeId, "ghost_block", blocksPayload, opts);
            }

            if (request.backend() == PreviewBackend.TRACKED_WORLD) {
                if (ctx == null || ctx.getWorld() == null) {
                    NodeCraft.LOGGER.warn("PreviewManager.showPreview TRACKED_WORLD: missing execution context / world");
                    return null;
                }
                List<PreviewBlock> cells = blocksPayload.getBlocks();
                if (cells.isEmpty()) {
                    TrackedPreviewPlacementService.getInstance().clearTrackedPreview(ctx.getWorld(), nodeId);
                    return nodeId + ":tracked:cleared";
                }
                BlockState state = resolveBlockStateForPreview(cells.get(0).blockId());
                if (state == null) {
                    NodeCraft.LOGGER.warn("PreviewManager.showPreview TRACKED_WORLD: invalid block id {}", cells.get(0).blockId());
                    return null;
                }
                List<BlockPos> positions = PreviewPayloadAdapters.toBlockPosList(blocksPayload);
                int placed = TrackedPreviewPlacementService.getInstance().updateTrackedPreview(
                    ctx.getWorld(),
                    nodeId,
                    new ArrayList<>(positions),
                    state,
                    PlacementMode.OVERWRITE
                );
                return nodeId + ":tracked:" + placed;
            }

            NodeCraft.LOGGER.warn("PreviewManager.showPreview: unsupported backend {}", request.backend());
            return null;
        } catch (Exception e) {
            NodeCraft.LOGGER.error("PreviewManager.showPreview failed: nodeId={}", request.ownerNodeId(), e);
            return null;
        }
    }

    @Nullable
    private static BlockState resolveBlockStateForPreview(String blockId) {
        if (blockId == null || blockId.isEmpty()) {
            return null;
        }
        try {
            Identifier id = Identifier.of(blockId);
            var block = Registries.BLOCK.get(id);
            return block != null ? block.getDefaultState() : null;
        } catch (Exception e) {
            return null;
        }
    }

    // Region box

    public static String showRegionBox(String nodeId, Vec3d min, Vec3d max) {
        return showRegionBox(nodeId, min, max, PreviewOptions.createRegionBox());
    }

    public static String showRegionBox(String nodeId, Vec3d min, Vec3d max, PreviewOptions options) {
        Object[] regionData = {min, max};
        return RENDERER.showPreview(nodeId, "region_box", regionData, options);
    }

    // Points

    public static String showPoints(String nodeId, List<Coordinate> points) {
        return showPoints(nodeId, points, PreviewOptions.createPoints());
    }

    public static String showPoints(String nodeId, List<Coordinate> points, PreviewOptions options) {
        return RENDERER.showPreview(nodeId, "points", points, options);
    }

    // Vectors

    public static String showVectors(String nodeId, List<Vec3d> vectors, List<Vec3d> startPoints) {
        return showVectors(nodeId, vectors, startPoints, PreviewOptions.createVectorArrows());
    }

    public static String showVectors(
            String nodeId,
            List<Vec3d> vectors,
            List<Vec3d> startPoints,
            PreviewOptions options
    ) {
        Object[] vectorData = {vectors, startPoints};
        return RENDERER.showPreview(nodeId, "vectors", vectorData, options);
    }

    // Plane / frame / path / labels

    public static String showPlaneGrid(String nodeId, PlaneGridPreviewData planeGridData, PreviewOptions options) {
        return RENDERER.showPreview(nodeId, "plane_grid", planeGridData, options);
    }

    public static String showFrameAxes(String nodeId, FrameAxesPreviewData frameAxesData, PreviewOptions options) {
        return RENDERER.showPreview(nodeId, "frame_axes", frameAxesData, options);
    }

    public static String showPaths(String nodeId, Object pathData, PreviewOptions options) {
        return RENDERER.showPreview(nodeId, "paths", pathData, options);
    }

    public static String showTextLabels(String nodeId, Object labelData, PreviewOptions options) {
        return RENDERER.showPreview(nodeId, "text_labels", labelData, options);
    }

    // Transform gizmo

    public static String showTransformGizmo(String nodeId, Vec3d center) {
        return showTransformGizmo(nodeId, center, PreviewOptions.createTransformGizmo());
    }

    public static String showTransformGizmo(String nodeId, Vec3d center, PreviewOptions options) {
        return RENDERER.showPreview(nodeId, "transformation_gizmo", center, options);
    }

    // Generic control

    public static void hidePreview(String previewId) {
        RENDERER.hidePreview(previewId);
    }

    public static void hideNodePreviews(String nodeId) {
        RENDERER.hidePreviewsByNode(nodeId);
    }

    public static void clearAllPreviews() {
        RENDERER.clearAllPreviews();
    }

    public static int getActivePreviewCount() {
        return RENDERER.getActivePreviewCount();
    }

    public static void updatePreview(String previewId, Object newData) {
        RENDERER.updatePreview(previewId, newData, null);
    }

    public static void updatePreviewOptions(String previewId, PreviewOptions newOptions) {
        RENDERER.updatePreview(previewId, null, newOptions);
    }

    // Global settings

    public static void setGlobalPreviewEnabled(boolean enabled) {
        RENDERER.setGlobalPreviewEnabled(enabled);
    }

    public static void setGlobalOpacity(float opacity) {
        RENDERER.setGlobalOpacity(opacity);
    }

    public static PreviewRenderer.PreviewRenderSettings getSettings() {
        return RENDERER.getSettings();
    }

    // Convenience helpers

    public static PreviewOptions createColoredHighlight(float r, float g, float b, float opacity) {
        return new PreviewOptions()
                .setColor(r, g, b)
                .setOpacity(opacity)
                .wireframeMode()
                .setLineWidth(2.0f);
    }

    public static PreviewOptions createPulsingRegionBox(float r, float g, float b) {
        return new PreviewOptions()
                .setColor(r, g, b)
                .setOpacity(0.4f)
                .enablePulse()
                .setLineWidth(1.5f);
    }

    public static PreviewOptions createTransparentGhostBlocks(float opacity) {
        return new PreviewOptions()
                .ghostBlockMode()
                .setOpacity(opacity);
    }
}
