package ru.veritas.veritas_ui.core.command;

public interface Command<T> {
    T execute();
    void undo();
    boolean isUndoable();
}
