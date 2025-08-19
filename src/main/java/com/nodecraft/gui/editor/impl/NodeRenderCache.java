package com.nodecraft.gui.editor.impl;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import imgui.ImGui;

/**
 * 节点渲染缓存管理器
 * 负责管理文本、颜色和类别相关的缓存以优化渲染性能
 */
public class NodeRenderCache {
    
    // 文本缓存系统，用于优化端口文本截断
    private static class TextCacheKey {
        final String text;
        final float maxWidth;

        TextCacheKey(String text, float maxWidth) {
            this.text = text;
            this.maxWidth = maxWidth;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            TextCacheKey that = (TextCacheKey) obj;
            return Float.compare(that.maxWidth, maxWidth) == 0 &&
                    (Objects.equals(text, that.text));
        }

        @Override
        public int hashCode() {
            int result = text != null ? text.hashCode() : 0;
            result = 31 * result + Float.floatToIntBits(maxWidth);
            return result;
        }
    }

    private static class TextSizeCache {
        final String truncatedText;
        final float textWidth;

        TextSizeCache(String truncatedText, float textWidth) {
            this.truncatedText = truncatedText;
            this.textWidth = textWidth;
        }
    }

    // 颜色缓存系统，用于优化颜色计算
    private static class ColorCacheKey {
        final int baseColor;
        final int adjustmentType; // 0=brightness, 1=alpha
        final int factorBits; // 使用整数存储浮点因子，避免浮点比较

        ColorCacheKey(int baseColor, int adjustmentType, float factor) {
            this.baseColor = baseColor;
            this.adjustmentType = adjustmentType;
            this.factorBits = Float.floatToIntBits(factor);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass() && !(obj instanceof ColorCacheKey)) return false;
            ColorCacheKey that = (ColorCacheKey) obj;
            return baseColor == that.baseColor &&
                    adjustmentType == that.adjustmentType &&
                    factorBits == that.factorBits;
        }

