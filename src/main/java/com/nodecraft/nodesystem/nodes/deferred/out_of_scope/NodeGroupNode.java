package com.nodecraft.nodesystem.nodes.deferred.out_of_scope;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Node Group 节点: 将一组节点封装为一个可重用的组件
 * 这是一个高级节点，允许创建自定义的复合节点
 */
@NodeInfo(
    id = "deferred.out_of_scope.node_group",
    displayName = "节点组",
    description = "将一组节点封装为一个可重用的组件",
    category = "deferred.out_of_scope"
)
public class NodeGroupNode extends BaseNode {
    
    // --- 节点属性 ---
    private String groupName = "Node Group"; // 节点组名称
    private String description = "自定义节点组"; // 描述
    private String version = "1.0"; // 版本
    private String author = ""; // 作者
    
    // --- 内部状态 ---
    private UUID groupId = UUID.randomUUID(); // 节点组唯一ID
    private List<UUID> containedNodes = new ArrayList<>(); // 包含的节点ID列表
    private Map<String, Object> groupMetadata = new HashMap<>(); // 组元数据
    
    // 输入输出端口是动态的，由编辑器管理
    private List<PortMappingInfo> inputMappings = new ArrayList<>(); // 输入端口映射
    private List<PortMappingInfo> outputMappings = new ArrayList<>(); // 输出端口映射
    
    /**
     * 构造一个新的节点组节点
     */
    public NodeGroupNode() {
        super(UUID.randomUUID(), "deferred.out_of_scope.node_group");
        
        // 节点组的端口是动态的，在编辑器中配置
        // 此构造函数创建一个空的节点组，端口将在以后添加
    }
    
    /**
     * 节点的计算逻辑
     * 对于节点组，这个方法通常不会被直接执行，而是由编辑器管理执行包含的节点
     */
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 节点组本身不进行处理，而是由编辑器管理其内部节点的执行
        // 这里仅提供一个占位实现
        
