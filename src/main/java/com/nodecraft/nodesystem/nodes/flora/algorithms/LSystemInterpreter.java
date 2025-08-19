package com.nodecraft.nodesystem.nodes.flora.algorithms;

import com.nodecraft.nodesystem.datatypes.LSystemRule;
import com.nodecraft.nodesystem.datatypes.PlantStructure;
import net.minecraft.util.math.BlockPos;
import org.joml.Vector3d;

import java.util.*;

/**
 * L-系统解释器，将L-系统字符串转换为三维植物结构
 * 使用乌龟几何算法实现
 */
public class LSystemInterpreter {
    
    /**
     * 乌龟状态，表示当前的位置、方向和参数
     */
    public static class TurtleState {
        public Vector3d position;           // 当前位置
        public Vector3d direction;          // 前进方向
        public Vector3d up;                 // 向上方向
        public Vector3d left;               // 向左方向
        public float segmentLength;         // 当前线段长度
        public float segmentWidth;          // 当前线段宽度
        public float angle;                 // 转向角度
        
        public TurtleState() {
            this.position = new Vector3d(0, 0, 0);
            this.direction = new Vector3d(0, 1, 0);  // 向上
            this.up = new Vector3d(0, 1, 0);
            this.left = new Vector3d(1, 0, 0);
            this.segmentLength = 1.0f;
            this.segmentWidth = 1.0f;
            this.angle = 25.0f; // 默认25度
        }
        
        public TurtleState(TurtleState other) {
            this.position = new Vector3d(other.position);
            this.direction = new Vector3d(other.direction);
            this.up = new Vector3d(other.up);
            this.left = new Vector3d(other.left);
            this.segmentLength = other.segmentLength;
            this.segmentWidth = other.segmentWidth;
            this.angle = other.angle;
        }
    }
    
    /**
     * 生成L-系统字符串
     * @param axiom 初始字符串
     * @param rules 生产规则列表
     * @param iterations 迭代次数
     * @param seed 随机种子
     * @return 生成的L-系统字符串
     */
    public static String generateString(String axiom, List<LSystemRule> rules, int iterations, long seed) {
        if (axiom == null || axiom.isEmpty()) return "";
        if (rules == null || rules.isEmpty()) return axiom;
        
        Random random = new Random(seed);
        String current = axiom;
        
        for (int i = 0; i < iterations; i++) {
            current = applyRules(current, rules, random);
        }
        
        return current;
    }
    
    /**
     * 对字符串应用L-系统规则
     */
    private static String applyRules(String input, List<LSystemRule> rules, Random random) {
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < input.length(); i++) {
            char symbol = input.charAt(i);
            String symbolStr = String.valueOf(symbol);
            
            // 查找匹配的规则
            LSystemRule applicableRule = null;
            for (LSystemRule rule : rules) {
                if (rule.getSymbol().equals(symbolStr)) {
                    // 检查概率
                    if (!rule.isProbabilistic() || random.nextFloat() < rule.getProbability()) {
                        applicableRule = rule;
                        break;
                    }
                }
            }
            
            if (applicableRule != null) {
                result.append(applicableRule.getProduction());
            } else {
                result.append(symbol);
            }
        }
        
