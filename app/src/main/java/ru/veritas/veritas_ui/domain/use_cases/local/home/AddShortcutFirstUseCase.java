package ru.veritas.veritas_ui.domain.use_cases.local.home;

import ru.veritas.veritas_ui.data.repositories.HomeRepository;
import ru.veritas.veritas_ui.domain.entities.AppShortcutDTO;

@FunctionalInterface
public interface AddShortcutFirstUseCase {
    void invoke(AppShortcutDTO shortcut);

    static AddShortcutFirstUseCase create(HomeRepository homeRepository) {
        return homeRepository::addShortcut;
    }
}