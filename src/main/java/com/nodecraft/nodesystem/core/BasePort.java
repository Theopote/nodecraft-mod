package com.nodecraft.nodesystem.core;

import com.nodecraft.nodesystem.api.INode;
import com.nodecraft.nodesystem.api.IPort;
import com.nodecraft.nodesystem.api.NodeDataType;

/**
 * 基础端口类，实现IPort接口
 */
public class BasePort implements IPort {
    
    private final String id;
    private final String displayName;
    private final String description;
    private final NodeDataType dataType;
    private final INode node;
    private boolean isConnected = false;
    private IPort connectedPort = null;
    private Object value;
    
    /**
     * 构造基础端口
     * @param id 端口ID
     * @param displayName 显示名称
     * @param description 端口描述
     * @param dataType 数据类型
     * @param node 父节点
     */
    public BasePort(String id, String displayName, String description, NodeDataType dataType, INode node) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.dataType = dataType;
        this.node = node;
        
        // 根据数据类型初始化默认值
        initializeDefaultValue();
    }
    
    /**
     * 根据数据类型初始化默认值
     */
    private void initializeDefaultValue() {
        switch (dataType) {
            case INTEGER:
                value = 0;
                break;
            case FLOAT:
                value = 0.0f;
                break;
            case DOUBLE:
                value = 0.0d;
                break;
            case BOOLEAN:
                value = false;
                break;
            case STRING:
                value = "";
                break;
            case VECTOR:
            case BLOCK_POS:
            case BLOCK_LIST:
            case CURVE:
            case COLOR:
            case ANY:
            default:
                value = null;
                break;
        }
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    @Override
    public NodeDataType getDataType() {
        return dataType;
    }
    
    @Override
    public boolean isInput() {
        return !isOutput();
    }
    
    /**
     * 判断此端口是否为输出端口
     * @return 如果是输出端口返回true，否则为false
     */
    public boolean isOutput() {
        return id.startsWith("output_");
    }
    
    @Override
    public boolean isConnected() {
        return isConnected;
    }
    
    @Override
    public INode getNode() {
        return node;
    }
    
    @Override
    public void setValue(Object newValue) {
        if (dataType.isCompatible(newValue)) {
            this.value = newValue;
        }
    }
    
    @Override
    public Object getValue() {
        return value;
    }
    
    @Override
    public boolean connectTo(IPort targetPort) {
        // 检查连接有效性
        if (targetPort == null || targetPort == this) {
            return false;
        }
        
        // 检查端口方向
        if (isInput() == targetPort.isInput()) {
            return false; // 两个输入/输出端口不能直接连接
        }
        
        // 检查数据类型兼容性
        if (dataType != NodeDataType.ANY && targetPort.getDataType() != NodeDataType.ANY && 
            dataType != targetPort.getDataType()) {
            return false;
        }
        
        // 断开现有连接
        if (isConnected) {
            disconnect();
        }
        
        // 如果目标端口已连接，断开它
        if (targetPort.isConnected()) {
            targetPort.disconnect();
        }
        
        // 创建连接
        isConnected = true;
        connectedPort = targetPort;
        
        // 双向连接
        if (targetPort instanceof BasePort) {
            ((BasePort) targetPort).isConnected = true;
            ((BasePort) targetPort).connectedPort = this;
        }
        
        return true;
    }
    
    @Override
    public void disconnect() {
        if (!isConnected || connectedPort == null) {
            return;
        }
        
        // 断开另一侧的连接
        if (connectedPort instanceof BasePort) {
            ((BasePort) connectedPort).isConnected = false;
            ((BasePort) connectedPort).connectedPort = null;
        }
        
        // 断开本侧的连接
        isConnected = false;
        connectedPort = null;
    }
} 