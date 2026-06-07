package com.nodecraft.gui.utils;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.parser.SVGLoader;
import com.nodecraft.core.NodeCraft;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 管理节点图标的加载和渲染。
 * 支持 SVG 格式，运行时通过 JSVG 光栅化为 OpenGL 纹理。
 * <p>
 * 图标查找顺序（以 flow.control.branch 为例）：
 * 1. textures/icons/nodes/flow/control/branch.svg  （节点专属图标）
 * 2. textures/icons/nodes/flow/control.svg          （子分类图标）
 * 3. textures/icons/nodes/flow/flow.svg             （主分类图标）
 * 4. 彩色占位纹理
 */
public class NodeIconManager {

    private static NodeIconManager instance;

    private static final String ICON_NAMESPACE = "nodecraft";
    private static final String ICON_BASE_PATH  = "textures/icons/nodes/";
    /** 图标渲染分辨率（SVG 光栅化目标尺寸，px） */
    private static final int ICON_RENDER_SIZE = 64;

    /** 已加载图标缓存 <iconCacheKey → glTextureId> */
    private final Map<String, Integer> textureCache = new HashMap<>();
    /** 分类默认颜色（作为最终 fallback） */
    private final Map<String, Integer> categoryColors = new HashMap<>();

    private static final SVGLoader SVG_LOADER = new SVGLoader();

    // -----------------------------------------------------------------------
    //  Singleton
    // -----------------------------------------------------------------------

    private NodeIconManager() {
        initCategoryColors();
    }

    public static NodeIconManager getInstance() {
        if (instance == null) {
            instance = new NodeIconManager();
        }
        return instance;
    }

    // -----------------------------------------------------------------------
    //  Lifecycle
    // -----------------------------------------------------------------------

    public void initialize() {
        NodeCraft.LOGGER.info("正在初始化节点图标管理器…");
        NodeCraft.LOGGER.info("节点图标管理器就绪，已缓存 {} 个纹理", textureCache.size());
    }

    public void cleanup() {
        for (int id : textureCache.values()) {
            GL11.glDeleteTextures(id);
        }
        textureCache.clear();
        NodeCraft.LOGGER.info("节点图标管理器已清理");
    }

    // -----------------------------------------------------------------------
    //  Public API
    // -----------------------------------------------------------------------

    /**
     * 获取节点图标纹理 ID。
     *
     * @param nodeId   节点全局 ID，如 "flow.control.branch"
     * @param category 节点分类，如 "flow.control"
     * @return OpenGL 纹理 ID；不存在时返回分类颜色占位纹理
     */
    public int loadNodeIcon(String nodeId, String category) {
        // 1. 精确匹配：节点专属 SVG
        String nodePath = buildNodePath(nodeId);
        int texId = loadOrGet(nodeId, nodePath);
        if (texId != 0) return texId;

        // 2. 子分类图标
        String subCatPath = buildSubCategoryPath(category);
        String subCatKey  = "subcat:" + category;
        texId = loadOrGet(subCatKey, subCatPath);
        if (texId != 0) return texId;

        // 3. 主分类图标
        String mainCat     = extractMainCategory(category);
        String mainCatPath = ICON_BASE_PATH + mainCat + "/" + mainCat + ".svg";
        String mainCatKey  = "cat:" + mainCat;
        texId = loadOrGet(mainCatKey, mainCatPath);
        if (texId != 0) return texId;

        // 4. 彩色占位
        return getFallbackTexture(mainCat);
    }

    /**
     * 通用图标加载（用于分类列表等非节点场景）。
     *
     * @param iconId  任意 key，同时用作缓存键
     * @return OpenGL 纹理 ID
     */
    public int loadIcon(String iconId) {
        if (textureCache.containsKey(iconId)) {
            return textureCache.get(iconId);
        }
        String path = ICON_BASE_PATH + iconId + ".svg";
        int texId = loadSvgFromResource(path);
        if (texId != 0) {
            textureCache.put(iconId, texId);
        }
        return texId;
    }

    /**
     * 获取分类图标 ID（供 UI 调用）。
     */
    public String getCategoryIconId(String categoryId) {
        if (categoryId.contains(".")) {
            categoryId = categoryId.substring(0, categoryId.indexOf("."));
        }
        return "cat:" + categoryId;
    }

    // -----------------------------------------------------------------------
    //  Path helpers
    // -----------------------------------------------------------------------

    /**
     * 将节点 ID 转换为 SVG 文件路径。
     * 例：flow.control.branch → textures/icons/nodes/flow/control/branch.svg
     *     variable.get         → textures/icons/nodes/variable/get.svg
     */
    private String buildNodePath(String nodeId) {
        String[] parts = nodeId.split("\\.");
        if (parts.length == 1) {
            return ICON_BASE_PATH + parts[0] + "/" + parts[0] + ".svg";
        } else if (parts.length == 2) {
            return ICON_BASE_PATH + parts[0] + "/" + parts[1] + ".svg";
        } else {
            // 3 parts: main.sub.leaf
            return ICON_BASE_PATH + parts[0] + "/" + parts[1] + "/" + parts[2] + ".svg";
        }
    }