        return result.toString();
    }
    
    /**
     * 解释L-系统字符串，生成植物结构
     * @param lSystemString L-系统字符串
     * @param initialPosition 初始位置
     * @param initialLength 初始线段长度
     * @param initialWidth 初始线段宽度
     * @param angle 转向角度（度）
     * @param lengthDecay 长度衰减因子
     * @param widthDecay 宽度衰减因子
     * @return 生成的植物结构
     */
    public static PlantStructure interpret(String lSystemString, 
                                         BlockPos initialPosition,
                                         float initialLength,
                                         float initialWidth,
                                         float angle,
                                         float lengthDecay,
                                         float widthDecay) {
        if (lSystemString == null || lSystemString.isEmpty()) {
            return new PlantStructure();
        }
        
        PlantStructure plant = new PlantStructure();
        TurtleState turtle = new TurtleState();
        Stack<TurtleState> stateStack = new Stack<>();
        
        // 设置初始参数
        turtle.position = new Vector3d(initialPosition.getX(), initialPosition.getY(), initialPosition.getZ());
        turtle.segmentLength = initialLength;
        turtle.segmentWidth = initialWidth;
        turtle.angle = (float) Math.toRadians(angle);
        
        // 解释字符串
        for (int i = 0; i < lSystemString.length(); i++) {
            char symbol = lSystemString.charAt(i);
            
            switch (symbol) {
                case 'F': // 前进并绘制线段
                case 'G': // 前进并绘制线段（另一种表示）
                    drawSegment(plant, turtle, true);
                    break;
                    
                case 'f': // 前进但不绘制
                    moveForward(turtle);
                    break;
                    
                case '+': // 左转
                    turnLeft(turtle);
                    break;
                    
                case '-': // 右转
                    turnRight(turtle);
                    break;
                    
                case '&': // 俯仰向下
                    pitchDown(turtle);
                    break;
                    
                case '^': // 俯仰向上
                    pitchUp(turtle);
                    break;
                    
                case '\\': // 滚转左
                    rollLeft(turtle);
                    break;
                    
                case '/': // 滚转右
                    rollRight(turtle);
                    break;
                    
                case '|': // 转向180度
                    turnAround(turtle);
                    break;
                    
                case '[': // 保存状态
                    stateStack.push(new TurtleState(turtle));
                    // 应用衰减
                    turtle.segmentLength *= lengthDecay;
                    turtle.segmentWidth *= widthDecay;
                    break;
                    
                case ']': // 恢复状态
                    if (!stateStack.isEmpty()) {
                        turtle = stateStack.pop();
                    }
                    break;
                    
                case 'L': // 添加叶子
                    addLeaf(plant, turtle);
                    break;
                    
                case 'W': // 添加花朵
                    addFlower(plant, turtle);
                    break;
                    
                case 'R': // 添加根
                    addRoot(plant, turtle);
                    break;
                    
                default:
                    // 忽略未知符号
                    break;
            }
        }
        
        return plant;
    }
    
    /**
     * 绘制线段
     */
    private static void drawSegment(PlantStructure plant, TurtleState turtle, boolean isTrunk) {
        Vector3d startPos = new Vector3d(turtle.position);
        moveForward(turtle);
        Vector3d endPos = new Vector3d(turtle.position);
        
        // 在起点和终点之间插值生成方块
        int steps = Math.max(1, (int) Math.ceil(startPos.distance(endPos)));
        for (int i = 0; i <= steps; i++) {
            float t = steps > 0 ? (float) i / steps : 0;
            Vector3d pos = new Vector3d();
            startPos.lerp(endPos, t, pos);
            
            BlockPos blockPos = new BlockPos((int) Math.round(pos.x), 
                                           (int) Math.round(pos.y), 
                                           (int) Math.round(pos.z));
            
            if (isTrunk) {
                plant.addTrunkBlock(blockPos, "minecraft:oak_log", turtle.segmentWidth);
            } else {
                plant.addBranchBlock(blockPos, "minecraft:oak_log", turtle.segmentWidth);
            }
        }
    }
    
    /**
     * 向前移动
     */
    private static void moveForward(TurtleState turtle) {
        Vector3d movement = new Vector3d(turtle.direction);
        movement.mul(turtle.segmentLength);
        turtle.position.add(movement);
    }
    
    /**
     * 左转
     */
    private static void turnLeft(TurtleState turtle) {
        rotateAroundAxis(turtle.direction, turtle.up, turtle.angle);
        updateLeftVector(turtle);
    }
    
    /**
     * 右转
     */
    private static void turnRight(TurtleState turtle) {
        rotateAroundAxis(turtle.direction, turtle.up, -turtle.angle);
        updateLeftVector(turtle);
    }
    
    /**
     * 俯仰向下
     */
    private static void pitchDown(TurtleState turtle) {
        rotateAroundAxis(turtle.direction, turtle.left, turtle.angle);
        rotateAroundAxis(turtle.up, turtle.left, turtle.angle);
    }
    
    /**
     * 俯仰向上
     */
    private static void pitchUp(TurtleState turtle) {
        rotateAroundAxis(turtle.direction, turtle.left, -turtle.angle);
        rotateAroundAxis(turtle.up, turtle.left, -turtle.angle);
    }
    
    /**
     * 滚转左
     */
    private static void rollLeft(TurtleState turtle) {
        rotateAroundAxis(turtle.up, turtle.direction, turtle.angle);
        updateLeftVector(turtle);
    }
    
    /**
     * 滚转右
     */
    private static void rollRight(TurtleState turtle) {
        rotateAroundAxis(turtle.up, turtle.direction, -turtle.angle);
        updateLeftVector(turtle);
    }
    
    /**
     * 转向180度
     */
    private static void turnAround(TurtleState turtle) {
        turtle.direction.negate();
        updateLeftVector(turtle);
    }
    
    /**
     * 绕轴旋转向量
     */
    private static void rotateAroundAxis(Vector3d vector, Vector3d axis, float angle) {
        // 使用罗德里格旋转公式
        Vector3d k = new Vector3d(axis).normalize();
        Vector3d v = new Vector3d(vector);
        
        float cosAngle = (float) Math.cos(angle);
        float sinAngle = (float) Math.sin(angle);
        
        Vector3d vCrossK = new Vector3d();
        v.cross(k, vCrossK);
        
        float vDotK = (float) v.dot(k);
        
        vector.set(v.mul(cosAngle)
                  .add(new Vector3d(vCrossK).mul(sinAngle))
                  .add(new Vector3d(k).mul(vDotK * (1 - cosAngle))));
    }
    
    /**
     * 更新左向量
     */
    private static void updateLeftVector(TurtleState turtle) {
        turtle.up.cross(turtle.direction, turtle.left);
        turtle.left.normalize();
    }
    
    /**
     * 添加叶子
     */
    private static void addLeaf(PlantStructure plant, TurtleState turtle) {
        BlockPos pos = new BlockPos((int) Math.round(turtle.position.x),
                                  (int) Math.round(turtle.position.y),
                                  (int) Math.round(turtle.position.z));
        plant.addLeafBlock(pos, "minecraft:oak_leaves");
    }
    
    /**
     * 添加花朵
     */
    private static void addFlower(PlantStructure plant, TurtleState turtle) {
        BlockPos pos = new BlockPos((int) Math.round(turtle.position.x),
                                  (int) Math.round(turtle.position.y),
                                  (int) Math.round(turtle.position.z));
        plant.addFlowerBlock(pos, "minecraft:poppy");
    }
    
    /**
     * 添加根
     */
    private static void addRoot(PlantStructure plant, TurtleState turtle) {
        BlockPos pos = new BlockPos((int) Math.round(turtle.position.x),
                                  (int) Math.round(turtle.position.y),
                                  (int) Math.round(turtle.position.z));
        plant.addRootBlock(pos, "minecraft:oak_log");
    }
} 