package ru.veritas.veritas_ui.core.command.local;

import java.util.List;

import ru.veritas.veritas_ui.core.entities.AppShortcut;
import ru.veritas.veritas_ui.core.repositories.AppRepository;

public class GetInstalledAppsUseCase {
    private final AppRepository repository;

    public GetInstalledAppsUseCase(AppRepository repository) {
        this.repository = repository;
    }

    public List<AppShortcut> invoke() {
        return repository.getInstalledApps();
    }
}