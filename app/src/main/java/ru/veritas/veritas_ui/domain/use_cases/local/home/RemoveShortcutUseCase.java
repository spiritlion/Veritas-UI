package ru.veritas.veritas_ui.domain.use_cases.local.home;

import ru.veritas.veritas_ui.data.repositories.HomeRepository;

@FunctionalInterface
public interface RemoveShortcutUseCase {
    void invoke(int page, int row, int col);

    static RemoveShortcutUseCase create(HomeRepository homeRepository) {
        return homeRepository::removeShortcut;
    }
}