package com.nodecraft.nodesystem.nodes.utilities.fileio;

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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Read Text File 节点: 读取文本文件内容为字符串
 */
@NodeInfo(
    id = "utilities.fileio.read_text_file",
    displayName = "读取文本文件",
    description = "读取文本文件内容为字符串",
    category = "utilities.fileio"
)
public class ReadTextFileNode extends BaseNode {

    // --- 节点属性 ---
    private String filePath = ""; // 文件路径
    private String encoding = "UTF-8"; // 文件编码
    private boolean splitLines = false; // 是否按行拆分
    private int maxLines = 10000; // 最大读取行数（防止内存溢出）
    private String description = "读取文本文件内容为字符串";
    
    // --- 输入端口 IDs ---
    private static final String INPUT_FILE_PATH_ID = "input_file_path";
    private static final String INPUT_ENCODING_ID = "input_encoding";
    private static final String INPUT_SPLIT_LINES_ID = "input_split_lines";
    private static final String INPUT_MAX_LINES_ID = "input_max_lines";
    private static final String INPUT_TRIGGER_ID = "input_trigger";
    
    // --- 输出端口 IDs ---
    private static final String OUTPUT_TEXT_ID = "output_text";
    private static final String OUTPUT_LINES_ID = "output_lines";
    private static final String OUTPUT_LINE_COUNT_ID = "output_line_count";
    private static final String OUTPUT_SUCCESS_ID = "output_success";
    private static final String OUTPUT_ERROR_ID = "output_error";

    /**
     * 构造一个新的读取文本文件节点
     */
    public ReadTextFileNode() {
        super(UUID.randomUUID(), "utilities.fileio.read_text_file");
        
        // 创建输入端口
        addInputPort(new BasePort(INPUT_FILE_PATH_ID, "File Path", 
                "要读取的文本文件路径", NodeDataType.FILE_PATH, this));
        
        addInputPort(new BasePort(INPUT_ENCODING_ID, "Encoding", 
                "文件编码（如UTF-8）", NodeDataType.STRING, this));
        
        addInputPort(new BasePort(INPUT_SPLIT_LINES_ID, "Split Lines", 
                "是否按行拆分文本", NodeDataType.BOOLEAN, this));
        
        addInputPort(new BasePort(INPUT_MAX_LINES_ID, "Max Lines", 
                "最大读取行数", NodeDataType.INTEGER, this));
        
        addInputPort(new BasePort(INPUT_TRIGGER_ID, "Trigger", 
                "触发读取操作", NodeDataType.ANY, this));
        
        // 创建输出端口
        addOutputPort(new BasePort(OUTPUT_TEXT_ID, "Text", 
                "读取的文本内容", NodeDataType.STRING, this));
        
        addOutputPort(new BasePort(OUTPUT_LINES_ID, "Lines", 
                "按行拆分的文本内容", NodeDataType.LIST, this));
        
        addOutputPort(new BasePort(OUTPUT_LINE_COUNT_ID, "Line Count", 
                "文本的行数", NodeDataType.INTEGER, this));
        
        addOutputPort(new BasePort(OUTPUT_SUCCESS_ID, "Success", 
                "是否成功读取文件", NodeDataType.BOOLEAN, this));
        
        addOutputPort(new BasePort(OUTPUT_ERROR_ID, "Error", 
                "读取失败时的错误信息", NodeDataType.STRING, this));
    }
    
    /**
     * 节点的计算逻辑
     */
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 初始化输出值
        String text = "";
        List<String> lines = new ArrayList<>();
        int lineCount = 0;
        boolean success = false;
        String errorMessage = "";
        
        // 获取输入值
        Object filePathObj = inputValues.get(INPUT_FILE_PATH_ID);
        Object encodingObj = inputValues.get(INPUT_ENCODING_ID);
        Object splitLinesObj = inputValues.get(INPUT_SPLIT_LINES_ID);
        Object maxLinesObj = inputValues.get(INPUT_MAX_LINES_ID);
        Object triggerObj = inputValues.get(INPUT_TRIGGER_ID);
        
        // 确定文件路径
        String filePathToRead = this.filePath;
        if (filePathObj instanceof String && !((String) filePathObj).isEmpty()) {
            filePathToRead = (String) filePathObj;
        }
        
