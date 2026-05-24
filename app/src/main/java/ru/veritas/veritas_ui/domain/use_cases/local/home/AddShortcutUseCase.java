package ru.veritas.veritas_ui.domain.use_cases.local.home;

import ru.veritas.veritas_ui.domain.entities.AppShortcut;
import ru.veritas.veritas_ui.domain.repositories.HomeRepository;

public class AddShortcutUseCase {
    private final HomeRepository repository;

    public AddShortcutUseCase(HomeRepository repository) {
        this.repository = repository;
    }

    public void invoke(int page, int row, int col, AppShortcut shortcut) {
        repository.addShortcut(page, row, col, shortcut);
    }

    public void invoke(AppShortcut shortcut) {
        repository.addShortcut(shortcut);
    }
}