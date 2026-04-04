package com.nodecraft.nodesystem.nodes.world.modification;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.datatypes.BoxGeometryData;
import com.nodecraft.nodesystem.datatypes.RegionData;
import com.nodecraft.nodesystem.datatypes.TorusGeometryData;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import com.nodecraft.nodesystem.util.BlockPlacementData;
import com.nodecraft.nodesystem.util.BlockPosList;
import com.nodecraft.nodesystem.util.BoxBlockGenerator;
import com.nodecraft.nodesystem.util.TorusBlockGenerator;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Maps a coordinate set to block IDs based on relative height bands.
 * Geometry inputs are voxelized internally so the material stage can sit
 * downstream of geometry-producing nodes.
 */
@NodeInfo(
    id = "world.modification.material_mapper",
    displayName = "Material Mapper",
    description = "Assigns bottom, middle, and top block types across a shape.",
    category = "world.modification"
)
public class MaterialMapperNode extends BaseNode {

    private static final String INPUT_COORDINATES_ID = "input_coordinates";
    private static final String INPUT_BOX_GEOMETRY_ID = "input_box_geometry";
    private static final String INPUT_TORUS_GEOMETRY_ID = "input_torus_geometry";
    private static final String INPUT_BOTTOM_ID = "input_bottom";
    private static final String INPUT_MIDDLE_ID = "input_middle";
    private static final String INPUT_TOP_ID = "input_top";

    private static final String OUTPUT_POSITIONS_ID = "output_positions";
    private static final String OUTPUT_BLOCK_IDS_ID = "output_block_ids";
    private static final String OUTPUT_PLACEMENTS_ID = "output_placements";

    public MaterialMapperNode() {
        super(UUID.randomUUID(), "world.modification.material_mapper");
        addInputPort(new BasePort(INPUT_COORDINATES_ID, "Coordinates", "Block coordinate list", NodeDataType.BLOCK_LIST, this));
        addInputPort(new BasePort(INPUT_BOX_GEOMETRY_ID, "Box Geometry", "Box geometry data to materialize", NodeDataType.BOX_GEOMETRY, this));
        addInputPort(new BasePort(INPUT_TORUS_GEOMETRY_ID, "Torus Geometry", "Torus geometry data to materialize", NodeDataType.TORUS_GEOMETRY, this));
        addInputPort(new BasePort(INPUT_BOTTOM_ID, "Bottom", "Block for the lower third", NodeDataType.BLOCK_TYPE, this));
        addInputPort(new BasePort(INPUT_MIDDLE_ID, "Middle", "Block for the middle third", NodeDataType.BLOCK_TYPE, this));
        addInputPort(new BasePort(INPUT_TOP_ID, "Top", "Block for the upper third", NodeDataType.BLOCK_TYPE, this));

        addOutputPort(new BasePort(OUTPUT_POSITIONS_ID, "Positions", "Resolved block positions", NodeDataType.BLOCK_LIST, this));
        addOutputPort(new BasePort(OUTPUT_BLOCK_IDS_ID, "Block IDs", "Block IDs aligned with the positions list", NodeDataType.BLOCK_INFO_LIST, this));
        addOutputPort(new BasePort(OUTPUT_PLACEMENTS_ID, "Block Placements", "Position and block pairs for baking", NodeDataType.BLOCK_PLACEMENT_LIST, this));
    }

