package com.nodecraft.nodesystem.nodes.animation.effects;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import com.nodecraft.nodesystem.util.BlockPosList;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import net.minecraft.util.math.BlockPos;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Shake Blocks: 震动方块效果节点
 * 使方块群或建筑部分随机颤抖
 */
@NodeInfo(
    id = "animation.effects.shake_blocks",
    displayName = "Shake Blocks",
    description = "使方块群或建筑部分随机颤抖",
    category = "animation.effects"
)
public class ShakeBlocksNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_COORDINATES_ID = "input_coordinates";
    private static final String INPUT_MAGNITUDE_ID = "input_magnitude";
    private static final String INPUT_FREQUENCY_ID = "input_frequency";
    private static final String INPUT_TIME_ID = "input_time";
    private static final String INPUT_SEED_ID = "input_seed";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_SHAKEN_COORDINATES_ID = "output_shaken_coordinates";

    // --- 状态变量 ---
    private Random random = new Random();

    // --- 构造函数 ---
    public ShakeBlocksNode() {
        super(UUID.randomUUID(), "animation.effects.shake_blocks");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_COORDINATES_ID, "Coordinates", "要震动的方块坐标列表", NodeDataType.BLOCK_LIST, this));
        addInputPort(new BasePort(INPUT_MAGNITUDE_ID, "Magnitude", "震动幅度（方块单位）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_FREQUENCY_ID, "Frequency", "震动频率", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_TIME_ID, "Time", "时间因子", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_SEED_ID, "Seed", "随机种子 (可选)", NodeDataType.INTEGER, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_SHAKEN_COORDINATES_ID, "Shaken Coordinates", "震动后的方块坐标列表", NodeDataType.BLOCK_LIST, this));
    }
    
    @Override
    public String getDescription() {
        return "使方块群或建筑部分随机颤抖";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        BlockPosList coordinates = (BlockPosList) inputValues.get(INPUT_COORDINATES_ID);
        Float magnitude = (Float) inputValues.getOrDefault(INPUT_MAGNITUDE_ID, 0.5f);
        Float frequency = (Float) inputValues.getOrDefault(INPUT_FREQUENCY_ID, 5.0f);
        Float time = (Float) inputValues.getOrDefault(INPUT_TIME_ID, 0.0f);
        Integer seed = (Integer) inputValues.getOrDefault(INPUT_SEED_ID, 0);
        
        // 如果坐标为null，则输出null
        if (coordinates == null) {
            outputValues.put(OUTPUT_SHAKEN_COORDINATES_ID, null);
            return;
        }
        
        // 确保幅度是正数
        magnitude = Math.abs(magnitude);
        
        // 确保频率是正数
        frequency = Math.max(0.1f, frequency);
        
        // 设置随机种子，使同一时间点的震动保持一致
        long combinedSeed = seed + Math.round(time * frequency * 1000);
        random.setSeed(combinedSeed);
        
        // 应用震动效果到每个方块坐标
        List<BlockPos> originalCoordinates = coordinates.getPositions();
        List<BlockPos> shakenCoordinates = new ArrayList<>(originalCoordinates.size());
        
        for (BlockPos pos : originalCoordinates) {
            // 为每个方块计算基于其位置的一致性随机偏移
            float offsetX = (random.nextFloat() * 2 - 1) * magnitude;
            float offsetY = (random.nextFloat() * 2 - 1) * magnitude;
            float offsetZ = (random.nextFloat() * 2 - 1) * magnitude;
            
            // 添加基于时间的震动组件，使震动随时间变化
            float timeComponent = (float) Math.sin(time * frequency * Math.PI * 2);
            offsetX *= timeComponent;
            offsetY *= timeComponent;
            offsetZ *= timeComponent;
            
            // 应用偏移创建新坐标
            BlockPos newPos = new BlockPos(
                (int) Math.round(pos.getX() + offsetX),
                (int) Math.round(pos.getY() + offsetY),
                (int) Math.round(pos.getZ() + offsetZ)
            );
            
            shakenCoordinates.add(newPos);
        }
        
        // 创建新的BlockPosList并设置为输出
        BlockPosList result = new BlockPosList(shakenCoordinates);
        outputValues.put(OUTPUT_SHAKEN_COORDINATES_ID, result);
    }
    
    /**
     * 使用柏林噪声计算更自然的震动
     * 为每个坐标创建一个基于时间和位置的随机但平滑的偏移
     */
    private Vector3d computeNoiseOffset(BlockPos pos, float magnitude, float time, float frequency) {
        // 创建不同维度的噪声输入
        float noiseX = pos.getX() * 0.1f + time * frequency;
        float noiseY = pos.getY() * 0.1f + time * frequency + 100;
        float noiseZ = pos.getZ() * 0.1f + time * frequency + 200;
        
        // 使用简单的噪声计算替代（实际应用中可以使用更高质量的柏林噪声）
        float offsetX = (float) (Math.sin(noiseX) * Math.cos(noiseY) * magnitude);
        float offsetY = (float) (Math.sin(noiseY) * Math.cos(noiseZ) * magnitude);
        float offsetZ = (float) (Math.sin(noiseZ) * Math.cos(noiseX) * magnitude);
        
        return new Vector3d(offsetX, offsetY, offsetZ);
    }
} 