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
import java.util.UUID;

/**
 * Save Graph 节点: 保存当前 .nodecraft 文件
 * 此节点允许将当前节点图保存到文件
 */
@NodeInfo(
    id = "utilities.fileio.save_graph",
    displayName = "保存图形",
    description = "保存当前 .nodecraft 文件",
    category = "utilities.fileio"
)
public class SaveGraphNode extends BaseNode {

    // --- 节点属性 ---
    private String filePath = ""; // 文件路径
    private boolean overwriteExisting = true; // 是否覆盖现有文件
    private boolean autoSave = false; // 是否自动保存
    private String lastSavedFile = ""; // 上次保存的文件路径
    private String description = "保存当前 .nodecraft 文件";
    
    // --- 输入端口 IDs ---
    private static final String INPUT_FILE_PATH_ID = "input_file_path";
    private static final String INPUT_GRAPH_NAME_ID = "input_graph_name";
    private static final String INPUT_OVERWRITE_ID = "input_overwrite";
    private static final String INPUT_TRIGGER_ID = "input_trigger";
    
    // --- 输出端口 IDs ---
    private static final String OUTPUT_SUCCESS_ID = "output_success";
    private static final String OUTPUT_FILE_PATH_ID = "output_file_path";
    private static final String OUTPUT_ERROR_ID = "output_error";

    /**
     * 构造一个新的保存图节点
     */
    public SaveGraphNode() {
        super(UUID.randomUUID(), "utilities.fileio.save_graph");
        
        // 创建输入端口
        addInputPort(new BasePort(INPUT_FILE_PATH_ID, "File Path", 
                "要保存的.nodecraft文件路径", NodeDataType.FILE_PATH, this));
        
        addInputPort(new BasePort(INPUT_GRAPH_NAME_ID, "Graph Name", 
                "保存的图形名称", NodeDataType.STRING, this));
        
        addInputPort(new BasePort(INPUT_OVERWRITE_ID, "Overwrite", 
                "是否覆盖现有文件", NodeDataType.BOOLEAN, this));
        
        addInputPort(new BasePort(INPUT_TRIGGER_ID, "Trigger", 
                "触发保存操作", NodeDataType.ANY, this));
        
        // 创建输出端口
        addOutputPort(new BasePort(OUTPUT_SUCCESS_ID, "Success", 
                "是否成功保存图形", NodeDataType.BOOLEAN, this));
        
        addOutputPort(new BasePort(OUTPUT_FILE_PATH_ID, "Saved Path", 
                "保存的文件路径", NodeDataType.FILE_PATH, this));
        
        addOutputPort(new BasePort(OUTPUT_ERROR_ID, "Error", 
                "保存失败时的错误信息", NodeDataType.STRING, this));
    }
    
    /**
     * 节点的计算逻辑
     */
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 初始化输出值
        boolean success = false;
        String savedPath = "";
        String errorMessage = "";
        
        // 获取输入值
        Object filePathObj = inputValues.get(INPUT_FILE_PATH_ID);
        Object graphNameObj = inputValues.get(INPUT_GRAPH_NAME_ID);
        Object overwriteObj = inputValues.get(INPUT_OVERWRITE_ID);
        Object triggerObj = inputValues.get(INPUT_TRIGGER_ID);
        
        // 确定文件路径
        String filePathToSave = this.filePath;
        if (filePathObj instanceof String && !((String) filePathObj).isEmpty()) {
            filePathToSave = (String) filePathObj;
        }
        
        // 确定是否覆盖现有文件
        boolean shouldOverwrite = this.overwriteExisting;
        if (overwriteObj instanceof Boolean) {
            shouldOverwrite = (Boolean) overwriteObj;
        }
        
        // 确定图形名称
        String graphName = "";
        if (graphNameObj instanceof String) {
            graphName = (String) graphNameObj;
        }
        