        // 复制输入值到输出（仅用于测试）
        for (PortMappingInfo mapping : outputMappings) {
            // 寻找对应的输入映射
            for (PortMappingInfo inputMapping : inputMappings) {
                if (inputMapping.getInternalPortId().equals(mapping.getInternalPortId())) {
                    // 复制对应的输入值到输出
                    Object value = inputValues.get(inputMapping.getExternalPortId());
                    if (value != null) {
                        outputValues.put(mapping.getExternalPortId(), value);
                    }
                    break;
                }
            }
        }
    }
    
    /**
     * 添加输入端口映射
     * @param externalPortId 外部端口ID
     * @param externalPortName 外部端口名称
     * @param externalPortType 外部端口类型
     * @param internalNodeId 内部节点ID
     * @param internalPortId 内部端口ID
     */
    public void addInputMapping(String externalPortId, String externalPortName, 
                               NodeDataType externalPortType, UUID internalNodeId, 
                               String internalPortId) {
        // 创建映射信息
        PortMappingInfo mapping = new PortMappingInfo(
            externalPortId, externalPortName, externalPortType, 
            internalNodeId, internalPortId);
        
        // 添加到映射列表
        inputMappings.add(mapping);
        
        // 创建对应的输入端口
        addInputPort(new BasePort(externalPortId, externalPortName, 
                "输入端口", externalPortType, this));
        
        markDirty();
    }
    
    /**
     * 添加输出端口映射
     * @param externalPortId 外部端口ID
     * @param externalPortName 外部端口名称
     * @param externalPortType 外部端口类型
     * @param internalNodeId 内部节点ID
     * @param internalPortId 内部端口ID
     */
    public void addOutputMapping(String externalPortId, String externalPortName, 
                                NodeDataType externalPortType, UUID internalNodeId, 
                                String internalPortId) {
        // 创建映射信息
        PortMappingInfo mapping = new PortMappingInfo(
            externalPortId, externalPortName, externalPortType, 
            internalNodeId, internalPortId);
        
        // 添加到映射列表
        outputMappings.add(mapping);
        
        // 创建对应的输出端口
        addOutputPort(new BasePort(externalPortId, externalPortName, 
                "输出端口", externalPortType, this));
        
        markDirty();
    }
    
    /**
     * 添加包含的节点ID
     * @param nodeId 节点ID
     */
    public void addContainedNode(UUID nodeId) {
        if (!containedNodes.contains(nodeId)) {
            containedNodes.add(nodeId);
            markDirty();
        }
    }
    
    /**
     * 移除包含的节点ID
     * @param nodeId 节点ID
     */
    public void removeContainedNode(UUID nodeId) {
        if (containedNodes.remove(nodeId)) {
            markDirty();
        }
        
        // 移除与该节点相关的端口映射
        inputMappings.removeIf(mapping -> mapping.getInternalNodeId().equals(nodeId));
        outputMappings.removeIf(mapping -> mapping.getInternalNodeId().equals(nodeId));
    }
    
    /**
     * 清除所有端口映射和包含的节点
     */
    public void clear() {
        // 清除包含的节点
        containedNodes.clear();
        
        // 清除端口映射
        inputMappings.clear();
        outputMappings.clear();
        
        // 清除输入输出端口
        while (!inputPorts.isEmpty()) {
            inputPorts.remove(0);
        }
        
        while (!outputPorts.isEmpty()) {
            outputPorts.remove(0);
        }
        
        markDirty();
    }
    
    // --- 内部类: 端口映射信息 ---
    
    /**
     * 表示端口映射的内部类
     */
    public static class PortMappingInfo {
        private final String externalPortId; // 外部端口ID
        private final String externalPortName; // 外部端口名称
        private final NodeDataType externalPortType; // 外部端口类型
        private final UUID internalNodeId; // 内部节点ID
        private final String internalPortId; // 内部端口ID
        
        public PortMappingInfo(String externalPortId, String externalPortName, 
                              NodeDataType externalPortType, UUID internalNodeId, 
                              String internalPortId) {
            this.externalPortId = externalPortId;
            this.externalPortName = externalPortName;
            this.externalPortType = externalPortType;
            this.internalNodeId = internalNodeId;
            this.internalPortId = internalPortId;
        }
        
        public String getExternalPortId() {
            return externalPortId;
        }
        
        public String getExternalPortName() {
            return externalPortName;
        }
        
        public NodeDataType getExternalPortType() {
            return externalPortType;
        }
        
        public UUID getInternalNodeId() {
            return internalNodeId;
        }
        
        public String getInternalPortId() {
            return internalPortId;
        }
    }
    
    // --- Getters/Setters for Properties ---
    
    public String getGroupName() {
        return groupName;
    }
    
    public void setGroupName(String groupName) {
        this.groupName = groupName;
        markDirty();
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
        markDirty();
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
        markDirty();
    }
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
        markDirty();
    }
    
    public UUID getGroupId() {
        return groupId;
    }
    
    public List<UUID> getContainedNodes() {
        return new ArrayList<>(containedNodes);
    }
    
    public List<PortMappingInfo> getInputMappings() {
        return new ArrayList<>(inputMappings);
    }
    
    public List<PortMappingInfo> getOutputMappings() {
        return new ArrayList<>(outputMappings);
    }
    
    public void setMetadata(String key, Object value) {
        groupMetadata.put(key, value);
        markDirty();
    }
    
    public Object getMetadata(String key) {
        return groupMetadata.get(key);
    }
    
    /**
     * 保存节点状态
     */
    @Override
    public @Nullable Object getNodeState() {
        Map<String, Object> state = new HashMap<>();
        
        // 保存基本属性
        state.put("groupName", groupName);
        state.put("description", description);
        state.put("version", version);
        state.put("author", author);
        state.put("groupId", groupId.toString());
        
        // 保存包含的节点ID
        List<String> nodeIds = new ArrayList<>();
        for (UUID id : containedNodes) {
            nodeIds.add(id.toString());
        }
        state.put("containedNodes", nodeIds);
        
        // 保存端口映射（需要将内部类转换为可序列化格式）
        List<Map<String, Object>> inputMappingsData = new ArrayList<>();
        for (PortMappingInfo mapping : inputMappings) {
            Map<String, Object> mappingData = new HashMap<>();
            mappingData.put("externalPortId", mapping.getExternalPortId());
            mappingData.put("externalPortName", mapping.getExternalPortName());
            mappingData.put("externalPortType", mapping.getExternalPortType().name());
            mappingData.put("internalNodeId", mapping.getInternalNodeId().toString());
            mappingData.put("internalPortId", mapping.getInternalPortId());
            inputMappingsData.add(mappingData);
        }
        state.put("inputMappings", inputMappingsData);
        
        List<Map<String, Object>> outputMappingsData = new ArrayList<>();
        for (PortMappingInfo mapping : outputMappings) {
            Map<String, Object> mappingData = new HashMap<>();
            mappingData.put("externalPortId", mapping.getExternalPortId());
            mappingData.put("externalPortName", mapping.getExternalPortName());
            mappingData.put("externalPortType", mapping.getExternalPortType().name());
            mappingData.put("internalNodeId", mapping.getInternalNodeId().toString());
            mappingData.put("internalPortId", mapping.getInternalPortId());
            outputMappingsData.add(mappingData);
        }
        state.put("outputMappings", outputMappingsData);
        
        // 保存元数据
        state.put("metadata", new HashMap<>(groupMetadata));
        
        return state;
    }
    
    /**
     * 恢复节点状态
     */
    @Override
    public void setNodeState(@Nullable Object state) {
        if (!(state instanceof Map)) {
            return;
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> stateMap = (Map<String, Object>) state;
        
        // 恢复基本属性
        if (stateMap.containsKey("groupName") && stateMap.get("groupName") instanceof String) {
            groupName = (String) stateMap.get("groupName");
        }
        
        if (stateMap.containsKey("description") && stateMap.get("description") instanceof String) {
            description = (String) stateMap.get("description");
        }
        
        if (stateMap.containsKey("version") && stateMap.get("version") instanceof String) {
            version = (String) stateMap.get("version");
        }
        
        if (stateMap.containsKey("author") && stateMap.get("author") instanceof String) {
            author = (String) stateMap.get("author");
        }
        
        if (stateMap.containsKey("groupId") && stateMap.get("groupId") instanceof String) {
            try {
                groupId = UUID.fromString((String) stateMap.get("groupId"));
            } catch (IllegalArgumentException e) {
                // 如果UUID无效，使用新的UUID
                groupId = UUID.randomUUID();
            }
        }
        
        // 恢复包含的节点ID
        containedNodes.clear();
        if (stateMap.containsKey("containedNodes") && stateMap.get("containedNodes") instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> nodeIds = (List<String>) stateMap.get("containedNodes");
            for (String idStr : nodeIds) {
                try {
                    containedNodes.add(UUID.fromString(idStr));
                } catch (IllegalArgumentException e) {
                    // 忽略无效的UUID
                }
            }
        }
        
        // 恢复端口映射
        inputMappings.clear();
        outputMappings.clear();
        
        // 清除现有的输入输出端口
        while (!inputPorts.isEmpty()) {
            inputPorts.remove(0);
        }
        
        while (!outputPorts.isEmpty()) {
            outputPorts.remove(0);
        }
        
        if (stateMap.containsKey("inputMappings") && stateMap.get("inputMappings") instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> inputMappingsData = (List<Map<String, Object>>) stateMap.get("inputMappings");
            
            for (Map<String, Object> mappingData : inputMappingsData) {
                try {
                    String externalPortId = (String) mappingData.get("externalPortId");
                    String externalPortName = (String) mappingData.get("externalPortName");
                    NodeDataType externalPortType = NodeDataType.valueOf((String) mappingData.get("externalPortType"));
                    UUID internalNodeId = UUID.fromString((String) mappingData.get("internalNodeId"));
                    String internalPortId = (String) mappingData.get("internalPortId");
                    
                    PortMappingInfo mapping = new PortMappingInfo(
                        externalPortId, externalPortName, externalPortType, 
                        internalNodeId, internalPortId);
                    
                    inputMappings.add(mapping);
                    
                    // 重新创建输入端口
                    addInputPort(new BasePort(externalPortId, externalPortName, 
                            "输入端口", externalPortType, this));
                } catch (Exception e) {
                    // 忽略无效的映射数据
                }
            }
        }
        
        if (stateMap.containsKey("outputMappings") && stateMap.get("outputMappings") instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> outputMappingsData = (List<Map<String, Object>>) stateMap.get("outputMappings");
            
            for (Map<String, Object> mappingData : outputMappingsData) {
                try {
                    String externalPortId = (String) mappingData.get("externalPortId");
                    String externalPortName = (String) mappingData.get("externalPortName");
                    NodeDataType externalPortType = NodeDataType.valueOf((String) mappingData.get("externalPortType"));
                    UUID internalNodeId = UUID.fromString((String) mappingData.get("internalNodeId"));
                    String internalPortId = (String) mappingData.get("internalPortId");
                    
                    PortMappingInfo mapping = new PortMappingInfo(
                        externalPortId, externalPortName, externalPortType, 
                        internalNodeId, internalPortId);
                    
                    outputMappings.add(mapping);
                    
                    // 重新创建输出端口
                    addOutputPort(new BasePort(externalPortId, externalPortName, 
                            "输出端口", externalPortType, this));
                } catch (Exception e) {
                    // 忽略无效的映射数据
                }
            }
        }
        
        // 恢复元数据
        groupMetadata.clear();
        if (stateMap.containsKey("metadata") && stateMap.get("metadata") instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> metadata = (Map<String, Object>) stateMap.get("metadata");
            groupMetadata.putAll(metadata);
        }
    }
} 
