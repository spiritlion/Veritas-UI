package ru.veritas.veritas_ui.ui.classic.main.home;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import ru.veritas.veritas_ui.data.repositories.HomeRepository;
import ru.veritas.veritas_ui.data.repositories.HomeRepositoryImpl;
import ru.veritas.veritas_ui.domain.use_cases.local.home.AddShortcutFirstUseCase;
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
            HomeRepositoryImpl homeRepository = new HomeRepositoryImpl(context);
            HomeRepositoryImpl.FavoritesImpl favoritesRep = homeRepository.new FavoritesImpl();
            PackageManager pm = context.getPackageManager();

            GetShortcutsUseCase getUseCase = GetShortcutsUseCase.create(homeRepository);
            AddShortcutUseCase addUseCase = AddShortcutUseCase.create(homeRepository);
            AddShortcutFirstUseCase addFirstUseCase = AddShortcutFirstUseCase.create(homeRepository);
            MoveShortcutUseCase moveUseCase = MoveShortcutUseCase.create(homeRepository);
            SetShortcutsUseCase setUseCase = SetShortcutsUseCase.create(homeRepository);
            RemoveShortcutUseCase removeUseCase = RemoveShortcutUseCase.create(homeRepository);

            GetFavoritesUseCase getFavorUseCase = GetFavoritesUseCase.create(favoritesRep);
            SetFavoritesUseCase setFavoritesUseCase = SetFavoritesUseCase.create(favoritesRep);

            return (T) new HomeViewModel(
                    (android.app.Application) context.getApplicationContext(),
                    getUseCase,
                    addUseCase,
                    addFirstUseCase,
                    moveUseCase,
                    setUseCase,
                    removeUseCase,
                    getFavorUseCase,
                    setFavoritesUseCase
            );
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}