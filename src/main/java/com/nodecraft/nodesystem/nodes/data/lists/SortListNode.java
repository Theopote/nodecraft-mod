package com.nodecraft.nodesystem.nodes.data.lists;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.api.IPort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * 排序列表节点，对列表元素进行排序
 */
@NodeInfo(
    id = "data.lists.sort_list",
    displayName = "Sort List",
    description = "Sorts elements of a list",
    category = "data.lists"
)
public class SortListNode extends BaseNode {
    
    // --- 节点属性 ---
    private boolean descending = false; // 是否降序排序
    private SortType sortType = SortType.AUTO; // 排序类型
    private String description = "Sorts elements of a list"; // 节点描述
    
    // --- 排序类型枚举 ---
    public enum SortType {
        AUTO("Auto"), // 自动检测类型并排序
        NUMERIC("Numeric"), // 数值排序
        TEXT("Text"), // 文本排序
        LENGTH("Length"); // 按元素长度排序（适用于字符串或集合）
        
        private final String label;
        
        SortType(String label) {
            this.label = label;
        }
        
        public String getLabel() {
            return label;
        }
    }
    
    // --- 输入/输出端口ID ---
    private static final String INPUT_LIST_ID = "input_list";
    private static final String OUTPUT_LIST_ID = "output_list";
    
    /**
     * 构造一个新的排序列表节点
     */
    public SortListNode() {
        // 使用分类命名 - data.lists.sort_list
        super(UUID.randomUUID(), "data.lists.sort_list");
        
        // 创建输入端口
        IPort listInput = new BasePort(INPUT_LIST_ID, "List", 
                "The list to sort", NodeDataType.LIST, this);
        addInputPort(listInput);
        
        // 创建输出端口
        IPort listOutput = new BasePort(OUTPUT_LIST_ID, "Sorted List", 
                "The sorted list", NodeDataType.LIST, this);
        addOutputPort(listOutput);
    }
    
    /**
     * 实现INode接口的getDescription方法
     * @return 节点描述
     */
    @Override
    public String getDescription() {
        return this.description;
    }
    
    /**
     * 节点的计算逻辑
     * @param context 执行上下文
     */
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入
        Object inputObj = inputValues.get(INPUT_LIST_ID);
        
        List<Object> resultList = new ArrayList<>();
        
        // 处理列表
        if (inputObj instanceof List) {
            List<?> inputList = (List<?>) inputObj;
            
            // 创建一个可修改的新列表并添加所有项
            resultList.addAll(inputList);
            
            // 如果列表为空，直接返回
            if (resultList.isEmpty()) {
                outputValues.put(OUTPUT_LIST_ID, resultList);
                return;
            }
            
            // 执行排序
            sort(resultList);
        }
        
