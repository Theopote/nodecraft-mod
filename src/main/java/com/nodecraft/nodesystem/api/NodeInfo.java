package com.nodecraft.nodesystem.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于标记节点类并提供元数据的注解
 * 被标记的类将被自动注册到节点库中
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface NodeInfo {
    /**
     * 节点ID，如不指定则使用类名转换为小写并添加"_node"后缀
     */
    String id() default "";
    
    /**
     * 节点显示名称，如不指定则使用类名的格式化版本
     */
    String displayName() default "";
    
    /**
     * 节点描述
     */
    String description() default "";
    
    /**
     * 节点分类ID，遵循格式：主分类.子分类
     * 例如："inputs.basic", "math.logic"等
     */
    String category();
} 