package ru.veritas.veritas_ui.core.command.local.home;

import ru.veritas.veritas_ui.core.command.NonUndoableCommand;
import ru.veritas.veritas_ui.core.repositories.HomeRepository;

public class RemoveShortcutCommand extends NonUndoableCommand<Void> {
    private final HomeRepository repository;
    private final int page, row, col;

    public RemoveShortcutCommand(HomeRepository repository, int page, int row, int col) {
        this.repository = repository;
        this.page = page; this.row = row; this.col = col;
    }

    @Override
    public Void execute() {
        repository.removeShortcut(page, row, col);
        return null;
    }
}