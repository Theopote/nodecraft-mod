package com.nodecraft.gui.recommendation;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nodecraft.core.NodeCraft;
import com.nodecraft.nodesystem.registry.NodeRegistry;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class NodeRecommendationRulesLoader {

    private static final String RESOURCE_PATH = "/nodecraft/node_recommendations.json";
    private static final Gson GSON = new Gson();

    private NodeRecommendationRulesLoader() {
    }

    public static NodeRecommendationRules load() {
        try (InputStream stream = openRulesStream()) {
            if (stream == null) {
                NodeCraft.LOGGER.warn("Node recommendation rules not found at {}", RESOURCE_PATH);
                return new NodeRecommendationRules();
            }

            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                    .getAsJsonObject();
            NodeRecommendationRules rules = GSON.fromJson(root, NodeRecommendationRules.class);
            if (rules == null) {
                return new NodeRecommendationRules();
            }

            coalesceStringListRules(root, rules);
            validateAgainstRegistry(rules);
            return rules;
        } catch (Exception e) {
            NodeCraft.LOGGER.error("Failed to load node recommendation rules: {}", e.getMessage(), e);
            return new NodeRecommendationRules();
        }
    }

    private static InputStream openRulesStream() {
        InputStream stream = NodeRecommendationRulesLoader.class.getResourceAsStream(RESOURCE_PATH);
        if (stream != null) {
            return stream;
        }

        stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(RESOURCE_PATH.substring(1));
        if (stream != null) {
            return stream;
        }

        try {
            Path devPath = Path.of("src/main/resources/nodecraft/node_recommendations.json");
            if (Files.isRegularFile(devPath)) {
                return Files.newInputStream(devPath);
            }
        } catch (Exception e) {
            NodeCraft.LOGGER.debug("Unable to read dev recommendation rules from filesystem: {}", e.getMessage());
        }
        return null;
    }

    private static void coalesceStringListRules(JsonObject root, NodeRecommendationRules rules) {
        if (root.has("sourceCategories") && rules.sourceCategories != null) {
            JsonObject categories = root.getAsJsonObject("sourceCategories");
            for (Map.Entry<String, NodeRecommendationRules.SourceCategoryRule> entry : rules.sourceCategories.entrySet()) {
                if (!categories.has(entry.getKey()) || entry.getValue().outputTypes == null) {
                    continue;
                }
                JsonObject categoryJson = categories.getAsJsonObject(entry.getKey());
                if (!categoryJson.has("outputTypes")) {
                    continue;
                }
                JsonObject outputTypesJson = categoryJson.getAsJsonObject("outputTypes");
                for (Map.Entry<String, NodeRecommendationRules.PortDirectionRule> typeEntry
                        : entry.getValue().outputTypes.entrySet()) {
                    if (!outputTypesJson.has(typeEntry.getKey())) {
                        continue;
                    }
                    JsonObject typeJson = outputTypesJson.getAsJsonObject(typeEntry.getKey());
                    NodeRecommendationRules.PortDirectionRule portRule = typeEntry.getValue();
                    portRule.downstream = mergeStringEntryList(typeJson, "downstream", portRule.downstream);
                    portRule.upstream = mergeStringEntryList(typeJson, "upstream", portRule.upstream);
                }
            }
        }

        if (root.has("outputTypes") && rules.outputTypes != null) {
            JsonObject outputTypesJson = root.getAsJsonObject("outputTypes");
            for (Map.Entry<String, NodeRecommendationRules.OutputTypeRule> entry : rules.outputTypes.entrySet()) {
                if (!outputTypesJson.has(entry.getKey())) {
                    continue;
                }
                JsonObject typeJson = outputTypesJson.getAsJsonObject(entry.getKey());
                NodeRecommendationRules.OutputTypeRule typeRule = entry.getValue();
                typeRule.downstream = mergeStringEntryList(typeJson, "downstream", typeRule.downstream);
                typeRule.upstream = mergeStringEntryList(typeJson, "upstream", typeRule.upstream);
            }
        }
    }

    private static List<NodeRecommendationRules.RuleEntry> mergeStringEntryList(
            JsonObject json,
            String key,
            List<NodeRecommendationRules.RuleEntry> existing) {
        if (!json.has(key)) {
            return existing == null ? List.of() : existing;
        }
        JsonArray array = json.getAsJsonArray(key);
        if (array.isEmpty() || !array.get(0).isJsonPrimitive()) {
            return existing == null ? List.of() : existing;
        }

        List<NodeRecommendationRules.RuleEntry> entries = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            JsonElement element = array.get(i);
            if (!element.isJsonPrimitive()) {
                continue;
            }
            NodeRecommendationRules.RuleEntry ruleEntry = new NodeRecommendationRules.RuleEntry();
            ruleEntry.nodeId = element.getAsString();
            ruleEntry.order = i * 10;
            entries.add(ruleEntry);
        }
        return entries;
    }

    private static void validateAgainstRegistry(NodeRecommendationRules rules) {
        NodeRegistry registry = NodeRegistry.getInstance();
        if (registry.getNodeCount() == 0) {
            return;
        }

        Set<String> knownIds = Set.copyOf(registry.getAllNodeIds());
        collectRuleNodeIds(rules, knownIds);
    }

    private static void collectRuleNodeIds(NodeRecommendationRules rules, Set<String> knownIds) {
        if (rules.sourceNodes != null) {
            for (NodeRecommendationRules.SourceNodeRule sourceRule : rules.sourceNodes.values()) {
                collectFromPortMap(sourceRule.outputs, knownIds);
                collectFromPortMap(sourceRule.inputs, knownIds);
            }
        }
        if (rules.sourceCategories != null) {
            for (NodeRecommendationRules.SourceCategoryRule categoryRule : rules.sourceCategories.values()) {
                if (categoryRule.outputTypes != null) {
                    for (NodeRecommendationRules.PortDirectionRule portRule : categoryRule.outputTypes.values()) {
                        collectFromDirectionRule(portRule, knownIds);
                    }
                }
            }
        }
        if (rules.outputTypes != null) {
            for (NodeRecommendationRules.OutputTypeRule typeRule : rules.outputTypes.values()) {
                collectEntries(typeRule.downstream, knownIds);
                collectEntries(typeRule.upstream, knownIds);
            }
        }
    }

    private static void collectFromPortMap(
            Map<String, NodeRecommendationRules.PortDirectionRule> portMap,
            Set<String> knownIds) {
        if (portMap == null) {
            return;
        }
        for (NodeRecommendationRules.PortDirectionRule rule : portMap.values()) {
            collectFromDirectionRule(rule, knownIds);
        }
    }

    private static void collectFromDirectionRule(
            NodeRecommendationRules.PortDirectionRule rule,
            Set<String> knownIds) {
        if (rule == null) {
            return;
        }
        collectEntries(rule.downstream, knownIds);
        collectEntries(rule.upstream, knownIds);
    }

    private static void collectEntries(List<NodeRecommendationRules.RuleEntry> entries, Set<String> knownIds) {
        if (entries == null) {
            return;
        }
        for (NodeRecommendationRules.RuleEntry entry : entries) {
            if (entry == null || entry.nodeId == null || entry.nodeId.isBlank()) {
                continue;
            }
            String normalized = entry.nodeId.toLowerCase();
            if (!knownIds.contains(normalized)) {
                NodeCraft.LOGGER.warn("Node recommendation rules reference unknown node id: {}", normalized);
            }
        }
    }
}
