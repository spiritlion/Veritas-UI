package ru.veritas.veritas_ui.ui.classic.home;

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

import ru.veritas.veritas_ui.domain.entities.AppShortcut;
import ru.veritas.veritas_ui.domain.use_cases.local.LaunchAppUseCase;
import ru.veritas.veritas_ui.domain.use_cases.local.home.GetShortcutsUseCase;
import ru.veritas.veritas_ui.domain.use_cases.local.home.SetShortcutsUseCase;
import ru.veritas.veritas_ui.domain.use_cases.local.home.favorites.GetFavoritesUseCase;
import ru.veritas.veritas_ui.domain.use_cases.local.home.favorites.SetFavoritesUseCase;
import ru.veritas.veritas_ui.ui.common.view.ToastData;

public class HomeViewModel extends AndroidViewModel {

    // region --- Поля и конструктор ---

    private final MutableLiveData<HomeScreenState> state = new MutableLiveData<>();
    private final MutableLiveData<List<List<AppShortcut>>> favoritesPagesLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isMultiTouch = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> dragEdge = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> isDragging = new MutableLiveData<>(false);
    private final MutableLiveData<ToastData> toastMessage = new MutableLiveData<>();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final GetShortcutsUseCase getShortcutsUseCase;
    private final SetShortcutsUseCase setShortcutsUseCase;
    private final GetFavoritesUseCase getFavoritesUseCase;
    private final SetFavoritesUseCase setFavoritesUseCase;
    private final LaunchAppUseCase launchAppUseCase;

    private List<List<List<AppShortcut>>> currentShortcuts = new ArrayList<>();
    private final int[] dragSource = {-1, -1, -1}; // page, row, col
    private static final int FAVORITE_COLUMNS = 5;

    public HomeViewModel(@NonNull Application application,
                         GetShortcutsUseCase getShortcutsUseCase,
                         SetShortcutsUseCase setShortcutsUseCase,
                         GetFavoritesUseCase getFavoritesUseCase,
                         SetFavoritesUseCase setFavoritesUseCase,
                         LaunchAppUseCase launchAppUseCase) {
        super(application);
        this.getShortcutsUseCase = getShortcutsUseCase;
        this.setShortcutsUseCase = setShortcutsUseCase;
        this.getFavoritesUseCase = getFavoritesUseCase;
        this.setFavoritesUseCase = setFavoritesUseCase;
        this.launchAppUseCase = launchAppUseCase;
    }

    // endregion

    // region --- Публичные LiveData и геттеры/сеттеры для UI ---

    public LiveData<HomeScreenState> getState() {
        return state;
    }

    public LiveData<List<List<AppShortcut>>> getFavoritesPages() {
        return favoritesPagesLiveData;
    }

    public LiveData<Boolean> isDragging() {
        return isDragging;
    }

    public void setDragging(boolean dragging) {
        isDragging.setValue(dragging);
    }

    public LiveData<Integer> getDragEdge() {
        return dragEdge;
    }

    public void setDragEdge(int direction) {
        dragEdge.setValue(direction);
    }

    public LiveData<Boolean> getIsMultiTouch() {
        return isMultiTouch;
    }

    public void setMultiTouch(boolean multiTouch) {
        isMultiTouch.postValue(multiTouch);
    }

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

    public LiveData<ToastData> getToastMessage() {
        return toastMessage;
    }

    public void clearToastMessage() {
        toastMessage.setValue(null);
    }


    // endregion

    // region --- Загрузка данных (чтение) ---

    /**
     * Загрузка ярлыков рабочего стола
     */
    public void loadShortcuts() {
        state.postValue(HomeScreenState.Loading.INSTANCE);
        executor.execute(() -> {
            try {
                List<List<List<AppShortcut>>> loaded = getShortcutsUseCase.invoke();
                currentShortcuts = deepCopyDesktop(loaded);
                state.postValue(new HomeScreenState.Content(currentShortcuts));
            } catch (Exception e) {
                Log.e("Home Screen", "Ошибка загрузки рабочего стола: " + e.getMessage());
                state.postValue(new HomeScreenState.Error("Ошибка загрузки рабочего стола: " + e.getMessage(),
                        this::loadShortcuts, "Повторить попытку"));
            }
        });
    }

