package ru.veritas.veritas_ui.ui.classic.main.home;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import ru.veritas.veritas_ui.data.repositories.HomeRepositoryImpl;
import ru.veritas.veritas_ui.domain.use_cases.local.home.AddShortcutUseCase;
import ru.veritas.veritas_ui.domain.use_cases.local.home.GetShortcutsUseCase;
import ru.veritas.veritas_ui.domain.use_cases.local.home.MoveShortcutUseCase;
import ru.veritas.veritas_ui.domain.use_cases.local.home.RemoveShortcutUseCase;

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
            PackageManager pm = context.getPackageManager();
            GetShortcutsUseCase getUseCase = new GetShortcutsUseCase(homeRepository, pm);
            AddShortcutUseCase addUseCase = new AddShortcutUseCase(homeRepository, pm);
            MoveShortcutUseCase moveUseCase = new MoveShortcutUseCase(homeRepository);
            RemoveShortcutUseCase removeUseCase = new RemoveShortcutUseCase(homeRepository);
            return (T) new HomeViewModel(
                    (android.app.Application) context.getApplicationContext(),
                    getUseCase,
                    addUseCase,
                    moveUseCase,
                    removeUseCase
            );
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}