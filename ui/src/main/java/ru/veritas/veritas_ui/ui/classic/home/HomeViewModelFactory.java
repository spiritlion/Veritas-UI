package ru.veritas.veritas_ui.ui.classic.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import ru.veritas.veritas_ui.core.command.CommandFactory;

public class HomeViewModelFactory implements ViewModelProvider.Factory {
    private final CommandFactory.HomeScreen homeCommandFactory;
    private final CommandFactory.Favorites favoritesCommandFactory;
    private final CommandFactory.UseCase useCaseFactory;

    public HomeViewModelFactory(CommandFactory.HomeScreen homeCommandFactory, CommandFactory.Favorites favoritesCommandFactory, CommandFactory.UseCase useCaseFactory) {
        this.homeCommandFactory = homeCommandFactory;
        this.favoritesCommandFactory = favoritesCommandFactory;
        this.useCaseFactory = useCaseFactory;
    }


    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(HomeViewModel.class)) {
            return (T) new HomeViewModel(
                    homeCommandFactory,
                    favoritesCommandFactory,
                    useCaseFactory
            );
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}