        // 设置输出
        outputValues.put(OUTPUT_LIST_ID, resultList);
    }
    
    /**
     * 对列表进行排序
     * @param list 要排序的列表
     */
    @SuppressWarnings("unchecked")
    private void sort(List<Object> list) {
        // 检测第一个非空元素的类型
        Object firstNonNull = null;
        for (Object item : list) {
            if (item != null) {
                firstNonNull = item;
                break;
            }
        }
        
        // 如果所有元素都为空，无需排序
        if (firstNonNull == null) {
            return;
        }
        
        // 根据排序类型选择比较器
        Comparator<Object> comparator = null;
        
        switch (sortType) {
            case NUMERIC:
                comparator = createNumericComparator();
                break;
                
            case TEXT:
                comparator = createTextComparator();
                break;
                
            case LENGTH:
                comparator = createLengthComparator();
                break;
                
            case AUTO:
            default:
                // 自动检测类型
                if (firstNonNull instanceof Number) {
                    comparator = createNumericComparator();
                } else if (firstNonNull instanceof String) {
                    comparator = createTextComparator();
                } else if (firstNonNull instanceof List || firstNonNull instanceof String) {
                    comparator = createLengthComparator();
                } else {
                    // 尝试使用自然排序（如果元素实现了Comparable接口）
                    try {
                        // 使用比较器而不是直接转换列表类型
                        comparator = new Comparator<Object>() {
                            @SuppressWarnings("unchecked")
                            @Override
                            public int compare(Object o1, Object o2) {
                                // 处理空值
                                if (o1 == null && o2 == null) return 0;
                                if (o1 == null) return descending ? 1 : -1;
                                if (o2 == null) return descending ? -1 : 1;
                                
                                // 尝试将对象作为Comparable处理
                                if (o1 instanceof Comparable && o2 instanceof Comparable) {
                                    int result = ((Comparable<Object>) o1).compareTo(o2);
                                    return descending ? -result : result;
                                } else {
                                    // 如果不是Comparable，则使用字符串比较
                                    int result = o1.toString().compareTo(o2.toString());
                                    return descending ? -result : result;
                                }
                            }
                        };
                    } catch (Exception e) {
                        // 元素不支持排序，使用toString进行文本排序
                        comparator = createTextComparator();
                    }
                }
                break;
        }
        
        // 应用排序
        if (comparator != null) {
            Collections.sort(list, comparator);
        }
    }
    
    /**
     * 创建数值比较器
     */
    private Comparator<Object> createNumericComparator() {
        return new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                // 处理空值
                if (o1 == null && o2 == null) return 0;
                if (o1 == null) return descending ? 1 : -1;
                if (o2 == null) return descending ? -1 : 1;
                
                // 尝试将对象转换为数值
                double v1 = toDouble(o1);
                double v2 = toDouble(o2);
                
                // 比较
                int result = Double.compare(v1, v2);
                return descending ? -result : result;
            }
            
            // 将对象转换为double
            private double toDouble(Object obj) {
                if (obj instanceof Number) {
                    return ((Number) obj).doubleValue();
                } else {
                    try {
                        return Double.parseDouble(obj.toString());
                    } catch (NumberFormatException e) {
                        return 0.0;
                    }
                }
            }
        };
    }
    
    /**
     * 创建文本比较器
     */
    private Comparator<Object> createTextComparator() {
        return new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                // 处理空值
                if (o1 == null && o2 == null) return 0;
                if (o1 == null) return descending ? 1 : -1;
                if (o2 == null) return descending ? -1 : 1;
                
                // 比较字符串表示
                int result = o1.toString().compareTo(o2.toString());
                return descending ? -result : result;
            }
        };
    }
    
    /**
     * 创建长度比较器
     */
    private Comparator<Object> createLengthComparator() {
        return new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                // 处理空值
                if (o1 == null && o2 == null) return 0;
                if (o1 == null) return descending ? 1 : -1;
                if (o2 == null) return descending ? -1 : 1;
                
                // 获取长度
                int len1 = getLength(o1);
                int len2 = getLength(o2);
                
                // 比较
                int result = Integer.compare(len1, len2);
                return descending ? -result : result;
            }
            
            // 获取对象的"长度"
            private int getLength(Object obj) {
                if (obj instanceof String) {
                    return ((String) obj).length();
                } else if (obj instanceof List) {
                    return ((List<?>) obj).size();
                } else if (obj instanceof Object[]) {
                    return ((Object[]) obj).length;
                } else {
                    return obj.toString().length();
                }
            }
        };
    }
    
    // --- Getters/Setters for Properties ---
    
    public boolean isDescending() {
        return descending;
    }
    
    public void setDescending(boolean descending) {
        this.descending = descending;
        markDirty();
    }
    
    public SortType getSortType() {
        return sortType;
    }
    
    public void setSortType(SortType sortType) {
        this.sortType = sortType;
        markDirty();
    }
    
    // --- 节点状态序列化 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("descending", isDescending());
        state.put("sortType", getSortType().name());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("descending")) {
                Object desc = stateMap.get("descending");
                if (desc instanceof Boolean) {
                    setDescending((Boolean) desc);
                }
            }
            
            if (stateMap.containsKey("sortType")) {
                Object type = stateMap.get("sortType");
                if (type instanceof String) {
                    try {
                        setSortType(SortType.valueOf((String) type));
                    } catch (IllegalArgumentException e) {
                        // 无效的排序类型，使用默认值
                        setSortType(SortType.AUTO);
                    }
                }
            }
        }
    }
} 