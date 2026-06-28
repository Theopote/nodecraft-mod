package com.nodecraft.nodesystem.preset;

import java.util.List;
import java.util.Map;

/**
 * Represents the node graph structure in a preset.
 *
 * <p>Contains node definitions, connections, and parameter bindings.</p>
 */
public class PresetGraph {
    private final List<PresetNodeDefinition> nodes;
    private final List<PresetConnectionDefinition> connections;

    public PresetGraph(List<PresetNodeDefinition> nodes, List<PresetConnectionDefinition> connections) {
        this.nodes = nodes != null ? nodes : List.of();
        this.connections = connections != null ? connections : List.of();
    }

    public List<PresetNodeDefinition> getNodes() {
        return nodes;
    }

    public List<PresetConnectionDefinition> getConnections() {
        return connections;
    }

    /**
     * Gets a node definition by its ID.
     *
     * @param nodeId the node identifier within this preset
     * @return the node definition, or null if not found
     */
    public PresetNodeDefinition getNode(String nodeId) {
        return nodes.stream()
                .filter(n -> n.getId().equals(nodeId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Represents a node definition in the preset graph.
     */
    public static class PresetNodeDefinition {
        private final String id;
        private final String type;
        private final Map<String, Double> position;
        private final Map<String, Object> parameters;

        public PresetNodeDefinition(String id, String type, Map<String, Double> position, Map<String, Object> parameters) {
            this.id = id;
            this.type = type;
            this.position = position;
            this.parameters = parameters != null ? parameters : Map.of();
        }

        public String getId() {
            return id;
        }

        public String getType() {
            return type;
        }

        public Map<String, Double> getPosition() {
            return position;
        }

        public Map<String, Object> getParameters() {
            return parameters;
        }
    }

    /**
     * Represents a connection between nodes in the preset graph.
     */
    public static class PresetConnectionDefinition {
        private final ConnectionEndpoint from;
        private final ConnectionEndpoint to;

        public PresetConnectionDefinition(ConnectionEndpoint from, ConnectionEndpoint to) {
            this.from = from;
            this.to = to;
        }

        public ConnectionEndpoint getFrom() {
            return from;
        }

        public ConnectionEndpoint getTo() {
            return to;
        }

        public static class ConnectionEndpoint {
            private final String node;
            private final String port;

            public ConnectionEndpoint(String node, String port) {
                this.node = node;
                this.port = port;
            }

            public String getNode() {
                return node;
            }

            public String getPort() {
                return port;
            }
        }
    }
}
