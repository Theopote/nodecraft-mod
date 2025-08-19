package com.nodecraft.gui.style;

import com.nodecraft.core.NodeCraft;

import imgui.ImGui;

/**
 * ImGui样式作用域类，用于自动管理ImGui样式栈。
 * 使用try-with-resources语法确保样式正确弹出，避免样式栈不平衡。
 */
public class ImGuiStyleScope implements AutoCloseable {
    private int varCount = 0;
    private int colorCount = 0;
    private boolean fontPushed = false;
    
    /**
     * 推入样式变量
     * @param styleVar 样式变量索引
     * @param value 样式值
     * @return 当前样式作用域实例，支持链式调用
     */
    public ImGuiStyleScope pushStyleVar(int styleVar, float value) {
        ImGui.pushStyleVar(styleVar, value);
        varCount++;
        return this;
    }
    
    /**
     * 推入样式变量（二维值）
     * @param styleVar 样式变量索引
     * @param value1 样式值1
     * @param value2 样式值2
     * @return 当前样式作用域实例，支持链式调用
     */
    public ImGuiStyleScope pushStyleVar(int styleVar, float value1, float value2) {
        ImGui.pushStyleVar(styleVar, value1, value2);
        varCount++;
        return this;
    }
    
    /**
     * 推入样式颜色
     * @param colorIdx 颜色索引
     * @param r 红色分量
     * @param g 绿色分量
     * @param b 蓝色分量
     * @param a 透明度分量
     * @return 当前样式作用域实例，支持链式调用
     */
    public ImGuiStyleScope pushStyleColor(int colorIdx, float r, float g, float b, float a) {
        ImGui.pushStyleColor(colorIdx, r, g, b, a);
        colorCount++;
        return this;
    }
    
    /**
     * 弹出样式变量
     * @param count 弹出数量
     * @return 当前样式作用域实例，支持链式调用
     */
    public ImGuiStyleScope popStyleVar(int count) {
        if (count <= 0) {
            return this;
        }
        
        // 确保不会尝试弹出超过已推入数量的样式变量
        int safeCount = Math.min(count, varCount);
        
        if (safeCount > 0) {
            try {
                ImGui.popStyleVar(safeCount);
                varCount -= safeCount;
                
                // 如果实际弹出数量小于请求弹出数量，记录警告
                if (safeCount < count) {
                    NodeCraft.LOGGER.warn("尝试弹出{}个样式变量，但只有{}个可弹出", count, safeCount);
                }
            } catch (Exception e) {
                NodeCraft.LOGGER.error("弹出样式变量时出错: {}", e.getMessage());
                // 重置计数以避免再次尝试弹出这些样式
                varCount = Math.max(0, varCount - safeCount);
            }
        }
        
        return this;
    }
    
    /**
     * 弹出样式颜色
     * @param count 弹出数量
     * @return 当前样式作用域实例，支持链式调用
     */
    public ImGuiStyleScope popStyleColor(int count) {
        if (count <= 0) {
            return this;
        }
        
        // 确保不会尝试弹出超过已推入数量的样式颜色
        int safeCount = Math.min(count, colorCount);
        
        if (safeCount > 0) {
            try {
                // 安全检查：由于ImGui Java绑定可能没有直接访问颜色栈大小的方法
                // 我们在此处使用当前追踪的colorCount值，但增加额外的检查机制
                if (safeCount > 0) {
                    ImGui.popStyleColor(safeCount);
                    colorCount -= safeCount;
                }
                
                // 如果实际弹出数量小于请求弹出数量，记录警告
                if (safeCount < count) {
                    NodeCraft.LOGGER.warn("尝试弹出{}个样式颜色，但只有{}个可弹出", count, safeCount);
                }
            } catch (Exception e) {
                NodeCraft.LOGGER.error("弹出样式颜色时出错: {}", e.getMessage());
                // 重置计数以避免再次尝试弹出这些样式
                colorCount = Math.max(0, colorCount - safeCount);
            }
        }
        
        return this;
    }
    
    /**
     * 获取当前变量计数
     * @return 变量计数
     */
    public int getVarCount() {
        return varCount;
    }
    
    /**
     * 获取当前颜色计数
     * @return 颜色计数
     */
    public int getColorCount() {
        return colorCount;
    }
    
