package com.nodecraft.nodesystem.nodes.utilities.fileio;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Load Graph 节点: 加载 .nodecraft 文件
 * 此节点允许从文件加载节点图，并触发加载事件
 */
@NodeInfo(
    id = "utilities.fileio.load_graph",
    displayName = "加载图形",
    description = "加载 .nodecraft 文件",
    category = "utilities.fileio"
)
public class LoadGraphNode extends BaseNode {

    // --- 节点属性 ---
    private String filePath = ""; // 文件路径
    private boolean autoLoad = false; // 是否自动加载
    private String lastLoadedFile = ""; // 上次加载的文件路径
    private String description = "加载 .nodecraft 文件";
    
    // --- 输入端口 IDs ---
    private static final String INPUT_FILE_PATH_ID = "input_file_path";
    private static final String INPUT_TRIGGER_ID = "input_trigger";
    
    // --- 输出端口 IDs ---
    private static final String OUTPUT_SUCCESS_ID = "output_success";
    private static final String OUTPUT_GRAPH_NAME_ID = "output_graph_name";
    private static final String OUTPUT_ERROR_ID = "output_error";

    /**
     * 构造一个新的加载图节点
     */
    public LoadGraphNode() {
        super(UUID.randomUUID(), "utilities.fileio.load_graph");
        
        // 创建输入端口
        addInputPort(new BasePort(INPUT_FILE_PATH_ID, "File Path", 
                "要加载的.nodecraft文件路径", NodeDataType.FILE_PATH, this));
        
        addInputPort(new BasePort(INPUT_TRIGGER_ID, "Trigger", 
                "触发加载操作", NodeDataType.ANY, this));
        
        // 创建输出端口
        addOutputPort(new BasePort(OUTPUT_SUCCESS_ID, "Success", 
                "是否成功加载图形", NodeDataType.BOOLEAN, this));
        
        addOutputPort(new BasePort(OUTPUT_GRAPH_NAME_ID, "Graph Name", 
                "加载的图形名称", NodeDataType.STRING, this));
        
        addOutputPort(new BasePort(OUTPUT_ERROR_ID, "Error", 
                "加载失败时的错误信息", NodeDataType.STRING, this));
    }
    
    /**
     * 节点的计算逻辑
     */
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 初始化输出值
        boolean success = false;
        String graphName = "";
        String errorMessage = "";
        
        // 获取输入值
        Object filePathObj = inputValues.get(INPUT_FILE_PATH_ID);
        Object triggerObj = inputValues.get(INPUT_TRIGGER_ID);
        
        // 确定文件路径
        String filePathToLoad = this.filePath;
        if (filePathObj instanceof String && !((String) filePathObj).isEmpty()) {
            filePathToLoad = (String) filePathObj;
        }
        
        // 检查是否有触发信号或设置了自动加载
        boolean shouldLoad = triggerObj != null || autoLoad;
        
        // 如果路径为空，或者未触发加载，则直接返回
        if (filePathToLoad.isEmpty() || !shouldLoad) {
            // 设置未成功状态和空错误消息
            outputValues.put(OUTPUT_SUCCESS_ID, false);
            outputValues.put(OUTPUT_GRAPH_NAME_ID, "");
            outputValues.put(OUTPUT_ERROR_ID, "未加载：无文件路径或未触发");
            return;
        }
        
        // 确保路径具有.nodecraft扩展名
        if (!filePathToLoad.toLowerCase().endsWith(".nodecraft")) {
            // 如果不是.nodecraft文件，自动追加扩展名
            filePathToLoad = filePathToLoad + ".nodecraft";
        }
        
        // 检查文件是否存在
        Path path = Paths.get(filePathToLoad);
        if (!Files.exists(path)) {
            errorMessage = "文件不存在: " + filePathToLoad;
        } else if (!Files.isRegularFile(path)) {
            errorMessage = "路径不是一个文件: " + filePathToLoad;
        } else {
            try {
                // 尝试加载文件
                if (context != null) {
                    // 注意：在实际实现中，加载图形需要与编辑器直接集成
                    // 但ExecutionContext当前不提供getEditor方法
                    // 这需要通过其他机制实现，比如使用专用的GraphManager服务
                    
                    // 模拟加载过程
                    graphName = extractGraphName(filePathToLoad);
                    success = true;
                    lastLoadedFile = filePathToLoad;
                    
                    // 这一步在实际实现中应该能够从加载的图形中获取
                    if (graphName.isEmpty()) {
                        graphName = "加载的图形";
                    }
                } else {
                    errorMessage = "无法访问执行上下文";
                }
            } catch (Exception e) {
                success = false;
                errorMessage = "加载图形时出错: " + e.getMessage();
            }
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_SUCCESS_ID, success);
        outputValues.put(OUTPUT_GRAPH_NAME_ID, graphName);
        outputValues.put(OUTPUT_ERROR_ID, errorMessage);
    }
    
    /**
     * 从文件名中提取图形名称
     * 实际实现中应该从图形文件内容中获取
     */
    private String extractGraphName(String filePath) {
        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toString();
        
        // 移除扩展名
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(0, lastDotIndex);
        }
        
        return fileName;
    }
    
    /**
     * 浏览选择文件
     * 实际实现中此方法应该由UI层调用，打开文件选择对话框
     */
    public void browseForFile() {
        // 实际实现在UI层
    }
    
    // --- Getters/Setters for Properties ---
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
        markDirty();
    }
    
    public boolean isAutoLoad() {
        return autoLoad;
    }
    
    public void setAutoLoad(boolean autoLoad) {
        this.autoLoad = autoLoad;
        markDirty();
    }
    
    public String getLastLoadedFile() {
        return lastLoadedFile;
    }
    
    /**
     * 保存节点状态
     */
    @Override
    public @Nullable Object getNodeState() {
        Object[] state = new Object[3];
        state[0] = filePath;
        state[1] = autoLoad;
        state[2] = lastLoadedFile;
        return state;
    }
    
    /**
     * 恢复节点状态
     */
    @Override
    public void setNodeState(@Nullable Object state) {
        if (state instanceof Object[]) {
            Object[] objState = (Object[]) state;
            if (objState.length >= 3) {
                if (objState[0] instanceof String) {
                    filePath = (String) objState[0];
                }
                if (objState[1] instanceof Boolean) {
                    autoLoad = (Boolean) objState[1];
                }
                if (objState[2] instanceof String) {
                    lastLoadedFile = (String) objState[2];
                }
            }
        }
    }

    @Override
    public String getDescription() {
        return this.description;
    }
} 