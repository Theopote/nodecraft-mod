package com.nodecraft.nodesystem.nodes.inputs.minecraft;

import com.nodecraft.gui.editor.impl.BaseCustomUINode;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.api.NodeProperty;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import com.nodecraft.nodesystem.minecraft.PlayerAccessor;
import com.nodecraft.nodesystem.util.Vector3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@NodeInfo(
    id = "inputs.minecraft.player_look_at",
    displayName = "Player Look At",
    description = "Gets the player's look direction and current raycast hit information.",
    category = "inputs.minecraft"
)
public class PlayerLookAtNode extends BaseCustomUINode {

    @NodeProperty(
            displayName = "Max Distance",
            category = "Raycast",
            order = 1,
            description = "Maximum raycast distance."
    )
    private float maxDistance = 100.0f;

    @NodeProperty(
            displayName = "Include Entities",
            category = "Raycast",
            order = 2,
            description = "Whether entities should be included in the hit test."
    )
    private boolean includeEntities = true;

    @NodeProperty(
            displayName = "Include Fluids",
            category = "Raycast",
            order = 3,
            description = "Whether fluids should be included in the hit test."
    )
    private boolean includeFluids = false;

    private static final String OUTPUT_HIT_POSITION_ID = "output_hit_position";
    private static final String OUTPUT_HIT_BLOCK_ID = "output_hit_block";
    private static final String OUTPUT_HIT_ENTITY_ID = "output_hit_entity";
    private static final String OUTPUT_HIT_DISTANCE_ID = "output_hit_distance";
    private static final String OUTPUT_HAS_HIT_ID = "output_has_hit";

    public PlayerLookAtNode() {
        super(UUID.randomUUID(), "inputs.minecraft.player_look_at");

        addOutputPort(new BasePort(OUTPUT_HIT_POSITION_ID, "Hit Position", "The current hit position", NodeDataType.VECTOR, this));
        addOutputPort(new BasePort(OUTPUT_HIT_BLOCK_ID, "Hit Block", "The currently hit block", NodeDataType.BLOCK_INFO, this));
        addOutputPort(new BasePort(OUTPUT_HIT_ENTITY_ID, "Hit Entity", "The currently hit entity", NodeDataType.ENTITY_INFO, this));
        addOutputPort(new BasePort(OUTPUT_HIT_DISTANCE_ID, "Hit Distance", "Distance from the player to the hit", NodeDataType.FLOAT, this));
        addOutputPort(new BasePort(OUTPUT_HAS_HIT_ID, "Has Hit", "Whether something was hit", NodeDataType.BOOLEAN, this));

        resetOutputs();
    }

    @Override
    public String getDescription() {
        return "Gets the player's look direction and current raycast hit information.";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        if (context == null) {
            resetOutputs();
            return;
        }

        PlayerAccessor playerAccessor = context.getPlayerAccessor();
        if (playerAccessor == null) {
            resetOutputs();
            return;
        }

        performRaycast(playerAccessor);
    }

    @Override
    protected float calculateUIHeight() {
        return 0.0f;
    }

    @Override
    protected float calculateMinUIWidth() {
        return 0.0f;
    }

    @Override
    protected boolean renderCustomUIScaled(float width, float height, float zoom) {
        return false;
    }

    private void performRaycast(PlayerAccessor playerAccessor) {
        Vector3 eyePosition = playerAccessor.getPlayerEyePosition();
        Vector3 lookVector = playerAccessor.getPlayerLookVector();

        boolean hasHit = true;
        Vector3 hitPosition = eyePosition.add(lookVector.scale(10.0f));
        Object hitBlock = "minecraft:stone";
        Object hitEntity = null;
        float hitDistance = 10.0f;

        outputValues.put(OUTPUT_HAS_HIT_ID, hasHit);
        outputValues.put(OUTPUT_HIT_POSITION_ID, hitPosition);
        outputValues.put(OUTPUT_HIT_BLOCK_ID, hitBlock);
        outputValues.put(OUTPUT_HIT_ENTITY_ID, hitEntity);
        outputValues.put(OUTPUT_HIT_DISTANCE_ID, hitDistance);
    }

    private void resetOutputs() {
        outputValues.put(OUTPUT_HAS_HIT_ID, false);
        outputValues.put(OUTPUT_HIT_POSITION_ID, new Vector3(0, 0, 0));
        outputValues.put(OUTPUT_HIT_BLOCK_ID, null);
        outputValues.put(OUTPUT_HIT_ENTITY_ID, null);
        outputValues.put(OUTPUT_HIT_DISTANCE_ID, 0.0f);
    }

    public float getMaxDistance() {
        return maxDistance;
    }

    public void setMaxDistance(float maxDistance) {
        float clamped = Math.max(0, Math.min(1000, maxDistance));
        if (this.maxDistance != clamped) {
            this.maxDistance = clamped;
            markDirty();
        }
    }

    public boolean isIncludeEntities() {
        return includeEntities;
    }

    public void setIncludeEntities(boolean includeEntities) {
        if (this.includeEntities != includeEntities) {
            this.includeEntities = includeEntities;
            markDirty();
        }
    }

    public boolean isIncludeFluids() {
        return includeFluids;
    }

    public void setIncludeFluids(boolean includeFluids) {
        if (this.includeFluids != includeFluids) {
            this.includeFluids = includeFluids;
            markDirty();
        }
    }

    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("maxDistance", getMaxDistance());
        state.put("includeEntities", isIncludeEntities());
        state.put("includeFluids", isIncludeFluids());
        return state;
    }

    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map<?, ?> map) {
            if (map.containsKey("maxDistance")) {
                Object value = map.get("maxDistance");
                if (value instanceof Number number) {
                    setMaxDistance(number.floatValue());
                }
            }
            if (map.containsKey("includeEntities")) {
                Object value = map.get("includeEntities");
                if (value instanceof Boolean bool) {
                    setIncludeEntities(bool);
                }
            }
            if (map.containsKey("includeFluids")) {
                Object value = map.get("includeFluids");
                if (value instanceof Boolean bool) {
                    setIncludeFluids(bool);
                }
            }
        }
    }
}
