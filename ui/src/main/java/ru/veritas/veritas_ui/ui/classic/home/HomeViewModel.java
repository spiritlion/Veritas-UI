package ru.veritas.veritas_ui.ui.classic.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.veritas.veritas_ui.core.command.Command;
import ru.veritas.veritas_ui.core.command.CommandFactory;
import ru.veritas.veritas_ui.core.entities.AppShortcut;
import ru.veritas.veritas_ui.ui.common.view.ToastData;

public class HomeViewModel extends ViewModel {
    private final CommandFactory.HomeScreen homeCommandFactory;
    private final CommandFactory.Favorites favoritesCommandFactory;
    private final CommandFactory.UseCase useCaseFactory;

    private final MutableLiveData<HomeScreenState> state = new MutableLiveData<>();
    private final MutableLiveData<List<List<AppShortcut>>> favoritesPages = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isDragging = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> dragEdge = new MutableLiveData<>(0);
    private final MutableLiveData<ToastData> toastMessage = new MutableLiveData<>();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final int FAVORITE_COLUMNS = 5; // количество иконок на странице избранного


    public HomeViewModel(CommandFactory.HomeScreen homeCommandFactory,
                         CommandFactory.Favorites favoritesCommandFactory,
                         CommandFactory.UseCase useCaseFactory) {
        this.homeCommandFactory = homeCommandFactory;
        this.favoritesCommandFactory = favoritesCommandFactory;
        this.useCaseFactory = useCaseFactory;
    }

    // --- Public LiveData ---
    public LiveData<HomeScreenState> getState() { return state; }
    public LiveData<List<List<AppShortcut>>> getFavoritesPages() { return favoritesPages; }
    public LiveData<Boolean> isDragging() { return isDragging; }
    public void setDragging(boolean dragging) { isDragging.setValue(dragging); }
    public LiveData<Integer> getDragEdge() { return dragEdge; }
    public void setDragEdge(int direction) { dragEdge.setValue(direction); }
    public LiveData<ToastData> getToastMessage() { return toastMessage; }

    // --- Загрузка данных ---
    public void loadShortcuts() {
        executor.execute(() -> {
            try {
                var shortcuts = useCaseFactory.getGetShortcutsUseCase().invoke();
                state.postValue(new HomeScreenState.Content(shortcuts));
            } catch (Exception e) {
                state.postValue(new HomeScreenState.Error(e.getMessage(), this::loadShortcuts, "Повторить"));
            }
        });
    }

    public void loadFavorites() {
        executor.execute(() -> {
            var raw = useCaseFactory.getGetFavoritesUseCase().invoke();
            var pages = convertToPages(raw);
            favoritesPages.postValue(pages);
        });
    }

    // --- Действия с рабочим столом ---
    public void addShortcutToDesktop(AppShortcut shortcut, int page, int row, int col) {
        execute(homeCommandFactory.createAddShortcutToDesktopCommand(shortcut, page, row, col));
    }

    public void moveShortcut(int fromPage, int fromRow, int fromCol,
                             int toPage, int toRow, int toCol) {
        execute(homeCommandFactory.createMoveShortcutCommand(fromPage, fromRow, fromCol, toPage, toRow, toCol));
    }

    public void swapShortcutWithFavoriteAndHome(int desktopPage, int desktopRow, int desktopCol,
                                                int favPage, int favPos) {
        execute(homeCommandFactory.createSwapShortcutWithFavoriteCommand(desktopPage, desktopRow, desktopCol, favPage, favPos));
    }

    // --- Действия с избранным ---
    public void addToFavorites(AppShortcut shortcut, int page, int pos) {
        execute(favoritesCommandFactory.createAddToFavoritesCommand(shortcut, page, pos));
    }

    public void removeFromFavorites(int page, int pos) {
        execute(favoritesCommandFactory.createRemoveFromFavoritesCommand(page, pos));
    }

    public void swapFavorites(int srcPage, int srcPos, int dstPage, int dstPos) {
        execute(favoritesCommandFactory.createSwapFavoritesCommand(srcPage, srcPos, dstPage, dstPos));
    }
    public void openSettings() {
        executor.execute(() ->
            useCaseFactory.getOpenSettingsUseCase().invoke());
    }

    // --- Drag source (для long press) ---
    public void setDragSource(int page, int row, int col) {
        // можно сохранить, если нужно
    }

    // --- Запуск приложения ---
    public void launchApp(String packageName) {
        useCaseFactory.getLaunchAppUseCase().invoke(packageName);
    }

    // --- Вспомогательные методы ---
    private void execute(Command<?> command) {
        executor.execute(() -> {
            try {
                command.execute();
                // После выполнения обновляем данные (перезагружаем)
                loadShortcuts();
                loadFavorites();
            } catch (Exception e) {
                toastMessage.postValue(ToastData.error("Ошибка: " + e.getMessage()));
            }
        });
    }

    private List<List<AppShortcut>> convertToPages(List<List<AppShortcut>> raw) {
        List<List<AppShortcut>> pages = new ArrayList<>();
        List<AppShortcut> currentPage = new ArrayList<>();
        for (List<AppShortcut> row : raw) {
            for (AppShortcut item : row) {
                currentPage.add(item);
                if (currentPage.size() == FAVORITE_COLUMNS) {
                    pages.add(new ArrayList<>(currentPage));
                    currentPage.clear();
                }
            }
        }
        if (!currentPage.isEmpty()) {
            pages.add(currentPage);
        }
        return pages;
    }

    @Override
    protected void onCleared() {
        executor.shutdown();
        super.onCleared();
    }
}