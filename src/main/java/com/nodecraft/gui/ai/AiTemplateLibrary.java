package com.nodecraft.gui.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nodecraft.core.NodeCraft;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public final class AiTemplateLibrary {

    private static final DateTimeFormatter SAVE_TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final double MIN_TEMPLATE_SCORE = 7.5d;
    private static final double MIN_TEMPLATE_MARGIN = 2.5d;
    private static final Set<String> GENERIC_TEMPLATE_TOKENS = Set.of(
            "ai", "mock", "template", "generated", "locally", "local", "plan", "graph", "node", "nodes",
            "input", "output", "reference", "geometry", "math", "world", "execute", "preview", "apply",
            "changes", "coordinate", "coordinates", "value", "values", "vector", "point", "points",
            "create", "build", "generate", "make", "add", "place", "insert", "using", "known", "ports",
            "fallback", "enabled", "disabled", "current", "selected", "with", "from", "to",
            "生成", "创建", "构建", "添加", "放置", "模板", "本地", "节点", "图", "计划", "输出", "输入", "坐标", "预览"
    );

    private AiTemplateLibrary() {
    }

    public record Template(String name, String description, List<String> keywords, String dslJson) {
    }

    public record MatchResult(Template template, double score) {
    }

    public static Path resolveTemplateDir() {
        try {
            Path gameDir = FabricLoader.getInstance().getGameDir();
            return gameDir.resolve("nodecraft").resolve("config").resolve("ai_templates");
        } catch (IllegalStateException e) {
            return Paths.get("nodecraft", "config", "ai_templates");
        }
    }

    public static List<Template> loadAll(Path templateDir) {
        if (templateDir == null) {
            return List.of();
        }

        List<Template> templates = new ArrayList<>();
        try {
            Files.createDirectories(templateDir);
            try (var stream = Files.list(templateDir)) {
                stream.filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".json"))
                        .sorted(Comparator.comparing(path -> path.getFileName().toString().toLowerCase(Locale.ROOT)))
                        .forEach(path -> loadTemplate(path).ifPresent(templates::add));
            }
        } catch (Exception e) {
            NodeCraft.LOGGER.warn("[AI_TEMPLATE] Failed to load templates from {}: {}", templateDir, e.getMessage());
        }
        return templates;
    }

    public static Optional<MatchResult> findBestMatch(String prompt, List<Template> templates) {
        if (templates == null || templates.isEmpty()) {
            return Optional.empty();
        }

        String lowerPrompt = prompt == null ? "" : prompt.toLowerCase(Locale.ROOT);
        Set<String> promptTokens = tokenize(lowerPrompt);
        if (promptTokens.size() < 2) {
            return Optional.empty();
        }
        boolean generationIntent = hasAny(lowerPrompt, "generate", "create", "build", "make", "add", "place", "生成", "创建", "构建", "添加", "放置");
        boolean geometryIntent = hasAny(lowerPrompt, "geometry", "sphere", "box", "torus", "curve", "path", "几何", "球", "圆", "曲线", "路径");
        boolean spatialIntent = hasAny(lowerPrompt, "above", "position", "offset", "world", "player", "上方", "位置", "坐标", "玩家");

        Template best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        double secondBestScore = Double.NEGATIVE_INFINITY;
        int bestSharedSpecificTokens = 0;

        for (Template template : templates) {
            double score = scoreTemplate(template, lowerPrompt, promptTokens, generationIntent, geometryIntent, spatialIntent);
            int sharedSpecificTokens = countSharedSpecificTokens(promptTokens, template);
            if (score > bestScore) {
                secondBestScore = bestScore;
                bestScore = score;
                best = template;
                bestSharedSpecificTokens = sharedSpecificTokens;
            } else if (score > secondBestScore) {
                secondBestScore = score;
            }
        }

        int minSharedSpecificTokens = promptTokens.size() <= 4 ? 1 : 2;
        if (best == null
                || bestScore < MIN_TEMPLATE_SCORE
                || bestSharedSpecificTokens < minSharedSpecificTokens) {
            return Optional.empty();
        }

        if (secondBestScore > Double.NEGATIVE_INFINITY
                && (bestScore - secondBestScore) < MIN_TEMPLATE_MARGIN
                && bestSharedSpecificTokens < 3) {
            return Optional.empty();
        }
        return Optional.of(new MatchResult(best, bestScore));
    }

    public static Path saveTemplate(String suggestedName, String dslJson) throws IOException {
        Path templateDir = resolveTemplateDir();
        Files.createDirectories(templateDir);

        String baseName = sanitizeFileName(suggestedName);
        if (baseName.isBlank()) {
            baseName = "template_" + LocalDateTime.now().format(SAVE_TS);
        }

        String fileName = baseName.endsWith(".json") ? baseName : baseName + ".json";
        Path target = templateDir.resolve(fileName);
        int suffix = 1;
        while (Files.exists(target)) {
            String stem = fileName.substring(0, fileName.length() - 5);
            target = templateDir.resolve(stem + "_" + suffix + ".json");
            suffix++;
        }

        Files.writeString(target, dslJson == null ? "" : dslJson, StandardCharsets.UTF_8);
        return target;
    }

    private static Optional<Template> loadTemplate(Path path) {
        try {
            String json = Files.readString(path, StandardCharsets.UTF_8);
            if (json == null || json.isBlank()) {
                return Optional.empty();
            }

            String name = stripJsonSuffix(path.getFileName().toString());
            String description = "Local template";
            Set<String> keywords = new HashSet<>();
            keywords.addAll(tokenize(name.toLowerCase(Locale.ROOT)));

            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            if (root.has("description") && root.get("description").isJsonPrimitive()) {
                description = root.get("description").getAsString();
                keywords.addAll(tokenize(description.toLowerCase(Locale.ROOT)));
            }
            if (root.has("nodes") && root.get("nodes").isJsonArray()) {
                JsonArray nodes = root.getAsJsonArray("nodes");
                for (JsonElement nodeElement : nodes) {
                    if (!nodeElement.isJsonObject()) {
                        continue;
                    }
                    JsonObject nodeObj = nodeElement.getAsJsonObject();
                    if (nodeObj.has("type") && nodeObj.get("type").isJsonPrimitive()) {
                        String typeId = nodeObj.get("type").getAsString();
                        keywords.addAll(tokenize(typeId.toLowerCase(Locale.ROOT).replace('.', ' ').replace('/', ' ')));
                    }
                }
            }

            return Optional.of(new Template(name, description, new ArrayList<>(keywords), json));
        } catch (Exception e) {
            NodeCraft.LOGGER.warn("[AI_TEMPLATE] Skip invalid template {}: {}", path, e.getMessage());
            return Optional.empty();
        }
    }

    private static double scoreTemplate(
            Template template,
            String lowerPrompt,
            Set<String> promptTokens,
            boolean generationIntent,
            boolean geometryIntent,
            boolean spatialIntent
    ) {
        String haystack = (template.name() + " " + template.description() + " " + String.join(" ", template.keywords())).toLowerCase(Locale.ROOT);
        double score = 0.0d;

        for (String token : promptTokens) {
            if (token.isBlank()) {
                continue;
            }
            if (containsToken(template.name(), token)) {
                score += 5.0d;
            }
            if (containsToken(template.description(), token)) {
                score += 3.0d;
            }
            if (containsToken(haystack, token)) {
                score += 1.5d;
            }
            for (String keyword : template.keywords()) {
                if (containsToken(keyword, token)) {
                    score += 2.0d;
                }
            }
        }

        if (generationIntent && hasAny(haystack, "generate", "create", "bake", "output", "生成", "创建", "输出")) {
            score += 2.0d;
        }
        if (geometryIntent && hasAny(haystack, "geometry", "sphere", "box", "torus", "curve", "path", "几何", "球", "环", "路径")) {
            score += 3.0d;
        }
        if (spatialIntent && hasAny(haystack, "player", "position", "above", "center", "玩家", "位置", "坐标", "上方")) {
            score += 2.0d;
        }

        return score;
    }

    private static boolean containsToken(String text, String token) {
        if (text == null || token == null || token.isBlank()) {
            return false;
        }
        return text.toLowerCase(Locale.ROOT).contains(token.toLowerCase(Locale.ROOT));
    }

    private static boolean hasAny(String text, String... keywords) {
        if (text == null || text.isBlank() || keywords == null) {
            return false;
        }
        String lower = text.toLowerCase(Locale.ROOT);
        for (String keyword : keywords) {
            if (keyword != null && !keyword.isBlank() && lower.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private static Set<String> tokenize(String text) {
        Set<String> tokens = new HashSet<>();
        if (text == null || text.isBlank()) {
            return tokens;
        }

        String normalized = text.toLowerCase(Locale.ROOT)
                .replace('_', ' ')
                .replace('-', ' ')
                .replace('/', ' ')
                .replace('.', ' ')
                .replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}\\u4e00-\\u9fff\\u3040-\\u30ff\\uac00-\\ud7af]+", " ");
        for (String part : normalized.split("\\s+")) {
            if (part.length() >= 2) {
                tokens.add(part);
            }
        }
        return tokens;
    }

    private static int countSharedSpecificTokens(Set<String> promptTokens, Template template) {
        if (promptTokens == null || promptTokens.isEmpty() || template == null) {
            return 0;
        }

        Set<String> templateTokens = new HashSet<>();
        templateTokens.addAll(tokenize(template.name()));
        templateTokens.addAll(tokenize(template.description()));
        if (template.keywords() != null) {
            for (String keyword : template.keywords()) {
                templateTokens.addAll(tokenize(keyword));
            }
        }

        int count = 0;
        for (String token : promptTokens) {
            if (token == null || token.isBlank() || GENERIC_TEMPLATE_TOKENS.contains(token)) {
                continue;
            }
            if (templateTokens.contains(token) && !GENERIC_TEMPLATE_TOKENS.contains(token)) {
                count++;
            }
        }
        return count;
    }

    private static String stripJsonSuffix(String name) {
        if (name == null) {
            return "template";
        }
        String lower = name.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".json")) {
            return name.substring(0, name.length() - 5);
        }
        return name;
    }

    private static String sanitizeFileName(String raw) {
        if (raw == null) {
            return "";
        }
        String sanitized = raw.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9_\\-]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_+|_+$", "");
        if (sanitized.length() > 64) {
            return sanitized.substring(0, 64);
        }
        return sanitized;
    }
}
