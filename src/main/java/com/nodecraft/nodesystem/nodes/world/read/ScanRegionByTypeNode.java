package com.nodecraft.nodesystem.nodes.world.read;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.datatypes.RegionData;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@NodeInfo(
    id = "world.read.scan_region_by_type",
    displayName = "Scan Region By Type",
    description = "Scans a region and returns per-block-type counts for analysis and conditional building",
    category = "world.read",
    order = 9
)
public class ScanRegionByTypeNode extends BaseNode {
    private static final String INPUT_REGION_ID = "input_region";
    private static final String INPUT_INCLUDE_AIR_ID = "input_include_air";
    private static final String INPUT_TARGET_BLOCK_ID = "input_target_block";

    private static final String OUTPUT_TYPE_COUNTS_ID = "output_type_counts";
    private static final String OUTPUT_TOTAL_SCANNED_ID = "output_total_scanned";
    private static final String OUTPUT_UNIQUE_TYPE_COUNT_ID = "output_unique_type_count";
    private static final String OUTPUT_MOST_COMMON_BLOCK_ID = "output_most_common_block";
    private static final String OUTPUT_MOST_COMMON_COUNT_ID = "output_most_common_count";
    private static final String OUTPUT_TARGET_COUNT_ID = "output_target_count";

    private boolean includeAir = false;

    public ScanRegionByTypeNode() {
        super(UUID.randomUUID(), "world.read.scan_region_by_type");
        addInputPort(new BasePort(INPUT_REGION_ID, "Region", "Region to scan", NodeDataType.REGION, this));
        addInputPort(new BasePort(INPUT_INCLUDE_AIR_ID, "Include Air", "Whether air blocks should be counted", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_TARGET_BLOCK_ID, "Target Block", "Optional target block id or block state for direct counting", NodeDataType.BLOCK_INFO, this));

        addOutputPort(new BasePort(OUTPUT_TYPE_COUNTS_ID, "Type Counts", "List of \"block_id=count\" pairs sorted by count desc", NodeDataType.LIST, this));
        addOutputPort(new BasePort(OUTPUT_TOTAL_SCANNED_ID, "Total Scanned", "Total scanned block positions", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_UNIQUE_TYPE_COUNT_ID, "Unique Type Count", "Number of distinct block types in scan", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_MOST_COMMON_BLOCK_ID, "Most Common Block", "Most common block id in scan", NodeDataType.STRING, this));
        addOutputPort(new BasePort(OUTPUT_MOST_COMMON_COUNT_ID, "Most Common Count", "Count of most common block id", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_TARGET_COUNT_ID, "Target Count", "Count of target block id if provided", NodeDataType.INTEGER, this));
    }

    @Override
    public String getDescription() {
        return "Scans a region and returns per-block-type counts for analysis and conditional building";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        List<String> typeCounts = new ArrayList<>();
        int totalScanned = 0;
        int uniqueTypeCount = 0;
        String mostCommonBlock = "";
        int mostCommonCount = 0;
        int targetCount = 0;

        Object regionObj = inputValues.get(INPUT_REGION_ID);
        boolean includeAirValue = inputValues.get(INPUT_INCLUDE_AIR_ID) instanceof Boolean value ? value : includeAir;
        String targetId = resolveTargetId(inputValues.get(INPUT_TARGET_BLOCK_ID));

        if (context != null && context.getWorld() != null && regionObj instanceof RegionData region && region.isComplete()) {
            Map<String, Integer> counts = new LinkedHashMap<>();
            BlockPos min = region.getMinCorner();
            BlockPos max = region.getMaxCorner();
            if (min != null && max != null) {
                for (BlockPos pos : BlockPos.iterate(min, max)) {
                    BlockPos immutablePos = pos.toImmutable();
                    boolean isAir = context.getWorld().isAir(immutablePos);
                    if (!includeAirValue && isAir) {
                        continue;
                    }
                    totalScanned++;
                    BlockState state = context.getWorld().getBlockState(immutablePos);
                    String blockId = Registries.BLOCK.getId(state.getBlock()).toString();
                    counts.put(blockId, counts.getOrDefault(blockId, 0) + 1);
                }
            }

            List<Map.Entry<String, Integer>> sorted = counts.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                    .thenComparing(Map.Entry.comparingByKey()))
                .toList();
            for (Map.Entry<String, Integer> entry : sorted) {
                typeCounts.add(entry.getKey() + "=" + entry.getValue());
            }
            uniqueTypeCount = counts.size();
            if (!sorted.isEmpty()) {
                mostCommonBlock = sorted.get(0).getKey();
                mostCommonCount = sorted.get(0).getValue();
            }
            if (targetId != null) {
                targetCount = counts.getOrDefault(targetId, 0);
            }
        }

        outputValues.put(OUTPUT_TYPE_COUNTS_ID, typeCounts);
        outputValues.put(OUTPUT_TOTAL_SCANNED_ID, totalScanned);
        outputValues.put(OUTPUT_UNIQUE_TYPE_COUNT_ID, uniqueTypeCount);
        outputValues.put(OUTPUT_MOST_COMMON_BLOCK_ID, mostCommonBlock);
        outputValues.put(OUTPUT_MOST_COMMON_COUNT_ID, mostCommonCount);
        outputValues.put(OUTPUT_TARGET_COUNT_ID, targetCount);
    }

    private String resolveTargetId(Object targetObj) {
        if (targetObj instanceof BlockState state) {
            return Registries.BLOCK.getId(state.getBlock()).toString();
        }
        if (targetObj instanceof String idString && !idString.isBlank()) {
            try {
                Identifier id = Identifier.of(idString.trim());
                if (Registries.BLOCK.containsId(id)) {
                    return id.toString();
                }
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }
}

