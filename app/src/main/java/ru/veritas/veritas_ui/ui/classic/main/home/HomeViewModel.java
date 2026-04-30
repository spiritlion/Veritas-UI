package ru.veritas.veritas_ui.ui.classic.main.home;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.veritas.veritas_ui.domain.entities.AppShortcutDTO;
import ru.veritas.veritas_ui.domain.use_cases.local.home.AddShortcutFirstUseCase;
import ru.veritas.veritas_ui.domain.use_cases.local.home.AddShortcutUseCase;
import ru.veritas.veritas_ui.domain.use_cases.local.home.GetShortcutsUseCase;
import ru.veritas.veritas_ui.domain.use_cases.local.home.MoveShortcutUseCase;
import ru.veritas.veritas_ui.domain.use_cases.local.home.RemoveShortcutUseCase;
import ru.veritas.veritas_ui.domain.use_cases.local.home.SetShortcutsUseCase;

public class HomeViewModel extends AndroidViewModel {
    private final MutableLiveData<HomeScreenState> state = new MutableLiveData<>();
    private final MutableLiveData<HomeScreenMode> mode = new MutableLiveData<>(HomeScreenMode.Base);
    private final GetShortcutsUseCase GetShortcutsUseCase;
    private final AddShortcutUseCase AddShortcutUseCase;
    private final AddShortcutFirstUseCase AddShortcutFirstUseCase;
    private final MoveShortcutUseCase MoveShortcutUseCase;
    private final SetShortcutsUseCase SetShortcutsUseCase;
    private final RemoveShortcutUseCase RemoveShortcutUseCase;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<Boolean> isMultiTouch = new MutableLiveData<>(false);

    private final List<List<List<AppShortcutDTO>>> currentShortcuts = new ArrayList<>();

    public HomeViewModel(@NonNull Application application,
                         GetShortcutsUseCase GetShortcutsUseCase,
                         AddShortcutUseCase AddShortcutUseCase,
                         AddShortcutFirstUseCase AddShortcutFirstUseCase,
                         MoveShortcutUseCase MoveShortcutUseCase,
                         SetShortcutsUseCase SetShortcutsUseCase,
                         RemoveShortcutUseCase RemoveShortcutUseCase) {
        super(application);
        this.GetShortcutsUseCase = GetShortcutsUseCase;
        this.AddShortcutUseCase = AddShortcutUseCase;
        this.AddShortcutFirstUseCase = AddShortcutFirstUseCase;
        this.MoveShortcutUseCase = MoveShortcutUseCase;
        this.SetShortcutsUseCase = SetShortcutsUseCase;
        this.RemoveShortcutUseCase = RemoveShortcutUseCase;
    }

    public void changeMode(HomeScreenMode mode) {
        this.mode.postValue(mode);
    }
    
    public void loadShortcuts() {
        state.postValue(HomeScreenState.Loading.INSTANCE);
        executor.execute(() -> {
            try {
                List<List<List<AppShortcutDTO>>> loaded = GetShortcutsUseCase.invoke();
                currentShortcuts.clear();
                currentShortcuts.addAll(loaded);
                state.postValue(new HomeScreenState.Content(currentShortcuts, mode.getValue()));
            } catch (Exception e) {
                Log.e("Home Screen", "Ошибка загрузки рабочего стола" + e.getMessage());
                state.postValue(new HomeScreenState.Error("Ошибка загрузки рабочего стола" + e.getMessage(), this::loadShortcuts, "Повторить попытку"));
            }
        });
    }

    public void addShortcut(AppShortcutDTO shortcut) {
        executor.execute(() -> {
            AddShortcutFirstUseCase.invoke(shortcut);
            loadShortcuts(); // обновляем список
        });
    }

    public void moveShortcut(int fromPage, int fromRow, int fromCol, int toPage, int toRow, int toCol) {
        // Проверки границ перед выполнением
        final List<List<List<AppShortcutDTO>>> snapshot = deepCopy(currentShortcuts);
        if (fromPage < 0 || toPage < 0 || fromPage >= snapshot.size() || toPage >= snapshot.size()) return;
        if (fromRow < 0 || toRow < 0 || fromRow >= snapshot.get(fromPage).size() || toRow >= snapshot.get(toPage).size()) return;
        if (fromCol < 0 || toCol < 0 || fromCol >= snapshot.get(fromPage).get(fromRow).size() || toCol >= snapshot.get(toPage).get(toRow).size()) return;

        executor.execute(() -> {
            // Глубокая копия
            List<List<List<AppShortcutDTO>>> updated = deepCopy(snapshot);

            AppShortcutDTO fromItem = updated.get(fromPage).get(fromRow).get(fromCol);
            AppShortcutDTO toItem = updated.get(toPage).get(toRow).get(toCol);
            updated.get(fromPage).get(fromRow).set(fromCol, toItem);
            updated.get(toPage).get(toRow).set(toCol, fromItem);

            // Замена in‑memory
            currentShortcuts.clear();
            currentShortcuts.addAll(updated);

            // Оповещение UI
            state.postValue(new HomeScreenState.Content(updated, mode.getValue()));
            scheduleSave(updated);
        });
    }

    private List<List<List<AppShortcutDTO>>> deepCopy(List<List<List<AppShortcutDTO>>> original) {
        List<List<List<AppShortcutDTO>>> copy = new ArrayList<>();
        for (List<List<AppShortcutDTO>> page : original) {
            List<List<AppShortcutDTO>> pageCopy = new ArrayList<>();
            for (List<AppShortcutDTO> row : page) {
                pageCopy.add(new ArrayList<>(row)); // копия строки
            }
            copy.add(pageCopy);
        }
        return copy;
    }

    private void scheduleSave(List<List<List<AppShortcutDTO>>> shortcuts) {
        executor.execute(() -> SetShortcutsUseCase.invoke(shortcuts));
    }

    private final int[] dragSource = {-1, -1, -1}; // page, row, col

    public void setDragSource(int page, int row, int col) {
        dragSource[0] = page;
        dragSource[1] = row;
        dragSource[2] = col;
    }

    public int[] getDragSource() {
        return dragSource;
    }

    public void clearDragSource() {
        dragSource[0] = -1;
        dragSource[1] = -1;
        dragSource[2] = -1;
    }


    private void saveToDisk() {
        SetShortcutsUseCase.invoke(currentShortcuts);
    }

    public void removeShortcut(int page, int row, int col) {
        final List<List<List<AppShortcutDTO>>> snapshot = currentShortcuts;
        if (page < 0 || page >= snapshot.size()) return;
        if (row < 0 || row >= snapshot.get(page).size()) return;
        if (col < 0 || col >= snapshot.get(page).get(row).size()) return;

        executor.execute(() -> {
            List<List<List<AppShortcutDTO>>> updated = deepCopy(snapshot);
            updated.get(page).get(row).set(col, null);

            currentShortcuts.clear();
            currentShortcuts.addAll(updated);
            state.postValue(new HomeScreenState.Content(updated, mode.getValue()));
            scheduleSave(updated);
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