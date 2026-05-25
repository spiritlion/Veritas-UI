package ru.veritas.veritas_ui.domain.command.local;

import java.util.List;

import ru.veritas.veritas_ui.domain.entities.AppShortcut;
import ru.veritas.veritas_ui.domain.repositories.AppRepository;

public class GetInstalledAppsUseCase {
    private final AppRepository repository;

    public GetInstalledAppsUseCase(AppRepository repository) {
        this.repository = repository;
    }

    public List<AppShortcut> invoke() {
        return repository.getInstalledApps();
    }
}