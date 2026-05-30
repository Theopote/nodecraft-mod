package com.nodecraft.gui.components.ai;

import com.nodecraft.gui.ai.AiGraphDiffService;
import com.nodecraft.gui.components.ai.AiAssistantComponent.AiPlanConnection;
import com.nodecraft.gui.components.ai.AiAssistantComponent.AiPlanNode;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class AiAssistantPlanPreviewRenderer {

    private AiAssistantPlanPreviewRenderer() {
    }

    record State(
            boolean hasPlan,
            String summary,
            String applyModeHint,
            String focusedNodeRef,
            boolean focusScrollPending,
            int nodeCount,
            int connectionCount,
            List<String> validationErrors,
            List<String> plannedNodeLines,
            List<String> plannedConnectionLines,
            List<AiPlanNode> planNodes,
            List<AiPlanConnection> planConnections,
            AiGraphDiffService.GraphDiffSummary heuristicDiff,
            AiGraphDiffService.MappedDiffSummary mappedDiff,
            boolean canApply,
            boolean canUndo,
                String undoDisabledReason,
            String statusMessage
    ) {
    }

    interface Actions {
        void applyPlan();

        void dryRunReport();

        void saveAsTemplate();

        void undoLastApply();

        void onTopologyNodeSelected(String nodeRef);

        void onTopologyFocusScrollConsumed();
    }

    static void renderPlanPreviewSection(State state, Actions actions) {
        ImGui.separator();
        ImGui.text("Plan Preview");

        if (!state.hasPlan()) {
            ImGui.textDisabled("No plan yet. Send a prompt to generate a plan.");
            if (state.statusMessage() != null && !state.statusMessage().isBlank()) {
                renderStatusMessage(state.statusMessage());
            }
            return;
        }

        ImGui.textWrapped(state.summary() == null ? "" : state.summary());
        if (state.applyModeHint() != null && !state.applyModeHint().isBlank()) {
            ImGui.textDisabled(state.applyModeHint());
        }
        ImGui.text("Nodes: " + state.nodeCount() + "  Connections: " + state.connectionCount());

        if (state.validationErrors() != null && !state.validationErrors().isEmpty()) {
            ImGui.textColored(1.0f, 0.45f, 0.35f, 1.0f, "Validation errors:");
            for (String error : state.validationErrors()) {
                ImGui.bulletText(error);
            }
        }

        if (ImGui.treeNode("Planned Nodes")) {
            if (state.planNodes() != null && !state.planNodes().isEmpty()) {
                boolean consumedScroll = false;
                for (AiPlanNode node : state.planNodes()) {
                    String line = buildPlanNodeLine(node);
                    boolean focused = state.focusedNodeRef() != null && state.focusedNodeRef().equals(node.ref());
                    if (focused) {
                        ImGui.bullet();
                        ImGui.sameLine();
                        ImGui.textColored(0.98f, 0.85f, 0.42f, 1.0f, line);
                        if (state.focusScrollPending() && !consumedScroll) {
                            ImGui.setScrollHereY(0.35f);
                            actions.onTopologyFocusScrollConsumed();
                            consumedScroll = true;
                        }
                    } else {
                        ImGui.bulletText(line);
                    }
                }
            } else if (state.plannedNodeLines() == null || state.plannedNodeLines().isEmpty()) {
                ImGui.textDisabled("None");
            } else {
                for (String line : state.plannedNodeLines()) {
                    ImGui.bulletText(line);
                }
            }
            ImGui.treePop();
        }

        if (ImGui.treeNode("Planned Connections")) {
            if (state.plannedConnectionLines() == null || state.plannedConnectionLines().isEmpty()) {
                ImGui.textDisabled("None");
            } else {
                for (String line : state.plannedConnectionLines()) {
                    ImGui.bulletText(line);
                }
            }
            ImGui.treePop();
        }

        if (ImGui.treeNode("Graph Topology Preview")) {
            String selectedNodeRef = renderTopologyPreview(state.planNodes(), state.planConnections(), state.focusedNodeRef());
            if (selectedNodeRef != null && !selectedNodeRef.isBlank()) {
                actions.onTopologyNodeSelected(selectedNodeRef);
            }
            ImGui.treePop();
        }

        AiGraphDiffService.GraphDiffSummary diff = state.heuristicDiff();
        if (diff != null && ImGui.treeNode("Graph Diff (Heuristic)")) {
            ImGui.textDisabled("Compared by node type+params signature and typed connection signature.");
            ImGui.text("Potential additions: nodes=" + diff.nodeAdditions() + ", connections=" + diff.connectionAdditions());
            ImGui.text("Potential missing from plan: nodes=" + diff.nodeMissingFromPlan() + ", connections=" + diff.connectionMissingFromPlan());

            renderDiffSamples("Node additions", diff.nodeAdditionSamples());
            renderDiffSamples("Node missing from plan", diff.nodeMissingSamples());
            renderDiffSamples("Connection additions", diff.connectionAdditionSamples());
            renderDiffSamples("Connection missing from plan", diff.connectionMissingSamples());
            ImGui.treePop();
        }

        AiGraphDiffService.MappedDiffSummary mapped = state.mappedDiff();
        if (mapped != null && ImGui.treeNode("Mapped Diff (Preview)")) {
            ImGui.textDisabled("Greedy matching by type+params, then type fallback. Estimates reusable vs new nodes.");
            ImGui.text("Reusable matches=" + mapped.reusableNodeMatches()
                    + ", new nodes=" + mapped.newNodesToCreate());
            ImGui.text("Unchanged reused=" + mapped.unchangedReusableNodes()
                    + ", param updates=" + mapped.paramUpdateCandidates());
            ImGui.text("Connection additions=" + mapped.connectionAdditions()
                    + ", connection removal candidates=" + mapped.connectionRemovalCandidates()
                    + ", incoming replacements=" + mapped.incomingReplacementCandidates());

            renderDiffSamples("Node reuse matches", mapped.nodeReuseSamples());
            renderDiffSamples("Node creation candidates", mapped.nodeCreationSamples());
            renderDiffSamples("Param update candidates", mapped.paramUpdateSamples());
            renderDiffSamples("Connection additions", mapped.connectionAdditionSamples());
            renderDiffSamples("Connection removal candidates", mapped.connectionRemovalSamples());
            renderDiffSamples("Incoming replacement candidates", mapped.incomingReplacementSamples());
            ImGui.treePop();
        }

        if (!state.canApply()) ImGui.beginDisabled();
        if (ImGui.button("Apply Plan")) {
            actions.applyPlan();
        }
        if (!state.canApply()) ImGui.endDisabled();

        ImGui.sameLine();
        if (!state.canApply()) ImGui.beginDisabled();
        if (ImGui.button("Dry Run Report")) {
            actions.dryRunReport();
        }
        if (!state.canApply()) ImGui.endDisabled();

        ImGui.sameLine();
        if (!state.hasPlan()) ImGui.beginDisabled();
        if (ImGui.button("Save as Template")) {
            actions.saveAsTemplate();
        }
        if (!state.hasPlan()) ImGui.endDisabled();

        ImGui.sameLine();
        if (!state.canUndo()) ImGui.beginDisabled();
        if (ImGui.button("Undo Last AI Apply")) {
            actions.undoLastApply();
        }
        if (!state.canUndo()) ImGui.endDisabled();

        if (!state.canUndo() && state.undoDisabledReason() != null && !state.undoDisabledReason().isBlank()) {
            ImGui.textDisabled(state.undoDisabledReason());
        }

        if (state.statusMessage() != null && !state.statusMessage().isBlank()) {
            renderStatusMessage(state.statusMessage());
        }
    }

    private static void renderDiffSamples(String title, List<String> samples) {
        if (!ImGui.treeNode(title)) {
            return;
        }
        if (samples == null || samples.isEmpty()) {
            ImGui.textDisabled("None");
        } else {
            for (String sample : samples) {
                ImGui.bulletText(sample);
            }
        }
        ImGui.treePop();
    }

    private static String renderTopologyPreview(
            List<AiPlanNode> nodes,
            List<AiPlanConnection> connections,
            String focusedNodeRef
    ) {
        if (nodes == null || nodes.isEmpty()) {
            ImGui.textDisabled("No nodes to preview.");
            return null;
        }

        float previewWidth = Math.max(240.0f, ImGui.getContentRegionAvailX());
        float previewHeight = 170.0f;
        float nodeWidth = 90.0f;
        float nodeHeight = 28.0f;
        float padding = 16.0f;

        ImDrawList drawList = ImGui.getWindowDrawList();
        ImVec2 cursor = ImGui.getCursorScreenPos();

        int bgColor = ImGui.colorConvertFloat4ToU32(0.10f, 0.12f, 0.14f, 1.0f);
        int borderColor = ImGui.colorConvertFloat4ToU32(0.23f, 0.27f, 0.32f, 1.0f);
        int nodeColor = ImGui.colorConvertFloat4ToU32(0.20f, 0.42f, 0.66f, 1.0f);
        int focusedNodeColor = ImGui.colorConvertFloat4ToU32(0.78f, 0.56f, 0.18f, 1.0f);
        int nodeBorderColor = ImGui.colorConvertFloat4ToU32(0.58f, 0.74f, 0.88f, 1.0f);
        int focusedNodeBorderColor = ImGui.colorConvertFloat4ToU32(0.98f, 0.88f, 0.58f, 1.0f);
        int textColor = ImGui.colorConvertFloat4ToU32(0.96f, 0.97f, 0.98f, 1.0f);
        int edgeColor = ImGui.colorConvertFloat4ToU32(0.72f, 0.78f, 0.84f, 0.95f);

        drawList.addRectFilled(cursor.x, cursor.y, cursor.x + previewWidth, cursor.y + previewHeight, bgColor, 6.0f);
        drawList.addRect(cursor.x, cursor.y, cursor.x + previewWidth, cursor.y + previewHeight, borderColor, 6.0f, 0, 1.0f);

        float minX = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        for (AiPlanNode node : nodes) {
            minX = Math.min(minX, node.offsetX());
            maxX = Math.max(maxX, node.offsetX());
            minY = Math.min(minY, node.offsetY());
            maxY = Math.max(maxY, node.offsetY());
        }

        float rangeX = Math.max(1.0f, maxX - minX);
        float rangeY = Math.max(1.0f, maxY - minY);
        float scaleX = (previewWidth - 2.0f * padding - nodeWidth) / rangeX;
        float scaleY = (previewHeight - 2.0f * padding - nodeHeight) / rangeY;
        float scale = Math.max(0.05f, Math.min(scaleX, scaleY));

        Map<String, float[]> nodeAnchors = new HashMap<>();
        for (AiPlanNode node : nodes) {
            float nx = cursor.x + padding + (node.offsetX() - minX) * scale;
            float ny = cursor.y + padding + (node.offsetY() - minY) * scale;
            nx = clamp(nx, cursor.x + 2.0f, cursor.x + previewWidth - nodeWidth - 2.0f);
            ny = clamp(ny, cursor.y + 2.0f, cursor.y + previewHeight - nodeHeight - 2.0f);
            nodeAnchors.put(node.ref(), new float[]{nx, ny});
        }

        if (connections != null) {
            for (AiPlanConnection connection : connections) {
                float[] from = nodeAnchors.get(connection.sourceRef());
                float[] to = nodeAnchors.get(connection.targetRef());
                if (from == null || to == null) {
                    continue;
                }
                float x1 = from[0] + nodeWidth;
                float y1 = from[1] + nodeHeight * 0.5f;
                float x2 = to[0];
                float y2 = to[1] + nodeHeight * 0.5f;
                drawList.addLine(x1, y1, x2, y2, edgeColor, 1.4f);
            }
        }

        for (AiPlanNode node : nodes) {
            float[] anchor = nodeAnchors.get(node.ref());
            if (anchor == null) {
                continue;
            }
            float nx = anchor[0];
            float ny = anchor[1];
            boolean focused = focusedNodeRef != null && focusedNodeRef.equals(node.ref());
            drawList.addRectFilled(nx, ny, nx + nodeWidth, ny + nodeHeight, focused ? focusedNodeColor : nodeColor, 4.0f);
            drawList.addRect(nx, ny, nx + nodeWidth, ny + nodeHeight, focused ? focusedNodeBorderColor : nodeBorderColor, 4.0f, 0, 1.0f);
            drawList.addText(nx + 5.0f, ny + 7.0f, textColor, shortTypeId(node.typeId()));
        }

        String clickedRef = null;
        if (ImGui.isWindowHovered() && ImGui.isMouseClicked(0)) {
            ImVec2 mouse = ImGui.getIO().getMousePos();
            for (AiPlanNode node : nodes) {
                float[] anchor = nodeAnchors.get(node.ref());
                if (anchor == null) {
                    continue;
                }
                float nx = anchor[0];
                float ny = anchor[1];
                if (mouse.x >= nx && mouse.x <= nx + nodeWidth && mouse.y >= ny && mouse.y <= ny + nodeHeight) {
                    clickedRef = node.ref();
                    break;
                }
            }
        }

        ImGui.dummy(previewWidth, previewHeight);
        return clickedRef;
    }

    private static String buildPlanNodeLine(AiPlanNode node) {
        if (node == null) {
            return "unknown";
        }
        return node.ref() + " -> " + node.typeId()
                + "  (" + Math.round(node.offsetX())
                + ", " + Math.round(node.offsetY()) + ")";
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static String shortTypeId(String typeId) {
        if (typeId == null || typeId.isBlank()) {
            return "unknown";
        }

        String normalized = typeId;
        int slash = normalized.lastIndexOf('/');
        if (slash >= 0 && slash + 1 < normalized.length()) {
            normalized = normalized.substring(slash + 1);
        }
        int dot = normalized.lastIndexOf('.');
        if (dot >= 0 && dot + 1 < normalized.length()) {
            normalized = normalized.substring(dot + 1);
        }
        if (normalized.length() > 15) {
            return normalized.substring(0, 12) + "...";
        }
        return normalized;
    }

    private static void renderStatusMessage(String message) {
        if (message == null || message.isBlank()) {
            return;
        }

        StatusTone tone = resolveStatusTone(message);
        switch (tone) {
            case ERROR -> ImGui.textColored(0.96f, 0.35f, 0.35f, 1.0f, "[Error] " + message);
            case WARN -> ImGui.textColored(0.95f, 0.74f, 0.30f, 1.0f, "[Warn] " + message);
            case SUCCESS -> ImGui.textColored(0.45f, 0.82f, 0.54f, 1.0f, "[OK] " + message);
            default -> ImGui.textWrapped(message);
        }
    }

    private static StatusTone resolveStatusTone(String message) {
        String lower = message.toLowerCase();
        if (containsAny(lower, "failed", "error", "invalid", "exception", "aborted")) {
            return StatusTone.ERROR;
        }
        if (containsAny(lower, "warn", "retry", "canceled", "unavailable", "busy")) {
            return StatusTone.WARN;
        }
        if (containsAny(lower, "saved", "loaded", "validated", "completed", "successful", "submitted", "applied")) {
            return StatusTone.SUCCESS;
        }
        return StatusTone.INFO;
    }

    private static boolean containsAny(String text, String... keywords) {
        if (text == null || text.isBlank() || keywords == null) {
            return false;
        }
        for (String keyword : keywords) {
            if (keyword != null && !keyword.isBlank() && text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private enum StatusTone {
        INFO,
        SUCCESS,
        WARN,
        ERROR
    }
}
