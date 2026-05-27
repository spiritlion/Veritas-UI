package ru.veritas.veritas_ui.core.command;

import java.util.ArrayList;
import java.util.List;

public class CompositeCommand<T> implements Command<T> {
    private final List<Command<?>> commands = new ArrayList<>();
    private final List<Command<?>> executed = new ArrayList<>();

    public CompositeCommand<T> add(Command<?> cmd) {
        commands.add(cmd);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T execute() {
        executed.clear();
        try {
            for (Command<?> cmd : commands) {
                cmd.execute();
                executed.add(cmd);
            }
        } catch (Exception e) {
            undo();
            throw e;
        }
        return null;
    }

    @Override
    public void undo() {
        for (int i = executed.size() - 1; i >= 0; i--) {
            executed.get(i).undo();
        }
        executed.clear();
    }

    @Override
    public boolean isUndoable() {
        return commands.stream().allMatch(Command::isUndoable);
    }
}