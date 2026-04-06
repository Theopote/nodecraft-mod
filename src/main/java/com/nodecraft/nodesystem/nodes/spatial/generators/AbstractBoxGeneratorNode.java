package com.nodecraft.nodesystem.nodes.spatial.generators;

/**
 * Compatibility bridge for legacy box generator nodes that still live in spatial.generators.
 * New construct-style box nodes are implemented in spatial.construct.
 */
abstract class AbstractBoxGeneratorNode extends com.nodecraft.nodesystem.nodes.spatial.construct.AbstractBoxGeneratorNode {

    protected AbstractBoxGeneratorNode(String typeId) {
        super(typeId);
    }
}
