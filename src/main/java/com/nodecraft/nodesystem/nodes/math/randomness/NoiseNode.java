package com.nodecraft.nodesystem.nodes.math.randomness;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.Random;
import java.util.UUID;

/**
 * Noise Node: 生成Perlin或Simplex噪声值，用于生成自然形状、地形或有机模式
 */
@NodeInfo(
    id = "math.randomness.noise",
    displayName = "噪声生成器",
    description = "生成Perlin或Simplex噪声值，用于生成自然形状、地形或有机模式",
    category = "math.randomness"
)
public class NoiseNode extends BaseNode {

    // --- 噪声类型枚举 ---
    private enum NoiseType {
        PERLIN,
        SIMPLEX
    }

    // --- 输入端口 IDs ---
    private static final String INPUT_X_ID = "input_x";
    private static final String INPUT_Y_ID = "input_y";
    private static final String INPUT_Z_ID = "input_z";
    private static final String INPUT_SCALE_ID = "input_scale";
    private static final String INPUT_OCTAVES_ID = "input_octaves";
    private static final String INPUT_PERSISTENCE_ID = "input_persistence";
    private static final String INPUT_LACUNARITY_ID = "input_lacunarity";
    private static final String INPUT_SEED_ID = "input_seed";
    private static final String INPUT_NOISE_TYPE_ID = "input_noise_type";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_NOISE_VALUE_ID = "output_noise_value";
    private static final String OUTPUT_NOISE_COLOR_ID = "output_noise_color";

    // 噪声生成器相关
    private Random random = new Random();
    private long currentSeed = 0;
    private PerlinNoise perlinNoise;
    private SimplexNoise simplexNoise;

