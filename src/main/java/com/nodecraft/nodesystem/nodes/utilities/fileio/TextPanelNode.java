package com.nodecraft.nodesystem.nodes.utilities.fileio;

import com.nodecraft.gui.editor.impl.BaseCustomUINode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.api.NodeProperty;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import imgui.ImGui;
import imgui.type.ImString;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiInputTextFlags;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Text panel node that provides a multi-line editable text area.
 */
@NodeInfo(
    id = "utilities.fileio.text_panel",
    displayName = "Text Panel",
    description = "Used to manually input text or display text data.",
    category = "utilities.fileio"
)
public class TextPanelNode extends BaseCustomUINode {

    private static final Logger LOGGER = LoggerFactory.getLogger(TextPanelNode.class);
    
    @NodeProperty(displayName = "Text Content", category = "Text", order = 1,
                  description = "Text content")
    private volatile String text = "";

    @NodeProperty(displayName = "Multiline Mode", category = "Settings", order = 10,
                  description = "Whether input is multiline")
    private boolean isMultiline = true;

    @NodeProperty(displayName = "Split Lines", category = "Settings", order = 11,
                  description = "Split text into lines for list output")
    private boolean splitLines = true;

    @NodeProperty(displayName = "Read Only", category = "Settings", order = 12,
                  description = "Whether editing is disabled")
    private boolean readOnly = false;

    @NodeProperty(displayName = "Delimiter", category = "Settings", order = 13,
                  description = "Line splitting delimiter")
    private String delimiter = "\n";
    
    // --- Ports ---
    private static final String INPUT_TEXT_ID = "input_text";
    private static final String OUTPUT_TEXT_ID = "output_text";
    private static final String OUTPUT_LINES_ID = "output_lines";
    private static final String OUTPUT_LINE_COUNT_ID = "output_line_count";
    
    // --- UI state ---
    private transient ImString textBuffer = new ImString(32768);
    private transient volatile boolean bufferNeedsSync = true;
    
    public TextPanelNode() {
        super(UUID.randomUUID(), "utilities.fileio.text_panel");
        
        addInputPort(new BasePort(INPUT_TEXT_ID, "Text Input", "Optional text input to display", NodeDataType.STRING, this));
        addOutputPort(new BasePort(OUTPUT_TEXT_ID, "Text", "The text content as a single string", NodeDataType.STRING, this));
        addOutputPort(new BasePort(OUTPUT_LINES_ID, "Lines", "The text content as a list of lines", NodeDataType.LIST, this));
        addOutputPort(new BasePort(OUTPUT_LINE_COUNT_ID, "Line Count", "The number of lines", NodeDataType.INTEGER, this));
    }
    
    @Override
    public String getDescription() { return "Used to manually input text or display text data."; }
    
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        String textValue = text;
        Object inputText = inputValues.get(INPUT_TEXT_ID);
        if (inputText != null) textValue = inputText.toString();
        
        List<String> lines;
        if (splitLines) {
            lines = new ArrayList<>(Arrays.asList(textValue.split(delimiter)));
        } else {
            lines = new ArrayList<>();
            if (!textValue.isEmpty()) lines.add(textValue);
        }
        
