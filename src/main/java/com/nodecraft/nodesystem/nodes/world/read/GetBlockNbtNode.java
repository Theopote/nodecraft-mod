package com.nodecraft.nodesystem.nodes.world.read;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.UUID;

@NodeInfo(
    id = "world.read.get_block_nbt",
    displayName = "Get Block NBT",
    description = "Reads full block-entity NBT data at a block position.",
    category = "world.read",
    order = 10
)
public class GetBlockNbtNode extends BaseNode {

    private static final String INPUT_COORDINATE_ID = "input_coordinate";

    private static final String OUTPUT_HAS_BLOCK_ENTITY_ID = "output_has_block_entity";
    private static final String OUTPUT_NBT_ID = "output_nbt";
    private static final String OUTPUT_NBT_STRING_ID = "output_nbt_string";
    private static final String OUTPUT_SUCCESS_ID = "output_success";

    public GetBlockNbtNode() {
        super(UUID.randomUUID(), "world.read.get_block_nbt");

        addInputPort(new BasePort(INPUT_COORDINATE_ID, "Coordinate", "Block position to inspect", NodeDataType.BLOCK_POS, this));

        addOutputPort(new BasePort(OUTPUT_HAS_BLOCK_ENTITY_ID, "Has Block Entity", "Whether this block has block-entity data", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_NBT_ID, "NBT", "Block-entity NBT compound", NodeDataType.NBT_COMPOUND, this));
        addOutputPort(new BasePort(OUTPUT_NBT_STRING_ID, "NBT String", "SNBT string representation", NodeDataType.STRING, this));
        addOutputPort(new BasePort(OUTPUT_SUCCESS_ID, "Success", "Whether NBT read succeeded", NodeDataType.BOOLEAN, this));
    }

    @Override
    public String getDescription() {
        return "Reads full block-entity NBT data at a block position.";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        if (context == null || context.getWorld() == null || !(inputValues.get(INPUT_COORDINATE_ID) instanceof BlockPos pos)) {
            writeInvalid();
            return;
        }

        BlockEntity blockEntity = context.getWorld().getBlockEntity(pos);
        if (blockEntity == null) {
            outputValues.put(OUTPUT_HAS_BLOCK_ENTITY_ID, false);
            outputValues.put(OUTPUT_NBT_ID, null);
            outputValues.put(OUTPUT_NBT_STRING_ID, "");
            outputValues.put(OUTPUT_SUCCESS_ID, false);
            return;
        }

        NbtCompound nbt = extractBlockEntityNbt(blockEntity, context);
        boolean success = nbt != null;
        outputValues.put(OUTPUT_HAS_BLOCK_ENTITY_ID, true);
        outputValues.put(OUTPUT_NBT_ID, success ? nbt : null);
        outputValues.put(OUTPUT_NBT_STRING_ID, success ? nbt.toString() : "");
        outputValues.put(OUTPUT_SUCCESS_ID, success);
    }

    private @Nullable NbtCompound extractBlockEntityNbt(BlockEntity blockEntity, ExecutionContext context) {
        Object lookup = context.getWorld() != null ? context.getWorld().getRegistryManager() : null;
        Method[] methods = blockEntity.getClass().getMethods();
        for (Method method : methods) {
            if (!method.getName().startsWith("createNbt")) {
                continue;
            }
            try {
                if (method.getParameterCount() == 0) {
                    Object result = method.invoke(blockEntity);
                    if (result instanceof NbtCompound nbt) {
                        return nbt;
                    }
                } else if (method.getParameterCount() == 1 && lookup != null) {
                    Object result = method.invoke(blockEntity, lookup);
                    if (result instanceof NbtCompound nbt) {
                        return nbt;
                    }
                }
            } catch (Exception ignored) {
            }
        }

        for (Method method : methods) {
            if (!"writeNbt".equals(method.getName())) {
                continue;
            }
            try {
                if (method.getParameterCount() == 1 && method.getParameterTypes()[0] == NbtCompound.class) {
                    NbtCompound out = new NbtCompound();
                    Object result = method.invoke(blockEntity, out);
                    if (result instanceof NbtCompound nbt) {
                        return nbt;
                    }
                    return out;
                }
                if (method.getParameterCount() == 2
                    && method.getParameterTypes()[0] == NbtCompound.class
                    && lookup != null) {
                    NbtCompound out = new NbtCompound();
                    Object result = method.invoke(blockEntity, out, lookup);
                    if (result instanceof NbtCompound nbt) {
                        return nbt;
                    }
                    return out;
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private void writeInvalid() {
        outputValues.put(OUTPUT_HAS_BLOCK_ENTITY_ID, false);
        outputValues.put(OUTPUT_NBT_ID, null);
        outputValues.put(OUTPUT_NBT_STRING_ID, "");
        outputValues.put(OUTPUT_SUCCESS_ID, false);
    }
}
