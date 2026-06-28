package com.nodecraft.nodesystem.preset;

import java.util.List;
import java.util.Map;

/**
 * Represents a complete preset definition loaded from JSON.
 *
 * <p>A preset encapsulates a reusable node graph template with configurable parameters.
 * Users can instantiate presets to quickly create common building patterns without
 * manually constructing node graphs.</p>
 */
public class PresetDefinition {
    private final String presetId;
    private final String version;
    private final String schemaVersion;
    private final PresetMetadata metadata;
    private final List<PresetParameter> parameters;
    private final PresetGraph graph;
    private final PresetDocumentation documentation;
    private final PresetThumbnails thumbnails;

    public PresetDefinition(
            String presetId,
            String version,
            String schemaVersion,
            PresetMetadata metadata,
            List<PresetParameter> parameters,
            PresetGraph graph,
            PresetDocumentation documentation,
            PresetThumbnails thumbnails
    ) {
        this.presetId = presetId;
        this.version = version;
        this.schemaVersion = schemaVersion;
        this.metadata = metadata;
        this.parameters = parameters;
        this.graph = graph;
        this.documentation = documentation;
        this.thumbnails = thumbnails;
    }

    public String getPresetId() {
        return presetId;
    }

    public String getVersion() {
        return version;
    }

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public PresetMetadata getMetadata() {
        return metadata;
    }

    public List<PresetParameter> getParameters() {
        return parameters;
    }

    public PresetGraph getGraph() {
        return graph;
    }

    public PresetDocumentation getDocumentation() {
        return documentation;
    }

    public PresetThumbnails getThumbnails() {
        return thumbnails;
    }

    /**
     * Gets a parameter by its ID.
     *
     * @param parameterId the parameter identifier
     * @return the parameter, or null if not found
     */
    public PresetParameter getParameter(String parameterId) {
        return parameters.stream()
                .filter(p -> p.getId().equals(parameterId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets default parameter values as a map.
     *
     * @return map of parameter ID to default value
     */
    public Map<String, Object> getDefaultParameterValues() {
        return parameters.stream()
                .collect(java.util.stream.Collectors.toMap(
                        PresetParameter::getId,
                        PresetParameter::getDefaultValue
                ));
    }

    @Override
    public String toString() {
        return "PresetDefinition{" +
                "presetId='" + presetId + '\'' +
                ", version='" + version + '\'' +
                ", name='" + metadata.getName() + '\'' +
                '}';
    }
}
