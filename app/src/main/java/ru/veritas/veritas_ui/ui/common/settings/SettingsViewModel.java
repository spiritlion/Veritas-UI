package ru.veritas.veritas_ui.ui.common.settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import ru.veritas.veritas_ui.domain.repositories.SettingsRepository;

public class SettingsViewModel extends ViewModel {
    private final SettingsRepository repository;
    private final MutableLiveData<Integer> rows = new MutableLiveData<>();
    private final MutableLiveData<Integer> columns = new MutableLiveData<>();
    private final MutableLiveData<Integer> pages = new MutableLiveData<>();
    private final MutableLiveData<Integer> padding = new MutableLiveData<>();

    public SettingsViewModel(SettingsRepository repository) {
        this.repository = repository;
        loadSettings();
    }

    private void loadSettings() {
        rows.setValue(repository.getRows());
        columns.setValue(repository.getColumns());
        pages.setValue(repository.getPages());
        padding.setValue(repository.getPadding());
    }

    public LiveData<Integer> getRows() { return rows; }
    public LiveData<Integer> getColumns() { return columns; }
    public LiveData<Integer> getPages() { return pages; }
    public LiveData<Integer> getPadding() { return padding; }

    public void setRows(int value) {
        repository.saveRows(value);
        rows.setValue(value);
    }

    public void setColumns(int value) {
        repository.saveColumns(value);
        columns.setValue(value);
    }

    public void setPages(int value) {
        repository.savePages(value);
        pages.setValue(value);
    }

    public void setPadding(int value) {
        repository.savePadding(value);
        padding.setValue(value);
    }
}