    /**
     * Загрузка ярлыков избранного
     */
    public void loadFavorites() {
        executor.execute(() -> {
            List<List<AppShortcut>> rawFavorites = getFavoritesUseCase.invoke();
            List<List<AppShortcut>> pages = convertRawFavoritesToPages(rawFavorites);
            favoritesPagesLiveData.postValue(pages);
        });
    }

    // endregion

    // region --- Операции с рабочим столом (add, delete, swap) ---

    /**
     * Добавить ярлык на рабочий стол в указанную ячейку.
     * Если ячейка занята, новый ярлык перезаписывает старый.
     */
    public void addShortcutToDesktop(@NonNull AppShortcut shortcut, int page, int row, int col) {
        executor.execute(() -> executeInTransaction(() -> {
            // Проверка границ
            if (page < 0 || page >= currentShortcuts.size()) return;
            List<List<AppShortcut>> pageData = currentShortcuts.get(page);
            if (row < 0 || row >= pageData.size()) return;
            List<AppShortcut> rowData = pageData.get(row);
            if (col < 0 || col >= rowData.size()) return;

            // Вставляем ярлык
            rowData.set(col, shortcut);
            }, () -> toastMessage.postValue(ToastData.error("Ошибка при добавлении"))));
    }

    /**
     * Удалить ярлык с рабочего стола (заменить на null).
     */
    public void removeShortcut(int page, int row, int col) {
        executor.execute(() -> executeInTransaction(() -> {
            if (page < 0 || page >= currentShortcuts.size()) return;
            List<List<AppShortcut>> pageData = currentShortcuts.get(page);
            if (row < 0 || row >= pageData.size()) return;
            List<AppShortcut> rowData = pageData.get(row);
            if (col < 0 || col >= rowData.size()) return;

            rowData.set(col, null);


        }, () -> toastMessage.postValue(ToastData.error("Не удалось удалить"))));
    }

    /**
     * Переместить ярлык (swap) между двумя ячейками рабочего стола.
     */
    public void moveShortcut(int fromPage, int fromRow, int fromCol,
                             int toPage, int toRow, int toCol) {
        executor.execute(() -> executeInTransaction(() -> {
            // region -- Валидация --
            if (fromPage < 0 || fromPage >= currentShortcuts.size() ||
                    toPage < 0 || toPage >= currentShortcuts.size())
                return;
            List<List<AppShortcut>> fromPageData = currentShortcuts.get(fromPage);
            List<List<AppShortcut>> toPageData = currentShortcuts.get(toPage);
            if (fromRow < 0 || fromRow >= fromPageData.size() ||
                    toRow < 0 || toRow >= toPageData.size())
                return;
            List<AppShortcut> fromRowData = fromPageData.get(fromRow);
            List<AppShortcut> toRowData = toPageData.get(toRow);
            if (fromCol < 0 || fromCol >= fromRowData.size() ||
                    toCol < 0 || toCol >= toRowData.size())
                return;
            // endregion

            AppShortcut fromItem = fromRowData.get(fromCol);
            AppShortcut toItem = toRowData.get(toCol);
            fromRowData.set(fromCol, toItem);
            toRowData.set(toCol, fromItem);



        }, () -> toastMessage.postValue(ToastData.error("Ошибка перемещения"))));
    }

    // endregion

    // region --- Операции с избранным (add. remove, swap) ---

