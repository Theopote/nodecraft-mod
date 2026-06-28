package com.nodecraft.nodesystem.preset;

import java.util.List;
import java.util.Map;

/**
 * Defines a configurable parameter for a preset.
 *
 * <p>Parameters allow users to customize preset behavior without editing the node graph.
 * Each parameter has a type, default value, constraints, and display properties.</p>
 */
public class PresetParameter {
    private final String id;
    private final String name;
    private final ParameterType type;
    private final Object defaultValue;
    private final Object minValue;
    private final Object maxValue;
    private final Object step;
    private final String description;
    private final String group;
    private final List<ParameterOption> options; // For dropdown type

    public PresetParameter(
            String id,
            String name,
            ParameterType type,
            Object defaultValue,
            Object minValue,
            Object maxValue,
            Object step,
            String description,
            String group,
            List<ParameterOption> options
    ) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.step = step;
        this.description = description;
        this.group = group;
        this.options = options;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ParameterType getType() {
        return type;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public Object getMinValue() {
        return minValue;
    }

    public Object getMaxValue() {
        return maxValue;
    }

    public Object getStep() {
        return step;
    }

    public String getDescription() {
        return description;
    }

    public String getGroup() {
        return group;
    }

    public List<ParameterOption> getOptions() {
        return options;
    }

    /**
     * Validates and constrains a parameter value.
     *
     * @param value the value to validate
     * @return the validated (and possibly clamped) value
     * @throws IllegalArgumentException if the value is invalid
     */
    public Object validateValue(Object value) {
        if (value == null) {
            return defaultValue;
        }

        switch (type) {
            case INTEGER:
                return validateInteger(value);
            case FLOAT:
                return validateFloat(value);
            case BOOLEAN:
                return validateBoolean(value);
            case STRING:
                return validateString(value);
            case DROPDOWN:
                return validateDropdown(value);
            case BLOCK_SELECTOR:
                return validateString(value); // Block ID as string
            case COLOR:
                return validateString(value); // Color as hex string
            case VECTOR3:
                return validateVector3(value);
            case ANGLE:
                return validateFloat(value);
            default:
                return value;
        }
    }

    private Object validateInteger(Object value) {
        int intValue;
        if (value instanceof Integer) {
            intValue = (Integer) value;
        } else if (value instanceof Number) {
            intValue = ((Number) value).intValue();
        } else {
            try {
                intValue = Integer.parseInt(value.toString());
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }

        if (minValue != null && intValue < ((Number) minValue).intValue()) {
            intValue = ((Number) minValue).intValue();
        }
        if (maxValue != null && intValue > ((Number) maxValue).intValue()) {
            intValue = ((Number) maxValue).intValue();
        }

        return intValue;
    }

    private Object validateFloat(Object value) {
        double doubleValue;
        if (value instanceof Double) {
            doubleValue = (Double) value;
        } else if (value instanceof Number) {
            doubleValue = ((Number) value).doubleValue();
        } else {
            try {
                doubleValue = Double.parseDouble(value.toString());
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }

        if (minValue != null && doubleValue < ((Number) minValue).doubleValue()) {
            doubleValue = ((Number) minValue).doubleValue();
        }
        if (maxValue != null && doubleValue > ((Number) maxValue).doubleValue()) {
            doubleValue = ((Number) maxValue).doubleValue();
        }

        return doubleValue;
    }

    private Object validateBoolean(Object value) {
        if (value instanceof Boolean) {
            return value;
        }
        return Boolean.parseBoolean(value.toString());
    }

    private Object validateString(Object value) {
        return value.toString();
    }

    private Object validateDropdown(Object value) {
        String strValue = value.toString();
        if (options != null) {
            boolean validOption = options.stream()
                    .anyMatch(opt -> opt.getValue().equals(strValue));
            if (!validOption) {
                return defaultValue;
            }
        }
        return strValue;
    }

    @SuppressWarnings("unchecked")
    private Object validateVector3(Object value) {
        if (value instanceof Map) {
            return value; // Already a map with x, y, z
        }
        return defaultValue;
    }

    /**
     * Represents an option for dropdown parameters.
     */
    public static class ParameterOption {
        private final String value;
        private final String label;

        public ParameterOption(String value, String label) {
            this.value = value;
            this.label = label;
        }

        public String getValue() {
            return value;
        }

        public String getLabel() {
            return label;
        }
    }
}
