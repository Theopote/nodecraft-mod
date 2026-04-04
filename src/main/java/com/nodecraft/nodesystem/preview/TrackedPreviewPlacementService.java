package com.nodecraft.nodesystem.preview;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.nodecraft.nodesystem.bake.PlacementMode;

/**
 * Tracks temporary preview blocks placed directly into the world.
 * Clearing a tracked preview restores the previous world state.
 * Committing a tracked preview only removes the tracking metadata.
 */
public final class TrackedPreviewPlacementService {

    private static final TrackedPreviewPlacementService INSTANCE = new TrackedPreviewPlacementService();

    private final Map<World, Map<String, TrackedPreviewState>> trackedPreviews = new IdentityHashMap<>();

    private TrackedPreviewPlacementService() {
    }

    public static TrackedPreviewPlacementService getInstance() {
        return INSTANCE;
    }

    public synchronized int updateTrackedPreview(World world,
                                                 String nodeId,
                                                 List<BlockPos> positions,
                                                 BlockState previewState,
                                                 PlacementMode placementMode) {
        if (world == null || nodeId == null || nodeId.isEmpty() || positions == null || positions.isEmpty() || previewState == null) {
            clearTrackedPreview(world, nodeId);
            return 0;
        }

        clearTrackedPreview(world, nodeId);

        Map<BlockPos, BlockState> previousStates = new LinkedHashMap<>();
        int placedCount = 0;

        for (BlockPos originalPos : positions) {
            if (originalPos == null) {
                continue;
            }

            BlockPos pos = originalPos.toImmutable();
            if (placementMode == PlacementMode.INCREMENTAL && !world.isAir(pos)) {
                continue;
            }

            previousStates.put(pos, world.getBlockState(pos));
            if (world.setBlockState(pos, previewState, Block.NOTIFY_ALL)) {
                placedCount++;
            }
        }

        if (!previousStates.isEmpty()) {
            trackedPreviews
                .computeIfAbsent(world, ignored -> new LinkedHashMap<>())
                .put(nodeId, new TrackedPreviewState(previousStates));
        }

        return placedCount;
    }

    public synchronized int clearTrackedPreview(World world, String nodeId) {
        if (world == null || nodeId == null || nodeId.isEmpty()) {
            return 0;
        }

        Map<String, TrackedPreviewState> byNode = trackedPreviews.get(world);
        if (byNode == null) {
            return 0;
        }

        TrackedPreviewState trackedState = byNode.remove(nodeId);
        if (trackedState == null) {
            if (byNode.isEmpty()) {
                trackedPreviews.remove(world);
            }
            return 0;
        }

        int restoredCount = 0;
        for (Map.Entry<BlockPos, BlockState> entry : trackedState.previousStates().entrySet()) {
            if (world.setBlockState(entry.getKey(), entry.getValue(), Block.NOTIFY_ALL)) {
                restoredCount++;
            }
        }

        if (byNode.isEmpty()) {
            trackedPreviews.remove(world);
        }

        return restoredCount;
    }

    public synchronized boolean commitTrackedPreview(World world, String nodeId) {
        if (world == null || nodeId == null || nodeId.isEmpty()) {
            return false;
        }

        Map<String, TrackedPreviewState> byNode = trackedPreviews.get(world);
        if (byNode == null) {
            return false;
        }

        boolean removed = byNode.remove(nodeId) != null;
        if (byNode.isEmpty()) {
            trackedPreviews.remove(world);
        }
        return removed;
    }

    public synchronized int getTrackedCount(World world, String nodeId) {
        if (world == null || nodeId == null || nodeId.isEmpty()) {
            return 0;
        }

        Map<String, TrackedPreviewState> byNode = trackedPreviews.get(world);
        if (byNode == null) {
            return 0;
        }

        TrackedPreviewState trackedState = byNode.get(nodeId);
        return trackedState == null ? 0 : trackedState.previousStates().size();
    }

    public synchronized List<String> getTrackedPreviewIds(World world) {
        Map<String, TrackedPreviewState> byNode = trackedPreviews.get(world);
        if (byNode == null || byNode.isEmpty()) {
            return List.of();
        }
        return new ArrayList<>(byNode.keySet());
    }

    private record TrackedPreviewState(Map<BlockPos, BlockState> previousStates) {
    }
}
