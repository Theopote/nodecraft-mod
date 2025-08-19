package com.nodecraft.nodesystem.nodes.animation.transforms;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Animate Translate Node: 动画平移节点
 * 在两个位置之间平移几何体
 */
@NodeInfo(
    id = "animation.transforms.animate_translate",
    displayName = "Animate Translate",
    description = "在两个位置之间平移几何体",
    category = "animation.transforms"
)
public class AnimateTranslateNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_GEOMETRY_ID = "input_geometry";
    private static final String INPUT_START_POS_ID = "input_start_pos";
    private static final String INPUT_END_POS_ID = "input_end_pos";
    private static final String INPUT_FACTOR_ID = "input_factor";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_TRANSFORMED_GEOMETRY_ID = "output_transformed_geometry";
    
    // --- 构造函数 ---
    public AnimateTranslateNode() {
        super(UUID.randomUUID(), "animation.transforms.animate_translate");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_GEOMETRY_ID, "Geometry", "需要变换的几何体（坐标列表）", NodeDataType.LIST, this));
        addInputPort(new BasePort(INPUT_START_POS_ID, "Start Position", "起始位置", NodeDataType.VECTOR, this));
        addInputPort(new BasePort(INPUT_END_POS_ID, "End Position", "结束位置", NodeDataType.VECTOR, this));
        addInputPort(new BasePort(INPUT_FACTOR_ID, "Factor", "动画进度因子（0-1）", NodeDataType.FLOAT, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_TRANSFORMED_GEOMETRY_ID, "Transformed Geometry", "变换后的几何体", NodeDataType.LIST, this));
    }
    
    @Override
    public String getDescription() {
        return "在两个位置之间平移几何体";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Object geometryObj = inputValues.get(INPUT_GEOMETRY_ID);
        float[] startPos = (float[]) inputValues.getOrDefault(INPUT_START_POS_ID, new float[]{0.0f, 0.0f, 0.0f});
        float[] endPos = (float[]) inputValues.getOrDefault(INPUT_END_POS_ID, new float[]{0.0f, 0.0f, 0.0f});
        Float factor = (Float) inputValues.getOrDefault(INPUT_FACTOR_ID, 0.0f);
        
        // 确保插值因子在0-1范围内
        factor = Math.max(0.0f, Math.min(1.0f, factor));
        
        // 确保位置向量有效
        if (startPos.length < 3) startPos = new float[]{0.0f, 0.0f, 0.0f};
        if (endPos.length < 3) endPos = new float[]{0.0f, 0.0f, 0.0f};
        
        // 计算当前位置
        float[] currentPos = new float[3];
        for (int i = 0; i < 3; i++) {
            currentPos[i] = startPos[i] + (endPos[i] - startPos[i]) * factor;
        }
        
        // 计算偏移量（从原点开始）
        float[] offset = currentPos;
        
        // 变换几何体
        List<int[]> transformedGeometry = transformGeometry(geometryObj, offset);
        
        // 设置输出值
        outputValues.put(OUTPUT_TRANSFORMED_GEOMETRY_ID, transformedGeometry);
    }
    
    /**
     * 变换几何体
     * @param geometryObj 输入几何体（坐标列表）
     * @param offset 偏移量
     * @return 变换后的几何体
     */
    @SuppressWarnings("unchecked")
    private List<int[]> transformGeometry(Object geometryObj, float[] offset) {
        List<int[]> result = new ArrayList<>();
        
        // 确保几何体是有效的坐标列表
        if (geometryObj instanceof List) {
            List<?> geometryList = (List<?>) geometryObj;
            
            for (Object coordObj : geometryList) {
                int[] transformedCoord = transformCoordinate(coordObj, offset);
                if (transformedCoord != null) {
                    result.add(transformedCoord);
                }
            }
        }
        
        return result;
    }
    
    /**
     * 变换单个坐标
     * @param coordObj 坐标对象
     * @param offset 偏移量
     * @return 变换后的坐标
     */
    private int[] transformCoordinate(Object coordObj, float[] offset) {
        // 处理int[]格式的坐标
        if (coordObj instanceof int[]) {
            int[] coord = (int[]) coordObj;
            if (coord.length >= 3) {
                return new int[]{
                    coord[0] + Math.round(offset[0]),
                    coord[1] + Math.round(offset[1]),
                    coord[2] + Math.round(offset[2])
                };
            }
        }
        // 处理包含x,y,z字段的对象（如Coordinate类）
        else if (coordObj instanceof Map) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> coordMap = (Map<String, Object>) coordObj;
                
                if (coordMap.containsKey("x") && coordMap.containsKey("y") && coordMap.containsKey("z")) {
                    int x = ((Number) coordMap.get("x")).intValue() + Math.round(offset[0]);
                    int y = ((Number) coordMap.get("y")).intValue() + Math.round(offset[1]);
                    int z = ((Number) coordMap.get("z")).intValue() + Math.round(offset[2]);
                    
                    return new int[]{x, y, z};
                }
            } catch (Exception e) {
                // 处理失败，返回null
                return null;
            }
        }
        
        return null;
    }
} 