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
import java.util.List;
import java.util.UUID;

/**
 * 鍙嶈浆鍒楄〃鑺傜偣锛屽皢鍒楄〃鍏冪礌椤哄簭鍙嶈浆
 */
@NodeInfo(
    id = "data.lists.reverse_list",
    displayName = "鍙嶈浆鍒楄〃",
    description = "灏嗗垪琛ㄥ厓绱犻『搴忓弽杞?,
    category = "data.lists"
)
public class ReverseListNode extends BaseNode {
    
    // --- 杈撳叆/杈撳嚭绔彛ID ---
    private static final String INPUT_LIST_ID = "input_list";
    private static final String OUTPUT_LIST_ID = "output_list";
    private String description; // 瀛樺偍鑺傜偣鎻忚堪
    
    /**
     * 鏋勯€犱竴涓柊鐨勫弽杞垪琛ㄨ妭鐐?
     */
    public ReverseListNode() {
        // 璋冪敤鐖剁被鏋勯€犲嚱鏁帮紝浣跨敤UUID.randomUUID()鐢熸垚鏂扮殑ID
        super(UUID.randomUUID(), "data.lists.reverse_list");
        
        // 璁剧疆鑺傜偣鎻忚堪
        this.description = "Reverses the order of elements in a list";
        
        // 鍒涘缓杈撳叆绔彛
        IPort listInput = new BasePort(INPUT_LIST_ID, "List", 
                "The list to reverse", NodeDataType.LIST, this);
        addInputPort(listInput);
        
        // 鍒涘缓杈撳嚭绔彛
        IPort listOutput = new BasePort(OUTPUT_LIST_ID, "Reversed List", 
                "The list with elements in reverse order", NodeDataType.LIST, this);
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
            
            // 鍙嶈浆鍒楄〃
            Collections.reverse(resultList);
        }
        
        // 璁剧疆杈撳嚭
        outputValues.put(OUTPUT_LIST_ID, resultList);
    }
    
    // 姝よ妭鐐规病鏈夐渶瑕佸簭鍒楀寲鐨勮嚜瀹氫箟鐘舵€?
    @Override
    public Object getNodeState() {
        return null;
    }
    
    @Override
    public void setNodeState(Object state) {
        // 鏃犻渶棰濆鐘舵€佸鐞?
    }
} 