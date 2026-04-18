package ru.veritas.veritas_ui.ui.classic.main.home;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.veritas.veritas_ui.domain.entities.AppShortcutDTO;
import ru.veritas.veritas_ui.domain.use_cases.local.home.AddShortcutUseCase;
import ru.veritas.veritas_ui.domain.use_cases.local.home.GetShortcutsUseCase;
import ru.veritas.veritas_ui.domain.use_cases.local.home.MoveShortcutUseCase;
import ru.veritas.veritas_ui.domain.use_cases.local.home.RemoveShortcutUseCase;

public class HomeViewModel extends AndroidViewModel {
    private final MutableLiveData<HomeScreenState> state = new MutableLiveData<>();
    private final MutableLiveData<HomeScreenMode> mode = new MutableLiveData<>(HomeScreenMode.Base);
    private final GetShortcutsUseCase GetShortcutsUseCase;
    private final AddShortcutUseCase AddShortcutUseCase;
    private final MoveShortcutUseCase moveShortcutUseCase;
    private final RemoveShortcutUseCase RemoveShortcutUseCase;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<Boolean> isMultiTouch = new MutableLiveData<>(false);


    public HomeViewModel(@NonNull Application application,
                         GetShortcutsUseCase GetShortcutsUseCase,
                         AddShortcutUseCase AddShortcutUseCase, MoveShortcutUseCase moveShortcutUseCase,
                         RemoveShortcutUseCase RemoveShortcutUseCase) {
        super(application);
        this.GetShortcutsUseCase = GetShortcutsUseCase;
        this.AddShortcutUseCase = AddShortcutUseCase;
        this.moveShortcutUseCase = moveShortcutUseCase;
        this.RemoveShortcutUseCase = RemoveShortcutUseCase;
    }

    public void changeMode(HomeScreenMode mode) {
        this.mode.postValue(mode);
    }
    
    public void loadShortcuts() {
        state.postValue(HomeScreenState.Loading.INSTANCE);
        executor.execute(() -> {
            try {
                List<List<List<AppShortcutDTO>>> list = GetShortcutsUseCase.invoke();
                Log.d("Home Screen", (list == null) + "");
                state.postValue(new HomeScreenState.Content(list, mode.getValue()));
            } catch (Exception e) {
                Log.e("Home Screen", "Ошибка загрузки рабочего стола" + e.getMessage());
                state.postValue(new HomeScreenState.Error("Ошибка загрузки рабочего стола" + e.getMessage(), this::loadShortcuts, "Повторить попытку"));
            }
        });
    }

    public void addShortcut(AppShortcutDTO shortcut) {
        executor.execute(() -> {
            AddShortcutUseCase.invoke(shortcut);
            loadShortcuts(); // обновляем список
        });
    }

    public void moveShortcut(int fromPage, int fromRow, int fromCol,
                             int toPage, int toRow, int toCol) {
        executor.execute(() -> {
            moveShortcutUseCase.invoke(fromPage, fromRow, fromCol, toPage, toRow, toCol);
            loadShortcuts(); // refresh UI
        });
    }

    public void removeShortcut(int i, int j, int k) {
        executor.execute(() -> {
            RemoveShortcutUseCase.invoke(i, j, k);
            loadShortcuts();
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }

    public LiveData<HomeScreenState> getState() {
        return state;
    }

    public MutableLiveData<HomeScreenMode> getMode() {
        return mode;
    }


    public void setMultiTouch(boolean multiTouch) {
        isMultiTouch.postValue(multiTouch);
    }

    public LiveData<Boolean> getIsMultiTouch() {
        return isMultiTouch;
    }
}