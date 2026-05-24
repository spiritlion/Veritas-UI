package ru.veritas.veritas_ui.ui.classic.apps;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import ru.veritas.veritas_ui.data.loaders.AndroidAppLauncher;
import ru.veritas.veritas_ui.data.repositories.AppRepositoryImpl;
import ru.veritas.veritas_ui.data.source.local.PackageManagerDataSource;
import ru.veritas.veritas_ui.di.DependencyContainer;
import ru.veritas.veritas_ui.domain.use_cases.local.GetInstalledAppsUseCase;
import ru.veritas.veritas_ui.domain.use_cases.local.LaunchAppUseCase;

/**
 * Создаёт {@link AppsScreenViewModel}
 */
public class AppsScreenViewModelFactory implements ViewModelProvider.Factory {
    private final DependencyContainer dependencyContainer;

    public AppsScreenViewModelFactory(DependencyContainer appContainer) {
        this.dependencyContainer = appContainer;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(AppsScreenViewModel.class)) {
            return (T) new AppsScreenViewModel(
                    (Application) dependencyContainer.getContext(),
                    dependencyContainer.getGetInstalledAppsUseCase(),
                    dependencyContainer.getLaunchAppUseCase()
            );
        }
        throw new IllegalArgumentException("Unknown ViewModel");
    }
}