package ru.veritas.veritas_ui.domain.use_cases.local.home;

import java.util.List;

import ru.veritas.veritas_ui.domain.repositories.HomeRepository;
import ru.veritas.veritas_ui.domain.entities.AppShortcut;

public class SetShortcutsUseCase {
    private final HomeRepository repository;

    public SetShortcutsUseCase(HomeRepository repository) {
        this.repository = repository;
    }

    public void invoke(List<List<List<AppShortcut>>> shortcuts) {
        repository.saveShortcuts(shortcuts);
    }
}