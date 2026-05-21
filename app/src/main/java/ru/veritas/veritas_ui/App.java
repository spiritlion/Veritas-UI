package ru.veritas.veritas_ui;

import android.app.Application;
import android.content.Context;

import ru.veritas.veritas_ui.data.repositories.AppRepository;
import ru.veritas.veritas_ui.data.repositories.AppRepositoryImpl;
import ru.veritas.veritas_ui.data.source.local.PackageManagerDataSource;
import ru.veritas.veritas_ui.domain.use_cases.local.GetInstalledAppsUseCase;
import ru.veritas.veritas_ui.domain.use_cases.local.LaunchAppUseCase;

public class App extends Application {
    private AppContainer appContainer;

    @Override
    public void onCreate() {
        super.onCreate();
        appContainer = new AppContainer(this);
    }

    public AppContainer getAppContainer() {
        return appContainer;
    }
}

