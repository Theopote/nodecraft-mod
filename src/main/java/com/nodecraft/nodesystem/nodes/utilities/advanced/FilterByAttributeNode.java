package com.nodecraft.nodesystem.nodes.utilities.advanced;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import com.nodecraft.nodesystem.util.BlockPosList;

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Filter by Attribute 节点: 根据属性值过滤坐标或方块列表
 */
@NodeInfo(
    id = "utilities.advanced.filter_by_attribute",
    displayName = "按属性过滤",
    description = "根据属性值过滤坐标或方块列表",
    category = "utilities.advanced"
)
public class FilterByAttributeNode extends BaseNode {
    
    // --- 节点属性 ---
    private String attributeName = "attribute"; // 属性名称
    private String filterType = "equals"; // 过滤类型
    private String filterValue = "true"; // 过滤值
    private boolean invertFilter = false; // 是否反转过滤
    private String description = "根据属性值过滤坐标或方块列表";
    
    // --- 过滤类型常量 ---
    private static final String FILTER_TYPE_EQUALS = "equals";         // 等于
    private static final String FILTER_TYPE_NOT_EQUALS = "notEquals";  // 不等于
    private static final String FILTER_TYPE_CONTAINS = "contains";     // 包含
    private static final String FILTER_TYPE_STARTS_WITH = "startsWith";// 开头为
    private static final String FILTER_TYPE_ENDS_WITH = "endsWith";    // 结尾为
    private static final String FILTER_TYPE_GREATER_THAN = "greaterThan"; // 大于
    private static final String FILTER_TYPE_LESS_THAN = "lessThan";    // 小于
    private static final String FILTER_TYPE_EXISTS = "exists";         // 存在
    
    // --- 输入端口 IDs ---
    private static final String INPUT_COORDINATES_ID = "input_coordinates";
    private static final String INPUT_ATTRIBUTE_MAP_ID = "input_attribute_map";
    private static final String INPUT_ATTRIBUTE_NAME_ID = "input_attribute_name";
    private static final String INPUT_FILTER_TYPE_ID = "input_filter_type";
    private static final String INPUT_FILTER_VALUE_ID = "input_filter_value";
    private static final String INPUT_INVERT_FILTER_ID = "input_invert_filter";
    
    // --- 输出端口 IDs ---
    private static final String OUTPUT_FILTERED_ID = "output_filtered";
    private static final String OUTPUT_REJECTED_ID = "output_rejected";
    private static final String OUTPUT_COUNT_ID = "output_count";
    
    /**
     * 构造属性过滤节点
     */
    public FilterByAttributeNode() {
        super(UUID.randomUUID(), "utilities.advanced.filter_by_attribute");
        
        // 创建输入端口
        addInputPort(new BasePort(INPUT_COORDINATES_ID, "Coordinates", 
                "要过滤的坐标或方块列表", NodeDataType.BLOCK_LIST, this));
        
        addInputPort(new BasePort(INPUT_ATTRIBUTE_MAP_ID, "Attribute Map", 
                "属性映射（通常来自Set Attribute节点）", NodeDataType.ANY, this));
        
        addInputPort(new BasePort(INPUT_ATTRIBUTE_NAME_ID, "Attribute Name", 
                "要过滤的属性名称", NodeDataType.STRING, this));
        
        addInputPort(new BasePort(INPUT_FILTER_TYPE_ID, "Filter Type", 
                "过滤条件类型 (equals, notEquals等)", NodeDataType.STRING, this));
        
        addInputPort(new BasePort(INPUT_FILTER_VALUE_ID, "Filter Value", 
                "过滤条件值", NodeDataType.ANY, this));
        
        addInputPort(new BasePort(INPUT_INVERT_FILTER_ID, "Invert Filter", 
                "是否反转过滤结果", NodeDataType.BOOLEAN, this));
        
        // 创建输出端口
        addOutputPort(new BasePort(OUTPUT_FILTERED_ID, "Filtered", 
                "符合过滤条件的坐标列表", NodeDataType.BLOCK_LIST, this));
        
        addOutputPort(new BasePort(OUTPUT_REJECTED_ID, "Rejected", 
                "不符合过滤条件的坐标列表", NodeDataType.BLOCK_LIST, this));
        
        addOutputPort(new BasePort(OUTPUT_COUNT_ID, "Count", 
                "符合过滤条件的坐标数量", NodeDataType.INTEGER, this));
    }
    