        // 确定编码
        String encodingToUse = this.encoding;
        if (encodingObj instanceof String && !((String) encodingObj).isEmpty()) {
            encodingToUse = (String) encodingObj;
        }
        
        // 确定是否按行拆分
        boolean shouldSplitLines = this.splitLines;
        if (splitLinesObj instanceof Boolean) {
            shouldSplitLines = (Boolean) splitLinesObj;
        }
        
        // 确定最大行数
        int maxLinesToRead = this.maxLines;
        if (maxLinesObj instanceof Number) {
            maxLinesToRead = Math.max(1, ((Number) maxLinesObj).intValue());
        }
        
        // 检查是否应该读取文件
        boolean shouldRead = triggerObj != null;
        
        // 如果路径为空，或者未触发读取，则直接返回
        if (filePathToRead.isEmpty() || !shouldRead) {
            // 设置未成功状态和错误消息
            outputValues.put(OUTPUT_TEXT_ID, "");
            outputValues.put(OUTPUT_LINES_ID, lines);
            outputValues.put(OUTPUT_LINE_COUNT_ID, 0);
            outputValues.put(OUTPUT_SUCCESS_ID, false);
            outputValues.put(OUTPUT_ERROR_ID, "未读取：无文件路径或未触发");
            return;
        }
        
        // 读取文件
        try {
            Path path = SafeFilePathResolver.resolveInAllowedDirectory(filePathToRead);
            if (!Files.exists(path)) {
                errorMessage = "文件不存在: " + filePathToRead;
            } else if (!Files.isReadable(path)) {
                errorMessage = "文件不可读: " + filePathToRead;
            } else {
                Charset charset;
                try {
                    charset = Charset.forName(encodingToUse);
                } catch (Exception e) {
                    // 如果指定的编码无效，使用UTF-8
                    charset = StandardCharsets.UTF_8;
                }
                
                if (shouldSplitLines) {
                    // 按行读取
                    lines = Files.readAllLines(path, charset);
                    
                    // 截断行数（如果超过最大行数）
                    if (lines.size() > maxLinesToRead) {
                        lines = lines.subList(0, maxLinesToRead);
                        errorMessage = "文件行数超过限制，已截断至" + maxLinesToRead + "行";
                    }
                    
                    // 行数统计
                    lineCount = lines.size();
                    
                    // 组合全文
                    StringBuilder sb = new StringBuilder();
                    for (String line : lines) {
                        if (sb.length() > 0) {
                            sb.append("\n");
                        }
                        sb.append(line);
                    }
                    text = sb.toString();
                } else {
                    // 一次性读取全部内容
                    text = Files.readString(path, charset);
                    
                    // 统计行数
                    String[] textLines = text.split("\r?\n");
                    lineCount = textLines.length;
                    
                    // 如果需要行列表（即使未要求分行，某些节点可能仍需要行列表）
                    for (String line : textLines) {
                        if (lines.size() >= maxLinesToRead) {
                            errorMessage = "文件行数超过限制，已截断至" + maxLinesToRead + "行";
                            break;
                        }
                        lines.add(line);
                    }
                }
                
                success = true;
            }
        } catch (Exception e) {
            success = false;
            errorMessage = "读取文件时出错: " + e.getMessage();
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_TEXT_ID, text);
        outputValues.put(OUTPUT_LINES_ID, lines);
        outputValues.put(OUTPUT_LINE_COUNT_ID, lineCount);
        outputValues.put(OUTPUT_SUCCESS_ID, success);
        outputValues.put(OUTPUT_ERROR_ID, errorMessage);
    }

    /**
     * 浏览选择文件
     * 实际实现中此方法应该由UI层调用，打开文件选择对话框
     */
    public boolean browseForFile() {
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
    
    public boolean isSplitLines() {
        return splitLines;
    }
    
    public void setSplitLines(boolean splitLines) {
        this.splitLines = splitLines;
        markDirty();
    }
    
    public int getMaxLines() {
        return maxLines;
    }
    
    public void setMaxLines(int maxLines) {
        this.maxLines = Math.max(1, maxLines);
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
        state[2] = splitLines;
        state[3] = maxLines;
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
                    splitLines = (Boolean) objState[2];
                }
                if (objState[3] instanceof Number) {
                    maxLines = Math.max(1, ((Number) objState[3]).intValue());
                }
            }
        }
    }

    @Override
    public String getDescription() {
        return this.description;
    }
} 