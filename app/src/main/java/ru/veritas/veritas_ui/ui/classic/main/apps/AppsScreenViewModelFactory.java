package ru.veritas.veritas_ui.ui.classic.main.apps;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import ru.veritas.veritas_ui.data.repositories.AppRepositoryImpl;
import ru.veritas.veritas_ui.data.source.local.PackageManagerDataSource;
import ru.veritas.veritas_ui.domain.use_cases.local.GetInstalledAppsUseCase;
import ru.veritas.veritas_ui.domain.use_cases.local.LaunchAppUseCase;

/**
 * Создаёт {@link AppsScreenViewModel}
 */
public class AppsScreenViewModelFactory implements ViewModelProvider.Factory {

    private final Context context;

    public AppsScreenViewModelFactory(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(AppsScreenViewModel.class)) {
            PackageManagerDataSource dataSource = new PackageManagerDataSource(context);
            AppRepositoryImpl repository = new AppRepositoryImpl(dataSource);
            GetInstalledAppsUseCase getAppsUseCase = GetInstalledAppsUseCase.create(repository);
            LaunchAppUseCase launchAppUseCase = LaunchAppUseCase.create(context);
            return (T) new AppsScreenViewModel((android.app.Application) context.getApplicationContext(), getAppsUseCase, launchAppUseCase);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}