    /**
     * 节点的计算逻辑
     */
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 初始化输出值
        BlockPosList filteredPositions = new BlockPosList();
        BlockPosList rejectedPositions = new BlockPosList();
        int count = 0;
        
        // 获取输入值
        Object coordinatesObj = inputValues.get(INPUT_COORDINATES_ID);
        Object attributeMapObj = inputValues.get(INPUT_ATTRIBUTE_MAP_ID);
        Object attributeNameObj = inputValues.get(INPUT_ATTRIBUTE_NAME_ID);
        Object filterTypeObj = inputValues.get(INPUT_FILTER_TYPE_ID);
        Object filterValueObj = inputValues.get(INPUT_FILTER_VALUE_ID);
        Object invertFilterObj = inputValues.get(INPUT_INVERT_FILTER_ID);
        
        // 获取属性名称
        String attrName = this.attributeName;
        if (attributeNameObj instanceof String && !((String) attributeNameObj).isEmpty()) {
            attrName = (String) attributeNameObj;
        }
        
        // 获取过滤类型
        String filterTypeToUse = this.filterType;
        if (filterTypeObj instanceof String && !((String) filterTypeObj).isEmpty()) {
            filterTypeToUse = (String) filterTypeObj;
        }
        
        // 获取过滤值
        Object filterValueToUse = filterValueObj != null ? filterValueObj : this.filterValue;
        
        // 获取是否反转过滤
        boolean invertFilterToUse = this.invertFilter;
        if (invertFilterObj instanceof Boolean) {
            invertFilterToUse = (Boolean) invertFilterObj;
        }
        
        // 解析属性映射
        Map<BlockPos, Object> attributeMap = new HashMap<>();
        if (attributeMapObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> attrMaps = (Map<String, Object>) attributeMapObj;
            
            // 查找指定名称的属性映射
            Object attrMapForName = attrMaps.get(attrName);
            if (attrMapForName instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<Object, Object> posToValueMap = (Map<Object, Object>) attrMapForName;
                
                // 从Object映射转换为BlockPos映射
                for (Map.Entry<Object, Object> entry : posToValueMap.entrySet()) {
                    if (entry.getKey() instanceof BlockPos) {
                        attributeMap.put((BlockPos) entry.getKey(), entry.getValue());
                    }
                }
            }
        }
        
        // 创建过滤谓词
        Predicate<Object> filterPredicate = createFilterPredicate(filterTypeToUse, filterValueToUse);
        
        // 处理坐标列表，过滤每个坐标
        List<BlockPos> positions = new ArrayList<>();
        
        // 从各种输入类型收集BlockPos
        if (coordinatesObj instanceof BlockPosList) {
            BlockPosList coordinates = (BlockPosList) coordinatesObj;
            for (BlockPos pos : coordinates) {
                positions.add(pos);
            }
        } else if (coordinatesObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> coordList = (List<Object>) coordinatesObj;
            
            for (Object obj : coordList) {
                if (obj instanceof BlockPos) {
                    positions.add((BlockPos) obj);
                }
            }
        }
        