    @Override
    public String getDescription() {
        return "Assigns bottom, middle, and top block types across a shape.";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        Object coordsObj = inputValues.get(INPUT_COORDINATES_ID);
        Object boxGeometryObj = inputValues.get(INPUT_BOX_GEOMETRY_ID);
        Object torusGeometryObj = inputValues.get(INPUT_TORUS_GEOMETRY_ID);

        String bottom = getInputString(INPUT_BOTTOM_ID, "minecraft:stone");
        String middle = getInputString(INPUT_MIDDLE_ID, "minecraft:dirt");
        String top = getInputString(INPUT_TOP_ID, "minecraft:grass_block");

        BlockPosList positions = resolveCoordinates(coordsObj, boxGeometryObj, torusGeometryObj);
        List<String> blockIds = new ArrayList<>();
        List<BlockPlacementData> placements = new ArrayList<>();

        if (positions.isEmpty()) {
            publishOutputs(new BlockPosList(), blockIds, placements);
            return;
        }

        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (BlockPos pos : positions) {
            minY = Math.min(minY, pos.getY());
            maxY = Math.max(maxY, pos.getY());
        }

        double span = maxY - minY;
        if (span < 1e-6) {
            span = 1.0;
        }

        BlockPosList outputPositions = new BlockPosList();
        for (BlockPos pos : positions) {
            double t = (pos.getY() - minY) / span;
            String blockId;
            if (t < 1.0 / 3.0) {
                blockId = bottom;
            } else if (t < 2.0 / 3.0) {
                blockId = middle;
            } else {
                blockId = top;
            }

            outputPositions.add(pos);
            blockIds.add(blockId);
            placements.add(new BlockPlacementData(pos, blockId));
        }

        publishOutputs(outputPositions, blockIds, placements);
    }

    private void publishOutputs(BlockPosList positions, List<String> blockIds, List<BlockPlacementData> placements) {
        outputValues.put(OUTPUT_POSITIONS_ID, positions);
        outputValues.put(OUTPUT_BLOCK_IDS_ID, blockIds);
        outputValues.put(OUTPUT_PLACEMENTS_ID, placements);
    }

    private String getInputString(String portId, String fallback) {
        Object value = inputValues.get(portId);
        return (value instanceof String && !((String) value).isEmpty()) ? (String) value : fallback;
    }

    private BlockPosList resolveCoordinates(Object coordsObj, Object boxGeometryObj, Object torusGeometryObj) {
        if (coordsObj instanceof BlockPosList blockPosList) {
            return blockPosList;
        }

        if (boxGeometryObj instanceof BoxGeometryData geometry) {
            return voxelizeBoxGeometry(geometry);
        }

        if (torusGeometryObj instanceof TorusGeometryData geometry) {
            return voxelizeTorusGeometry(geometry);
        }

        return new BlockPosList();
    }

    private BlockPosList voxelizeBoxGeometry(BoxGeometryData geometry) {
        BlockPosList blocks = new BlockPosList();

        if (geometry.isOriented()) {
            RegionData region = BoxBlockGenerator.createOrientedBoundingRegion(
                geometry.getCenter(),
                geometry.getHalfExtents(),
                geometry.getOrientationMatrix()
            );
            if (region == null || !region.isComplete()) {
                return blocks;
            }

            BlockPos minCorner = region.getMinCorner();
            BlockPos maxCorner = region.getMaxCorner();
            if (minCorner == null || maxCorner == null) {
                return blocks;
            }

            BoxBlockGenerator.populateOrientedBox(
                blocks,
                minCorner,
                maxCorner,
                geometry.getCenter(),
                geometry.getHalfExtents(),
                geometry.getOrientationMatrix(),
                true
            );
            return blocks;
        }

        Vector3d center = geometry.getCenter();
        Vector3d halfExtents = geometry.getHalfExtents();
        BlockPos minCorner = BlockPos.ofFloored(
            center.x - halfExtents.x,
            center.y - halfExtents.y,
            center.z - halfExtents.z
        );
        BlockPos maxCorner = BlockPos.ofFloored(
            center.x + halfExtents.x,
            center.y + halfExtents.y,
            center.z + halfExtents.z
        );
        BoxBlockGenerator.populateAxisAlignedBox(blocks, minCorner, maxCorner, true);
        return blocks;
    }

    private BlockPosList voxelizeTorusGeometry(TorusGeometryData geometry) {
        BlockPosList blocks = new BlockPosList();
        RegionData region = TorusBlockGenerator.createBoundingRegion(geometry);
        TorusBlockGenerator.populateTorus(blocks, region, geometry, true);
        return blocks;
    }
}
