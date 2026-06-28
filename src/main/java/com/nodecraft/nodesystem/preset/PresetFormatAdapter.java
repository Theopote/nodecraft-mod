package com.nodecraft.nodesystem.preset;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nodecraft.gui.preset.GraphPresetRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Converts new format presets (preset.json) to old format (GraphPresetRules).
 *
 * <p>This adapter allows the new parametric preset system to work with the existing UI.</p>
 */
public class PresetFormatAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(PresetFormatAdapter.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Converts a new format preset to old format GraphPresetDefinition.
     *
     * @param newPreset the new preset definition
     * @return the old format preset definition
     */
    public static GraphPresetRules.GraphPresetDefinition convertToOldFormat(PresetDefinition newPreset) {
        GraphPresetRules.GraphPresetDefinition oldPreset = new GraphPresetRules.GraphPresetDefinition();

        // Basic metadata
        oldPreset.id = newPreset.getPresetId();
        oldPreset.displayName = newPreset.getMetadata().getName();
        oldPreset.description = newPreset.getMetadata().getDescription();
        oldPreset.kind = "composite"; // Mark as composite so it's usable

        // Convert nodes
        List<GraphPresetRules.PresetNode> oldNodes = new ArrayList<>();
        for (PresetGraph.PresetNodeDefinition nodeDef : newPreset.getGraph().getNodes()) {
            GraphPresetRules.PresetNode oldNode = new GraphPresetRules.PresetNode();
            oldNode.ref = nodeDef.getId();
            oldNode.typeId = nodeDef.getType();

            // Get position
            Map<String, Double> pos = nodeDef.getPosition();
            if (pos != null) {
                oldNode.x = pos.getOrDefault("x", 0.0).floatValue();
                oldNode.y = pos.getOrDefault("y", 0.0).floatValue();
            }

            oldNodes.add(oldNode);
        }
        oldPreset.nodes = oldNodes;

        // Convert connections
        List<GraphPresetRules.PresetConnection> oldConnections = new ArrayList<>();
        for (PresetGraph.PresetConnectionDefinition connDef : newPreset.getGraph().getConnections()) {
            GraphPresetRules.PresetConnection oldConn = new GraphPresetRules.PresetConnection();
            oldConn.fromRef = connDef.getFrom().getNode();
            oldConn.fromPort = mapPortName(connDef.getFrom().getPort(), true);  // output
            oldConn.toRef = connDef.getTo().getNode();
            oldConn.toPort = mapPortName(connDef.getTo().getPort(), false);     // input
            oldConnections.add(oldConn);
        }
        oldPreset.connections = oldConnections;

        return oldPreset;
    }

    /**
     * Scans a preset directory and converts all presets to old format.
     *
     * @param presetDirectory the directory containing preset.json files
     * @return GraphPresetRules with all converted presets
     */
    public static GraphPresetRules convertDirectory(Path presetDirectory) {
        GraphPresetRules rules = new GraphPresetRules();
        rules.version = 1;

        Map<String, List<GraphPresetRules.GraphPresetDefinition>> categoryMap = new HashMap<>();

        try (Stream<Path> paths = Files.walk(presetDirectory)) {
            List<Path> presetFiles = paths
                .filter(p -> p.getFileName().toString().equals("preset.json"))
                .toList();

            for (Path presetFile : presetFiles) {
                try {
                    PresetDefinition newPreset = PresetLoader.load(presetFile);
                    GraphPresetRules.GraphPresetDefinition oldPreset = convertToOldFormat(newPreset);

                    String category = newPreset.getMetadata().getCategory();
                    categoryMap.computeIfAbsent(category, k -> new ArrayList<>()).add(oldPreset);

                    LOGGER.debug("Converted preset: {}", newPreset.getPresetId());
                } catch (IOException e) {
                    LOGGER.error("Failed to convert preset: {}", presetFile, e);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to scan preset directory", e);
        }

        // Create categories
        List<GraphPresetRules.PresetCategory> categories = new ArrayList<>();
        for (Map.Entry<String, List<GraphPresetRules.GraphPresetDefinition>> entry : categoryMap.entrySet()) {
            GraphPresetRules.PresetCategory category = new GraphPresetRules.PresetCategory();
            category.id = entry.getKey();
            category.displayName = getCategoryDisplayName(entry.getKey());
            category.presets = entry.getValue();
            categories.add(category);
        }

        rules.categories = categories;
        return rules;
    }

    /**
     * Maps port names from new format to old format.
     *
     * <p>Old format expects ports prefixed with "output_" or "input_".</p>
     *
     * @param portName the port name from new format
     * @param isOutput true if this is an output port, false for input
     * @return mapped port name
     */
    private static String mapPortName(String portName, boolean isOutput) {
        if (portName == null || portName.isEmpty()) {
            return portName;
        }

        // If already prefixed, return as-is
        if (portName.startsWith("output_") || portName.startsWith("input_")) {
            return portName;
        }

        // Add appropriate prefix
        String prefix = isOutput ? "output_" : "input_";
        return prefix + portName;
    }

    /**
     * Gets a human-readable display name for a category ID.
     */
    private static String getCategoryDisplayName(String categoryId) {
        return switch (categoryId) {
            case "quickstart" -> "快速入门";
            case "building_elements" -> "建筑元素";
            case "architectural" -> "建筑结构";
            case "decorative" -> "装饰元素";
            case "styles" -> "建筑风格";
            default -> categoryId;
        };
    }

    /**
     * Merges new presets with existing graph_presets.json.
     *
     * @param existingRules existing rules from graph_presets.json
     * @param newPresetDirectory directory with new presets
     * @return merged rules
     */
    public static GraphPresetRules mergePresets(GraphPresetRules existingRules, Path newPresetDirectory) {
        GraphPresetRules newRules = convertDirectory(newPresetDirectory);

        // Create a map of existing categories
        Map<String, GraphPresetRules.PresetCategory> existingCategories = new HashMap<>();
        for (GraphPresetRules.PresetCategory cat : existingRules.categories) {
            existingCategories.put(cat.id, cat);
        }

        // Merge or add new categories
        for (GraphPresetRules.PresetCategory newCat : newRules.categories) {
            GraphPresetRules.PresetCategory existingCat = existingCategories.get(newCat.id);
            if (existingCat != null) {
                // Merge presets
                List<GraphPresetRules.GraphPresetDefinition> merged = new ArrayList<>(existingCat.presets);
                merged.addAll(newCat.presets);
                existingCat.presets = merged;
            } else {
                // Add new category
                existingCategories.put(newCat.id, newCat);
            }
        }

        GraphPresetRules result = new GraphPresetRules();
        result.version = existingRules.version;
        result.categories = new ArrayList<>(existingCategories.values());

        return result;
    }

    /**
     * Generates a new graph_presets.json file with converted presets.
     *
     * @param presetDirectory directory containing new format presets
     * @param outputPath path to write the merged graph_presets.json
     * @param existingJsonPath path to existing graph_presets.json (optional)
     */
    public static void generateGraphPresetsJson(Path presetDirectory, Path outputPath, Path existingJsonPath)
            throws IOException {

        GraphPresetRules rules;

        if (existingJsonPath != null && Files.exists(existingJsonPath)) {
            // Load existing
            String existingJson = Files.readString(existingJsonPath);
            GraphPresetRules existingRules = GSON.fromJson(existingJson, GraphPresetRules.class);

            // Merge with new
            rules = mergePresets(existingRules, presetDirectory);
            LOGGER.info("Merged new presets with existing graph_presets.json");
        } else {
            // Convert all new presets
            rules = convertDirectory(presetDirectory);
            LOGGER.info("Generated graph_presets.json from scratch");
        }

        // Write output
        String json = GSON.toJson(rules);
        Files.writeString(outputPath, json);

        LOGGER.info("Wrote graph_presets.json to: {}", outputPath);
        LOGGER.info("Total categories: {}", rules.categories.size());
        int totalPresets = rules.categories.stream()
            .mapToInt(c -> c.presets.size())
            .sum();
        LOGGER.info("Total presets: {}", totalPresets);
    }
}
