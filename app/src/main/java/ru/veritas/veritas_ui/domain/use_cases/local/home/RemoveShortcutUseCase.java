package ru.veritas.veritas_ui.domain.use_cases.local.home;

import ru.veritas.veritas_ui.domain.repositories.HomeRepository;


public class RemoveShortcutUseCase {
    private final HomeRepository repository;

    public RemoveShortcutUseCase(HomeRepository repository) {
        this.repository = repository;
    }

    public void invoke(int page, int row, int col) {
        repository.removeShortcut(page,row,col);
    }
}