package com.nodecraft.nodesystem.nodes.math.list_sequence;

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
 * 鎺掑簭鍒楄〃鑺傜偣锛屽鍒楄〃鍏冪礌杩涜鎺掑簭
 */
@NodeInfo(
    id = "data.lists.sort_list",
    displayName = "Sort List",
    description = "Sorts elements of a list",
    category = "data.lists"
)
public class SortListNode extends BaseNode {
    
    // --- 鑺傜偣灞炴€?---
    private boolean descending = false; // 鏄惁闄嶅簭鎺掑簭
    private SortType sortType = SortType.AUTO; // 鎺掑簭绫诲瀷
    private String description = "Sorts elements of a list"; // 鑺傜偣鎻忚堪
    
    // --- 鎺掑簭绫诲瀷鏋氫妇 ---
    public enum SortType {
        AUTO("Auto"), // 鑷姩妫€娴嬬被鍨嬪苟鎺掑簭
        NUMERIC("Numeric"), // 鏁板€兼帓搴?
        TEXT("Text"), // 鏂囨湰鎺掑簭
        LENGTH("Length"); // 鎸夊厓绱犻暱搴︽帓搴忥紙閫傜敤浜庡瓧绗︿覆鎴栭泦鍚堬級
        
        private final String label;
        
        SortType(String label) {
            this.label = label;
        }
        
        public String getLabel() {
            return label;
        }
    }
    
    // --- 杈撳叆/杈撳嚭绔彛ID ---
    private static final String INPUT_LIST_ID = "input_list";
    private static final String OUTPUT_LIST_ID = "output_list";
    
    /**
     * 鏋勯€犱竴涓柊鐨勬帓搴忓垪琛ㄨ妭鐐?
     */
    public SortListNode() {
        // 浣跨敤鍒嗙被鍛藉悕 - data.lists.sort_list
        super(UUID.randomUUID(), "data.lists.sort_list");
        
        // 鍒涘缓杈撳叆绔彛
        IPort listInput = new BasePort(INPUT_LIST_ID, "List", 
                "The list to sort", NodeDataType.LIST, this);
        addInputPort(listInput);
        
        // 鍒涘缓杈撳嚭绔彛
        IPort listOutput = new BasePort(OUTPUT_LIST_ID, "Sorted List", 
                "The sorted list", NodeDataType.LIST, this);
        addOutputPort(listOutput);
    }
    
    /**
     * 瀹炵幇INode鎺ュ彛鐨刧etDescription鏂规硶
     * @return 鑺傜偣鎻忚堪
     */
    @Override
    public String getDescription() {
        return this.description;
    }
    
    /**
     * 鑺傜偣鐨勮绠楅€昏緫
     * @param context 鎵ц涓婁笅鏂?
     */
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 鑾峰彇杈撳叆
        Object inputObj = inputValues.get(INPUT_LIST_ID);
        
        List<Object> resultList = new ArrayList<>();
        
        // 澶勭悊鍒楄〃
        if (inputObj instanceof List) {
            List<?> inputList = (List<?>) inputObj;
            
            // 鍒涘缓涓€涓彲淇敼鐨勬柊鍒楄〃骞舵坊鍔犳墍鏈夐」
            resultList.addAll(inputList);
            
            // 濡傛灉鍒楄〃涓虹┖锛岀洿鎺ヨ繑鍥?
            if (resultList.isEmpty()) {
                outputValues.put(OUTPUT_LIST_ID, resultList);
                return;
            }
            
            // 鎵ц鎺掑簭
            sort(resultList);
        }
        
