package com.nodecraft.nodesystem.nodes.animation.transforms;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import com.nodecraft.nodesystem.util.BlockPosList;

import org.jetbrains.annotations.Nullable;
import net.minecraft.util.math.BlockPos;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Animate Visibility: 可见性动画节点
 * 使方块逐渐出现或消失
 */
@NodeInfo(
    id = "animation.properties.animate_visibility",
    displayName = "Animate Visibility",
    description = "使方块逐渐出现或消失",
    category = "animation.properties"
)
public class AnimateVisibilityNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_TARGET_BLOCKS_ID = "input_target_blocks";
    private static final String INPUT_TIME_FACTOR_ID = "input_time_factor";
    private static final String INPUT_FADE_DURATION_ID = "input_fade_duration";
    private static final String INPUT_MODE_ID = "input_mode";
    private static final String INPUT_PATTERN_ID = "input_pattern";
    private static final String INPUT_SEED_ID = "input_seed";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_VISIBLE_BLOCKS_ID = "output_visible_blocks";
    private static final String OUTPUT_PROGRESS_ID = "output_progress";

    // --- 淡入淡出模式枚举 ---
    public enum VisibilityMode {
        APPEAR(0, "Appear", "从无到有"),
        DISAPPEAR(1, "Disappear", "从有到无");
        
        private final int id;
        private final String name;
        private final String description;
        
        VisibilityMode(int id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }
        
        public static VisibilityMode fromId(int id) {
            return id == 1 ? DISAPPEAR : APPEAR;
        }
    }
    
    // --- 出现模式枚举 ---
    public enum AppearancePattern {
        RANDOM(0, "Random", "随机顺序"),
        BOTTOM_UP(1, "Bottom Up", "从下到上"),
        TOP_DOWN(2, "Top Down", "从上到下"),
        CENTER_OUT(3, "Center Out", "从中心向外"),
        OUTSIDE_IN(4, "Outside In", "从外向中心"),
        LEFT_RIGHT(5, "Left to Right", "从左到右"),
        RIGHT_LEFT(6, "Right to Left", "从右到左"),
        FRONT_BACK(7, "Front to Back", "从前到后"),
        BACK_FRONT(8, "Back to Front", "从后到前");
        
        private final int id;
        private final String name;
        private final String description;
        
        AppearancePattern(int id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }
        
        public static AppearancePattern fromId(int id) {
            for (AppearancePattern pattern : values()) {
                if (pattern.id == id) {
                    return pattern;
                }
            }
            return RANDOM;
        }
    }

    // --- 状态变量 ---
    private Random random = new Random();
    private List<BlockPos> sortedBlocks = null;
    private UUID lastInputId = null;

    // --- 构造函数 ---
    public AnimateVisibilityNode() {
        super(UUID.randomUUID(), "animation.properties.animate_visibility");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_TARGET_BLOCKS_ID, "Target Blocks", "目标方块坐标列表", NodeDataType.BLOCK_LIST, this));
        addInputPort(new BasePort(INPUT_TIME_FACTOR_ID, "Time Factor", "时间因子 (0-1)", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_FADE_DURATION_ID, "Fade Duration", "淡入淡出持续时间", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_MODE_ID, "Mode", "模式 (0=出现, 1=消失)", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_PATTERN_ID, "Pattern", "出现/消失模式 (0-8)", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_SEED_ID, "Seed", "随机种子", NodeDataType.INTEGER, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_VISIBLE_BLOCKS_ID, "Visible Blocks", "当前可见的方块坐标列表", NodeDataType.BLOCK_LIST, this));
        addOutputPort(new BasePort(OUTPUT_PROGRESS_ID, "Progress", "当前进度 (0-1)", NodeDataType.FLOAT, this));
    }
    
    @Override
    public String getDescription() {
        return "使方块逐渐出现或消失";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        BlockPosList targetBlocks = (BlockPosList) inputValues.get(INPUT_TARGET_BLOCKS_ID);
        Float timeFactor = (Float) inputValues.getOrDefault(INPUT_TIME_FACTOR_ID, 0.0f);
        Float fadeDuration = (Float) inputValues.getOrDefault(INPUT_FADE_DURATION_ID, 1.0f);
        Integer modeId = (Integer) inputValues.getOrDefault(INPUT_MODE_ID, 0);
        Integer patternId = (Integer) inputValues.getOrDefault(INPUT_PATTERN_ID, 0);
        Integer seed = (Integer) inputValues.getOrDefault(INPUT_SEED_ID, 0);
        
        // 确保时间因子在[0,1]范围内
        timeFactor = Math.max(0f, Math.min(1f, timeFactor));
        
        // 确保淡入淡出持续时间为正数
        fadeDuration = Math.max(0.01f, fadeDuration);
        
        // 获取模式和模式
        VisibilityMode mode = VisibilityMode.fromId(modeId);
        AppearancePattern pattern = AppearancePattern.fromId(patternId);
        
        // 默认输出
        BlockPosList visibleBlocks = null;
        float progress = 0.0f;
        
        // 如果目标块为空，则输出空值
        if (targetBlocks == null || targetBlocks.isEmpty()) {
            outputValues.put(OUTPUT_VISIBLE_BLOCKS_ID, null);
            outputValues.put(OUTPUT_PROGRESS_ID, progress);
            return;
        }
        
        // 检查输入是否改变（通过比较UUID），如果改变则重新排序
        UUID currentInputId = UUID.nameUUIDFromBytes((targetBlocks.toString() + seed + patternId).getBytes());
        if (sortedBlocks == null || !currentInputId.equals(lastInputId)) {
            sortBlocks(targetBlocks.getPositions(), pattern, seed);
            lastInputId = currentInputId;
        }
        
        // 计算当前应该显示多少个方块
        int totalBlocks = sortedBlocks.size();
        int visibleCount = Math.round(totalBlocks * timeFactor);
        
        // 根据模式调整可见数量
        if (mode == VisibilityMode.DISAPPEAR) {
            visibleCount = totalBlocks - visibleCount;
        }
        
        // 创建可见方块列表
        List<BlockPos> currentVisibleBlocks = new ArrayList<>();
        for (int i = 0; i < visibleCount; i++) {
            currentVisibleBlocks.add(sortedBlocks.get(i));
        }
        
        // 设置输出值
        visibleBlocks = new BlockPosList(currentVisibleBlocks);
        progress = (float) visibleCount / totalBlocks;
        if (mode == VisibilityMode.DISAPPEAR) {
            progress = 1 - progress;
        }
        
        outputValues.put(OUTPUT_VISIBLE_BLOCKS_ID, visibleBlocks);
        outputValues.put(OUTPUT_PROGRESS_ID, progress);
    }
    
    /**
     * 根据指定模式对方块进行排序
     */
    private void sortBlocks(List<BlockPos> blocks, AppearancePattern pattern, int seed) {
        // 创建工作副本
        sortedBlocks = new ArrayList<>(blocks);
        
        // 计算中心点（用于某些模式）
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        
        for (BlockPos pos : blocks) {
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }
        
        int centerX = (minX + maxX) / 2;
        int centerY = (minY + maxY) / 2;
        int centerZ = (minZ + maxZ) / 2;
        BlockPos center = new BlockPos(centerX, centerY, centerZ);
        
        // 根据模式排序
        switch (pattern) {
            case RANDOM:
                // 使用固定种子进行随机洗牌
                random.setSeed(seed);
                Collections.shuffle(sortedBlocks, random);
                break;
                
            case BOTTOM_UP:
                sortedBlocks.sort((a, b) -> a.getY() - b.getY());
                break;
                
            case TOP_DOWN:
                sortedBlocks.sort((a, b) -> b.getY() - a.getY());
                break;
                
            case CENTER_OUT:
                sortedBlocks.sort((a, b) -> {
                    double distA = getDistanceSquared(a, center);
                    double distB = getDistanceSquared(b, center);
                    return Double.compare(distA, distB);
                });
                break;
                
            case OUTSIDE_IN:
                sortedBlocks.sort((a, b) -> {
                    double distA = getDistanceSquared(a, center);
                    double distB = getDistanceSquared(b, center);
                    return Double.compare(distB, distA);
                });
                break;
                
            case LEFT_RIGHT:
                sortedBlocks.sort((a, b) -> a.getX() - b.getX());
                break;
                
            case RIGHT_LEFT:
                sortedBlocks.sort((a, b) -> b.getX() - a.getX());
                break;
                
            case FRONT_BACK:
                sortedBlocks.sort((a, b) -> a.getZ() - b.getZ());
                break;
                
            case BACK_FRONT:
                sortedBlocks.sort((a, b) -> b.getZ() - a.getZ());
                break;
        }
    }
    
    /**
     * 计算两个坐标之间的距离平方
     */
    private double getDistanceSquared(BlockPos a, BlockPos b) {
        int dx = a.getX() - b.getX();
        int dy = a.getY() - b.getY();
        int dz = a.getZ() - b.getZ();
        return dx * dx + dy * dy + dz * dz;
    }
} 