        // 处理收集到的位置
        for (BlockPos pos : positions) {
            boolean matches = false;
            
            // 检查该位置是否有属性值
            if (attributeMap.containsKey(pos)) {
                Object value = attributeMap.get(pos);
                matches = filterPredicate.test(value);
                
                // 如果需要反转过滤结果
                if (invertFilterToUse) {
                    matches = !matches;
                }
            } else if (FILTER_TYPE_EXISTS.equals(filterTypeToUse)) {
                // 特殊情况：检查属性是否存在
                matches = false; // 属性不存在
                
                // 如果需要反转过滤结果
                if (invertFilterToUse) {
                    matches = !matches;
                }
            }
            
            // 根据过滤结果添加到相应列表
            if (matches) {
                filteredPositions.add(pos);
                count++;
            } else {
                rejectedPositions.add(pos);
            }
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_FILTERED_ID, filteredPositions);
        outputValues.put(OUTPUT_REJECTED_ID, rejectedPositions);
        outputValues.put(OUTPUT_COUNT_ID, count);
    }
    
    /**
     * 创建过滤谓词
     * @param filterType 过滤类型
     * @param filterValue 过滤值
     * @return 用于过滤的谓词
     */
    private Predicate<Object> createFilterPredicate(String filterType, Object filterValue) {
        switch (filterType) {
            case FILTER_TYPE_EQUALS:
                return value -> {
                    if (value == null) return filterValue == null;
                    return value.equals(filterValue);
                };
                
            case FILTER_TYPE_NOT_EQUALS:
                return value -> {
                    if (value == null) return filterValue != null;
                    return !value.equals(filterValue);
                };
                
            case FILTER_TYPE_CONTAINS:
                return value -> {
                    if (value == null) return false;
                    return value.toString().contains(filterValue != null ? filterValue.toString() : "");
                };
                
            case FILTER_TYPE_STARTS_WITH:
                return value -> {
                    if (value == null) return false;
                    return value.toString().startsWith(filterValue != null ? filterValue.toString() : "");
                };
                
            case FILTER_TYPE_ENDS_WITH:
                return value -> {
                    if (value == null) return false;
                    return value.toString().endsWith(filterValue != null ? filterValue.toString() : "");
                };
                
            case FILTER_TYPE_GREATER_THAN:
                return value -> {
                    if (value instanceof Number && filterValue instanceof Number) {
                        return ((Number) value).doubleValue() > ((Number) filterValue).doubleValue();
                    }
                    return false;
                };
                
            case FILTER_TYPE_LESS_THAN:
                return value -> {
                    if (value instanceof Number && filterValue instanceof Number) {
                        return ((Number) value).doubleValue() < ((Number) filterValue).doubleValue();
                    }
                    return false;
                };
                
            case FILTER_TYPE_EXISTS:
                return value -> true; // 只要属性存在就返回true
                
            default:
                // 默认使用equals
                return value -> {
                    if (value == null) return filterValue == null;
                    return value.equals(filterValue);
                };
        }
    }
    
    // --- Getters/Setters for Properties ---
    
    public String getAttributeName() {
        return attributeName;
    }
    
    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
        markDirty();
    }
    
    public String getFilterType() {
        return filterType;
    }
    
    public void setFilterType(String filterType) {
        this.filterType = filterType;
        markDirty();
    }
    
    public String getFilterValue() {
        return filterValue;
    }
    
    public void setFilterValue(String filterValue) {
        this.filterValue = filterValue;
        markDirty();
    }
    
    public boolean isInvertFilter() {
        return invertFilter;
    }
    
    public void setInvertFilter(boolean invertFilter) {
        this.invertFilter = invertFilter;
        markDirty();
    }
    
    /**
     * 保存节点状态
     */
    @Override
    public @Nullable Object getNodeState() {
        Object[] state = new Object[4];
        state[0] = attributeName;
        state[1] = filterType;
        state[2] = filterValue;
        state[3] = invertFilter;
        return state;
    }
    
    /**
     * 恢复节点状态
     */
    @Override
    public void setNodeState(@Nullable Object state) {
        if (state instanceof Object[]) {
            Object[] objState = (Object[]) state;
            if (objState.length >= 4) {
                if (objState[0] instanceof String) {
                    attributeName = (String) objState[0];
                }
                if (objState[1] instanceof String) {
                    filterType = (String) objState[1];
                }
                if (objState[2] instanceof String) {
                    filterValue = (String) objState[2];
                }
                if (objState[3] instanceof Boolean) {
                    invertFilter = (Boolean) objState[3];
                }
            }
        }
    }

    @Override
    public String getDescription() {
        return this.description;
    }
} 