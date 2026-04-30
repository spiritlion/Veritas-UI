package ru.veritas.veritas_ui.domain.use_cases.local.home;

import java.util.List;

import ru.veritas.veritas_ui.data.repositories.HomeRepository;
import ru.veritas.veritas_ui.domain.entities.AppShortcutDTO;

@FunctionalInterface
public interface GetShortcutsUseCase {

    List<List<List<AppShortcutDTO>>> invoke();

    static GetShortcutsUseCase create(HomeRepository homeRepository) {
        return homeRepository::getShortcuts;
    }
}