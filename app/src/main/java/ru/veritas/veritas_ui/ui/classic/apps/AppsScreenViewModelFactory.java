package ru.veritas.veritas_ui.ui.classic.apps;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import ru.veritas.veritas_ui.di.DependencyContainer;

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
                    dependencyContainer.getGetInstalledAppsUseCase(),
                    dependencyContainer.getLaunchAppUseCase()
            );
        }
        throw new IllegalArgumentException("Unknown ViewModel");
    }
}