package ru.veritas.veritas_ui.domain.use_cases.local;

import java.util.List;

import ru.veritas.veritas_ui.domain.repositories.AppRepository;
import ru.veritas.veritas_ui.domain.entities.AppShortcut;

public class GetInstalledAppsUseCase {
    private final AppRepository repository;

    public GetInstalledAppsUseCase(AppRepository repository) {
        this.repository = repository;
    }

    public List<AppShortcut> invoke() {
        return repository.getInstalledApps();
    }
}