package ru.veritas.veritas_ui.domain.command;

public interface Command<T> {
    T execute();
    void undo();
    boolean isUndoable();
}
