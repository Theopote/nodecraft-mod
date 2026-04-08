package com.nodecraft.nodesystem.nodes.flora.modifiers;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.datatypes.PlantStructure;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;

/**
 * Transform Plant 节点: 对植物进行复合变换操作
 */
@NodeInfo(
    id = "flora.modifiers.transform_plant",
    displayName = "Transform Plant",
    description = "Applies transformation operations to plants",
    category = "flora.modifiers"
)
public class TransformPlantNode extends BaseNode {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransformPlantNode.class);
    
    /**
     * 变换类型枚举
     */
    public enum TransformType {
        TRANSLATE("平移", "移动植物位置"),
        SCALE("缩放", "缩放植物大小"),
        ROTATE("旋转", "旋转植物"),
        MIRROR("镜像", "镜像翻转植物"),
        COMPOSITE("复合", "组合多种变换");
        
        private final String displayName;
        private final String description;
        
        TransformType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public String getId() { return name().toLowerCase(); }
        
        public static TransformType fromString(String str) {
            for (TransformType type : values()) {
                if (type.getId().equalsIgnoreCase(str) || type.displayName.equals(str)) {
                    return type;
                }
            }
            return TRANSLATE;
        }
    }
    
    // --- 节点属性 ---
    private TransformType transformType = TransformType.TRANSLATE;
    private float translateX = 0.0f;                    // X轴平移
    private float translateY = 0.0f;                    // Y轴平移
    private float translateZ = 0.0f;                    // Z轴平移
    private float scaleX = 1.0f;                        // X轴缩放
    private float scaleY = 1.0f;                        // Y轴缩放
    private float scaleZ = 1.0f;                        // Z轴缩放
    private float rotateX = 0.0f;                       // X轴旋转（度）
    private float rotateY = 0.0f;                       // Y轴旋转（度）
    private float rotateZ = 0.0f;                       // Z轴旋转（度）
    private boolean mirrorX = false;                    // X轴镜像
    private boolean mirrorY = false;                    // Y轴镜像
    private boolean mirrorZ = false;                    // Z轴镜像
    private boolean useCustomCenter = false;            // 使用自定义变换中心
    private float centerX = 0.0f;                       // 自定义中心X
    private float centerY = 0.0f;                       // 自定义中心Y
    private float centerZ = 0.0f;                       // 自定义中心Z
    private String description = "对植物进行复合变换操作";
    
    // --- 输入端口 IDs ---
    private static final String INPUT_PLANT_STRUCTURE_ID = "input_plant_structure";
    private static final String INPUT_TRANSFORM_TYPE_ID = "input_transform_type";
    private static final String INPUT_TRANSLATE_X_ID = "input_translate_x";
    private static final String INPUT_TRANSLATE_Y_ID = "input_translate_y";
    private static final String INPUT_TRANSLATE_Z_ID = "input_translate_z";
    private static final String INPUT_SCALE_X_ID = "input_scale_x";
    private static final String INPUT_SCALE_Y_ID = "input_scale_y";
    private static final String INPUT_SCALE_Z_ID = "input_scale_z";
    private static final String INPUT_ROTATE_X_ID = "input_rotate_x";
    private static final String INPUT_ROTATE_Y_ID = "input_rotate_y";
    private static final String INPUT_ROTATE_Z_ID = "input_rotate_z";
    private static final String INPUT_MIRROR_X_ID = "input_mirror_x";
    private static final String INPUT_MIRROR_Y_ID = "input_mirror_y";
    private static final String INPUT_MIRROR_Z_ID = "input_mirror_z";
    private static final String INPUT_USE_CUSTOM_CENTER_ID = "input_use_custom_center";
    private static final String INPUT_CENTER_X_ID = "input_center_x";
    private static final String INPUT_CENTER_Y_ID = "input_center_y";
    private static final String INPUT_CENTER_Z_ID = "input_center_z";
    
    // --- 输出端口 IDs ---
    private static final String OUTPUT_PLANT_STRUCTURE_ID = "output_plant_structure";
    private static final String OUTPUT_TRANSFORM_INFO_ID = "output_transform_info";
    
    /**
     * 构造一个新的植物变形节点
     */
    public TransformPlantNode() {
        super(UUID.randomUUID(), "flora.modifiers.transform_plant");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_PLANT_STRUCTURE_ID, "Plant Structure", 
                "要变形的植物结构", NodeDataType.PLANT_STRUCTURE, this));
        addInputPort(new BasePort(INPUT_TRANSFORM_TYPE_ID, "Transform Type", 
                "变换类型", NodeDataType.STRING, this));
        addInputPort(new BasePort(INPUT_TRANSLATE_X_ID, "Translate X", 
                "X轴平移距离", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_TRANSLATE_Y_ID, "Translate Y", 
                "Y轴平移距离", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_TRANSLATE_Z_ID, "Translate Z", 
                "Z轴平移距离", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_SCALE_X_ID, "Scale X", 
                "X轴缩放比例", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_SCALE_Y_ID, "Scale Y", 
                "Y轴缩放比例", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_SCALE_Z_ID, "Scale Z", 
                "Z轴缩放比例", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_ROTATE_X_ID, "Rotate X", 
                "X轴旋转角度（度）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_ROTATE_Y_ID, "Rotate Y", 
                "Y轴旋转角度（度）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_ROTATE_Z_ID, "Rotate Z", 
                "Z轴旋转角度（度）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_MIRROR_X_ID, "Mirror X", 
                "是否沿X轴镜像", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_MIRROR_Y_ID, "Mirror Y", 
                "是否沿Y轴镜像", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_MIRROR_Z_ID, "Mirror Z", 
                "是否沿Z轴镜像", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_USE_CUSTOM_CENTER_ID, "Use Custom Center", 
                "是否使用自定义变换中心", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_CENTER_X_ID, "Center X", 
                "变换中心X坐标", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_CENTER_Y_ID, "Center Y", 
                "变换中心Y坐标", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_CENTER_Z_ID, "Center Z", 
                "变换中心Z坐标", NodeDataType.FLOAT, this));
        
        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_PLANT_STRUCTURE_ID, "Transformed Plant", 
                "变形后的植物结构", NodeDataType.PLANT_STRUCTURE, this));
        addOutputPort(new BasePort(OUTPUT_TRANSFORM_INFO_ID, "Transform Info", 
                "变形操作的详细信息", NodeDataType.STRING, this));
    }
    
    @Override
    public String getDescription() {
        return this.description;
    }
    
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        PlantStructure inputPlant = getInputValue(INPUT_PLANT_STRUCTURE_ID, null);
        String transformTypeStr = getInputValue(INPUT_TRANSFORM_TYPE_ID, this.transformType.getId());
        Float translateXValue = getInputValue(INPUT_TRANSLATE_X_ID, this.translateX);
        Float translateYValue = getInputValue(INPUT_TRANSLATE_Y_ID, this.translateY);
        Float translateZValue = getInputValue(INPUT_TRANSLATE_Z_ID, this.translateZ);
        Float scaleXValue = getInputValue(INPUT_SCALE_X_ID, this.scaleX);
        Float scaleYValue = getInputValue(INPUT_SCALE_Y_ID, this.scaleY);
        Float scaleZValue = getInputValue(INPUT_SCALE_Z_ID, this.scaleZ);
        Float rotateXValue = getInputValue(INPUT_ROTATE_X_ID, this.rotateX);
        Float rotateYValue = getInputValue(INPUT_ROTATE_Y_ID, this.rotateY);
        Float rotateZValue = getInputValue(INPUT_ROTATE_Z_ID, this.rotateZ);
        Boolean mirrorXValue = getInputValue(INPUT_MIRROR_X_ID, this.mirrorX);
        Boolean mirrorYValue = getInputValue(INPUT_MIRROR_Y_ID, this.mirrorY);
        Boolean mirrorZValue = getInputValue(INPUT_MIRROR_Z_ID, this.mirrorZ);
        Boolean useCustomCenterValue = getInputValue(INPUT_USE_CUSTOM_CENTER_ID, this.useCustomCenter);
        Float centerXValue = getInputValue(INPUT_CENTER_X_ID, this.centerX);
        Float centerYValue = getInputValue(INPUT_CENTER_Y_ID, this.centerY);
        Float centerZValue = getInputValue(INPUT_CENTER_Z_ID, this.centerZ);
        
        // 默认输出值
        PlantStructure transformedPlant = new PlantStructure();
        String transformInfo = "No plant to transform";
        
        if (inputPlant != null && !inputPlant.isEmpty()) {
            try {
                // 验证变换参数
                TransformType currentTransformType = TransformType.fromString(transformTypeStr);
                translateXValue = translateXValue != null ? translateXValue : 0.0f;
                translateYValue = translateYValue != null ? translateYValue : 0.0f;
                translateZValue = translateZValue != null ? translateZValue : 0.0f;
                scaleXValue = Math.max(0.1f, scaleXValue != null ? scaleXValue : 1.0f);
                scaleYValue = Math.max(0.1f, scaleYValue != null ? scaleYValue : 1.0f);
                scaleZValue = Math.max(0.1f, scaleZValue != null ? scaleZValue : 1.0f);
                rotateXValue = rotateXValue != null ? rotateXValue : 0.0f;
                rotateYValue = rotateYValue != null ? rotateYValue : 0.0f;
                rotateZValue = rotateZValue != null ? rotateZValue : 0.0f;
                mirrorXValue = mirrorXValue != null ? mirrorXValue : false;
                mirrorYValue = mirrorYValue != null ? mirrorYValue : false;
                mirrorZValue = mirrorZValue != null ? mirrorZValue : false;
                useCustomCenterValue = useCustomCenterValue != null ? useCustomCenterValue : false;
                centerXValue = centerXValue != null ? centerXValue : 0.0f;
                centerYValue = centerYValue != null ? centerYValue : 0.0f;
                centerZValue = centerZValue != null ? centerZValue : 0.0f;
                
                // 计算变换中心
                Vector3f transformCenter = calculateTransformCenter(inputPlant, useCustomCenterValue, 
                    centerXValue, centerYValue, centerZValue);
                
                // 根据变换类型执行变换
                transformedPlant = performTransform(inputPlant, currentTransformType,
                    translateXValue, translateYValue, translateZValue,
                    scaleXValue, scaleYValue, scaleZValue,
                    rotateXValue, rotateYValue, rotateZValue,
                    mirrorXValue, mirrorYValue, mirrorZValue,
                    transformCenter);
                
                // 复制原始元数据并添加变换信息
                if (inputPlant.getMetadata() != null) {
                    for (Map.Entry<String, Object> entry : inputPlant.getMetadata().entrySet()) {
                        transformedPlant.setMetadata(entry.getKey(), entry.getValue());
                    }
                }
                transformedPlant.setMetadata("transform_type", currentTransformType.getId());
                transformedPlant.setMetadata("translate", new float[]{translateXValue, translateYValue, translateZValue});
                transformedPlant.setMetadata("scale", new float[]{scaleXValue, scaleYValue, scaleZValue});
                transformedPlant.setMetadata("rotate", new float[]{rotateXValue, rotateYValue, rotateZValue});
                transformedPlant.setMetadata("mirror", new boolean[]{mirrorXValue, mirrorYValue, mirrorZValue});
                transformedPlant.setMetadata("transform_center", new float[]{transformCenter.x, transformCenter.y, transformCenter.z});
                
                // 生成变换信息
                transformInfo = String.format("Transform: %s, Translate: (%.1f,%.1f,%.1f), Scale: (%.2fx%.2fx%.2f), Rotate: (%.1f°,%.1f°,%.1f°), Blocks: %d",
                    currentTransformType.getDisplayName(), 
                    translateXValue, translateYValue, translateZValue,
                    scaleXValue, scaleYValue, scaleZValue,
                    rotateXValue, rotateYValue, rotateZValue,
                    transformedPlant.getTotalBlockCount());
                
            } catch (Exception e) {
                LOGGER.error("Error in Transform Plant", e);
                transformedPlant = inputPlant; // 返回原始植物
                transformInfo = "Error during transformation";
            }
        }
        
        // 设置输出
        outputValues.put(OUTPUT_PLANT_STRUCTURE_ID, transformedPlant);
        outputValues.put(OUTPUT_TRANSFORM_INFO_ID, transformInfo);
    }
    
    /**
     * 计算变换中心
     */
    private Vector3f calculateTransformCenter(PlantStructure plant, boolean useCustom, 
                                            float customX, float customY, float customZ) {
        if (useCustom) {
            return new Vector3f(customX, customY, customZ);
        }
        
        // 计算植物的几何中心
        java.util.List<PlantStructure.PlantBlock> allBlocks = plant.getAllBlocks();
        if (allBlocks.isEmpty()) {
            return new Vector3f(0, 0, 0);
        }
        
        float minX = Float.MAX_VALUE, maxX = Float.MIN_VALUE;
        float minY = Float.MAX_VALUE, maxY = Float.MIN_VALUE;
        float minZ = Float.MAX_VALUE, maxZ = Float.MIN_VALUE;
        
        for (PlantStructure.PlantBlock block : allBlocks) {
            BlockPos pos = block.getPosition();
            minX = Math.min(minX, pos.getX());
            maxX = Math.max(maxX, pos.getX());
            minY = Math.min(minY, pos.getY());
            maxY = Math.max(maxY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxZ = Math.max(maxZ, pos.getZ());
        }
        
        return new Vector3f(
            (minX + maxX) / 2.0f,
            (minY + maxY) / 2.0f,
            (minZ + maxZ) / 2.0f
        );
    }
    
    /**
     * 执行变换操作
     */
    private PlantStructure performTransform(PlantStructure original, TransformType transformType,
                                          float tx, float ty, float tz,
                                          float sx, float sy, float sz,
                                          float rx, float ry, float rz,
                                          boolean mx, boolean my, boolean mz,
                                          Vector3f center) {
        
        Matrix4f transformMatrix = new Matrix4f();
        transformMatrix.identity();
        
        // 构建变换矩阵
        switch (transformType) {
            case TRANSLATE:
                transformMatrix.translate(tx, ty, tz);
                break;
                
            case SCALE:
                transformMatrix.translate(center.x, center.y, center.z);
                transformMatrix.scale(sx, sy, sz);
                transformMatrix.translate(-center.x, -center.y, -center.z);
                break;
                
            case ROTATE:
                transformMatrix.translate(center.x, center.y, center.z);
                transformMatrix.rotateXYZ((float) Math.toRadians(rx), 
                                        (float) Math.toRadians(ry), 
                                        (float) Math.toRadians(rz));
                transformMatrix.translate(-center.x, -center.y, -center.z);
                break;
                
            case MIRROR:
                transformMatrix.translate(center.x, center.y, center.z);
                transformMatrix.scale(mx ? -1.0f : 1.0f, my ? -1.0f : 1.0f, mz ? -1.0f : 1.0f);
                transformMatrix.translate(-center.x, -center.y, -center.z);
                break;
                
            case COMPOSITE:
                // 组合变换：平移 -> 缩放 -> 旋转 -> 镜像
                transformMatrix.translate(tx, ty, tz);
                
                transformMatrix.translate(center.x, center.y, center.z);
                
                if (mx || my || mz) {
                    transformMatrix.scale(mx ? -1.0f : 1.0f, my ? -1.0f : 1.0f, mz ? -1.0f : 1.0f);
                }
                
                if (rx != 0 || ry != 0 || rz != 0) {
                    transformMatrix.rotateXYZ((float) Math.toRadians(rx), 
                                            (float) Math.toRadians(ry), 
                                            (float) Math.toRadians(rz));
                }
                
                if (sx != 1.0f || sy != 1.0f || sz != 1.0f) {
                    transformMatrix.scale(sx, sy, sz);
                }
                
                transformMatrix.translate(-center.x, -center.y, -center.z);
                break;
        }
        
        // 应用变换到植物结构
        return applyMatrixTransform(original, transformMatrix);
    }
    
    /**
     * 应用矩阵变换到植物结构
     */
    private PlantStructure applyMatrixTransform(PlantStructure original, Matrix4f matrix) {
        PlantStructure transformed = new PlantStructure();
        Vector3f position = new Vector3f();
        
        // 变换主干
        for (PlantStructure.PlantBlock block : original.getTrunkBlocks()) {
            BlockPos originalPos = block.getPosition();
            position.set(originalPos.getX(), originalPos.getY(), originalPos.getZ());
            matrix.transformPosition(position);
            
            BlockPos newPos = new BlockPos(
                Math.round(position.x),
                Math.round(position.y),
                Math.round(position.z)
            );
            
            transformed.addTrunkBlock(newPos, block.getBlockType(), block.getThickness());
        }
        
        // 变换分支
        for (PlantStructure.PlantBlock block : original.getBranchBlocks()) {
            BlockPos originalPos = block.getPosition();
            position.set(originalPos.getX(), originalPos.getY(), originalPos.getZ());
            matrix.transformPosition(position);
            
            BlockPos newPos = new BlockPos(
                Math.round(position.x),
                Math.round(position.y),
                Math.round(position.z)
            );
            
            transformed.addBranchBlock(newPos, block.getBlockType(), block.getThickness());
        }
        
        // 变换叶子
        for (PlantStructure.PlantBlock block : original.getLeafBlocks()) {
            BlockPos originalPos = block.getPosition();
            position.set(originalPos.getX(), originalPos.getY(), originalPos.getZ());
            matrix.transformPosition(position);
            
            BlockPos newPos = new BlockPos(
                Math.round(position.x),
                Math.round(position.y),
                Math.round(position.z)
            );
            
            transformed.addLeafBlock(newPos, block.getBlockType());
        }
        
        // 变换花朵
        for (PlantStructure.PlantBlock block : original.getFlowerBlocks()) {
            BlockPos originalPos = block.getPosition();
            position.set(originalPos.getX(), originalPos.getY(), originalPos.getZ());
            matrix.transformPosition(position);
            
            BlockPos newPos = new BlockPos(
                Math.round(position.x),
                Math.round(position.y),
                Math.round(position.z)
            );
            
            transformed.addFlowerBlock(newPos, block.getBlockType());
        }
        
        // 变换根系
        for (PlantStructure.PlantBlock block : original.getRootBlocks()) {
            BlockPos originalPos = block.getPosition();
            position.set(originalPos.getX(), originalPos.getY(), originalPos.getZ());
            matrix.transformPosition(position);
            
            BlockPos newPos = new BlockPos(
                Math.round(position.x),
                Math.round(position.y),
                Math.round(position.z)
            );
            
            transformed.addRootBlock(newPos, block.getBlockType());
        }
        
        return transformed;
    }
    
    /**
     * 获取输入值的辅助方法
     */
    @SuppressWarnings("unchecked")
    private <T> T getInputValue(String portId, T defaultValue) {
        Object value = inputValues.get(portId);
        if (value != null) {
            try {
                return (T) value;
            } catch (ClassCastException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    // --- Getters and Setters ---
    
    public TransformType getTransformType() {
        return transformType;
    }
    
    public void setTransformType(TransformType transformType) {
        this.transformType = transformType != null ? transformType : TransformType.TRANSLATE;
        markDirty();
    }
    
    public float getTranslateX() { return translateX; }
    public void setTranslateX(float translateX) { this.translateX = translateX; markDirty(); }
    
    public float getTranslateY() { return translateY; }
    public void setTranslateY(float translateY) { this.translateY = translateY; markDirty(); }
    
    public float getTranslateZ() { return translateZ; }
    public void setTranslateZ(float translateZ) { this.translateZ = translateZ; markDirty(); }
    
    public float getScaleX() { return scaleX; }
    public void setScaleX(float scaleX) { this.scaleX = Math.max(0.1f, scaleX); markDirty(); }
    
    public float getScaleY() { return scaleY; }
    public void setScaleY(float scaleY) { this.scaleY = Math.max(0.1f, scaleY); markDirty(); }
    
    public float getScaleZ() { return scaleZ; }
    public void setScaleZ(float scaleZ) { this.scaleZ = Math.max(0.1f, scaleZ); markDirty(); }
    
    public float getRotateX() { return rotateX; }
    public void setRotateX(float rotateX) { this.rotateX = rotateX; markDirty(); }
    
    public float getRotateY() { return rotateY; }
    public void setRotateY(float rotateY) { this.rotateY = rotateY; markDirty(); }
    
    public float getRotateZ() { return rotateZ; }
    public void setRotateZ(float rotateZ) { this.rotateZ = rotateZ; markDirty(); }
    
    public boolean isMirrorX() { return mirrorX; }
    public void setMirrorX(boolean mirrorX) { this.mirrorX = mirrorX; markDirty(); }
    
    public boolean isMirrorY() { return mirrorY; }
    public void setMirrorY(boolean mirrorY) { this.mirrorY = mirrorY; markDirty(); }
    
    public boolean isMirrorZ() { return mirrorZ; }
    public void setMirrorZ(boolean mirrorZ) { this.mirrorZ = mirrorZ; markDirty(); }
    
    public boolean isUseCustomCenter() { return useCustomCenter; }
    public void setUseCustomCenter(boolean useCustomCenter) { this.useCustomCenter = useCustomCenter; markDirty(); }
    
    public float getCenterX() { return centerX; }
    public void setCenterX(float centerX) { this.centerX = centerX; markDirty(); }
    
    public float getCenterY() { return centerY; }
    public void setCenterY(float centerY) { this.centerY = centerY; markDirty(); }
    
    public float getCenterZ() { return centerZ; }
    public void setCenterZ(float centerZ) { this.centerZ = centerZ; markDirty(); }
    
    // --- 节点状态序列化 ---
    
    @Override
    public Object getNodeState() {
        Map<String, Object> state = new java.util.HashMap<>();
        state.put("transformType", getTransformType().getId());
        state.put("translateX", getTranslateX());
        state.put("translateY", getTranslateY());
        state.put("translateZ", getTranslateZ());
        state.put("scaleX", getScaleX());
        state.put("scaleY", getScaleY());
        state.put("scaleZ", getScaleZ());
        state.put("rotateX", getRotateX());
        state.put("rotateY", getRotateY());
        state.put("rotateZ", getRotateZ());
        state.put("mirrorX", isMirrorX());
        state.put("mirrorY", isMirrorY());
        state.put("mirrorZ", isMirrorZ());
        state.put("useCustomCenter", isUseCustomCenter());
        state.put("centerX", getCenterX());
        state.put("centerY", getCenterY());
        state.put("centerZ", getCenterZ());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof Map) {
            Map<?, ?> stateMap = (Map<?, ?>) state;
            
            if (stateMap.containsKey("transformType")) {
                Object typeObj = stateMap.get("transformType");
                if (typeObj instanceof String) {
                    setTransformType(TransformType.fromString((String) typeObj));
                }
            }
            
            // 设置平移参数
            if (stateMap.containsKey("translateX")) {
                Object obj = stateMap.get("translateX");
                if (obj instanceof Number) setTranslateX(((Number) obj).floatValue());
            }
            if (stateMap.containsKey("translateY")) {
                Object obj = stateMap.get("translateY");
                if (obj instanceof Number) setTranslateY(((Number) obj).floatValue());
            }
            if (stateMap.containsKey("translateZ")) {
                Object obj = stateMap.get("translateZ");
                if (obj instanceof Number) setTranslateZ(((Number) obj).floatValue());
            }
            
            // 设置缩放参数
            if (stateMap.containsKey("scaleX")) {
                Object obj = stateMap.get("scaleX");
                if (obj instanceof Number) setScaleX(((Number) obj).floatValue());
            }
            if (stateMap.containsKey("scaleY")) {
                Object obj = stateMap.get("scaleY");
                if (obj instanceof Number) setScaleY(((Number) obj).floatValue());
            }
            if (stateMap.containsKey("scaleZ")) {
                Object obj = stateMap.get("scaleZ");
                if (obj instanceof Number) setScaleZ(((Number) obj).floatValue());
            }
            
            // 设置旋转参数
            if (stateMap.containsKey("rotateX")) {
                Object obj = stateMap.get("rotateX");
                if (obj instanceof Number) setRotateX(((Number) obj).floatValue());
            }
            if (stateMap.containsKey("rotateY")) {
                Object obj = stateMap.get("rotateY");
                if (obj instanceof Number) setRotateY(((Number) obj).floatValue());
            }
            if (stateMap.containsKey("rotateZ")) {
                Object obj = stateMap.get("rotateZ");
                if (obj instanceof Number) setRotateZ(((Number) obj).floatValue());
            }
            
            // 设置镜像参数
            if (stateMap.containsKey("mirrorX")) {
                Object obj = stateMap.get("mirrorX");
                if (obj instanceof Boolean) setMirrorX((Boolean) obj);
            }
            if (stateMap.containsKey("mirrorY")) {
                Object obj = stateMap.get("mirrorY");
                if (obj instanceof Boolean) setMirrorY((Boolean) obj);
            }
            if (stateMap.containsKey("mirrorZ")) {
                Object obj = stateMap.get("mirrorZ");
                if (obj instanceof Boolean) setMirrorZ((Boolean) obj);
            }
            
            // 设置中心参数
            if (stateMap.containsKey("useCustomCenter")) {
                Object obj = stateMap.get("useCustomCenter");
                if (obj instanceof Boolean) setUseCustomCenter((Boolean) obj);
            }
            if (stateMap.containsKey("centerX")) {
                Object obj = stateMap.get("centerX");
                if (obj instanceof Number) setCenterX(((Number) obj).floatValue());
            }
            if (stateMap.containsKey("centerY")) {
                Object obj = stateMap.get("centerY");
                if (obj instanceof Number) setCenterY(((Number) obj).floatValue());
            }
            if (stateMap.containsKey("centerZ")) {
                Object obj = stateMap.get("centerZ");
                if (obj instanceof Number) setCenterZ(((Number) obj).floatValue());
            }
        }
    }
} 