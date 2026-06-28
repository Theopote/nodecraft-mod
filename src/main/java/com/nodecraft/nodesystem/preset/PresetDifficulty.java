package com.nodecraft.nodesystem.preset;

/**
 * Difficulty levels for presets.
 */
public enum PresetDifficulty {
    BEGINNER("Beginner", "zh_CN:初学者"),
    INTERMEDIATE("Intermediate", "zh_CN:中级"),
    ADVANCED("Advanced", "zh_CN:高级"),
    EXPERT("Expert", "zh_CN:专家");

    private final String displayName;
    private final String i18nKey;

    PresetDifficulty(String displayName, String i18nKey) {
        this.displayName = displayName;
        this.i18nKey = i18nKey;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getI18nKey() {
        return i18nKey;
    }

    public static PresetDifficulty fromString(String value) {
        if (value == null) {
            return BEGINNER;
        }
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return BEGINNER;
        }
    }
}
