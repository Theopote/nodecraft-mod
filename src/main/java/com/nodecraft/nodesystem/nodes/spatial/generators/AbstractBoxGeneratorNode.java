package com.nodecraft.nodesystem.nodes.spatial.generators;

/**
 * Compatibility bridge for legacy box generator nodes that still live in spatial.generators.
 * New construct-style box nodes are implemented in geometry.primitives.
 */
abstract class AbstractBoxGeneratorNode extends com.nodecraft.nodesystem.nodes.geometry.primitives.AbstractBoxGeneratorNode {

    protected AbstractBoxGeneratorNode(String typeId) {
        super(typeId);
    }
}
