package com.nodecraft.nodesystem.preset;

import java.util.List;
import java.util.Map;

/**
 * Metadata about a preset including name, description, author, tags, and categorization.
 */
public class PresetMetadata {
    private final String name;
    private final Map<String, String> nameI18n;
    private final String description;
    private final Map<String, String> descriptionI18n;
    private final String author;
    private final List<String> tags;
    private final String category;
    private final PresetDifficulty difficulty;
    private final String estimatedBuildTime;
    private final int estimatedNodeCount;

    public PresetMetadata(
            String name,
            Map<String, String> nameI18n,
            String description,
            Map<String, String> descriptionI18n,
            String author,
            List<String> tags,
            String category,
            PresetDifficulty difficulty,
            String estimatedBuildTime,
            int estimatedNodeCount
    ) {
        this.name = name;
        this.nameI18n = nameI18n != null ? nameI18n : Map.of();
        this.description = description;
        this.descriptionI18n = descriptionI18n != null ? descriptionI18n : Map.of();
        this.author = author;
        this.tags = tags != null ? tags : List.of();
        this.category = category;
        this.difficulty = difficulty;
        this.estimatedBuildTime = estimatedBuildTime;
        this.estimatedNodeCount = estimatedNodeCount;
    }

    public String getName() {
        return name;
    }

    public String getName(String locale) {
        return nameI18n.getOrDefault(locale, name);
    }

    public String getDescription() {
        return description;
    }

    public String getDescription(String locale) {
        return descriptionI18n.getOrDefault(locale, description);
    }

    public String getAuthor() {
        return author;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getCategory() {
        return category;
    }

    public PresetDifficulty getDifficulty() {
        return difficulty;
    }

    public String getEstimatedBuildTime() {
        return estimatedBuildTime;
    }

    public int getEstimatedNodeCount() {
        return estimatedNodeCount;
    }

    /**
     * Checks if this preset matches a search query.
     *
     * @param query the search query (case-insensitive)
     * @return true if name, description, or any tag matches
     */
    public boolean matchesQuery(String query) {
        if (query == null || query.isEmpty()) {
            return true;
        }
        String lowerQuery = query.toLowerCase();
        return name.toLowerCase().contains(lowerQuery)
                || description.toLowerCase().contains(lowerQuery)
                || tags.stream().anyMatch(tag -> tag.toLowerCase().contains(lowerQuery));
    }

    /**
     * Checks if this preset has all specified tags.
     *
     * @param requiredTags tags that must all be present
     * @return true if all required tags are present
     */
    public boolean hasTags(List<String> requiredTags) {
        if (requiredTags == null || requiredTags.isEmpty()) {
            return true;
        }
        return tags.containsAll(requiredTags);
    }
}
