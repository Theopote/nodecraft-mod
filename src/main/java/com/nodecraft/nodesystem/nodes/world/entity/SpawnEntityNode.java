package com.nodecraft.nodesystem.nodes.world.entity;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.UUID;

/**
 * Spawn Entity 节点: 生成实体。
 */
@NodeInfo(
    id = "world.entity.spawn_entity",
    displayName = "生成实体",
    description = "生成实体",
    category = "world.entity"
)
public class SpawnEntityNode extends BaseNode {

    // --- 节点属性 ---
    private boolean applyMotion = false; // 是否设置实体初始速度
    private boolean setNoAI = false; // 是否禁用AI
    private boolean setInvulnerable = false; // 是否设置为无敌
    private boolean setPersistent = true; // 是否设置为持久性实体（不会自然消失）
    private String description = "在指定坐标生成实体";

    // --- 输入端口 IDs ---
    private static final String INPUT_POSITION_ID = "input_position";
    private static final String INPUT_ENTITY_TYPE_ID = "input_entity_type";
    private static final String INPUT_NBT_DATA_ID = "input_nbt_data";
    private static final String INPUT_MOTION_ID = "input_motion";
    private static final String INPUT_NO_AI_ID = "input_no_ai";
    private static final String INPUT_INVULNERABLE_ID = "input_invulnerable";
    private static final String INPUT_PERSISTENT_ID = "input_persistent";
    private static final String INPUT_ROTATION_YAW_ID = "input_rotation_yaw";
    private static final String INPUT_ROTATION_PITCH_ID = "input_rotation_pitch";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_ENTITY_ID = "output_entity";
    private static final String OUTPUT_SUCCESS_ID = "output_success";
    private static final String OUTPUT_ENTITY_UUID_ID = "output_entity_uuid";

