package ru.veritas.veritas_ui.ui.classic.settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SettingsViewModel extends ViewModel {
    private final MutableLiveData<String> someSetting = new MutableLiveData<>();

    public LiveData<String> getSomeSetting() {
        return someSetting;
    }

    public void saveSetting(String value) {
        // Тут может быть вызов use case
        someSetting.setValue(value);
    }
}