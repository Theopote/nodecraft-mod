package com.nodecraft.gui.editor.base;

/**
 * Read-only view of editor undo history for AI apply validation and diagnostics.
 */
public interface GraphApplyHistoryView {

    GraphApplyHistoryView EMPTY = new GraphApplyHistoryView() {
        @Override
        public boolean canUndo() {
            return false;
        }

        @Override
        public boolean canRedo() {
            return false;
        }

        @Override
        public int undoStackSize() {
            return 0;
        }

        @Override
        public int redoStackSize() {
            return 0;
        }

        @Override
        public String undoTopActionType() {
            return "null";
        }

        @Override
        public boolean isUndoTopAiPatch() {
            return false;
        }
    };

    boolean canUndo();

    boolean canRedo();

    int undoStackSize();

    int redoStackSize();

    String undoTopActionType();

    boolean isUndoTopAiPatch();
}
