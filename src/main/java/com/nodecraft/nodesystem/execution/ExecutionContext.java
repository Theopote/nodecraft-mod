package com.nodecraft.nodesystem.execution;

import com.nodecraft.nodesystem.minecraft.PlayerAccessor;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Holds context information for a node graph execution.
 */
public class ExecutionContext {
    
    private final World world;
    @Nullable
    private final ServerPlayerEntity player;
    
    // 存储上下文变量
    private final Map<String, Object> variables = new HashMap<>();
    
    // 玩家数据访问器
    private PlayerAccessor playerAccessor;
    
    // 可以添加更多上下文信息，例如当前迭代索引 (用于循环)
    
    public ExecutionContext(World world, @Nullable ServerPlayerEntity player) {
        this.world = world;
        this.player = player;
    }
    
    public World getWorld() {
        return world;
    }
    
    @Nullable
    public ServerPlayerEntity getPlayer() {
        return player;
    }
    
    // 静态方法创建默认/空的上下文
    public static ExecutionContext createEmpty(World world) {
        return new ExecutionContext(world, null);
    }
    
    /**
     * 获取玩家数据访问器
     * @return 玩家数据访问器，如果不可用则返回null
     */
    public PlayerAccessor getPlayerAccessor() {
        return playerAccessor;
    }
    
    /**
     * 设置玩家数据访问器
     * @param playerAccessor 玩家数据访问器
     */
    public void setPlayerAccessor(PlayerAccessor playerAccessor) {
        this.playerAccessor = playerAccessor;
    }
    
    /**
     * 设置变量值
     * @param key 变量名
     * @param value 变量值
     */
    public void setVariable(String key, Object value) {
        variables.put(key, value);
    }
    
    /**
     * 获取变量值
     * @param key 变量名
     * @return 变量值，如果不存在则返回null
     */
    public Object getVariable(String key) {
        return variables.get(key);
    }
    
    /**
     * 检查变量是否存在
     * @param key 变量名
     * @return 变量是否存在
     */
    public boolean hasVariable(String key) {
        return variables.containsKey(key);
    }
    
    /**
     * 删除变量
     * @param key 变量名
     * @return 被删除的变量值，如果不存在则返回null
     */
    public Object removeVariable(String key) {
        return variables.remove(key);
    }
    
    /**
     * 清除所有变量
     */
    public void clearVariables() {
        variables.clear();
    }
    
    /**
     * 获取所有变量的只读视图
     * @return 变量映射的不可修改视图
     */
    public Map<String, Object> getVariables() {
        return java.util.Collections.unmodifiableMap(variables);
    }
}
