package ru.veritas.veritas_ui.domain.use_cases.local.home;

import java.util.List;

import ru.veritas.veritas_ui.domain.repositories.HomeRepository;
import ru.veritas.veritas_ui.domain.entities.AppShortcut;


public class GetShortcutsUseCase {
    private final HomeRepository repository;

    public GetShortcutsUseCase(HomeRepository repository) {
        this.repository = repository;
    }

    public List<List<List<AppShortcut>>> invoke() {
        return repository.getShortcuts();
    }
}