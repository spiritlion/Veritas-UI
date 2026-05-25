package ru.veritas.veritas_ui.ui.classic.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import ru.veritas.veritas_ui.di.DependencyContainer;

public class HomeViewModelFactory implements ViewModelProvider.Factory {
    private final DependencyContainer dependencyContainer;

    public HomeViewModelFactory(DependencyContainer dependencyContainer) {
        this.dependencyContainer = dependencyContainer;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(HomeViewModel.class)) {
            return (T) new HomeViewModel(
                    dependencyContainer.getCommandFactory(),
                    dependencyContainer.getGetShortcutsUseCase(),
                    dependencyContainer.getGetFavoritesUseCase(),
                    dependencyContainer.getLaunchAppUseCase()
            );
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}