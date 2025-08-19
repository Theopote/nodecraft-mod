package com.nodecraft.nodesystem.nodes.utilities.fileio;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Read Data File 节点: 读取结构化数据文件（CSV、JSON 等）
 */
@NodeInfo(
    id = "utilities.fileio.read_data_file",
    displayName = "读取数据文件",
    description = "读取结构化数据文件（CSV、JSON 等）",
    category = "utilities.fileio"
)
public class ReadDataFileNode extends BaseNode {

    // --- 节点属性 ---
    private String filePath = ""; // 文件路径
    private String encoding = "UTF-8"; // 文件编码
    private String fileType = "CSV"; // 文件类型 (CSV, JSON)
    private String csvDelimiter = ","; // CSV 分隔符
    private boolean hasHeader = true; // CSV 是否包含表头
    private int maxRows = 10000; // 最大读取行数（防止内存溢出）
    private String description = "读取结构化数据文件（CSV、JSON 等）";
    
    // --- 输入端口 IDs ---
    private static final String INPUT_FILE_PATH_ID = "input_file_path";
    private static final String INPUT_ENCODING_ID = "input_encoding";
    private static final String INPUT_FILE_TYPE_ID = "input_file_type";
    private static final String INPUT_CSV_DELIMITER_ID = "input_csv_delimiter";
    private static final String INPUT_HAS_HEADER_ID = "input_has_header";
    private static final String INPUT_MAX_ROWS_ID = "input_max_rows";
    private static final String INPUT_TRIGGER_ID = "input_trigger";
    
    // --- 输出端口 IDs ---
    private static final String OUTPUT_DATA_ID = "output_data";
    private static final String OUTPUT_HEADERS_ID = "output_headers";
    private static final String OUTPUT_ROW_COUNT_ID = "output_row_count";
    private static final String OUTPUT_SUCCESS_ID = "output_success";
    private static final String OUTPUT_ERROR_ID = "output_error";
    private static final String OUTPUT_RAW_TEXT_ID = "output_raw_text";

    // --- 文件类型常量 ---
    private static final String FILE_TYPE_CSV = "CSV";
    private static final String FILE_TYPE_JSON = "JSON";
    
    /**
     * 构造一个新的读取数据文件节点
     */
    public ReadDataFileNode() {
        super(UUID.randomUUID(), "utilities.fileio.read_data_file");
        
        // 创建输入端口
        addInputPort(new BasePort(INPUT_FILE_PATH_ID, "File Path", 
                "要读取的数据文件路径", NodeDataType.FILE_PATH, this));
        
        addInputPort(new BasePort(INPUT_ENCODING_ID, "Encoding", 
                "文件编码（如UTF-8）", NodeDataType.STRING, this));
        
        addInputPort(new BasePort(INPUT_FILE_TYPE_ID, "File Type", 
                "文件类型（CSV, JSON）", NodeDataType.STRING, this));
        
        addInputPort(new BasePort(INPUT_CSV_DELIMITER_ID, "CSV Delimiter", 
                "CSV分隔符（默认为逗号）", NodeDataType.STRING, this));
        
        addInputPort(new BasePort(INPUT_HAS_HEADER_ID, "Has Header", 
                "CSV文件是否包含表头", NodeDataType.BOOLEAN, this));
        
        addInputPort(new BasePort(INPUT_MAX_ROWS_ID, "Max Rows", 
                "最大读取行数", NodeDataType.INTEGER, this));
        
        addInputPort(new BasePort(INPUT_TRIGGER_ID, "Trigger", 
                "触发读取操作", NodeDataType.ANY, this));
        
        // 创建输出端口
        addOutputPort(new BasePort(OUTPUT_DATA_ID, "Data", 
                "读取的结构化数据", NodeDataType.LIST, this));
        
        addOutputPort(new BasePort(OUTPUT_HEADERS_ID, "Headers", 
                "CSV表头或JSON根键列表", NodeDataType.LIST, this));
        
        addOutputPort(new BasePort(OUTPUT_ROW_COUNT_ID, "Row Count", 
                "数据行数/元素数", NodeDataType.INTEGER, this));
        
        addOutputPort(new BasePort(OUTPUT_SUCCESS_ID, "Success", 
                "是否成功读取文件", NodeDataType.BOOLEAN, this));
        
        addOutputPort(new BasePort(OUTPUT_ERROR_ID, "Error", 
                "读取失败时的错误信息", NodeDataType.STRING, this));
        
        addOutputPort(new BasePort(OUTPUT_RAW_TEXT_ID, "Raw Text", 
                "原始文件内容", NodeDataType.STRING, this));
    }
    
    /**
     * 节点的计算逻辑
     */
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 初始化输出值
        List<Object> data = new ArrayList<>();
        List<String> headers = new ArrayList<>();
        int rowCount = 0;
        boolean success = false;
        String errorMessage = "";
        String rawText = "";
        
