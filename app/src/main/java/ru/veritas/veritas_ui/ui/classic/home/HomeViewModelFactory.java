package ru.veritas.veritas_ui.ui.classic.home;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import ru.veritas.veritas_ui.data.loaders.AndroidAppLauncher;
import ru.veritas.veritas_ui.data.repositories.FavoritesRepositoryImpl;
import ru.veritas.veritas_ui.data.repositories.HomeRepositoryImpl;
import ru.veritas.veritas_ui.domain.repositories.FavoritesRepository;
import ru.veritas.veritas_ui.domain.repositories.HomeRepository;
import ru.veritas.veritas_ui.domain.use_cases.local.LaunchAppUseCase;
import ru.veritas.veritas_ui.domain.use_cases.local.home.AddShortcutUseCase;
import ru.veritas.veritas_ui.domain.use_cases.local.home.GetShortcutsUseCase;
import ru.veritas.veritas_ui.domain.use_cases.local.home.MoveShortcutUseCase;
import ru.veritas.veritas_ui.domain.use_cases.local.home.RemoveShortcutUseCase;
import ru.veritas.veritas_ui.domain.use_cases.local.home.SetShortcutsUseCase;
import ru.veritas.veritas_ui.domain.use_cases.local.home.favorites.GetFavoritesUseCase;
import ru.veritas.veritas_ui.domain.use_cases.local.home.favorites.SetFavoritesUseCase;

public class HomeViewModelFactory implements ViewModelProvider.Factory {
    private final Context context;

    public HomeViewModelFactory(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(HomeViewModel.class)) {
            HomeRepository homeRepository = new HomeRepositoryImpl(context);
            FavoritesRepository favoritesRepository = new FavoritesRepositoryImpl(context);

            GetShortcutsUseCase getShortcutsUseCase = new GetShortcutsUseCase(homeRepository);
            AddShortcutUseCase addShortcutUseCase = new AddShortcutUseCase(homeRepository);
            MoveShortcutUseCase moveShortcutUseCase = new MoveShortcutUseCase(homeRepository);
            SetShortcutsUseCase setShortcutsUseCase = new SetShortcutsUseCase(homeRepository);
            RemoveShortcutUseCase removeShortcutUseCase = new RemoveShortcutUseCase(homeRepository);
            GetFavoritesUseCase getFavoritesUseCase = new GetFavoritesUseCase(favoritesRepository);
            SetFavoritesUseCase setFavoritesUseCase = new SetFavoritesUseCase(favoritesRepository);
            LaunchAppUseCase launchAppUseCase = new LaunchAppUseCase(new AndroidAppLauncher(context));

            return (T) new HomeViewModel(
                    (android.app.Application) context.getApplicationContext(),
                    getShortcutsUseCase,
                    addShortcutUseCase,
                    moveShortcutUseCase,
                    setShortcutsUseCase,
                    removeShortcutUseCase,
                    getFavoritesUseCase,
                    setFavoritesUseCase,
                    launchAppUseCase
            );
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}