        @Override
        public int hashCode() {
            int result = baseColor;
            result = 31 * result + adjustmentType;
            result = 31 * result + factorBits;
            return result;
        }
    }

    // 预计算的类别颜色缓存
    public static class CategoryColorCache {
        public final int baseColor;
        public final int nodeBgColor;
        public final int borderColor;
        public final int darkBorderColor;
        public final int lightBgColor;

        public CategoryColorCache(int baseColor) {
            this.baseColor = baseColor;
            this.nodeBgColor = adjustBrightnessFast(baseColor, 0.7f);
            this.borderColor = adjustBrightnessFast(baseColor, 1.2f);
            this.darkBorderColor = adjustBrightnessFast(baseColor, 0.8f);
            this.lightBgColor = adjustBrightnessFast(baseColor, 1.1f);
        }
    }

    // 缓存映射
    private final Map<TextCacheKey, TextSizeCache> textTruncationCache = new ConcurrentHashMap<>();
    private final Map<String, Float> textSizeCache = new ConcurrentHashMap<>();
    private final Map<ColorCacheKey, Integer> colorAdjustmentCache = new ConcurrentHashMap<>();
    private final Map<String, CategoryColorCache> categoryColorCache = new ConcurrentHashMap<>();

    // 缓存清理计数器，避免缓存无限增长
    private int cacheCleanupCounter = 0;

    public NodeRenderCache() {
        initializeCategoryColorCache();
    }

    private void initializeCategoryColorCache() {
        // 预加载常用类别的颜色缓存
        String[] categories = {"Params", "Maths", "Sets", "Logic", "Geometry", "Minecraft", "Utilities", "General"};
        for (String category : categories) {
            int baseColor = NodeRenderConstants.getCategoryColor(category);
            categoryColorCache.put(category, new CategoryColorCache(baseColor));
        }
    }

    /**
     * 获取缓存的文本宽度
     */
    public float getCachedTextWidth(String text) {
        if (text == null || text.isEmpty()) return 0f;
        return textSizeCache.computeIfAbsent(text, t -> ImGui.calcTextSize(t).x);
    }

    /**
     * 获取截断文本的缓存结果
     */
    public String truncateTextWithEllipsisOptimized(String text, float maxWidthUnscaled) {
        if (text == null || text.isEmpty() || maxWidthUnscaled <= 0) return "";

        TextCacheKey cacheKey = new TextCacheKey(text, maxWidthUnscaled);
        TextSizeCache cached = textTruncationCache.get(cacheKey);
        if (cached != null) {
            return cached.truncatedText;
        }

        float textWidthUnscaled = getCachedTextWidth(text);
        if (textWidthUnscaled <= maxWidthUnscaled) {
            textTruncationCache.put(cacheKey, new TextSizeCache(text, textWidthUnscaled));
            return text;
        }

        String ellipsis = "..";
        float ellipsisWidthUnscaled = getCachedTextWidth(ellipsis);

        if (maxWidthUnscaled < ellipsisWidthUnscaled) {
            ellipsis = ".";
            ellipsisWidthUnscaled = getCachedTextWidth(ellipsis);
            if (maxWidthUnscaled < ellipsisWidthUnscaled) {
                textTruncationCache.put(cacheKey, new TextSizeCache("", 0f));
                return "";
            }
        }

        int left = 0;
        int right = text.length();
        int bestFit = 0;

        while (left <= right) {
            int mid = (left + right) / 2;
            String currentSub = text.substring(0, mid);
            String testText = currentSub + ellipsis;
            float testWidth = getCachedTextWidth(testText);

            if (testWidth <= maxWidthUnscaled) {
                bestFit = mid;
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }

        String result;
        if (bestFit == 0) {
            result = (ellipsisWidthUnscaled <= maxWidthUnscaled) ? ellipsis : "";
        } else {
            result = text.substring(0, bestFit) + ellipsis;
        }

        float resultWidth = getCachedTextWidth(result);
        textTruncationCache.put(cacheKey, new TextSizeCache(result, resultWidth));

        return result;
    }

    /**
     * 获取调整透明度后的颜色（缓存版本）
     */
    public int adjustAlphaCached(int color, float alpha) {
        ColorCacheKey key = new ColorCacheKey(color, 1, alpha);
        return colorAdjustmentCache.computeIfAbsent(key, k -> adjustAlphaFast(color, alpha));
    }

    /**
     * 获取调整亮度后的颜色（缓存版本）
     */
    public int adjustBrightnessCached(int color, float factor) {
        ColorCacheKey key = new ColorCacheKey(color, 0, factor);
        return colorAdjustmentCache.computeIfAbsent(key, k -> adjustBrightnessFast(color, factor));
    }

    /**
     * 获取类别颜色缓存
     */
    public CategoryColorCache getCategoryColorCache(String category) {
        return categoryColorCache.computeIfAbsent(category, cat -> {
            int baseColor = NodeRenderConstants.getCategoryColor(cat);
            return new CategoryColorCache(baseColor);
        });
    }

    /**
     * 清理缓存（如果需要）
     */
    public void cleanupCacheIfNeeded() {
        cacheCleanupCounter++;
        if (cacheCleanupCounter >= NodeRenderConstants.CACHE_CLEANUP_INTERVAL) {
            cacheCleanupCounter = 0;

            if (textTruncationCache.size() > NodeRenderConstants.MAX_CACHE_SIZE) {
                textTruncationCache.clear();
            }
            if (textSizeCache.size() > NodeRenderConstants.MAX_CACHE_SIZE) {
                textSizeCache.clear();
            }
            if (colorAdjustmentCache.size() > NodeRenderConstants.MAX_CACHE_SIZE) {
                colorAdjustmentCache.clear();
            }
        }
    }

    /**
     * 快速调整亮度
     */
    private static int adjustBrightnessFast(int color, float factor) {
        int alpha = (color >> 24) & 0xFF;
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        red = Math.min(255, (int)(red * factor));
        green = Math.min(255, (int)(green * factor));
        blue = Math.min(255, (int)(blue * factor));

        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    /**
     * 快速调整透明度
     */
    private static int adjustAlphaFast(int color, float alpha) {
        int alphaInt = Math.min(Math.max((int)(alpha * 255), 0), 255);
        return (color & 0x00FFFFFF) | (alphaInt << 24);
    }
} 