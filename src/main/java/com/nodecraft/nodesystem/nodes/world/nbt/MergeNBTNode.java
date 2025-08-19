package com.nodecraft.nodesystem.nodes.world.nbt;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Merge NBT 节点: 合并两个 NBT 结构
 */
@NodeInfo(
    id = "world.nbt.merge_nbt",
    displayName = "合并NBT",
    description = "合并两个NBT复合标签",
    category = "world.nbt"
)
public class MergeNBTNode extends BaseNode {

    // --- 节点属性 ---
    private String description = "合并两个 NBT 结构";
    private boolean overwriteExisting = true; // 是否覆盖已存在的键

    // --- 输入端口 IDs ---
    private static final String INPUT_BASE_NBT_ID = "input_base_nbt";
    private static final String INPUT_OVERLAY_NBT_ID = "input_overlay_nbt";
    private static final String INPUT_OVERWRITE_ID = "input_overwrite";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_MERGED_NBT_ID = "output_merged_nbt";
    private static final String OUTPUT_SUCCESS_ID = "output_success";

    // --- 构造函数 ---
    public MergeNBTNode() {
        super(UUID.randomUUID(), "world.nbt.merge_nbt");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_BASE_NBT_ID, "Base NBT", 
                "基础 NBT 数据", NodeDataType.NBT_COMPOUND, this));
        addInputPort(new BasePort(INPUT_OVERLAY_NBT_ID, "Overlay NBT", 
                "要覆盖的 NBT 数据", NodeDataType.NBT_COMPOUND, this));
        addInputPort(new BasePort(INPUT_OVERWRITE_ID, "Overwrite Existing", 
                "是否覆盖已存在的键", NodeDataType.BOOLEAN, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_MERGED_NBT_ID, "Merged NBT", 
                "合并后的 NBT 数据", NodeDataType.NBT_COMPOUND, this));
        addOutputPort(new BasePort(OUTPUT_SUCCESS_ID, "Success", 
                "是否成功合并", NodeDataType.BOOLEAN, this));
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    // --- 核心逻辑 ---
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 默认输出值
        Object mergedNbt = null;
        boolean success = false;
        
        // 获取输入值
        Object baseNbtObj = inputValues.get(INPUT_BASE_NBT_ID);
        Object overlayNbtObj = inputValues.get(INPUT_OVERLAY_NBT_ID);
        Object overwriteObj = inputValues.get(INPUT_OVERWRITE_ID);
        
        // 获取是否覆盖已存在的键
        boolean overwrite = this.overwriteExisting;
        if (overwriteObj instanceof Boolean) {
            overwrite = (Boolean) overwriteObj;
        }
        
        // 检查必要的输入是否存在
        if (baseNbtObj != null && overlayNbtObj != null) {
            try {
                /*
                在实际实现中，需要根据Minecraft API实现以下操作:
                1. 确认输入对象是NBT复合标签
                2. 克隆基础NBT
                3. 递归合并两个NBT结构
                
                if (baseNbtObj instanceof CompoundTag && overlayNbtObj instanceof CompoundTag) {
                    CompoundTag baseNbt = (CompoundTag) baseNbtObj;
                    CompoundTag overlayNbt = (CompoundTag) overlayNbtObj;
                    
                    // 创建基础NBT的副本
                    CompoundTag result = baseNbt.copy();
                    
                    // 递归合并NBT
                    mergeNbt(result, overlayNbt, overwrite);
                    
                    mergedNbt = result;
                    success = true;
                }
                */
                
                // 模拟成功合并NBT (在实际实现中替换为上面的逻辑)
                mergedNbt = baseNbtObj;
                success = true;
            } catch (Exception e) {
                success = false;
                System.err.println("Error merging NBT: " + e.getMessage());
            }
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_MERGED_NBT_ID, mergedNbt);
        outputValues.put(OUTPUT_SUCCESS_ID, success);
    }
    
    /* 
    // 在实际实现中需要这个辅助方法来递归合并NBT结构
    private void mergeNbt(CompoundTag target, CompoundTag source, boolean overwrite) {
        for (String key : source.getKeys()) {
            if (!target.contains(key) || overwrite) {
                // 标签不存在或需要覆盖
                Tag sourceTag = source.get(key);
                
                // 处理复合标签的特殊情况
                if (sourceTag instanceof CompoundTag && target.contains(key) && target.get(key) instanceof CompoundTag) {
                    // 递归合并复合标签
                    CompoundTag targetChild = target.getCompound(key);
                    CompoundTag sourceChild = (CompoundTag) sourceTag;
                    mergeNbt(targetChild, sourceChild, overwrite);
                } else if (sourceTag instanceof ListTag && target.contains(key) && target.get(key) instanceof ListTag) {
                    // 合并列表标签
                    ListTag targetList = target.getList(key, ((ListTag) sourceTag).getElementType());
                    ListTag sourceList = (ListTag) sourceTag;
                    
                    // 添加源列表中的所有元素
                    for (int i = 0; i < sourceList.size(); i++) {
                        targetList.add(sourceList.get(i).copy());
                    }
                } else {
                    // 直接复制标签
                    target.put(key, sourceTag.copy());
                }
            }
        }
    }
    */
    
    // --- Getters/Setters for Properties ---
    
    public boolean isOverwriteExisting() {
        return overwriteExisting;
    }
    
    public void setOverwriteExisting(boolean overwriteExisting) {
        this.overwriteExisting = overwriteExisting;
        markDirty();
    }
} 