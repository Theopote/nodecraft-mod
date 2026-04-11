package com.nodecraft.nodesystem.nodes.math.list_sequence;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.IPort;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * 鍒楄〃闀垮害鑺傜偣锛岃幏鍙栧垪琛ㄧ殑闀垮害锛堝厓绱犳暟閲忥級
 */
@NodeInfo(
    id = "data.lists.list_length",
    displayName = "鍒楄〃闀垮害",
    description = "鑾峰彇鍒楄〃涓厓绱犵殑鏁伴噺",
    category = "data.lists"
)
public class ListLengthNode extends BaseNode {
    
    // --- 杈撳叆/杈撳嚭绔彛ID ---
    private static final String INPUT_LIST_ID = "input_list";
    private static final String OUTPUT_LENGTH_ID = "output_length";
    private String description; // 瀛樺偍鑺傜偣鎻忚堪
    
    /**
     * 鏋勯€犱竴涓柊鐨勫垪琛ㄩ暱搴﹁妭鐐?
     */
    public ListLengthNode() {
        // 璋冪敤鐖剁被鏋勯€犲嚱鏁帮紝浣跨敤UUID.randomUUID()鐢熸垚鏂扮殑ID
        super(UUID.randomUUID(), "data.lists.list_length");
        
        // 璁剧疆鑺傜偣鎻忚堪
        this.description = "Returns the number of items in a list";
        
        // 鍒涘缓杈撳叆绔彛
        IPort listInput = new BasePort(INPUT_LIST_ID, "List", 
                "The list to get the length of", NodeDataType.LIST, this);
        addInputPort(listInput);
        
        // 鍒涘缓杈撳嚭绔彛
        IPort lengthOutput = new BasePort(OUTPUT_LENGTH_ID, "Length", 
                "The number of items in the list", NodeDataType.INTEGER, this);
        addOutputPort(lengthOutput);
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
        // 鑾峰彇杈撳叆鍒楄〃
        Object inputObj = inputValues.get(INPUT_LIST_ID);
        int length = 0;
        
        // 璁＄畻鍒楄〃闀垮害
        if (inputObj instanceof List) {
            List<?> list = (List<?>) inputObj;
            length = list.size();
        }
        
        // 璁剧疆杈撳嚭
        outputValues.put(OUTPUT_LENGTH_ID, length);
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