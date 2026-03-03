package com.nodecraft.gui.utils;

import com.nodecraft.core.NodeCraft;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 管理节点图标的加载和渲染
 */
public class NodeIconManager {
    private static NodeIconManager instance;
    
    // 图标命名空间和路径
    private static final String ICON_NAMESPACE = "nodecraft";
    private static final String ICON_BASE_PATH = "textures/icons/nodes/";
    private static final String CATEGORY_PREFIX = "category_";
    
    // 存储已加载图标的映射
    private final Map<String, Integer> iconTextureMap = new HashMap<>();
    private final Map<String, Integer> categoryColorMap = new HashMap<>();
    
    // 默认图标ID
    private static final String DEFAULT_ICON = "default";
    
    private NodeIconManager() {
        // 私有构造函数确保单例
        initializeCategoryColors();
    }
    
    /**
     * 初始化分类颜色
     */
    private void initializeCategoryColors() {
        // 主分类颜色 - 作为备选方案，当图标文件不存在时使用
        categoryColorMap.put("inputs", 0xFF3498DB);     // 蓝色
        categoryColorMap.put("data", 0xFF9B59B6);       // 紫色
        categoryColorMap.put("math", 0xFFE74C3C);       // 红色
        categoryColorMap.put("spatial", 0xFF2ECC71);    // 绿色
        categoryColorMap.put("world", 0xFFF39C12);      // 橙色
        categoryColorMap.put("visualization", 0xFF1ABC9C); // 青色
        categoryColorMap.put("utilities", 0xFF7F8C8D);  // 灰色
    }
    
    /**
     * 获取单例实例
     */
    public static NodeIconManager getInstance() {
        if (instance == null) {
            instance = new NodeIconManager();
        }
        return instance;
    }
    
    /**
     * 初始化图标管理器
     */
    public void initialize() {
        NodeCraft.LOGGER.info("正在初始化节点图标管理器...");
        
        // 加载默认图标
        loadIcon(DEFAULT_ICON);
        
        // 预加载常用分类图标
        loadCategoryIcons();
        
        NodeCraft.LOGGER.info("节点图标管理器初始化完成，加载了 {} 个图标", iconTextureMap.size());
    }
    
    /**
     * 预加载分类图标
     */
    private void loadCategoryIcons() {
        // 主分类图标
        String[] mainCategories = {
            "inputs", "data", "math", "spatial", "world", "visualization", "utilities"
        };
        
        // 加载主分类图标
        for (String category : mainCategories) {
            // 尝试从对应分类文件夹加载图标
            loadCategoryIcon(category);
        }
    }
    
    /**
     * 加载分类图标
     * @param category 分类名称
     * @return 纹理ID
     */
    private int loadCategoryIcon(String category) {
        // 构造图标路径：分类文件夹 + category.png
        String iconPath = ICON_BASE_PATH + category + "/" + category + ".png";
        String iconId = CATEGORY_PREFIX + category;
        
        // 如果已加载，直接返回
        if (iconTextureMap.containsKey(iconId)) {
            return iconTextureMap.get(iconId);
        }
        
        // 尝试从资源加载
        int textureId = loadTextureFromResource(iconPath);
        
        // 如果加载失败，创建彩色纹理作为替代
        if (textureId == 0 && categoryColorMap.containsKey(category)) {
            textureId = createColorTexture(category, categoryColorMap.get(category));
        }
        
        // 存储并返回
        if (textureId != 0) {
            iconTextureMap.put(iconId, textureId);
        }
        
        return textureId;
    }
    
    /**
     * 加载节点图标
     * @param nodeId 节点ID
     * @param category 节点分类
     * @return 纹理ID
     */
    public int loadNodeIcon(String nodeId, String category) {
        // 提取主分类
        String mainCategory = category;
        if (category.contains(".")) {
            mainCategory = category.substring(0, category.indexOf("."));
        }
        
        // 构造图标路径：分类文件夹 + 节点ID + .png
        String iconPath = ICON_BASE_PATH + mainCategory + "/" + nodeId + ".png";
        
        // 如果已加载，直接返回
        if (iconTextureMap.containsKey(nodeId)) {
            return iconTextureMap.get(nodeId);
        }
        
        // 尝试从资源加载
        int textureId = loadTextureFromResource(iconPath);
        
        // 如果加载失败，返回分类图标
        if (textureId == 0) {
            return getTextureId(CATEGORY_PREFIX + mainCategory);
        }
        
        // 存储并返回
        iconTextureMap.put(nodeId, textureId);
        return textureId;
    }
    
