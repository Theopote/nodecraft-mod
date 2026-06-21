package com.nodecraft.nodesystem.execution;

/**
 * Runtime safety limits for graph execution (steps and wall-clock time).
 */
public record ExecutionRunLimits(long maxSteps, long maxDurationMs) {

    public static final long DEFAULT_MAX_STEPS = 100_000L;
    public static final long DEFAULT_MAX_DURATION_MS = 30_000L;

    public static ExecutionRunLimits defaults() {
        return new ExecutionRunLimits(DEFAULT_MAX_STEPS, DEFAULT_MAX_DURATION_MS);
    }

    public ExecutionRunLimits {
        if (maxSteps < 1L) {
            maxSteps = 1L;
        }
        if (maxDurationMs < 1L) {
            maxDurationMs = 1L;
        }
    }
}
