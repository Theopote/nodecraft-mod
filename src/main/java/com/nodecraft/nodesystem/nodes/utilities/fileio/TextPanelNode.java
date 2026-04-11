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
 * 鏂囨湰闈㈡澘鑺傜偣锛屾彁渚涘琛屾枃鏈紪杈戝尯鍩熴€?
 */
@NodeInfo(
    id = "inputs.sources.text_panel",
    displayName = "鏂囨湰闈㈡澘",
    description = "鐢ㄤ簬鎵嬪姩杈撳叆鏂囨湰鍒楄〃鎴栨樉绀烘暟鎹?,
    category = "inputs.sources"
)
public class TextPanelNode extends BaseCustomUINode {

    private static final Logger LOGGER = LoggerFactory.getLogger(TextPanelNode.class);
    
    @NodeProperty(displayName = "鏂囨湰鍐呭", category = "鏂囨湰", order = 1,
                  description = "鏂囨湰鍐呭")
    private volatile String text = "";

    @NodeProperty(displayName = "澶氳妯″紡", category = "璁剧疆", order = 10,
                  description = "鏄惁涓哄琛屾枃鏈?)
    private boolean isMultiline = true;

    @NodeProperty(displayName = "鍒嗚杈撳嚭", category = "璁剧疆", order = 11,
                  description = "鏄惁灏嗘枃鏈寜琛屽垎鍓蹭负鍒楄〃杈撳嚭")
    private boolean splitLines = true;

    @NodeProperty(displayName = "鍙", category = "璁剧疆", order = 12,
                  description = "鏄惁涓哄彧璇绘ā寮?)
    private boolean readOnly = false;

    @NodeProperty(displayName = "鍒嗛殧绗?, category = "璁剧疆", order = 13,
                  description = "琛屽垎闅旂")
    private String delimiter = "\n";
    
    // --- 绔彛 ---
    private static final String INPUT_TEXT_ID = "input_text";
    private static final String OUTPUT_TEXT_ID = "output_text";
    private static final String OUTPUT_LINES_ID = "output_lines";
    private static final String OUTPUT_LINE_COUNT_ID = "output_line_count";
    
    // --- UI鐘舵€?---
    private transient ImString textBuffer = new ImString(32768);
    private transient volatile boolean bufferNeedsSync = true;
    
    public TextPanelNode() {
        super(UUID.randomUUID(), "inputs.sources.text_panel");
        
        addInputPort(new BasePort(INPUT_TEXT_ID, "Text Input", "Optional text input to display", NodeDataType.STRING, this));
        addOutputPort(new BasePort(OUTPUT_TEXT_ID, "Text", "The text content as a single string", NodeDataType.STRING, this));
        addOutputPort(new BasePort(OUTPUT_LINES_ID, "Lines", "The text content as a list of lines", NodeDataType.LIST, this));
        addOutputPort(new BasePort(OUTPUT_LINE_COUNT_ID, "Line Count", "The number of lines", NodeDataType.INTEGER, this));
    }
    
    @Override
    public String getDescription() { return "鎵嬪姩杈撳叆鏂囨湰鎴栨樉绀烘暟鎹?; }
    
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
            height += ImGui.getTextLineHeightWithSpacing() * 6; // 6琛岄珮搴︾殑鏂囨湰鍖?
        } else {
            height += ImGui.getFrameHeight(); // 鍗曡杈撳叆
        }
        height += getSmallPadding();
        height += ImGui.getTextLineHeight(); // 淇℃伅琛?
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
                    // === 澶氳鏂囨湰鍖?===
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
                    // === 鍗曡杈撳叆 ===
                    l.pushFramePadding(4.0f, 3.0f);
                    ImGui.setCursorPosX(baseCursorX + edgeMargin);
                    l.setItemWidth(availableWidth / Math.max(zoom, 0.001f));
                    if (ImGui.inputTextWithHint("##text_panel", "杈撳叆鏂囨湰...", textBuffer, flags)) {
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
                
                // === 淇℃伅琛?===
                int charCount = text.length();
                int lineCount = text.isEmpty() ? 0 : text.split(delimiter, -1).length;
                ImGui.setCursorPosX(baseCursorX + edgeMargin);
                ImGui.pushStyleColor(ImGuiCol.Text, 0.5f, 0.5f, 0.5f, 1.0f);
                ImGui.text(lineCount + " 琛?| " + charCount + " 瀛楃");
                ImGui.popStyleColor();
                
                l.addVerticalSpacing(getSmallPadding());
            } catch (Exception e) {
                LOGGER.error("TextPanelNode UI娓叉煋澶辫触", e);
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