    /**
     * 子分类图标路径。
     * 例：flow.control → textures/icons/nodes/flow/control.svg
     *     variable     → textures/icons/nodes/variable/variable.svg
     */
    private String buildSubCategoryPath(String category) {
        if (category.contains(".")) {
            String[] parts = category.split("\\.", 2);
            return ICON_BASE_PATH + parts[0] + "/" + parts[1] + ".svg";
        }
        return ICON_BASE_PATH + category + "/" + category + ".svg";
    }

    private String extractMainCategory(String category) {
        if (category.contains(".")) {
            return category.substring(0, category.indexOf("."));
        }
        return category;
    }

    // -----------------------------------------------------------------------
    //  Texture loading
    // -----------------------------------------------------------------------

    /** 从缓存或资源加载纹理，0 表示不存在。 */
    private int loadOrGet(String cacheKey, String resourcePath) {
        if (textureCache.containsKey(cacheKey)) {
            return textureCache.get(cacheKey);
        }
        int texId = loadSvgFromResource(resourcePath);
        if (texId != 0) {
            textureCache.put(cacheKey, texId);
        }
        return texId;
    }

    /**
     * 从 Minecraft 资源系统加载 SVG 并光栅化为 OpenGL 纹理。
     *
     * @return 纹理 ID，失败返回 0
     */
    private int loadSvgFromResource(String resourcePath) {
        try {
            String full = ICON_NAMESPACE + ":" + resourcePath;
            Identifier id = Identifier.tryParse(full);
            if (id == null) return 0;

            Optional<Resource> res = MinecraftClient.getInstance()
                    .getResourceManager().getResource(id);
            if (res.isEmpty()) return 0;

            try (InputStream stream = res.get().getInputStream()) {
                SVGDocument doc = SVG_LOADER.load(stream, null, null, null);
                if (doc == null) return 0;

                BufferedImage img = rasterizeSvg(doc, ICON_RENDER_SIZE, ICON_RENDER_SIZE);
                int texId = uploadBufferedImage(img);
                NodeCraft.LOGGER.debug("已加载SVG图标: {} (texId={})", resourcePath, texId);
                return texId;
            }
        } catch (Exception e) {
            NodeCraft.LOGGER.debug("SVG图标加载失败: {} — {}", resourcePath, e.getMessage());
            return 0;
        }
    }

    /**
     * 将 SVGDocument 光栅化为 BufferedImage。
     */
    private BufferedImage rasterizeSvg(SVGDocument doc, int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING,     RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setComposite(AlphaComposite.Clear);
            g2d.fillRect(0, 0, width, height);
            g2d.setComposite(AlphaComposite.SrcOver);

            doc.render(null, g2d, new java.awt.geom.Rectangle2D.Float(0, 0, width, height));
        } finally {
            g2d.dispose();
        }
        return img;
    }

    /**
     * 将 BufferedImage 上传至 OpenGL，返回纹理 ID。
     */
    private int uploadBufferedImage(BufferedImage img) {
        int width  = img.getWidth();
        int height = img.getHeight();

        int[] pixels = img.getRGB(0, 0, width, height, null, 0, width);
        ByteBuffer buf = BufferUtils.createByteBuffer(width * height * 4);

        for (int argb : pixels) {
            buf.put((byte) ((argb >> 16) & 0xFF)); // R
            buf.put((byte) ((argb >>  8) & 0xFF)); // G
            buf.put((byte) (argb         & 0xFF)); // B
            buf.put((byte) ((argb >> 24) & 0xFF)); // A
        }
        buf.flip();

        int texId = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA,
                width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
        return texId;
    }

    // -----------------------------------------------------------------------
    //  Fallback
    // -----------------------------------------------------------------------

    private int getFallbackTexture(String mainCategory) {
        String key = "fallback:" + mainCategory;
        if (textureCache.containsKey(key)) {
            return textureCache.get(key);
        }
        int color = categoryColors.getOrDefault(mainCategory, 0xFF607D8B);
        int texId = createColorTexture(color);
        textureCache.put(key, texId);
        return texId;
    }

    private int createColorTexture(int argb) {
        int r = (argb >> 16) & 0xFF;
        int g = (argb >>  8) & 0xFF;
        int b =  argb        & 0xFF;
        int a = (argb >> 24) & 0xFF;

        ByteBuffer buf = BufferUtils.createByteBuffer(16 * 16 * 4);
        for (int i = 0; i < 16 * 16; i++) {
            buf.put((byte) r).put((byte) g).put((byte) b).put((byte) a);
        }
        buf.flip();

        int texId = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA,
                16, 16, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
        return texId;
    }

    private void initCategoryColors() {
        categoryColors.put("flow",       0xFFF59E0B);
        categoryColors.put("geometry",   0xFF3B82F6);
        categoryColors.put("input",      0xFF10B981);
        categoryColors.put("material",   0xFF8B5CF6);
        categoryColors.put("math",       0xFFEF4444);
        categoryColors.put("output",     0xFF06B6D4);
        categoryColors.put("pattern",    0xFFEAB308);
        categoryColors.put("reference",  0xFF6366F1);
        categoryColors.put("transform",  0xFFEC4899);
        categoryColors.put("utilities",  0xFF64748B);
        categoryColors.put("variable",   0xFFF97316);
        categoryColors.put("world",      0xFF14B8A6);
    }
}