package com.nodecraft.nodesystem.preset;

import java.util.List;

/**
 * Documentation and learning materials for a preset.
 */
public class PresetDocumentation {
    private final String learningNotes;
    private final List<String> tips;
    private final List<String> relatedPresets;

    public PresetDocumentation(String learningNotes, List<String> tips, List<String> relatedPresets) {
        this.learningNotes = learningNotes;
        this.tips = tips != null ? tips : List.of();
        this.relatedPresets = relatedPresets != null ? relatedPresets : List.of();
    }

    public String getLearningNotes() {
        return learningNotes;
    }

    public List<String> getTips() {
        return tips;
    }

    public List<String> getRelatedPresets() {
        return relatedPresets;
    }
}