    /**
     * 从资源加载纹理
     * @param resourcePath 资源路径
     * @return 纹理ID，加载失败返回0
     */
    private int loadTextureFromResource(String resourcePath) {
        try {
            // 构建资源标识符，使用Minecraft命名空间格式
            String fullPath = ICON_NAMESPACE + ":" + resourcePath;
            Identifier resourceId;
            
            try {
                // 尝试通过解析字符串创建，格式为"namespace:path"
                resourceId = Identifier.tryParse(fullPath);
                if (resourceId == null) {
                    NodeCraft.LOGGER.warn("无法解析资源标识符: {}", fullPath);
                    return 0;
                }
            } catch (Exception e) {
                NodeCraft.LOGGER.warn("创建资源标识符时出错: {}", e.getMessage());
                return 0;
            }
            
            // 尝试加载资源
            Optional<Resource> resourceOptional = MinecraftClient.getInstance().getResourceManager().getResource(resourceId);
            
            if (resourceOptional.isPresent()) {
                try (InputStream stream = resourceOptional.get().getInputStream()) {
                    // 加载图像
                    NativeImage image = NativeImage.read(stream);
                    int textureId;
                    try {
                        textureId = createTextureFromNativeImage(image);
                    } finally {
                        image.close();
                    }
                    
                    NodeCraft.LOGGER.debug("加载图标纹理: {} (纹理ID: {})", resourcePath, textureId);
                    return textureId;
                }
            } else {
                NodeCraft.LOGGER.debug("找不到图标资源: {}", resourceId);
                return 0;
            }
        } catch (IOException e) {
            NodeCraft.LOGGER.debug("加载图标时出错: {} - {}", resourcePath, e.getMessage());
            return 0;
        }
    }

    /**
     * 从 NativeImage 创建 OpenGL 纹理
     */
    private int createTextureFromNativeImage(NativeImage image) {
        int textureId = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        int width = image.getWidth();
        int height = image.getHeight();
        ByteBuffer pixels = BufferUtils.createByteBuffer(width * height * 4);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = image.getColorArgb(x, y);
                pixels.put((byte) ((argb >> 16) & 0xFF));
                pixels.put((byte) ((argb >> 8) & 0xFF));
                pixels.put((byte) (argb & 0xFF));
                pixels.put((byte) ((argb >> 24) & 0xFF));
            }
        }
        pixels.flip();

        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
        return textureId;
    }
    
    /**
     * 创建彩色纹理
     * @param id 纹理ID
     * @param color ARGB颜色
     * @return 纹理ID
     */
    private int createColorTexture(String id, int color) {
        // 创建彩色纹理
        int textureId = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        
        // 提取颜色分量
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        int a = (color >> 24) & 0xFF;
        
        // 创建纹理
        int width = 16;
        int height = 16;
        ByteBuffer pixels = BufferUtils.createByteBuffer(width * height * 4);
        
        for (int i = 0; i < width * height; i++) {
            pixels.put((byte)r);
            pixels.put((byte)g);
            pixels.put((byte)b);
            pixels.put((byte)a);
        }
        pixels.flip();
        
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
        
        NodeCraft.LOGGER.debug("创建颜色纹理: {} (纹理ID: {})", id, textureId);
        return textureId;
    }
    
    /**
     * 加载指定ID的图标
     * @param iconId 图标ID
     * @return 纹理ID
     */
    public int loadIcon(String iconId) {
        // 如果已加载，直接返回
        if (iconTextureMap.containsKey(iconId)) {
            return iconTextureMap.get(iconId);
        }
        
        // 构造图标路径
        String iconPath = ICON_BASE_PATH + iconId + ".png";
        
        // 尝试从资源加载
        int textureId = loadTextureFromResource(iconPath);
        
        // 如果加载失败，创建默认纹理
        if (textureId == 0) {
            if (DEFAULT_ICON.equals(iconId)) {
                // 默认图标使用灰色
                textureId = createColorTexture(iconId, 0xFF808080);
            } else {
                // 其他图标使用默认图标
                return getDefaultTextureId();
            }
        }
        
        // 存储并返回
        iconTextureMap.put(iconId, textureId);
        return textureId;
    }
    
    /**
     * 获取图标的GL纹理ID
     * @param iconId 图标ID
     * @return GL纹理ID，如果不存在则返回默认图标
     */
    public int getTextureId(String iconId) {
        // 如果是分类前缀，尝试加载分类图标
        if (iconId.startsWith(CATEGORY_PREFIX)) {
            String category = iconId.substring(CATEGORY_PREFIX.length());
            return loadCategoryIcon(category);
        }
        
        // 如果图标已存在，直接返回
        if (iconTextureMap.containsKey(iconId)) {
            return iconTextureMap.get(iconId);
        }
        
        // 尝试加载图标
        return loadIcon(iconId);
    }
    
    /**
     * 获取默认纹理ID
     */
    private int getDefaultTextureId() {
        if (!iconTextureMap.containsKey(DEFAULT_ICON)) {
            loadIcon(DEFAULT_ICON);
        }
        return iconTextureMap.getOrDefault(DEFAULT_ICON, 0);
    }
    
    /**
     * 根据分类ID获取合适的图标ID
     * @param categoryId 分类ID
     * @return 图标ID
     */
    public String getCategoryIconId(String categoryId) {
        // 如果有点号，取主分类部分
        if (categoryId.contains(".")) {
            categoryId = categoryId.substring(0, categoryId.indexOf("."));
        }
        return CATEGORY_PREFIX + categoryId;
    }
    
    /**
     * 清理资源
     */
    public void cleanup() {
        for (int textureId : iconTextureMap.values()) {
            // 删除纹理
            GL11.glDeleteTextures(textureId);
        }
        
        iconTextureMap.clear();
    }
} 