package com.gtocore.common.machine.multiblock.part.ae;

import appeng.api.stacks.GenericStack;

import com.gto.datasynclib.annotations.SaveToDisk;

public abstract class AbstractRecipeInternalSlot extends MEPatternPartMachine.AbstractInternalSlot {

    static final GenericStack[] EMPTY_EDITOR_STACKS = new GenericStack[0];

    @SaveToDisk(defaultValue = "false")
    boolean editingPattern;
    @SaveToDisk(skipWhen = "skipEditorStacks")
    GenericStack[] editorInputs = EMPTY_EDITOR_STACKS;
    @SaveToDisk(skipWhen = "skipEditorStacks")
    GenericStack[] editorOutputs = EMPTY_EDITOR_STACKS;
    boolean writingEditorPattern;

    private Runnable onContentsChanged = () -> {};
    private boolean isContentsChanged = true;

    public abstract boolean isEmpty();

    private boolean skipEditorStacks(GenericStack[] stacks) {
        return !editingPattern;
    }

    final boolean hasIncompleteEditorPattern() {
        return editingPattern && (!hasEditorStack(editorInputs) || !hasEditorStack(editorOutputs));
    }

    final void setEditorTemplate(GenericStack[] inputs, GenericStack[] outputs) {
        editorInputs = inputs;
        editorOutputs = outputs;
        editingPattern = true;
        markAsChanged();
    }

    final void resetEditorTemplate() {
        editingPattern = false;
        editorInputs = EMPTY_EDITOR_STACKS;
        editorOutputs = EMPTY_EDITOR_STACKS;
        markAsChanged();
    }

    private static boolean hasEditorStack(GenericStack[] stacks) {
        for (var stack : stacks) if (stack != null) return true;
        return false;
    }

    public final void markContentsChanged() {
        isContentsChanged = true;
        onContentsChanged.run();
    }

    public boolean isContentsChanged() {
        if (isContentsChanged) {
            isContentsChanged = false;
            return true;
        }
        return false;
    }

    public final void setOnContentsChanged(final Runnable onContentsChanged) {
        this.onContentsChanged = onContentsChanged;
    }
}
