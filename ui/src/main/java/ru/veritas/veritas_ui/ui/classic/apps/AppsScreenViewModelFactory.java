package ru.veritas.veritas_ui.ui.classic.apps;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import ru.veritas.veritas_ui.core.command.CommandFactory;

/**
 * Создаёт {@link AppsScreenViewModel}
 */
public class AppsScreenViewModelFactory implements ViewModelProvider.Factory {
    private final CommandFactory.UseCase useCaseFactory;

    public AppsScreenViewModelFactory(CommandFactory.UseCase useCaseFactory) {
        this.useCaseFactory = useCaseFactory;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(AppsScreenViewModel.class)) {
            return (T) new AppsScreenViewModel(
                    useCaseFactory
            );
        }
        throw new IllegalArgumentException("Unknown ViewModel");
    }
}