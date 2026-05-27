package ru.veritas.veritas_ui.core.command.local.home;

import ru.veritas.veritas_ui.core.command.NonUndoableCommand;
import ru.veritas.veritas_ui.core.entities.AppShortcut;
import ru.veritas.veritas_ui.core.repositories.HomeRepository;
import java.util.List;

public class SetShortcutsCommand extends NonUndoableCommand<Void> {
    private final HomeRepository repository;
    private final List<List<List<AppShortcut>>> shortcuts;

    public SetShortcutsCommand(HomeRepository repository,
                               List<List<List<AppShortcut>>> shortcuts) {
        this.repository = repository;
        this.shortcuts = shortcuts;
    }

    @Override
    public Void execute() {
        repository.saveShortcuts(shortcuts);
        return null;
    }
}