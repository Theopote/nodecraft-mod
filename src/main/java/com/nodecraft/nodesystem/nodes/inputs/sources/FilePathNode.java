package com.nodecraft.nodesystem.nodes.inputs.sources;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.IPort;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 文件路径节点，用于输入文件路径
 */
@NodeInfo(
    id = "inputs.sources.file_path",
    displayName = "文件路径",
    description = "用于输入文件或目录路径",
    category = "inputs.sources"
)
public class FilePathNode extends BaseNode {
    
    // --- 节点属性 ---
    private String filePath = ""; // 文件路径
    private boolean mustExist = false; // 文件是否必须存在
    private String fileFilter = "*.*"; // 文件过滤器
    private boolean isDirectory = false; // 是否为目录
    
    // --- 输出端口ID ---
    private static final String OUTPUT_PATH_ID = "output_path";
    private static final String OUTPUT_EXISTS_ID = "output_exists";
    private static final String OUTPUT_FILENAME_ID = "output_filename";
    private static final String OUTPUT_EXTENSION_ID = "output_extension";
    private static final String OUTPUT_DIRECTORY_ID = "output_directory";
    
    /**
     * 构造一个新的文件路径节点
     */
    public FilePathNode() {
        // 使用新的分类命名 - inputs.sources.file_path
        super(UUID.randomUUID(), "inputs.sources.file_path");
        
        // 创建输出端口
        IPort pathOutput = new BasePort(OUTPUT_PATH_ID, "Path", 
                "The full file or directory path", NodeDataType.FILE_PATH, this);
        addOutputPort(pathOutput);
        
        IPort existsOutput = new BasePort(OUTPUT_EXISTS_ID, "Exists", 
                "Whether the file or directory exists", NodeDataType.BOOLEAN, this);
        addOutputPort(existsOutput);
        
        IPort filenameOutput = new BasePort(OUTPUT_FILENAME_ID, "Filename", 
                "The filename without extension", NodeDataType.STRING, this);
        addOutputPort(filenameOutput);
        
        IPort extensionOutput = new BasePort(OUTPUT_EXTENSION_ID, "Extension", 
                "The file extension", NodeDataType.STRING, this);
        addOutputPort(extensionOutput);
        
        IPort directoryOutput = new BasePort(OUTPUT_DIRECTORY_ID, "Directory", 
                "The directory containing the file", NodeDataType.STRING, this);
        addOutputPort(directoryOutput);
    }
    
    @Override
    public String getDescription() {
        return "Input a file or directory path";
    }
    
    @Override
    public String getDisplayName() {
        return "File Path";
    }
    
    /**
     * 节点的计算逻辑
     * @param context 执行上下文
     */
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 如果路径为空，则所有输出置空或置默认值
        if (filePath == null || filePath.isEmpty()) {
            outputValues.put(OUTPUT_PATH_ID, "");
            outputValues.put(OUTPUT_EXISTS_ID, false);
            outputValues.put(OUTPUT_FILENAME_ID, "");
            outputValues.put(OUTPUT_EXTENSION_ID, "");
            outputValues.put(OUTPUT_DIRECTORY_ID, "");
            return;
        }
        
        // 处理文件路径
        File file = new File(filePath);
        boolean exists = file.exists();
        
        // 获取文件名和扩展名
        String filename = "";
        String extension = "";
        String directory = "";
        
        try {
            Path path = Paths.get(filePath);
            Path fileName = path.getFileName();
            Path parent = path.getParent();
            
            if (fileName != null) {
                String fileNameStr = fileName.toString();
                int lastDotIndex = fileNameStr.lastIndexOf('.');
                
                if (lastDotIndex > 0) {
                    filename = fileNameStr.substring(0, lastDotIndex);
                    extension = fileNameStr.substring(lastDotIndex + 1);
                } else {
                    filename = fileNameStr;
                }
            }
            
            if (parent != null) {
                directory = parent.toString();
            }
        } catch (Exception e) {
            // 如果出现异常，保持默认空值
        }
        
        // 设置输出
        outputValues.put(OUTPUT_PATH_ID, filePath);
        outputValues.put(OUTPUT_EXISTS_ID, exists);
        outputValues.put(OUTPUT_FILENAME_ID, filename);
        outputValues.put(OUTPUT_EXTENSION_ID, extension);
        outputValues.put(OUTPUT_DIRECTORY_ID, directory);
    }
    
    /**
     * 打开文件选择对话框
     * 注意：实际实现应该由UI层负责调用此方法
     * @return 是否选择了文件
     */
    public boolean browseForFile() {
        // 这里仅是一个占位符方法
        // 实际的文件浏览功能应该由UI层实现
        // 并在选择文件后调用setFilePath方法
        return false;
    }
    
    /**
     * 设置文件路径
     * @param path 文件路径
     */
    public void setFilePath(String path) {
        if (path == null) {
            path = "";
        }
        
        if (!this.filePath.equals(path)) {
            this.filePath = path;
            markDirty();
        }
    }
    
    // --- Getters/Setters for Properties ---
    
    public String getFilePath() {
        return filePath;
    }
    
    public boolean isMustExist() {
        return mustExist;
    }
    
    public void setMustExist(boolean mustExist) {
        this.mustExist = mustExist;
    }
    
    public String getFileFilter() {
        return fileFilter;
    }
    
    public void setFileFilter(String fileFilter) {
        if (fileFilter == null || fileFilter.isEmpty()) {
            fileFilter = "*.*";
        }
        this.fileFilter = fileFilter;
    }
    
    public boolean isDirectory() {
        return isDirectory;
    }
    
    public void setDirectory(boolean directory) {
        this.isDirectory = directory;
    }
    
    /**
     * 检查文件是否存在
     * @return 文件是否存在
     */
    public boolean fileExists() {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }
        return new File(filePath).exists();
    }
    
    // --- 节点状态序列化 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("filePath", getFilePath());
        state.put("mustExist", isMustExist());
        state.put("fileFilter", getFileFilter());
        state.put("isDirectory", isDirectory());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("mustExist")) {
                Object must = stateMap.get("mustExist");
                if (must instanceof Boolean) {
                    setMustExist((Boolean) must);
                }
            }
            
            if (stateMap.containsKey("fileFilter")) {
                Object filter = stateMap.get("fileFilter");
                if (filter instanceof String) {
                    setFileFilter((String) filter);
                }
            }
            
            if (stateMap.containsKey("isDirectory")) {
                Object dir = stateMap.get("isDirectory");
                if (dir instanceof Boolean) {
                    setDirectory((Boolean) dir);
                }
            }
            
            // 最后设置文件路径，因为这会触发markDirty
            if (stateMap.containsKey("filePath")) {
                Object path = stateMap.get("filePath");
                if (path instanceof String) {
                    setFilePath((String) path);
                }
            }
        }
    }
} 