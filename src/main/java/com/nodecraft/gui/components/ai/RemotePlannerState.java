package com.nodecraft.gui.components.ai;

import com.nodecraft.gui.ai.AiRemotePlannerService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

final class RemotePlannerState {

    private static final int STREAMING_BUFFER_MAX_CHARS = 6000;
    private static final int STREAMING_BUFFER_HEAD_CHARS = 1200;
    private static final String STREAMING_BUFFER_TRUNCATION_MARKER = "\n...[stream truncated]...\n";

    private final AtomicReference<CompletableFuture<AiRemotePlannerService.RemotePlanResult>> remotePlanFutureRef = new AtomicReference<>(null);
    private volatile String remotePendingPrompt = "";
    private volatile String lastRemoteRawResponse = "";
    private volatile String lastRemoteModelText = "";
    private volatile String lastRemoteRequestSnapshot = "";
    private volatile String lastRemoteErrorCategory = "";
    private volatile String lastRemoteErrorMessage = "";
    private volatile int lastRemoteStatusCode = 0;
    private volatile int lastRemoteAttempts = 0;
    private final StringBuilder remoteStreamingBuffer = new StringBuilder();

    boolean isBusy() {
        CompletableFuture<AiRemotePlannerService.RemotePlanResult> future = remotePlanFutureRef.get();
        return future != null && !future.isDone();
    }

    boolean beginRequest(String prompt, String requestSnapshot) {
        if (isBusy()) {
            lastRemoteErrorCategory = "request";
            lastRemoteErrorMessage = "Remote planner is already running.";
            return false;
        }

        remotePendingPrompt = prompt == null ? "" : prompt;
        lastRemoteRawResponse = "";
        lastRemoteModelText = "";
        lastRemoteRequestSnapshot = requestSnapshot == null ? "" : requestSnapshot;
        lastRemoteErrorCategory = "";
        lastRemoteErrorMessage = "";
        lastRemoteStatusCode = 0;
        lastRemoteAttempts = 0;
        clearStreamingBuffer();
        return true;
    }

    void setRemotePlanFuture(CompletableFuture<AiRemotePlannerService.RemotePlanResult> future) {
        remotePlanFutureRef.set(future);
    }

    AiAssistantComponent.RemotePollResult pollRemotePlannerResultIfReady() {
        CompletableFuture<AiRemotePlannerService.RemotePlanResult> future = remotePlanFutureRef.get();
        if (future == null || !future.isDone()) {
            return null;
        }

        String prompt = remotePendingPrompt;
        AiRemotePlannerService.RemotePlanResult result;
        try {
            result = future.join();
        } catch (Exception e) {
            remotePlanFutureRef.set(null);
            remotePendingPrompt = "";
            clearStreamingBuffer();
            lastRemoteErrorCategory = "request";
            lastRemoteErrorMessage = e.getMessage() == null ? "Remote planner failed." : e.getMessage();
            return new AiAssistantComponent.RemotePollResult(prompt, null, lastRemoteErrorMessage);
        }

        remotePlanFutureRef.set(null);
        remotePendingPrompt = "";
        lastRemoteAttempts = result.attempts();
        lastRemoteErrorCategory = result.errorCategory();
        lastRemoteErrorMessage = result.errorMessage() == null ? "" : result.errorMessage();
        lastRemoteStatusCode = result.statusCode();
        lastRemoteRawResponse = result.rawResponse() == null ? "" : result.rawResponse();
        lastRemoteModelText = result.modelContent() == null ? "" : result.modelContent();
        clearStreamingBuffer();
        return new AiAssistantComponent.RemotePollResult(prompt, result, null);
    }

    void cancelRemotePlannerRequest() {
        CompletableFuture<AiRemotePlannerService.RemotePlanResult> future = remotePlanFutureRef.getAndSet(null);
        if (future != null && !future.isDone()) {
            future.cancel(true);
        }
        remotePendingPrompt = "";
        clearStreamingBuffer();
    }

    void clearRemoteDebugState() {
        lastRemoteRawResponse = "";
        lastRemoteModelText = "";
        lastRemoteRequestSnapshot = "";
        lastRemoteErrorCategory = "";
        lastRemoteErrorMessage = "";
        lastRemoteStatusCode = 0;
        lastRemoteAttempts = 0;
        clearStreamingBuffer();
    }

    String getRemoteStreamingBuffer() {
        synchronized (remoteStreamingBuffer) {
            return remoteStreamingBuffer.toString();
        }
    }

    void appendStreamingToken(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        synchronized (remoteStreamingBuffer) {
            remoteStreamingBuffer.append(token);
            applyStreamingBufferLimit();
        }
    }

    private void applyStreamingBufferLimit() {
        int currentLength = remoteStreamingBuffer.length();
        if (currentLength <= STREAMING_BUFFER_MAX_CHARS) {
            return;
        }

        int headChars = Math.min(STREAMING_BUFFER_HEAD_CHARS, STREAMING_BUFFER_MAX_CHARS / 2);
        int tailChars = STREAMING_BUFFER_MAX_CHARS - headChars - STREAMING_BUFFER_TRUNCATION_MARKER.length();
        if (tailChars < 0) {
            tailChars = 0;
        }
        if (headChars + tailChars > currentLength) {
            int overflow = (headChars + tailChars) - currentLength;
            tailChars = Math.max(0, tailChars - overflow);
        }

        String head = remoteStreamingBuffer.substring(0, Math.min(headChars, currentLength));
        String tail = tailChars <= 0
                ? ""
                : remoteStreamingBuffer.substring(Math.max(0, currentLength - tailChars));

        remoteStreamingBuffer.setLength(0);
        remoteStreamingBuffer.append(head)
                .append(STREAMING_BUFFER_TRUNCATION_MARKER)
                .append(tail);

        if (remoteStreamingBuffer.length() > STREAMING_BUFFER_MAX_CHARS) {
            remoteStreamingBuffer.setLength(STREAMING_BUFFER_MAX_CHARS);
        }
    }

    private void clearStreamingBuffer() {
        synchronized (remoteStreamingBuffer) {
            remoteStreamingBuffer.setLength(0);
        }
    }

    String getLastRemoteRawResponse() {
        return lastRemoteRawResponse;
    }

    String getLastRemoteModelText() {
        return lastRemoteModelText;
    }

    String getLastRemoteRequestSnapshot() {
        return lastRemoteRequestSnapshot;
    }

    String getLastRemoteErrorCategory() {
        return lastRemoteErrorCategory;
    }

    String getLastRemoteErrorMessage() {
        return lastRemoteErrorMessage;
    }

    int getLastRemoteStatusCode() {
        return lastRemoteStatusCode;
    }

    int getLastRemoteAttempts() {
        return lastRemoteAttempts;
    }

    AiAssistantComponent.RemotePlannerSnapshot snapshot() {
        return new AiAssistantComponent.RemotePlannerSnapshot(
                lastRemoteRawResponse,
                lastRemoteModelText,
                lastRemoteRequestSnapshot,
                lastRemoteErrorCategory,
                lastRemoteErrorMessage,
                lastRemoteStatusCode,
                lastRemoteAttempts
        );
    }
}