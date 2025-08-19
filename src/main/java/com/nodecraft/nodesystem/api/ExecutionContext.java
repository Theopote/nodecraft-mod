package com.nodecraft.nodesystem.api;

import java.util.Map;

/**
 * 节点执行上下文，提供节点执行期间的信息和服务
 */
public interface ExecutionContext {
    
    /**
     * 获取全局变量
     * @param key 变量键
     * @return 变量值，如果不存在返回null
     */
    Object getVariable(String key);
    
    /**
     * 设置全局变量
     * @param key 变量键
     * @param value 变量值
     */
    void setVariable(String key, Object value);
    
    /**
     * 获取所有全局变量
     * @return 变量映射
     */
    Map<String, Object> getAllVariables();
    
    /**
     * 设置执行是否成功
     * @param success 是否成功
     */
    void setSuccess(boolean success);
    
    /**
     * 获取执行是否成功
     * @return 是否成功
     */
    boolean isSuccess();
    
    /**
     * 设置错误消息
     * @param message 错误消息
     */
    void setErrorMessage(String message);
    
    /**
     * 获取错误消息
     * @return 错误消息
     */
    String getErrorMessage();
    
    /**
     * 设置执行结果
     * @param result 执行结果
     */
    void setResult(Object result);
    
    /**
     * 获取执行结果
     * @return 执行结果
     */
    Object getResult();
} 