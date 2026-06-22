package com.nodecraft.gui.recommendation;

import com.nodecraft.gui.ai.AiNodeSchemaCatalog;
import com.nodecraft.gui.ai.AiNodeSchemaCatalog.NodeSchema;
import com.nodecraft.gui.ai.AiNodeSchemaCatalog.PortSchema;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.registry.NodeRegistry;

import java.util.ArrayList;
import java.util.List;

public final class NodePortIndex {

    private volatile List<NodeSchema> cachedSchemas = List.of();
    private volatile long cachedEpoch = -1L;

    public List<NodeSchema> schemas() {
        NodeRegistry registry = NodeRegistry.getInstance();
        long epoch = registry.getIntrospectionEpoch();
        if (cachedSchemas != null && cachedEpoch == epoch) {
            return cachedSchemas;
        }
        synchronized (this) {
            if (cachedEpoch != registry.getIntrospectionEpoch()) {
                cachedSchemas = AiNodeSchemaCatalog.collectAll(registry);
                cachedEpoch = registry.getIntrospectionEpoch();
            }
            return cachedSchemas;
        }
    }

    public void invalidate() {
        synchronized (this) {
            cachedSchemas = List.of();
            cachedEpoch = -1L;
        }
        AiNodeSchemaCatalog.invalidateCache();
    }

    public List<CandidatePort> findDownstreamCandidates(NodeDataType outputType) {
        if (outputType == null) {
            return List.of();
        }
        List<CandidatePort> candidates = new ArrayList<>();
        for (NodeSchema schema : schemas()) {
            for (PortSchema input : schema.inputs()) {
                NodeDataType inputType = parseType(input.dataType());
                if (NodeDataType.isConnectableTo(outputType, inputType)) {
                    candidates.add(new CandidatePort(
                            schema.typeId(),
                            schema.displayName(),
                            schema.category(),
                            input.id(),
                            inputType,
                            input.required()));
                }
            }
        }
        return candidates;
    }

    public List<CandidatePort> findUpstreamCandidates(NodeDataType inputType) {
        if (inputType == null) {
            return List.of();
        }
        List<CandidatePort> candidates = new ArrayList<>();
        for (NodeSchema schema : schemas()) {
            for (PortSchema output : schema.outputs()) {
                NodeDataType outputType = parseType(output.dataType());
                if (NodeDataType.isConnectableTo(outputType, inputType)) {
                    candidates.add(new CandidatePort(
                            schema.typeId(),
                            schema.displayName(),
                            schema.category(),
                            output.id(),
                            outputType,
                            true));
                }
            }
        }
        return candidates;
    }

    public CandidatePort findPort(String nodeId, String portId, RecommendationDirection direction) {
        if (nodeId == null || portId == null) {
            return null;
        }
        for (NodeSchema schema : schemas()) {
            if (!schema.typeId().equalsIgnoreCase(nodeId)) {
                continue;
            }
            if (direction == RecommendationDirection.DOWNSTREAM) {
                for (PortSchema input : schema.inputs()) {
                    if (input.id().equals(portId)) {
                        return new CandidatePort(
                                schema.typeId(),
                                schema.displayName(),
                                schema.category(),
                                input.id(),
                                parseType(input.dataType()),
                                input.required());
                    }
                }
            } else {
                for (PortSchema output : schema.outputs()) {
                    if (output.id().equals(portId)) {
                        return new CandidatePort(
                                schema.typeId(),
                                schema.displayName(),
                                schema.category(),
                                output.id(),
                                parseType(output.dataType()),
                                true);
                    }
                }
            }
        }
        return null;
    }

    public static NodeDataType parseType(String typeId) {
        if (typeId == null || typeId.isBlank()) {
            return NodeDataType.ANY;
        }
        return NodeDataType.fromId(typeId.toLowerCase());
    }

    public record CandidatePort(
            String nodeId,
            String displayName,
            String categoryId,
            String portId,
            NodeDataType dataType,
            boolean required
    ) {
    }
}
