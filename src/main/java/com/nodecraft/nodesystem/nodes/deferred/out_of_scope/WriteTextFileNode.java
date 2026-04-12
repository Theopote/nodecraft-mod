package com.nodecraft.nodesystem.nodes.deferred.out_of_scope;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Write Text File 节点: 将字符串写入文本文件
 */
@NodeInfo(
    id = "deferred.out_of_scope.write_text_file",
    displayName = "写入文本文件",
    description = "将字符串写入文本文件",
    category = "deferred.out_of_scope"
)
public class WriteTextFileNode extends BaseNode {

    // --- 节点属性 ---
    private String filePath = ""; // 文件路径
    private String encoding = "UTF-8"; // 文件编码
    private boolean appendMode = false; // 是否追加模式
    private boolean createDirectory = true; // 是否创建目录
    private String description = "将字符串写入文本文件";
    
    // --- 输入端口 IDs ---
    private static final String INPUT_FILE_PATH_ID = "input_file_path";
    private static final String INPUT_TEXT_ID = "input_text";
    private static final String INPUT_LINES_ID = "input_lines";
    private static final String INPUT_ENCODING_ID = "input_encoding";
    private static final String INPUT_APPEND_ID = "input_append";
    private static final String INPUT_TRIGGER_ID = "input_trigger";
    
    // --- 输出端口 IDs ---
    private static final String OUTPUT_SUCCESS_ID = "output_success";
    private static final String OUTPUT_BYTES_WRITTEN_ID = "output_bytes_written";
    private static final String OUTPUT_FILE_PATH_ID = "output_file_path";
    private static final String OUTPUT_ERROR_ID = "output_error";

    /**
     * 构造一个新的写入文本文件节点
     */
    public WriteTextFileNode() {
        super(UUID.randomUUID(), "deferred.out_of_scope.write_text_file");
        
        // 创建输入端口
        addInputPort(new BasePort(INPUT_FILE_PATH_ID, "File Path", 
                "要写入的文本文件路径", NodeDataType.FILE_PATH, this));
        
        addInputPort(new BasePort(INPUT_TEXT_ID, "Text", 
                "要写入的文本内容", NodeDataType.STRING, this));
        
        addInputPort(new BasePort(INPUT_LINES_ID, "Lines", 
                "要写入的文本行列表（优先于Text）", NodeDataType.LIST, this));
        
        addInputPort(new BasePort(INPUT_ENCODING_ID, "Encoding", 
                "文件编码（如UTF-8）", NodeDataType.STRING, this));
        
        addInputPort(new BasePort(INPUT_APPEND_ID, "Append", 
                "是否追加到现有文件", NodeDataType.BOOLEAN, this));
        
        addInputPort(new BasePort(INPUT_TRIGGER_ID, "Trigger", 
                "触发写入操作", NodeDataType.ANY, this));
        
        // 创建输出端口
        addOutputPort(new BasePort(OUTPUT_SUCCESS_ID, "Success", 
                "是否成功写入文件", NodeDataType.BOOLEAN, this));
        
        addOutputPort(new BasePort(OUTPUT_BYTES_WRITTEN_ID, "Bytes Written", 
                "写入的字节数", NodeDataType.INTEGER, this));
        
        addOutputPort(new BasePort(OUTPUT_FILE_PATH_ID, "File Path", 
                "写入的文件路径", NodeDataType.FILE_PATH, this));
        
        addOutputPort(new BasePort(OUTPUT_ERROR_ID, "Error", 
                "写入失败时的错误信息", NodeDataType.STRING, this));
    }
    
    /**
     * 节点的计算逻辑
     */
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 初始化输出值
        boolean success = false;
        long bytesWritten = 0;
        String savedPath = "";
        String errorMessage = "";
        
        // 获取输入值
        Object filePathObj = inputValues.get(INPUT_FILE_PATH_ID);
        Object textObj = inputValues.get(INPUT_TEXT_ID);
        Object linesObj = inputValues.get(INPUT_LINES_ID);
        Object encodingObj = inputValues.get(INPUT_ENCODING_ID);
        Object appendObj = inputValues.get(INPUT_APPEND_ID);
        Object triggerObj = inputValues.get(INPUT_TRIGGER_ID);
        
        // 确定文件路径
        String filePathToWrite = this.filePath;
        if (filePathObj instanceof String && !((String) filePathObj).isEmpty()) {
            filePathToWrite = (String) filePathObj;
        }
        