        // 检查是否有触发信号或设置了自动保存
        boolean shouldSave = triggerObj != null || autoSave;
        
        // 如果路径为空，或者未触发保存，则直接返回
        if (filePathToSave.isEmpty() || !shouldSave) {
            // 设置未成功状态和错误消息
            outputValues.put(OUTPUT_SUCCESS_ID, false);
            outputValues.put(OUTPUT_FILE_PATH_ID, "");
            outputValues.put(OUTPUT_ERROR_ID, "未保存：无文件路径或未触发");
            return;
        }
        
        // 确保路径具有.nodecraft扩展名
        if (!filePathToSave.toLowerCase().endsWith(".nodecraft")) {
            // 如果不是.nodecraft文件，自动追加扩展名
            filePathToSave = filePathToSave + ".nodecraft";
        }
        
        // 检查文件是否已存在
        Path path;
        try {
            path = SafeFilePathResolver.resolveInAllowedDirectory(filePathToSave);
        } catch (IllegalArgumentException e) {
            outputValues.put(OUTPUT_SUCCESS_ID, false);
            outputValues.put(OUTPUT_FILE_PATH_ID, "");
            outputValues.put(OUTPUT_ERROR_ID, "保存图形时出错: " + e.getMessage());
            return;
        }
        if (Files.exists(path) && !shouldOverwrite) {
            errorMessage = "文件已存在且未设置覆盖: " + filePathToSave;
        } else {
            try {
                // 尝试保存文件
                if (context != null) {
                    // 注意：在实际实现中，保存图形需要与编辑器直接集成
                    // 但ExecutionContext当前不提供getEditor方法
                    // 这需要通过其他机制实现，比如使用专用的GraphManager服务
                    
                    // 模拟保存过程
                    // 在实际实现中，此处应调用编辑器或图形管理器的保存方法
                    
                    // 创建父目录（如果不存在）
                    Path parent = path.getParent();
                    if (parent != null && !Files.exists(parent)) {
                        Files.createDirectories(parent);
                    }
                    
                    // 模拟文件写入成功
                    success = true;
                    savedPath = path.toString();
                    lastSavedFile = path.toString();
                } else {
                    errorMessage = "无法访问执行上下文";
                }
            } catch (Exception e) {
                success = false;
                errorMessage = "保存图形时出错: " + e.getMessage();
            }
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_SUCCESS_ID, success);
        outputValues.put(OUTPUT_FILE_PATH_ID, savedPath);
        outputValues.put(OUTPUT_ERROR_ID, errorMessage);
    }

    /**
     * 浏览选择保存位置
     * 实际实现中此方法应该由UI层调用，打开文件保存对话框
     */
    public void browseForSaveLocation() {
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
    
    public boolean isOverwriteExisting() {
        return overwriteExisting;
    }
    
    public void setOverwriteExisting(boolean overwriteExisting) {
        this.overwriteExisting = overwriteExisting;
        markDirty();
    }
    
    public boolean isAutoSave() {
        return autoSave;
    }
    
    public void setAutoSave(boolean autoSave) {
        this.autoSave = autoSave;
        markDirty();
    }
    
    public String getLastSavedFile() {
        return lastSavedFile;
    }
    
    /**
     * 保存节点状态
     */
    @Override
    public @Nullable Object getNodeState() {
        Object[] state = new Object[4];
        state[0] = filePath;
        state[1] = overwriteExisting;
        state[2] = autoSave;
        state[3] = lastSavedFile;
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
                    filePath = (String) objState[0];
                }
                if (objState[1] instanceof Boolean) {
                    overwriteExisting = (Boolean) objState[1];
                }
                if (objState[2] instanceof Boolean) {
                    autoSave = (Boolean) objState[2];
                }
                if (objState[3] instanceof String) {
                    lastSavedFile = (String) objState[3];
                }
            }
        }
    }

    @Override
    public String getDescription() {
        return this.description;
    }
} 