        // 获取输入值
        Object filePathObj = inputValues.get(INPUT_FILE_PATH_ID);
        Object encodingObj = inputValues.get(INPUT_ENCODING_ID);
        Object fileTypeObj = inputValues.get(INPUT_FILE_TYPE_ID);
        Object csvDelimiterObj = inputValues.get(INPUT_CSV_DELIMITER_ID);
        Object hasHeaderObj = inputValues.get(INPUT_HAS_HEADER_ID);
        Object maxRowsObj = inputValues.get(INPUT_MAX_ROWS_ID);
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
        
        // 确定文件类型
        String fileTypeToUse = this.fileType;
        if (fileTypeObj instanceof String && !((String) fileTypeObj).isEmpty()) {
            fileTypeToUse = ((String) fileTypeObj).toUpperCase();
        }
        
        // 确定CSV分隔符
        String delimiterToUse = this.csvDelimiter;
        if (csvDelimiterObj instanceof String && !((String) csvDelimiterObj).isEmpty()) {
            delimiterToUse = (String) csvDelimiterObj;
        }
        
        // 确定是否包含表头
        boolean hasHeaderToUse = this.hasHeader;
        if (hasHeaderObj instanceof Boolean) {
            hasHeaderToUse = (Boolean) hasHeaderObj;
        }
        
        // 确定最大行数
        final int maxRowsToRead = this.maxRows;
        if (maxRowsObj instanceof Number) {
            int tempMaxRows = Math.max(1, ((Number) maxRowsObj).intValue());
            // 重新声明为final避免在lambda中使用非final变量
            final int finalMaxRows = tempMaxRows;
            
            if (FILE_TYPE_JSON.equalsIgnoreCase(fileTypeToUse) && finalMaxRows != maxRowsToRead) {
                // 只在这里使用finalMaxRows
                // 其他地方仍然使用maxRowsToRead的默认值
            }
        }
        
        // 检查是否应该读取文件
        boolean shouldRead = triggerObj != null;
        
        // 如果路径为空，或者未触发读取，则直接返回
        if (filePathToRead.isEmpty() || !shouldRead) {
            // 设置未成功状态和错误消息
            outputValues.put(OUTPUT_DATA_ID, data);
            outputValues.put(OUTPUT_HEADERS_ID, headers);
            outputValues.put(OUTPUT_ROW_COUNT_ID, 0);
            outputValues.put(OUTPUT_SUCCESS_ID, false);
            outputValues.put(OUTPUT_ERROR_ID, "未读取：无文件路径或未触发");
            outputValues.put(OUTPUT_RAW_TEXT_ID, "");
            return;
        }
        