        outputValues.put(OUTPUT_TEXT_ID, textValue);
        outputValues.put(OUTPUT_LINES_ID, lines);
        outputValues.put(OUTPUT_LINE_COUNT_ID, lines.size());
    }
    
    @Override
    protected float calculateUIHeight() {
        float height = getSmallPadding();
        if (isMultiline) {
            height += ImGui.getTextLineHeightWithSpacing() * 6; // 6-line text area
        } else {
            height += ImGui.getFrameHeight(); // single-line input
        }
        height += getSmallPadding();
        height += ImGui.getTextLineHeight(); // status line
        height += getSmallPadding();
        return height;
    }

    @Override
    protected float calculateMinUIWidth() {
        return 188f + getContentMargin();
    }

    @Override
    protected boolean renderCustomUIScaled(float width, float height, float zoom) {
        return layout(zoom, l -> {
            boolean changed = false;
            try {
                float edgeMargin = l.toPixels(getSmallPadding());
                float availableWidth = Math.max(96.0f, l.toPixelsExact(width) - edgeMargin * 2.0f);
                float baseCursorX = ImGui.getCursorPosX();
                l.addVerticalSpacing(getSmallPadding());
                
                ensureBuffer();
                
                int flags = 0;
                if (readOnly) flags |= ImGuiInputTextFlags.ReadOnly;
                
                if (isMultiline) {
                    // === Multiline input ===
                    float textAreaHeight = ImGui.getTextLineHeightWithSpacing() * 6;
                    ImGui.setCursorPosX(baseCursorX + edgeMargin);
                    if (ImGui.inputTextMultiline("##text_panel", textBuffer, 
                            availableWidth, textAreaHeight, flags)) {
                        String newText = textBuffer.get();
                        if (!newText.equals(text)) {
                            text = newText;
                            markDirty();
                            changed = true;
                        }
                    }
                } else {
                    // === Single line input ===
                    l.pushFramePadding(4.0f, 3.0f);
                    ImGui.setCursorPosX(baseCursorX + edgeMargin);
                    l.setItemWidth(availableWidth / Math.max(zoom, 0.001f));
                    if (ImGui.inputTextWithHint("##text_panel", "Enter text...", textBuffer, flags)) {
                        String newText = textBuffer.get();
                        if (!newText.equals(text)) {
                            text = newText;
                            markDirty();
                            changed = true;
                        }
                    }
                    l.popItemWidth();
                    l.popStyleVar();
                }
                
                l.addVerticalSpacing(getSmallPadding());
                
                // === Status line ===
                int charCount = text.length();
                int lineCount = text.isEmpty() ? 0 : text.split(delimiter, -1).length;
                ImGui.setCursorPosX(baseCursorX + edgeMargin);
                ImGui.pushStyleColor(ImGuiCol.Text, 0.5f, 0.5f, 0.5f, 1.0f);
                ImGui.text(lineCount + " lines | " + charCount + " chars");
                ImGui.popStyleColor();
                
                l.addVerticalSpacing(getSmallPadding());
            } catch (Exception e) {
                LOGGER.error("TextPanelNode UI render failed", e);
            }
            return changed;
        });
    }
    
    private void ensureBuffer() {
        if (textBuffer == null) textBuffer = new ImString(32768);
        if (bufferNeedsSync) {
            textBuffer.set(text != null ? text : "");
            bufferNeedsSync = false;
        }
    }
    
    public void setText(String text) {
        if (text == null) text = "";
        if (!this.text.equals(text)) {
            this.text = text;
            bufferNeedsSync = true;
            markDirty();
        }
    }
    
    public String getText() { return text; }
    public boolean isMultiline() { return isMultiline; }
    public void setMultiline(boolean multiline) { this.isMultiline = multiline; invalidateCache(); }
    public boolean isSplitLines() { return splitLines; }
    public void setSplitLines(boolean splitLines) { if (this.splitLines != splitLines) { this.splitLines = splitLines; markDirty(); } }
    public boolean isReadOnly() { return readOnly; }
    public void setReadOnly(boolean readOnly) { this.readOnly = readOnly; }
    public String getDelimiter() { return delimiter; }
    public void setDelimiter(String delimiter) { if (delimiter == null || delimiter.isEmpty()) delimiter = "\n"; if (!this.delimiter.equals(delimiter)) { this.delimiter = delimiter; markDirty(); } }
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("text", getText());
        state.put("isMultiline", isMultiline());
        state.put("splitLines", isSplitLines());
        state.put("readOnly", isReadOnly());
        state.put("delimiter", getDelimiter());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> m = (java.util.Map<?, ?>) state;
            if (m.containsKey("isMultiline")) { Object v = m.get("isMultiline"); if (v instanceof Boolean) setMultiline((Boolean) v); }
            if (m.containsKey("splitLines")) { Object v = m.get("splitLines"); if (v instanceof Boolean) setSplitLines((Boolean) v); }
            if (m.containsKey("readOnly")) { Object v = m.get("readOnly"); if (v instanceof Boolean) setReadOnly((Boolean) v); }
            if (m.containsKey("delimiter")) { Object v = m.get("delimiter"); if (v instanceof String) setDelimiter((String) v); }
            if (m.containsKey("text")) { Object v = m.get("text"); if (v instanceof String) setText((String) v); }
        }
    }
}