    /**
     * 弹出所有已推入的样式变量和颜色
     * 用于在复用 ImGuiStyleScope 对象时清理之前的样式
     * @return 当前样式作用域实例，支持链式调用
     */
    public ImGuiStyleScope popAll() {
        // 弹出样式颜色 (先弹出颜色, 因为通常这些是后推入的)
        if (colorCount > 0) {
            try {
                ImGui.popStyleColor(colorCount);
                NodeCraft.LOGGER.debug("弹出所有 {} 个样式颜色", colorCount);
                colorCount = 0;
            } catch (Exception e) {
                NodeCraft.LOGGER.error("弹出所有样式颜色时出错: {}", e.getMessage());
                colorCount = 0; // 重置计数器，即使出错
            }
        }
        
        // 弹出样式变量
        if (varCount > 0) {
            try {
                ImGui.popStyleVar(varCount);
                NodeCraft.LOGGER.debug("弹出所有 {} 个样式变量", varCount);
                varCount = 0;
            } catch (Exception e) {
                NodeCraft.LOGGER.error("弹出所有样式变量时出错: {}", e.getMessage());
                varCount = 0; // 重置计数器，即使出错
            }
        }
        
        // 处理字体样式
        if (fontPushed) {
            try {
                ImGui.popFont();
                fontPushed = false;
                NodeCraft.LOGGER.debug("弹出字体样式");
            } catch (Exception e) {
                NodeCraft.LOGGER.error("弹出字体样式时出错: {}", e.getMessage());
                fontPushed = false; // 重置状态，即使出错
            }
        }
        
        return this;
    }
    
    @Override
    public void close() {
        // 为每种类型的样式操作添加独立的try-catch块，避免一处失败导致全部失败
        
        // 处理样式颜色 (先处理颜色, 通常颜色后推入所以先弹出)
        if (colorCount > 0) {
            try {
                // 逐个弹出样式颜色
                for (int i = 0; i < colorCount; i++) {
                    try {
                        ImGui.popStyleColor(1);
                        // NodeCraft.LOGGER.debug("成功弹出一个样式颜色，剩余: {}", (colorCount - i - 1));
                    } catch (Exception e) {
                        NodeCraft.LOGGER.error("弹出第{}个样式颜色时出错: {}", i + 1, e.getMessage());
                        break;
                    }
                }
                
                // NodeCraft.LOGGER.debug("样式颜色清理完成，重置计数器，原始值: {}", colorCount);
                colorCount = 0;
            } catch (Exception e) {
                NodeCraft.LOGGER.error("弹出样式颜色过程中发生严重错误: {}", e.getMessage());
                colorCount = 0;
            }
        }
        
        // 处理样式变量
        if (varCount > 0) {
            try {
                // 逐个弹出样式变量以增加稳定性
                // 这种方式虽然效率较低，但更能应对单个变量弹出失败的情况
                for (int i = 0; i < varCount; i++) {
                    try {
                        ImGui.popStyleVar(1);
                        // NodeCraft.LOGGER.debug("成功弹出一个样式变量，剩余: {}", (varCount - i - 1));
                    } catch (Exception e) {
                        NodeCraft.LOGGER.error("弹出第{}个样式变量时出错: {}", i + 1, e.getMessage());
                        break; // 一旦单个弹出失败，停止继续尝试
                    }
                }
                
                // 不管上面的循环成功与否，都重置计数器避免之后重复尝试
                // NodeCraft.LOGGER.debug("样式变量清理完成，重置计数器，原始值: {}", varCount);
                varCount = 0;
            } catch (Exception e) {
                // 捕获任何其他异常
                NodeCraft.LOGGER.error("弹出样式变量过程中发生严重错误: {}", e.getMessage());
                varCount = 0; // 重置计数器
            }
        }
        
        // 处理字体样式
        if (fontPushed) {
            try {
                ImGui.popFont();
                fontPushed = false;
                // NodeCraft.LOGGER.debug("成功弹出字体样式");
            } catch (Exception e) {
                NodeCraft.LOGGER.error("弹出字体样式时出错: {}", e.getMessage());
                fontPushed = false; // 重置状态
            }
        }
    }
} 