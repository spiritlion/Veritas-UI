package ru.veritas.veritas_ui.domain.use_cases.local;

import java.util.List;

import ru.veritas.veritas_ui.data.repositories.AppRepository;
import ru.veritas.veritas_ui.domain.entities.AppShortcutDTO;

@FunctionalInterface
public interface GetInstalledAppsUseCase {
    List<AppShortcutDTO> invoke();

    static GetInstalledAppsUseCase create(AppRepository repository) {
        return repository::getInstalledApps;
    }
}