    // --- 构造函数 ---
    public SpawnEntityNode() {
        super(UUID.randomUUID(), "world.entity.spawn_entity");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_POSITION_ID, "Position", 
                "生成实体的位置", NodeDataType.POSITION, this));
        addInputPort(new BasePort(INPUT_ENTITY_TYPE_ID, "Entity Type", 
                "实体类型", NodeDataType.ENTITY_TYPE, this));
        addInputPort(new BasePort(INPUT_NBT_DATA_ID, "NBT Data", 
                "实体的NBT数据", NodeDataType.NBT, this));
        addInputPort(new BasePort(INPUT_MOTION_ID, "Motion", 
                "实体初始速度向量", NodeDataType.VECTOR, this));
        addInputPort(new BasePort(INPUT_NO_AI_ID, "No AI", 
                "是否禁用AI", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_INVULNERABLE_ID, "Invulnerable", 
                "是否设置为无敌", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_PERSISTENT_ID, "Persistent", 
                "是否设置为持久性实体", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_ROTATION_YAW_ID, "Yaw", 
                "水平旋转角度", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_ROTATION_PITCH_ID, "Pitch", 
                "垂直旋转角度", NodeDataType.FLOAT, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_ENTITY_ID, "Entity", 
                "生成的实体", NodeDataType.MINECRAFT_ENTITY, this));
        addOutputPort(new BasePort(OUTPUT_SUCCESS_ID, "Success", 
                "是否成功生成实体", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_ENTITY_UUID_ID, "Entity UUID", 
                "生成的实体UUID", NodeDataType.STRING, this));
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    // --- 核心逻辑 ---
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 默认输出值
        Object entityObj = null;
        boolean success = false;
        String entityUUID = "";
        
        // 获取输入值
        Object positionObj = inputValues.get(INPUT_POSITION_ID);
        Object entityTypeObj = inputValues.get(INPUT_ENTITY_TYPE_ID);
        Object nbtDataObj = inputValues.get(INPUT_NBT_DATA_ID);
        Object motionObj = inputValues.get(INPUT_MOTION_ID);
        
        // 获取布尔值参数
        boolean noAIValue = this.setNoAI;
        Object noAIObj = inputValues.get(INPUT_NO_AI_ID);
        if (noAIObj instanceof Boolean) {
            noAIValue = (Boolean) noAIObj;
        }
        
        boolean invulnerableValue = this.setInvulnerable;
        Object invulnerableObj = inputValues.get(INPUT_INVULNERABLE_ID);
        if (invulnerableObj instanceof Boolean) {
            invulnerableValue = (Boolean) invulnerableObj;
        }
        
        boolean persistentValue = this.setPersistent;
        Object persistentObj = inputValues.get(INPUT_PERSISTENT_ID);
        if (persistentObj instanceof Boolean) {
            persistentValue = (Boolean) persistentObj;
        }
        
        // 获取旋转角度
        float yaw = 0.0f; 
        Object yawObj = inputValues.get(INPUT_ROTATION_YAW_ID);
        if (yawObj instanceof Number) {
            yaw = ((Number) yawObj).floatValue();
        }
        
        float pitch = 0.0f;
        Object pitchObj = inputValues.get(INPUT_ROTATION_PITCH_ID);
        if (pitchObj instanceof Number) {
            pitch = ((Number) pitchObj).floatValue();
        }
        
        // 处理速度向量
        double motionX = 0.0;
        double motionY = 0.0;
        double motionZ = 0.0;
        
        if (applyMotion && motionObj instanceof Vector3d) {
            Vector3d motion = (Vector3d) motionObj;
            motionX = motion.x;
            motionY = motion.y;
            motionZ = motion.z;
        }
        
        // 检查执行上下文和必要输入是否有效
        if (context != null && context.getWorld() != null && 
                positionObj != null && entityTypeObj != null) {
            // 处理位置
            double x = 0.0, y = 0.0, z = 0.0;
            
            if (positionObj instanceof Vector3d) {
                Vector3d pos = (Vector3d) positionObj;
                x = pos.x;
                y = pos.y;
                z = pos.z;
            } else if (positionObj instanceof BlockPos) {
                BlockPos pos = (BlockPos) positionObj;
                x = pos.getX() + 0.5; // 中心对齐
                y = pos.getY();
                z = pos.getZ() + 0.5; // 中心对齐
            }
            
            try {
                // 在实际实现中，创建并生成实体
                // 以下仅为示例代码，需要根据Minecraft API调整
                
                // 获取实体类型
                String entityType = entityTypeObj.toString();
                
                // 创建基础NBT数据
                // 在实际实现中，应该使用Minecraft的NBT API创建复合标签
                /*
                CompoundTag nbt = new CompoundTag();
                nbt.putString("id", entityType);
                nbt.putDouble("Pos", new ListTag().add(DoubleTag.of(x)).add(DoubleTag.of(y)).add(DoubleTag.of(z)));
                
                // 设置旋转
                nbt.putFloat("Rotation", new ListTag().add(FloatTag.of(yaw)).add(FloatTag.of(pitch)));
                
                // 设置速度
                if (applyMotion) {
                    nbt.putDouble("Motion", new ListTag().add(DoubleTag.of(motionX)).add(DoubleTag.of(motionY)).add(DoubleTag.of(motionZ)));
                }
                
                // 设置AI
                if (noAIValue) {
                    nbt.putByte("NoAI", (byte) 1);
                }
                
                // 设置无敌
                if (invulnerableValue) {
                    nbt.putByte("Invulnerable", (byte) 1);
                }
                
                // 设置持久性
                if (persistentValue) {
                    nbt.putByte("PersistenceRequired", (byte) 1);
                }
                
                // 合并额外的NBT数据
                if (nbtDataObj instanceof CompoundTag) {
                    nbt.merge((CompoundTag) nbtDataObj);
                }
                
                // 生成实体
                Entity entity = EntityType.loadEntityWithPassengers(nbt, context.getWorld(), (loadedEntity) -> {
                    loadedEntity.setPos(x, y, z);
                    return loadedEntity;
                });
                
                if (entity != null) {
                    // 添加实体到世界
                    success = context.getWorld().spawnEntity(entity);
                    if (success) {
                        entityObj = entity;
                        entityUUID = entity.getUuidAsString();
                    }
                }
                */
                
                // 模拟成功
                success = true;
                entityObj = new Object(); // 模拟实体对象
                entityUUID = "00000000-0000-0000-0000-000000000000"; // 模拟UUID
            } catch (Exception e) {
                // 记录错误
                System.err.println("Error spawning entity: " + e.getMessage());
                success = false;
            }
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_ENTITY_ID, entityObj);
        outputValues.put(OUTPUT_SUCCESS_ID, success);
        outputValues.put(OUTPUT_ENTITY_UUID_ID, entityUUID);
    }
    
    // --- Getters/Setters for Properties ---
    
    public boolean isApplyMotion() {
        return applyMotion;
    }
    
    public void setApplyMotion(boolean applyMotion) {
        this.applyMotion = applyMotion;
        markDirty();
    }
    
    public boolean isSetNoAI() {
        return setNoAI;
    }
    
    public void setSetNoAI(boolean setNoAI) {
        this.setNoAI = setNoAI;
        markDirty();
    }
    
    public boolean isSetInvulnerable() {
        return setInvulnerable;
    }
    
    public void setSetInvulnerable(boolean setInvulnerable) {
        this.setInvulnerable = setInvulnerable;
        markDirty();
    }
    
    public boolean isSetPersistent() {
        return setPersistent;
    }
    
    public void setSetPersistent(boolean setPersistent) {
        this.setPersistent = setPersistent;
        markDirty();
    }
} 