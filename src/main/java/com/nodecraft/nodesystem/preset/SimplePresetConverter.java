package com.nodecraft.nodesystem.preset;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Simple preset converter without SLF4J dependency
 */
public class SimplePresetConverter {

    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("NodeCraft Preset Converter (Simple Version)");
        System.out.println("=".repeat(60));
        System.out.println();

        try {
            Path presetDir = Paths.get("presets");
            Path existingJson = Paths.get("src/main/resources/nodecraft/graph_presets.json");
            Path outputJson = Paths.get("src/main/resources/nodecraft/graph_presets_updated.json");

            System.out.println("Preset directory: " + presetDir.toAbsolutePath());
            System.out.println("Existing JSON: " + existingJson.toAbsolutePath());
            System.out.println("Output JSON: " + outputJson.toAbsolutePath());
            System.out.println();

            if (!Files.exists(presetDir)) {
                System.err.println("ERROR: Preset directory does not exist!");
                System.exit(1);
            }

            // Use PresetFormatAdapter
            PresetFormatAdapter.generateGraphPresetsJson(
                presetDir,
                outputJson,
                existingJson
            );

            System.out.println();
            System.out.println("=".repeat(60));
            System.out.println("✓ Conversion complete!");
            System.out.println("=".repeat(60));
            System.out.println();
            System.out.println("Output file: " + outputJson.toAbsolutePath());
            System.out.println("File size: " + Files.size(outputJson) + " bytes");
            System.out.println();
            System.out.println("Next steps:");
            System.out.println("1. Review the output file");
            System.out.println("2. Copy to graph_presets.json:");
            System.out.println("   copy " + outputJson + " " + existingJson);
            System.out.println("3. Restart NodeCraft");

        } catch (Exception e) {
            System.err.println("ERROR: Conversion failed!");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
