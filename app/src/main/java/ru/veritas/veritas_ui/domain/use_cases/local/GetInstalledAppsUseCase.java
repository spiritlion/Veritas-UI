package ru.veritas.veritas_ui.domain.use_cases.local;

import ru.veritas.veritas_ui.data.repositories.AppRepository;
import ru.veritas.veritas_ui.domain.entities.AppInfoEntity;

import java.util.List;

public class GetInstalledAppsUseCase {
    private final AppRepository repository;

    public GetInstalledAppsUseCase(AppRepository repository) {
        this.repository = repository;
    }

    /**
     * Возращает все установленные приложения, имеющие лаунчер
     */
    public List<AppInfoEntity> invoke() {
        return repository.getInstalledApps();
    }
}