    // --- 构造函数 ---
    public NoiseNode() {
        super(UUID.randomUUID(), "math.randomness.noise");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_X_ID, "X", "X坐标", NodeDataType.DOUBLE, this));
        addInputPort(new BasePort(INPUT_Y_ID, "Y", "Y坐标", NodeDataType.DOUBLE, this));
        addInputPort(new BasePort(INPUT_Z_ID, "Z", "Z坐标 (可选)", NodeDataType.DOUBLE, this));
        addInputPort(new BasePort(INPUT_SCALE_ID, "Scale", "噪声缩放（较大的值=更平滑）", NodeDataType.DOUBLE, this));
        addInputPort(new BasePort(INPUT_OCTAVES_ID, "Octaves", "叠加的噪声层数（更多=更多细节）", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_PERSISTENCE_ID, "Persistence", "每个八度下降的强度", NodeDataType.DOUBLE, this));
        addInputPort(new BasePort(INPUT_LACUNARITY_ID, "Lacunarity", "每个八度的频率增加", NodeDataType.DOUBLE, this));
        addInputPort(new BasePort(INPUT_SEED_ID, "Seed", "噪声种子", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_NOISE_TYPE_ID, "NoiseType", "噪声类型 (0=Perlin, 1=Simplex)", NodeDataType.INTEGER, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_NOISE_VALUE_ID, "Value", "噪声值 (-1到1)", NodeDataType.DOUBLE, this));
        addOutputPort(new BasePort(OUTPUT_NOISE_COLOR_ID, "Color", "噪声值对应的颜色", NodeDataType.COLOR, this));
        
        // 初始化默认噪声生成器
        initializeNoiseGenerators(0);
    }

    // 实现 getDescription 方法
    @Override
    public String getDescription() {
        return "生成噪声值，用于有机形状和地形";
    }

    // 实现 getDisplayName 方法
    @Override
    public String getDisplayName() {
        return "Noise (Perlin/Simplex)";
    }

    // --- 核心逻辑 ---
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        double x = getValueAsDouble(inputValues.get(INPUT_X_ID), 0.0);
        double y = getValueAsDouble(inputValues.get(INPUT_Y_ID), 0.0);
        double z = getValueAsDouble(inputValues.get(INPUT_Z_ID), 0.0);
        double scale = getValueAsDouble(inputValues.get(INPUT_SCALE_ID), 0.01);
        int octaves = getValueAsInt(inputValues.get(INPUT_OCTAVES_ID), 4);
        double persistence = getValueAsDouble(inputValues.get(INPUT_PERSISTENCE_ID), 0.5);
        double lacunarity = getValueAsDouble(inputValues.get(INPUT_LACUNARITY_ID), 2.0);
        long seed = getValueAsLong(inputValues.get(INPUT_SEED_ID), 0);
        int noiseTypeInt = getValueAsInt(inputValues.get(INPUT_NOISE_TYPE_ID), 0);
        
        NoiseType noiseType = (noiseTypeInt == 1) ? NoiseType.SIMPLEX : NoiseType.PERLIN;
        
        // 验证和限制输入值
        scale = Math.max(0.0001, scale); // 防止除以零或极小值
        octaves = Math.max(1, Math.min(octaves, 16)); // 限制八度数量
        persistence = Math.max(0.0, Math.min(persistence, 1.0));
        lacunarity = Math.max(1.0, lacunarity);
        
        // 如果种子改变了，重新初始化噪声生成器
        if (seed != currentSeed) {
            initializeNoiseGenerators(seed);
        }
        
        // 计算噪声值
        double noiseValue;
        if (noiseType == NoiseType.PERLIN) {
            noiseValue = perlinNoise.noise(x * scale, y * scale, z * scale, octaves, persistence, lacunarity);
        } else {
            noiseValue = simplexNoise.noise(x * scale, y * scale, z * scale, octaves, persistence, lacunarity);
        }
        
        // 将值限制在-1到1的范围内
        noiseValue = Math.max(-1.0, Math.min(noiseValue, 1.0));
        
        // 设置噪声值输出
        outputValues.put(OUTPUT_NOISE_VALUE_ID, noiseValue);
        
        // 生成对应的颜色（灰度，将-1到1映射到0到1范围）
        float normalizedValue = (float) ((noiseValue + 1.0) / 2.0);
        
        // 创建颜色值（这里使用灰度，但可以通过HSV来创建更丰富的颜色映射）
        int r = (int) (normalizedValue * 255);
        int g = (int) (normalizedValue * 255);
        int b = (int) (normalizedValue * 255);
        int color = (r << 16) | (g << 8) | b;
        
        outputValues.put(OUTPUT_NOISE_COLOR_ID, color);
    }
    
    // 初始化噪声生成器
    private void initializeNoiseGenerators(long seed) {
        currentSeed = seed;
        random.setSeed(seed);
        perlinNoise = new PerlinNoise(seed);
        simplexNoise = new SimplexNoise(seed);
    }
    
    /** Perlin噪声实现 */
    private class PerlinNoise {
        private int[] permutation;
        private static final int SIZE = 256;
        
        public PerlinNoise(long seed) {
            Random random = new Random(seed);
            permutation = new int[SIZE * 2];
            
            // 填充基本排列
            for (int i = 0; i < SIZE; i++) {
                permutation[i] = i;
            }
            
            // 打乱排列
            for (int i = 0; i < SIZE; i++) {
                int j = random.nextInt(SIZE);
                int temp = permutation[i];
                permutation[i] = permutation[j];
                permutation[j] = temp;
            }
            
            // 复制排列以避免溢出
            for (int i = 0; i < SIZE; i++) {
                permutation[SIZE + i] = permutation[i];
            }
        }
        
        // 生成分形噪声
        public double noise(double x, double y, double z, int octaves, double persistence, double lacunarity) {
            double total = 0;
            double frequency = 1;
            double amplitude = 1;
            double maxValue = 0;
            
            for (int i = 0; i < octaves; i++) {
                total += noise(x * frequency, y * frequency, z * frequency) * amplitude;
                maxValue += amplitude;
                amplitude *= persistence;
                frequency *= lacunarity;
            }
            
            return (total / maxValue);
        }
        
        // 基础Perlin噪声
        private double noise(double x, double y, double z) {
            // 找到单位立方体，包含这个点
            int X = (int) Math.floor(x) & 255;
            int Y = (int) Math.floor(y) & 255;
            int Z = (int) Math.floor(z) & 255;
            
            // 相对立方体原点的xyz
            x -= Math.floor(x);
            y -= Math.floor(y);
            z -= Math.floor(z);
            
            // 计算淡出曲线
            double u = fade(x);
            double v = fade(y);
            double w = fade(z);
            
            // 哈希坐标
            int A = permutation[X] + Y;
            int AA = permutation[A] + Z;
            int AB = permutation[A + 1] + Z;
            int B = permutation[X + 1] + Y;
            int BA = permutation[B] + Z;
            int BB = permutation[B + 1] + Z;
            
            // 混合结果
            return lerp(w, lerp(v, lerp(u, grad(permutation[AA], x, y, z),
                                       grad(permutation[BA], x - 1, y, z)),
                              lerp(u, grad(permutation[AB], x, y - 1, z),
                                 grad(permutation[BB], x - 1, y - 1, z))),
                     lerp(v, lerp(u, grad(permutation[AA + 1], x, y, z - 1),
                               grad(permutation[BA + 1], x - 1, y, z - 1)),
                        lerp(u, grad(permutation[AB + 1], x, y - 1, z - 1),
                           grad(permutation[BB + 1], x - 1, y - 1, z - 1))));
        }
        
        private double fade(double t) {
            return t * t * t * (t * (t * 6 - 15) + 10);
        }
        
        private double lerp(double t, double a, double b) {
            return a + t * (b - a);
        }
        
        private double grad(int hash, double x, double y, double z) {
            // 转换下4位为一个方向向量
            int h = hash & 15;
            double u = h < 8 ? x : y;
            double v = h < 4 ? y : (h == 12 || h == 14 ? x : z);
            return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
        }
    }
    
    /** Simplex噪声实现 */
    private class SimplexNoise {
        private int[] perm = new int[512];
        private static final double F3 = 1.0 / 3.0;
        private static final double G3 = 1.0 / 6.0;
        
        public SimplexNoise(long seed) {
            Random random = new Random(seed);
            for (int i = 0; i < 256; i++) {
                perm[i] = i;
            }
            
            for (int i = 0; i < 256; i++) {
                int j = random.nextInt(256);
                int temp = perm[i];
                perm[i] = perm[j];
                perm[j] = temp;
            }
            
            for (int i = 0; i < 256; i++) {
                perm[i + 256] = perm[i];
            }
        }
        
        // 生成分形噪声
        public double noise(double x, double y, double z, int octaves, double persistence, double lacunarity) {
            double total = 0;
            double frequency = 1;
            double amplitude = 1;
            double maxValue = 0;
            
            for (int i = 0; i < octaves; i++) {
                total += noise(x * frequency, y * frequency, z * frequency) * amplitude;
                maxValue += amplitude;
                amplitude *= persistence;
                frequency *= lacunarity;
            }
            
            return (total / maxValue);
        }
        
        // 基础Simplex噪声
        private double noise(double xin, double yin, double zin) {
            double n0, n1, n2, n3; // 四个单纯形角点的噪声贡献
            
            // 将输入点映射到单纯形空间
            double s = (xin + yin + zin) * F3;
            int i = fastfloor(xin + s);
            int j = fastfloor(yin + s);
            int k = fastfloor(zin + s);
            
            double t = (i + j + k) * G3;
            double X0 = i - t;
            double Y0 = j - t;
            double Z0 = k - t;
            double x0 = xin - X0;
            double y0 = yin - Y0;
            double z0 = zin - Z0;
            
            // 确定包含点的单纯形
            int i1, j1, k1; // 第二个角点的偏移量
            int i2, j2, k2; // 第三个角点的偏移量
            
            if (x0 >= y0) {
                if (y0 >= z0) { i1=1; j1=0; k1=0; i2=1; j2=1; k2=0; } // X Y Z 顺序
                else if (x0 >= z0) { i1=1; j1=0; k1=0; i2=1; j2=0; k2=1; } // X Z Y 顺序
                else { i1=0; j1=0; k1=1; i2=1; j2=0; k2=1; } // Z X Y 顺序
            } else {
                if (y0 < z0) { i1=0; j1=0; k1=1; i2=0; j2=1; k2=1; } // Z Y X 顺序
                else if (x0 < z0) { i1=0; j1=1; k1=0; i2=0; j2=1; k2=1; } // Y Z X 顺序
                else { i1=0; j1=1; k1=0; i2=1; j2=1; k2=0; } // Y X Z 顺序
            }
            
            // 相对于其他三个角点的坐标
            double x1 = x0 - i1 + G3;
            double y1 = y0 - j1 + G3;
            double z1 = z0 - k1 + G3;
            double x2 = x0 - i2 + 2.0 * G3;
            double y2 = y0 - j2 + 2.0 * G3;
            double z2 = z0 - k2 + 2.0 * G3;
            double x3 = x0 - 1.0 + 3.0 * G3;
            double y3 = y0 - 1.0 + 3.0 * G3;
            double z3 = z0 - 1.0 + 3.0 * G3;
            
            // 哈希坐标
            int ii = i & 255;
            int jj = j & 255;
            int kk = k & 255;
            
            // 计算每个角点的贡献
            double t0 = 0.6 - x0*x0 - y0*y0 - z0*z0;
            if (t0 < 0) n0 = 0.0;
            else {
                t0 *= t0;
                n0 = t0 * t0 * grad(perm[ii+perm[jj+perm[kk]]], x0, y0, z0);
            }
            
            double t1 = 0.6 - x1*x1 - y1*y1 - z1*z1;
            if (t1 < 0) n1 = 0.0;
            else {
                t1 *= t1;
                n1 = t1 * t1 * grad(perm[ii+i1+perm[jj+j1+perm[kk+k1]]], x1, y1, z1);
            }
            
            double t2 = 0.6 - x2*x2 - y2*y2 - z2*z2;
            if (t2 < 0) n2 = 0.0;
            else {
                t2 *= t2;
                n2 = t2 * t2 * grad(perm[ii+i2+perm[jj+j2+perm[kk+k2]]], x2, y2, z2);
            }
            
            double t3 = 0.6 - x3*x3 - y3*y3 - z3*z3;
            if (t3 < 0) n3 = 0.0;
            else {
                t3 *= t3;
                n3 = t3 * t3 * grad(perm[ii+1+perm[jj+1+perm[kk+1]]], x3, y3, z3);
            }
            
            // 将四个噪声值缩放到-1到1之间
            return 32.0 * (n0 + n1 + n2 + n3);
        }
        
        private int fastfloor(double x) {
            return x > 0 ? (int) x : (int) x - 1;
        }
        
        private double grad(int hash, double x, double y, double z) {
            int h = hash & 15;
            double u = h < 8 ? x : y;
            double v = h < 4 ? y : (h == 12 || h == 14 ? x : z);
            return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
        }
    }
    
    /** Helper method to safely convert an input object to double. */
    private double getValueAsDouble(Object value, double defaultValue) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return defaultValue;
    }
    
    /** Helper method to safely convert an input object to int. */
    private int getValueAsInt(Object value, int defaultValue) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }
    
    /** Helper method to safely convert an input object to long. */
    private long getValueAsLong(Object value, long defaultValue) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return defaultValue;
    }
} 