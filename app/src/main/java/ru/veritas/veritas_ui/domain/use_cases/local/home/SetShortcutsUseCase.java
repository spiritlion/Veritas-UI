package ru.veritas.veritas_ui.domain.use_cases.local.home;

import java.util.List;
import ru.veritas.veritas_ui.data.repositories.HomeRepository;
import ru.veritas.veritas_ui.domain.entities.AppShortcutDTO;

@FunctionalInterface
public interface SetShortcutsUseCase {
    void invoke(List<List<List<AppShortcutDTO>>> shortcuts);

    static SetShortcutsUseCase create(HomeRepository homeRepository) {
        return homeRepository::saveShortcuts;
    }
}