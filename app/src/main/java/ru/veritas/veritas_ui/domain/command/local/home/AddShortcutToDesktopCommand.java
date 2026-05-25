package ru.veritas.veritas_ui.domain.command.local.home;

import ru.veritas.veritas_ui.domain.command.NonUndoableCommand;
import ru.veritas.veritas_ui.domain.entities.AppShortcut;
import ru.veritas.veritas_ui.domain.repositories.HomeRepository;

public class AddShortcutToDesktopCommand extends NonUndoableCommand<Void> {
    private final HomeRepository repository;
    private final AppShortcut shortcut;
    private final int page, row, col;

    public AddShortcutToDesktopCommand(HomeRepository repository,
                                       AppShortcut shortcut,
                                       int page, int row, int col) {
        this.repository = repository;
        this.shortcut = shortcut;
        this.page = page; this.row = row; this.col = col;
    }

    @Override
    public Void execute() {
        repository.addShortcut(page, row, col, shortcut);
        return null;
    }
}