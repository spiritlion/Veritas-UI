package ru.veritas.veritas_ui.domain.use_cases.local.home;

import ru.veritas.veritas_ui.data.repositories.HomeRepository;
import ru.veritas.veritas_ui.domain.entities.AppShortcutDTO;

@FunctionalInterface
public interface MoveShortcutUseCase {
    void invoke(int fromPage, int fromRow, int fromCol,
                int toPage, int toRow, int toCol);

    static MoveShortcutUseCase create(HomeRepository repository) {
        return (fromPage, fromRow, fromCol, toPage, toRow, toCol) -> {
            AppShortcutDTO fromApp = repository.getShortcut(fromPage, fromRow, fromCol);
            AppShortcutDTO toApp = repository.getShortcut(toPage, toRow, toCol);
            repository.addShortcut(toPage, toRow, toCol, fromApp);
            repository.addShortcut(fromPage, fromRow, fromCol, toApp);
        };
    }
}