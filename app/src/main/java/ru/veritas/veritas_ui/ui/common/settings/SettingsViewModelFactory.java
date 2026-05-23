// ui/classic/settings/SettingsViewModelFactory.java
package ru.veritas.veritas_ui.ui.common.settings;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import ru.veritas.veritas_ui.data.repositories.SettingsRepositoryImpl;
import ru.veritas.veritas_ui.domain.repositories.SettingsRepository;

public class SettingsViewModelFactory implements ViewModelProvider.Factory {
    private final Context context;

    public SettingsViewModelFactory(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(SettingsViewModel.class)) {
            SettingsRepository repository = new SettingsRepositoryImpl(context);
            return (T) new SettingsViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}