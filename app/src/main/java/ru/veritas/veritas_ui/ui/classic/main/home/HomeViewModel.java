package ru.veritas.veritas_ui.ui.classic.main.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.veritas.veritas_ui.domain.entities.AppShortcut;
import ru.veritas.veritas_ui.domain.use_cases.local.home.AddShortcutUseCase;
import ru.veritas.veritas_ui.domain.use_cases.local.home.GetShortcutsUseCase;
import ru.veritas.veritas_ui.domain.use_cases.local.home.RemoveShortcutUseCase;

public class HomeViewModel extends AndroidViewModel {
    private final MutableLiveData<List<AppShortcut>> shortcuts = new MutableLiveData<>();
    private final GetShortcutsUseCase GetShortcutsUseCase;
    private final AddShortcutUseCase AddShortcutUseCase;
    private final RemoveShortcutUseCase RemoveShortcutUseCase;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public HomeViewModel(@NonNull Application application,
                         GetShortcutsUseCase GetShortcutsUseCase,
                         AddShortcutUseCase AddShortcutUseCase,
                         RemoveShortcutUseCase RemoveShortcutUseCase) {
        super(application);
        this.GetShortcutsUseCase = GetShortcutsUseCase;
        this.AddShortcutUseCase = AddShortcutUseCase;
        this.RemoveShortcutUseCase = RemoveShortcutUseCase;
    }

    public LiveData<List<AppShortcut>> getShortcuts() {
        return shortcuts;
    }

    public void loadShortcuts() {
        executor.execute(() -> {
            List<AppShortcut> list = GetShortcutsUseCase.invoke();
            shortcuts.postValue(list);
        });
    }

    public void addShortcut(String packageName) {
        executor.execute(() -> {
            AddShortcutUseCase.invoke(packageName);
            loadShortcuts(); // обновляем список
        });
    }

    public void removeShortcut(String packageName) {
        executor.execute(() -> {
            RemoveShortcutUseCase.execute(packageName);
            loadShortcuts();
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}