        // 确定编码
        String encodingToUse = this.encoding;
        if (encodingObj instanceof String && !((String) encodingObj).isEmpty()) {
            encodingToUse = (String) encodingObj;
        }
        
        // 确定是否追加模式
        boolean shouldAppend = this.appendMode;
        if (appendObj instanceof Boolean) {
            shouldAppend = (Boolean) appendObj;
        }
        
        // 确定要写入的文本
        String textToWrite = "";
        if (linesObj instanceof List) {
            // 使用列表优先于单个文本
            List<?> linesList = (List<?>) linesObj;
            StringBuilder sb = new StringBuilder();
            for (Object lineObj : linesList) {
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append(lineObj != null ? lineObj.toString() : "");
            }
            textToWrite = sb.toString();
        } else if (textObj instanceof String) {
            textToWrite = (String) textObj;
        }
        
        // 检查是否应该写入文件
        boolean shouldWrite = triggerObj != null;
        
        // 如果路径为空，或者未触发写入，则直接返回
        if (filePathToWrite.isEmpty() || !shouldWrite) {
            // 设置未成功状态和错误消息
            outputValues.put(OUTPUT_SUCCESS_ID, false);
            outputValues.put(OUTPUT_BYTES_WRITTEN_ID, 0);
            outputValues.put(OUTPUT_FILE_PATH_ID, "");
            outputValues.put(OUTPUT_ERROR_ID, "未写入：无文件路径或未触发");
            return;
        }
        
        // 写入文件
        try {
            Path path = SafeFilePathResolver.resolveInAllowedDirectory(filePathToWrite);
            Path parent = path.getParent();
            
            // 如果需要，创建父目录
            if (parent != null && !Files.exists(parent) && createDirectory) {
                Files.createDirectories(parent);
            }
            
            // 确定字符集
            Charset charset;
            try {
                charset = Charset.forName(encodingToUse);
            } catch (Exception e) {
                // 如果指定的编码无效，使用UTF-8
                charset = StandardCharsets.UTF_8;
            }
            
            // 写入文件
            if (shouldAppend && Files.exists(path)) {
                // 追加模式
                bytesWritten = Files.write(path, textToWrite.getBytes(charset), 
                        StandardOpenOption.APPEND, StandardOpenOption.CREATE).toFile().length();
            } else {
                // 覆盖模式
                bytesWritten = Files.write(path, textToWrite.getBytes(charset)).toFile().length();
            }
            
            success = true;
            savedPath = path.toString();
        } catch (Exception e) {
            success = false;
            errorMessage = "写入文件时出错: " + e.getMessage();
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_SUCCESS_ID, success);
        outputValues.put(OUTPUT_BYTES_WRITTEN_ID, bytesWritten);
        outputValues.put(OUTPUT_FILE_PATH_ID, savedPath);
        outputValues.put(OUTPUT_ERROR_ID, errorMessage);
    }

    /**
     * 浏览选择保存位置
     * 实际实现中此方法应该由UI层调用，打开文件保存对话框
     */
    public boolean browseForSaveLocation() {
        // 实际实现在UI层
        return false;
    }
    
    // --- Getters/Setters for Properties ---
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
        markDirty();
    }
    
    public String getEncoding() {
        return encoding;
    }
    
    public void setEncoding(String encoding) {
        this.encoding = encoding;
        markDirty();
    }
    
    public boolean isAppendMode() {
        return appendMode;
    }
    
    public void setAppendMode(boolean appendMode) {
        this.appendMode = appendMode;
        markDirty();
    }
    
    public boolean isCreateDirectory() {
        return createDirectory;
    }
    
    public void setCreateDirectory(boolean createDirectory) {
        this.createDirectory = createDirectory;
        markDirty();
    }
    
    /**
     * 保存节点状态
     */
    @Override
    public @Nullable Object getNodeState() {
        Object[] state = new Object[4];
        state[0] = filePath;
        state[1] = encoding;
        state[2] = appendMode;
        state[3] = createDirectory;
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
                if (objState[1] instanceof String) {
                    encoding = (String) objState[1];
                }
                if (objState[2] instanceof Boolean) {
                    appendMode = (Boolean) objState[2];
                }
                if (objState[3] instanceof Boolean) {
                    createDirectory = (Boolean) objState[3];
                }
            }
        }
    }

    @Override
    public String getDescription() {
        return this.description;
    }
} 
