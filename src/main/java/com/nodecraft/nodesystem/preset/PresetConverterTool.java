package com.nodecraft.nodesystem.preset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Command-line tool to generate graph_presets.json from new format presets.
 *
 * <p>Usage: Run this main method to convert all presets in the presets/ directory
 * and merge them with the existing graph_presets.json file.</p>
 */
public class PresetConverterTool {
    private static final Logger LOGGER = LoggerFactory.getLogger(PresetConverterTool.class);

    public static void main(String[] args) {
        try {
            // Paths
            Path presetDirectory = Path.of("presets");
            Path existingJson = Path.of("src/main/resources/nodecraft/graph_presets.json");
            Path outputJson = Path.of("src/main/resources/nodecraft/graph_presets_updated.json");

            LOGGER.info("Starting preset conversion...");
            LOGGER.info("Preset directory: {}", presetDirectory.toAbsolutePath());
            LOGGER.info("Existing JSON: {}", existingJson.toAbsolutePath());
            LOGGER.info("Output JSON: {}", outputJson.toAbsolutePath());

            // Generate merged file
            PresetFormatAdapter.generateGraphPresetsJson(
                presetDirectory,
                outputJson,
                existingJson
            );

            LOGGER.info("Conversion complete!");
            LOGGER.info("Review the file at: {}", outputJson.toAbsolutePath());
            LOGGER.info("If it looks good, replace the original graph_presets.json with this file.");

        } catch (IOException e) {
            LOGGER.error("Failed to generate graph_presets.json", e);
            System.exit(1);
        }
    }
}
