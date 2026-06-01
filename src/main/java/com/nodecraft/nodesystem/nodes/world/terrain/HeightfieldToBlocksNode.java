package com.nodecraft.nodesystem.nodes.world.terrain;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.datatypes.RegionData;
import com.nodecraft.nodesystem.datatypes.ScalarFieldData;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import com.nodecraft.nodesystem.util.BlockPlacementData;
import com.nodecraft.nodesystem.util.BlockPosList;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@NodeInfo(
    id = "world.terrain.heightfield_to_blocks",
    displayName = "Heightfield To Blocks",
    description = "Converts a height field inside a region to terrain block placements.",
    category = "world.terrain",
    order = 16
)
public class HeightfieldToBlocksNode extends BaseNode {

    private static final String INPUT_REGION_ID = "input_region";
    private static final String INPUT_HEIGHT_FIELD_ID = "input_height_field";
    private static final String INPUT_SURFACE_BLOCK_ID = "input_surface_block";
    private static final String INPUT_SUBSURFACE_BLOCK_ID = "input_subsurface_block";
    private static final String INPUT_WATER_LEVEL_ID = "input_water_level";

    private static final String OUTPUT_BLOCK_PLACEMENTS_ID = "output_block_placements";
    private static final String OUTPUT_SURFACE_POINTS_ID = "output_surface_points";

    public HeightfieldToBlocksNode() {
        super(UUID.randomUUID(), "world.terrain.heightfield_to_blocks");

        addInputPort(new BasePort(INPUT_REGION_ID, "Region", "Region to rasterize", NodeDataType.REGION, this));
        addInputPort(new BasePort(INPUT_HEIGHT_FIELD_ID, "Height Field", "Input terrain height field", NodeDataType.SCALAR_FIELD, this));
        addInputPort(new BasePort(INPUT_SURFACE_BLOCK_ID, "Surface Block", "Top-layer block id", NodeDataType.BLOCK_TYPE, this));
        addInputPort(new BasePort(INPUT_SUBSURFACE_BLOCK_ID, "Subsurface Block", "Inner-layer block id", NodeDataType.BLOCK_TYPE, this));
        addInputPort(new BasePort(INPUT_WATER_LEVEL_ID, "Water Level", "Absolute water level in world Y", NodeDataType.DOUBLE, this));

        addOutputPort(new BasePort(OUTPUT_BLOCK_PLACEMENTS_ID, "Block Placements", "Generated placements for world write nodes", NodeDataType.BLOCK_PLACEMENT_LIST, this));
        addOutputPort(new BasePort(OUTPUT_SURFACE_POINTS_ID, "Surface Points", "Top point per sampled X/Z column", NodeDataType.BLOCK_LIST, this));
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        Object regionObj = inputValues.get(INPUT_REGION_ID);
        Object heightObj = inputValues.get(INPUT_HEIGHT_FIELD_ID);

        if (!(regionObj instanceof RegionData region) || !region.isComplete() || !(heightObj instanceof ScalarFieldData heightField)) {
            outputValues.put(OUTPUT_BLOCK_PLACEMENTS_ID, List.of());
            outputValues.put(OUTPUT_SURFACE_POINTS_ID, new BlockPosList());
            return;
        }

        BlockPos min = region.getMinCorner();
        BlockPos max = region.getMaxCorner();
        if (min == null || max == null) {
            outputValues.put(OUTPUT_BLOCK_PLACEMENTS_ID, List.of());
            outputValues.put(OUTPUT_SURFACE_POINTS_ID, new BlockPosList());
            return;
        }

        String surfaceBlock = getInputString(INPUT_SURFACE_BLOCK_ID, "minecraft:grass_block");
        String subsurfaceBlock = getInputString(INPUT_SUBSURFACE_BLOCK_ID, "minecraft:stone");
        int waterLevel = (int) Math.round(getInputDouble(INPUT_WATER_LEVEL_ID, min.getY() + 62.0d));

        List<BlockPlacementData> placements = new ArrayList<>();
        BlockPosList surfacePoints = new BlockPosList();

        Vector3d samplePoint = new Vector3d();
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int z = min.getZ(); z <= max.getZ(); z++) {
                samplePoint.set(x, min.getY(), z);
                double sampled = heightField.sampleScalar(samplePoint);

                int columnTop = toColumnTopY(sampled, min.getY(), max.getY());
                for (int y = min.getY(); y <= columnTop; y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    String blockId = (y == columnTop) ? surfaceBlock : subsurfaceBlock;
                    placements.add(new BlockPlacementData(pos, blockId));
                }

                if (columnTop < waterLevel) {
                    for (int y = columnTop + 1; y <= Math.min(waterLevel, max.getY()); y++) {
                        placements.add(new BlockPlacementData(new BlockPos(x, y, z), "minecraft:water"));
                    }
                }

                surfacePoints.add(new BlockPos(x, columnTop, z));
            }
        }

        outputValues.put(OUTPUT_BLOCK_PLACEMENTS_ID, placements);
        outputValues.put(OUTPUT_SURFACE_POINTS_ID, surfacePoints);
    }

    private int toColumnTopY(double sampledHeight, int minY, int maxY) {
        double normalized = clamp(sampledHeight, -1.0d, 1.0d);
        double t = (normalized + 1.0d) * 0.5d;
        int y = (int) Math.round(minY + t * (maxY - minY));
        return clamp(y, minY, maxY);
    }

    private String getInputString(String portId, String fallback) {
        Object value = inputValues.get(portId);
        return (value instanceof String text && !text.isBlank()) ? text : fallback;
    }

    private double getInputDouble(String portId, double fallback) {
        Object value = inputValues.get(portId);
        return value instanceof Number number ? number.doubleValue() : fallback;
    }

    private int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        return Math.min(value, max);
    }

    private double clamp(double value, double min, double max) {
        if (value < min) {
            return min;
        }
        return Math.min(value, max);
    }
}