    /**
     * Добавить ярлык в избранное (в первую свободную позицию).
     */
    public void addToFavorites(@NonNull AppShortcut shortcut) {
        executor.execute(() -> executeInTransaction(() -> {
            List<List<AppShortcut>> currentPages = favoritesPagesLiveData.getValue();
            if (currentPages == null) currentPages = new ArrayList<>();

            // Ищем первую null-ячейку во всех страницах
            for (int p = 0; p < currentPages.size(); p++) {
                List<AppShortcut> page = currentPages.get(p);
                for (int i = 0; i < page.size(); i++) {
                    if (page.get(i) == null) {
                        page.set(i, shortcut);
                        favoritesPagesLiveData.postValue(currentPages);

                    }
                }
            }

            // Если свободного места нет – создаём новую страницу
            List<AppShortcut> newPage = new ArrayList<>();
            newPage.add(shortcut);
            for (int i = 1; i < FAVORITE_COLUMNS; i++) newPage.add(null);
            currentPages.add(newPage);
            favoritesPagesLiveData.postValue(currentPages);

        }, () -> toastMessage.postValue(ToastData.error("Ошибка при добавлении"))));
    }

    public void addToFavorites(AppShortcut shortcut, int pageIndex, int targetPos) {
        executor.execute(() -> executeInTransaction(() -> {
            List<List<AppShortcut>> currentPages = favoritesPagesLiveData.getValue();
            if (currentPages == null) currentPages = new ArrayList<>();

            currentPages.get(pageIndex).set(targetPos, shortcut);
            favoritesPagesLiveData.postValue(currentPages);

        }, () -> toastMessage.postValue(ToastData.error("Ошибка при добавлении"))));
    }


    /**
     * Удалить ярлык из избранного по странице и позиции на странице.
     */
    public void removeFromFavorites(int pageIndex, int positionInPage) {
        executor.execute(() -> executeInTransaction(() -> {
            List<List<AppShortcut>> pages = favoritesPagesLiveData.getValue();
            if (pages == null || pageIndex < 0 || pageIndex >= pages.size())
                return;
            List<AppShortcut> page = pages.get(pageIndex);
            if (positionInPage < 0 || positionInPage >= page.size())
                return;

            page.set(positionInPage, null);
            favoritesPagesLiveData.postValue(pages);

        }, () -> toastMessage.postValue(ToastData.error("Не удалось удалить"))));
    }

    /**
     * Переместить ярлык внутри избранного (swap между двумя позициями).
     */
    public void swapFavorites(int srcPage, int srcPos, int dstPage, int dstPos) {
        executor.execute(() -> executeInTransaction(() -> {
            List<List<AppShortcut>> pages = favoritesPagesLiveData.getValue();
            if (pages == null) return;
            if (srcPage < 0 || srcPage >= pages.size() || dstPage < 0 || dstPage >= pages.size())
                return;
            List<AppShortcut> srcList = pages.get(srcPage);
            List<AppShortcut> dstList = pages.get(dstPage);
            if (srcPos < 0 || srcPos >= srcList.size() || dstPos < 0 || dstPos >= dstList.size())
                return;

            AppShortcut temp = srcList.get(srcPos);
            srcList.set(srcPos, dstList.get(dstPos));
            dstList.set(dstPos, temp);

            favoritesPagesLiveData.postValue(pages);

        }, () -> toastMessage.postValue(ToastData.error("Ошибка при перемещении"))));
    }

    // endregion


    /**
     * Обменять местами ярлык рабочего стола и ярлык избранного.
     */
    public void swapShortcutWithFavoriteAndHome(int desktopPage, int desktopRow, int desktopCol,
                                                int favPageIndex, int favPositionInPage) {
        executor.execute(() -> executeInTransaction(() -> {
            // Делаем рабочие копии для проверок
            List<List<List<AppShortcut>>> desktop = currentShortcuts;
            List<List<AppShortcut>> favPages = favoritesPagesLiveData.getValue();

            if (favPages == null) return;
            if (desktopPage < 0 || desktopPage >= desktop.size()) return;
            List<List<AppShortcut>> desktopPageData = desktop.get(desktopPage);
            if (desktopRow < 0 || desktopRow >= desktopPageData.size()) return;
            List<AppShortcut> desktopRowData = desktopPageData.get(desktopRow);
            if (desktopCol < 0 || desktopCol >= desktopRowData.size()) return;

            if (favPageIndex < 0 || favPageIndex >= favPages.size()) return;
            List<AppShortcut> favPage = favPages.get(favPageIndex);
            if (favPositionInPage < 0 || favPositionInPage >= favPage.size()) return;

            AppShortcut desktopItem = desktopRowData.get(desktopCol);
            AppShortcut favItem = favPage.get(favPositionInPage);

            // Обмен
            desktopRowData.set(desktopCol, favItem);
            favPage.set(favPositionInPage, desktopItem);

            // Обновляем UI
            state.postValue(new HomeScreenState.Content(desktop));
            favoritesPagesLiveData.postValue(favPages);
        }, () -> toastMessage.postValue(ToastData.error("ошибка при обменивании"))));
    }


