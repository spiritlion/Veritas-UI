package ru.veritas.veritas_ui.domain.use_cases.local.home;

import ru.veritas.veritas_ui.domain.entities.AppShortcut;
import ru.veritas.veritas_ui.domain.repositories.HomeRepository;

public class MoveShortcutUseCase {
    private final HomeRepository repository;

    public MoveShortcutUseCase(HomeRepository repository) {
        this.repository = repository;
    }

    public void invoke(int fromPage, int fromRow, int fromCol,
                int toPage, int toRow, int toCol) {
        AppShortcut fromApp = repository.getShortcut(fromPage, fromRow, fromCol);
        AppShortcut toApp = repository.getShortcut(toPage, toRow, toCol);
        repository.addShortcut(toPage, toRow, toCol, fromApp);
        repository.addShortcut(fromPage, fromRow, fromCol, toApp);
    }

}