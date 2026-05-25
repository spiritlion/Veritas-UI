package ru.veritas.veritas_ui.domain.command;

public abstract class NonUndoableCommand<T> implements Command<T> {
    @Override
    public void undo() {
        throw new UnsupportedOperationException("Undo not supported");
    }

    @Override
    public boolean isUndoable() {
        return false;
    }
}
