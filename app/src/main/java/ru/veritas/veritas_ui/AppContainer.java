package ru.veritas.veritas_ui;

import android.content.Context;

import ru.veritas.veritas_ui.data.repositories.AppRepository;
import ru.veritas.veritas_ui.data.repositories.AppRepositoryImpl;
import ru.veritas.veritas_ui.data.source.local.PackageManagerDataSource;
import ru.veritas.veritas_ui.domain.use_cases.local.GetInstalledAppsUseCase;
import ru.veritas.veritas_ui.domain.use_cases.local.LaunchAppUseCase;

public class AppContainer {
    private final Context context;
    public final PackageManagerDataSource dataSource;
    public final AppRepository repository;
    public final GetInstalledAppsUseCase getInstalledAppsUseCase;
    public final LaunchAppUseCase launchAppUseCase;

    public AppContainer(App context) {
        this.context = context;
        dataSource = new PackageManagerDataSource(context);
        repository = new AppRepositoryImpl(dataSource);
        getInstalledAppsUseCase = GetInstalledAppsUseCase.create(repository);
        launchAppUseCase = LaunchAppUseCase.create(context);
    }
}
