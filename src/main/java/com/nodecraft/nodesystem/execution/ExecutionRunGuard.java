package com.nodecraft.nodesystem.execution;

import com.nodecraft.core.exception.NodeExecutionException;

/**
 * Tracks per-run execution budget and aborts runaway graphs.
 */
public final class ExecutionRunGuard {

    private final ExecutionRunLimits limits;
    private final long startedAtMs;
    private long steps;

    public ExecutionRunGuard() {
        this(ExecutionRunLimits.defaults());
    }

    public ExecutionRunGuard(ExecutionRunLimits limits) {
        this.limits = limits == null ? ExecutionRunLimits.defaults() : limits;
        this.startedAtMs = System.currentTimeMillis();
    }

    public long steps() {
        return steps;
    }

    public long elapsedMs() {
        return System.currentTimeMillis() - startedAtMs;
    }

    public void recordStep() {
        steps++;
        checkLimits();
    }

    public void checkLimits() {
        if (steps > limits.maxSteps()) {
            throw new NodeExecutionException(
                    "Graph execution exceeded max steps (" + limits.maxSteps() + ")"
            );
        }
        long elapsed = elapsedMs();
        if (elapsed > limits.maxDurationMs()) {
            throw new NodeExecutionException(
                    "Graph execution exceeded max duration (" + limits.maxDurationMs() + " ms, elapsed=" + elapsed + " ms)"
            );
        }
    }
}