        // 璁剧疆杈撳嚭
        outputValues.put(OUTPUT_LIST_ID, resultList);
    }
    
    /**
     * 瀵瑰垪琛ㄨ繘琛屾帓搴?
     * @param list 瑕佹帓搴忕殑鍒楄〃
     */
    @SuppressWarnings("unchecked")
    private void sort(List<Object> list) {
        // 妫€娴嬬涓€涓潪绌哄厓绱犵殑绫诲瀷
        Object firstNonNull = null;
        for (Object item : list) {
            if (item != null) {
                firstNonNull = item;
                break;
            }
        }
        
        // 濡傛灉鎵€鏈夊厓绱犻兘涓虹┖锛屾棤闇€鎺掑簭
        if (firstNonNull == null) {
            return;
        }
        
        // 鏍规嵁鎺掑簭绫诲瀷閫夋嫨姣旇緝鍣?
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
                // 鑷姩妫€娴嬬被鍨?
                if (firstNonNull instanceof Number) {
                    comparator = createNumericComparator();
                } else if (firstNonNull instanceof String) {
                    comparator = createTextComparator();
                } else if (firstNonNull instanceof List || firstNonNull instanceof String) {
                    comparator = createLengthComparator();
                } else {
                    // 灏濊瘯浣跨敤鑷劧鎺掑簭锛堝鏋滃厓绱犲疄鐜颁簡Comparable鎺ュ彛锛?
                    try {
                        // 浣跨敤姣旇緝鍣ㄨ€屼笉鏄洿鎺ヨ浆鎹㈠垪琛ㄧ被鍨?
                        comparator = new Comparator<Object>() {
                            @SuppressWarnings("unchecked")
                            @Override
                            public int compare(Object o1, Object o2) {
                                // 澶勭悊绌哄€?
                                if (o1 == null && o2 == null) return 0;
                                if (o1 == null) return descending ? 1 : -1;
                                if (o2 == null) return descending ? -1 : 1;
                                
                                // 灏濊瘯灏嗗璞′綔涓篊omparable澶勭悊
                                if (o1 instanceof Comparable && o2 instanceof Comparable) {
                                    int result = ((Comparable<Object>) o1).compareTo(o2);
                                    return descending ? -result : result;
                                } else {
                                    // 濡傛灉涓嶆槸Comparable锛屽垯浣跨敤瀛楃涓叉瘮杈?
                                    int result = o1.toString().compareTo(o2.toString());
                                    return descending ? -result : result;
                                }
                            }
                        };
                    } catch (Exception e) {
                        // 鍏冪礌涓嶆敮鎸佹帓搴忥紝浣跨敤toString杩涜鏂囨湰鎺掑簭
                        comparator = createTextComparator();
                    }
                }
                break;
        }
        
        // 搴旂敤鎺掑簭
        if (comparator != null) {
            Collections.sort(list, comparator);
        }
    }
    
    /**
     * 鍒涘缓鏁板€兼瘮杈冨櫒
     */
    private Comparator<Object> createNumericComparator() {
        return new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                // 澶勭悊绌哄€?
                if (o1 == null && o2 == null) return 0;
                if (o1 == null) return descending ? 1 : -1;
                if (o2 == null) return descending ? -1 : 1;
                
                // 灏濊瘯灏嗗璞¤浆鎹负鏁板€?
                double v1 = toDouble(o1);
                double v2 = toDouble(o2);
                
                // 姣旇緝
                int result = Double.compare(v1, v2);
                return descending ? -result : result;
            }
            
            // 灏嗗璞¤浆鎹负double
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
     * 鍒涘缓鏂囨湰姣旇緝鍣?
     */
    private Comparator<Object> createTextComparator() {
        return new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                // 澶勭悊绌哄€?
                if (o1 == null && o2 == null) return 0;
                if (o1 == null) return descending ? 1 : -1;
                if (o2 == null) return descending ? -1 : 1;
                
                // 姣旇緝瀛楃涓茶〃绀?
                int result = o1.toString().compareTo(o2.toString());
                return descending ? -result : result;
            }
        };
    }
    
    /**
     * 鍒涘缓闀垮害姣旇緝鍣?
     */
    private Comparator<Object> createLengthComparator() {
        return new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                // 澶勭悊绌哄€?
                if (o1 == null && o2 == null) return 0;
                if (o1 == null) return descending ? 1 : -1;
                if (o2 == null) return descending ? -1 : 1;
                
                // 鑾峰彇闀垮害
                int len1 = getLength(o1);
                int len2 = getLength(o2);
                
                // 姣旇緝
                int result = Integer.compare(len1, len2);
                return descending ? -result : result;
            }
            
            // 鑾峰彇瀵硅薄鐨?闀垮害"
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
    
    // --- 鑺傜偣鐘舵€佸簭鍒楀寲 ---
    
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
                        // 鏃犳晥鐨勬帓搴忕被鍨嬶紝浣跨敤榛樿鍊?
                        setSortType(SortType.AUTO);
                    }
                }
            }
        }
    }
} 