    /**
     * Запуск приложения
     * @param packageName - пакет запускаемого приложения
     */
    public void launchApp(String packageName) {
        launchAppUseCase.invoke(packageName);
    }

    // region --- Вспомогательные методы (глубокое копирование, конвертация) ---

    private List<List<List<AppShortcut>>> deepCopyDesktop(List<List<List<AppShortcut>>> original) {
        List<List<List<AppShortcut>>> copy = new ArrayList<>();
        for (List<List<AppShortcut>> page : original) {
            List<List<AppShortcut>> pageCopy = new ArrayList<>();
            for (List<AppShortcut> row : page) {
                pageCopy.add(new ArrayList<>(row));
            }
            copy.add(pageCopy);
        }
        return copy;
    }

    private List<List<AppShortcut>> deepCopyFavoritesPages(List<List<AppShortcut>> original) {
        if (original == null) return new ArrayList<>();
        List<List<AppShortcut>> copy = new ArrayList<>();
        for (List<AppShortcut> page : original) {
            copy.add(new ArrayList<>(page));
        }
        return copy;
    }

    private List<List<AppShortcut>> convertRawFavoritesToPages(List<List<AppShortcut>> rawFavorites) {
        List<List<AppShortcut>> pages = new ArrayList<>();
        List<AppShortcut> currentPage = new ArrayList<>();
        for (List<AppShortcut> row : rawFavorites) {
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

    private List<List<AppShortcut>> convertFavoritesPagesToRaw(List<List<AppShortcut>> pages) {
        List<List<AppShortcut>> raw = new ArrayList<>();
        for (List<AppShortcut> page : pages) {
            for (int i = 0; i < page.size(); i += FAVORITE_COLUMNS) {
                int end = Math.min(i + FAVORITE_COLUMNS, page.size());
                raw.add(new ArrayList<>(page.subList(i, end)));
            }
        }
        return raw;
    }

    // endregion

    // region --- Unit of Work ---

    /**
     * Атомарно выполняет действие {@code action} с возможностью отката.
     * @param action   действие, изменяющее {@code currentShortcuts} и/или {@code favoritesPagesLiveData}
     * @param onError  вызывается при ошибке (логирование, уведомление UI)
     * @return         результат действия
     * @throws RuntimeException если транзакция не удалась (после отката)
     */
    private void executeInTransaction(Runnable action, Runnable onError) {
        List<List<List<AppShortcut>>> desktopSnapshot = deepCopyDesktop(currentShortcuts);
        List<List<AppShortcut>> favSnapshot = deepCopyFavoritesPages(favoritesPagesLiveData.getValue());

        try {
            action.run();
            setShortcutsUseCase.invoke(currentShortcuts);
            setFavoritesUseCase.invoke(convertFavoritesPagesToRaw(favoritesPagesLiveData.getValue()));

            state.postValue(new HomeScreenState.Content(currentShortcuts));
            favoritesPagesLiveData.postValue(favoritesPagesLiveData.getValue());
        } catch (Exception e) {
            rollback(desktopSnapshot, favSnapshot);
            if (onError != null) onError.run();
            throw new RuntimeException("Transaction failed", e);
        }
    }

    private void rollback(List<List<List<AppShortcut>>> desktopSnapshot,
                          List<List<AppShortcut>> favSnapshot) {
        currentShortcuts = deepCopyDesktop(desktopSnapshot);
        favoritesPagesLiveData.postValue(deepCopyFavoritesPages(favSnapshot));
        state.postValue(new HomeScreenState.Content(currentShortcuts));
    }

    // endregion

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}