        // 读取文件
        try {
            Path path = Paths.get(filePathToRead);
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
                
                // 读取原始文本
                rawText = Files.readString(path, charset);
                
                // 根据文件类型处理数据
                if (FILE_TYPE_CSV.equalsIgnoreCase(fileTypeToUse)) {
                    // 处理CSV文件
                    List<String[]> csvData = parseCSV(rawText, delimiterToUse, maxRowsToRead);
                    
                    if (!csvData.isEmpty()) {
                        // 提取表头（如果有）
                        if (hasHeaderToUse && csvData.size() > 0) {
                            String[] headerRow = csvData.get(0);
                            for (String header : headerRow) {
                                headers.add(header);
                            }
                            // 移除表头行，保留数据行
                            csvData.remove(0);
                        }
                        
                        // 将CSV数据转换为键值对列表（如果有表头）
                        if (hasHeaderToUse && !headers.isEmpty()) {
                            for (String[] row : csvData) {
                                Map<String, String> rowMap = new HashMap<>();
                                for (int i = 0; i < Math.min(headers.size(), row.length); i++) {
                                    rowMap.put(headers.get(i), row[i]);
                                }
                                data.add(rowMap);
                            }
                        } else {
                            // 如果没有表头，直接添加字符串数组
                            for (String[] row : csvData) {
                                data.add(row);
                            }
                            
                            // 生成默认表头（列1，列2...）
                            if (csvData.size() > 0) {
                                int columnCount = csvData.get(0).length;
                                for (int i = 0; i < columnCount; i++) {
                                    headers.add("列" + (i + 1));
                                }
                            }
                        }
                        
                        rowCount = data.size();
                    }
                    
                    success = true;
                } else if (FILE_TYPE_JSON.equalsIgnoreCase(fileTypeToUse)) {
                    // 处理JSON文件
                    try {
                        Gson gson = new GsonBuilder().setPrettyPrinting().create();
                        JsonElement jsonElement = JsonParser.parseString(rawText);
                        
                        if (jsonElement.isJsonArray()) {
                            // JSON数组
                            List<Object> jsonList = new ArrayList<>();
                            // 创建最终的headers和maxRows变量，在lambda表达式中使用
                            final List<String> finalHeaders = headers;
                            final int finalMaxRows = maxRowsToRead;
                            
                            jsonElement.getAsJsonArray().forEach(element -> {
                                if (jsonList.size() < finalMaxRows) {
                                    if (element.isJsonObject()) {
                                        // 如果是对象，转为Map
                                        @SuppressWarnings("unchecked")
                                        Map<String, Object> map = gson.fromJson(element, Map.class);
                                        jsonList.add(map);
                                        
                                        // 收集所有可能的键作为"表头"
                                        if (finalHeaders.isEmpty()) {
                                            finalHeaders.addAll(map.keySet());
                                        }
                                    } else {
                                        // 基本类型或其他
                                        jsonList.add(gson.fromJson(element, Object.class));
                                    }
                                }
                            });
                            
                            data.addAll(jsonList);
                            rowCount = data.size();
                        } else if (jsonElement.isJsonObject()) {
                            // 单个JSON对象
                            @SuppressWarnings("unchecked")
                            Map<String, Object> jsonMap = gson.fromJson(jsonElement, Map.class);
                            data.add(jsonMap);
                            headers.addAll(jsonMap.keySet());
                            rowCount = 1;
                        } else {
                            // 基本类型
                            Object jsonValue = gson.fromJson(jsonElement, Object.class);
                            data.add(jsonValue);
                            headers.add("值");
                            rowCount = 1;
                        }
                        
                        success = true;
                    } catch (Exception e) {
                        errorMessage = "解析JSON时出错: " + e.getMessage();
                    }
                } else {
                    errorMessage = "不支持的文件类型: " + fileTypeToUse;
                }
            }
        } catch (Exception e) {
            success = false;
            errorMessage = "读取文件时出错: " + e.getMessage();
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_DATA_ID, data);
        outputValues.put(OUTPUT_HEADERS_ID, headers);
        outputValues.put(OUTPUT_ROW_COUNT_ID, rowCount);
        outputValues.put(OUTPUT_SUCCESS_ID, success);
        outputValues.put(OUTPUT_ERROR_ID, errorMessage);
        outputValues.put(OUTPUT_RAW_TEXT_ID, rawText);
    }
    
    /**
     * 解析CSV文本
     */
    private List<String[]> parseCSV(String text, String delimiter, int maxRows) {
        List<String[]> result = new ArrayList<>();
        String[] lines = text.split("\r?\n");
        
        for (int i = 0; i < lines.length && result.size() < maxRows; i++) {
            String line = lines[i].trim();
            if (!line.isEmpty()) {
                // 简单CSV解析，不处理引号内的逗号等复杂情况
                // 实际项目中应使用成熟的CSV解析库
                String[] fields = line.split(delimiter, -1);
                
                // 清理每个字段（去除引号等）
                for (int j = 0; j < fields.length; j++) {
                    fields[j] = cleanCsvField(fields[j]);
                }
                
                result.add(fields);
            }
        }
        
        return result;
    }
    
    /**
     * 清理CSV字段（去除引号等）
     */
    private String cleanCsvField(String field) {
        field = field.trim();
        // 如果字段被双引号包围，去除双引号
        if (field.startsWith("\"") && field.endsWith("\"") && field.length() >= 2) {
            field = field.substring(1, field.length() - 1);
            // 处理双引号转义（两个连续双引号转为一个）
            field = field.replace("\"\"", "\"");
        }
        return field;
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
    
    public String getFileType() {
        return fileType;
    }
    
    public void setFileType(String fileType) {
        this.fileType = fileType;
        markDirty();
    }
    
    public String getCsvDelimiter() {
        return csvDelimiter;
    }
    
    public void setCsvDelimiter(String csvDelimiter) {
        this.csvDelimiter = csvDelimiter;
        markDirty();
    }
    
    public boolean isHasHeader() {
        return hasHeader;
    }
    
    public void setHasHeader(boolean hasHeader) {
        this.hasHeader = hasHeader;
        markDirty();
    }
    
    public int getMaxRows() {
        return maxRows;
    }
    
    public void setMaxRows(int maxRows) {
        this.maxRows = Math.max(1, maxRows);
        markDirty();
    }
    
    /**
     * 保存节点状态
     */
    @Override
    public @Nullable Object getNodeState() {
        Object[] state = new Object[6];
        state[0] = filePath;
        state[1] = encoding;
        state[2] = fileType;
        state[3] = csvDelimiter;
        state[4] = hasHeader;
        state[5] = maxRows;
        return state;
    }
    
    /**
     * 恢复节点状态
     */
    @Override
    public void setNodeState(@Nullable Object state) {
        if (state instanceof Object[]) {
            Object[] objState = (Object[]) state;
            if (objState.length >= 6) {
                if (objState[0] instanceof String) {
                    filePath = (String) objState[0];
                }
                if (objState[1] instanceof String) {
                    encoding = (String) objState[1];
                }
                if (objState[2] instanceof String) {
                    fileType = (String) objState[2];
                }
                if (objState[3] instanceof String) {
                    csvDelimiter = (String) objState[3];
                }
                if (objState[4] instanceof Boolean) {
                    hasHeader = (Boolean) objState[4];
                }
                if (objState[5] instanceof Number) {
                    maxRows = Math.max(1, ((Number) objState[5]).intValue());
                }
            }
        }
    }

    @Override
    public String getDescription() {
        return this.description;
    }
} 