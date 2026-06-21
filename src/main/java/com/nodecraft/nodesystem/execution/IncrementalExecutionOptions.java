package com.nodecraft.nodesystem.execution;

/**
 * Options for incremental partial graph execution.
 */
public record IncrementalExecutionOptions(boolean skipCachedNodesInPartialScope) {

    public static IncrementalExecutionOptions defaults() {
        return new IncrementalExecutionOptions(false);
    }

    public static IncrementalExecutionOptions previewDefaults() {
        return new IncrementalExecutionOptions(true);
    }
}
