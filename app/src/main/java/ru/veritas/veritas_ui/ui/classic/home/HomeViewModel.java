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
import ru.veritas.veritas_ui.domain.use_cases.local.GetInstalledAppsUseCase;
import ru.veritas.veritas_ui.domain.use_cases.local.LaunchAppUseCase;
import ru.veritas.veritas_ui.domain.use_cases.local.home.AddShortcutUseCase;
import ru.veritas.veritas_ui.domain.use_cases.local.home.favorites.GetFavoritesUseCase;
import ru.veritas.veritas_ui.domain.use_cases.local.home.GetShortcutsUseCase;
import ru.veritas.veritas_ui.domain.use_cases.local.home.MoveShortcutUseCase;
import ru.veritas.veritas_ui.domain.use_cases.local.home.RemoveShortcutUseCase;
import ru.veritas.veritas_ui.domain.use_cases.local.home.favorites.SetFavoritesUseCase;
import ru.veritas.veritas_ui.domain.use_cases.local.home.SetShortcutsUseCase;

public class HomeViewModel extends AndroidViewModel {
    private final MutableLiveData<HomeScreenState> state = new MutableLiveData<>();
    private final GetShortcutsUseCase getShortcutsUseCase;
    private final AddShortcutUseCase addShortcutUseCase;
    private final MoveShortcutUseCase moveShortcutUseCase;
    private final SetShortcutsUseCase setShortcutsUseCase;
    private final RemoveShortcutUseCase removeShortcutUseCase;
    private final MutableLiveData<List<AppShortcut>> favoritesLiveData = new MutableLiveData<>(new ArrayList<>());
    private final GetFavoritesUseCase getFavoritesUseCase;
    private final SetFavoritesUseCase setFavoritesUseCase;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<Boolean> isMultiTouch = new MutableLiveData<>(false);

    private final List<List<List<AppShortcut>>> currentShortcuts = new ArrayList<>();

    private static final int FAVORITE_COLUMNS = 5;
    private final MutableLiveData<List<List<AppShortcut>>> favoritesPagesLiveData = new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<Integer> dragEdge = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> isDragging = new MutableLiveData<>(false);
    private final LaunchAppUseCase launchAppUseCase;

    public LiveData<Boolean> isDragging() {
        return isDragging;
    }

    public void setDragging(boolean dragging) {
        isDragging.setValue(dragging);
    }
    public LiveData<Integer> getDragEdge() { return dragEdge; }
    public void setDragEdge(int direction) { dragEdge.setValue(direction); }
    public HomeViewModel(@NonNull Application application,
                         GetShortcutsUseCase getShortcutsUseCase,
                         AddShortcutUseCase addShortcutUseCase,
                         MoveShortcutUseCase moveShortcutUseCase,
                         SetShortcutsUseCase setShortcutsUseCase,
                         RemoveShortcutUseCase removeShortcutUseCase,
                         GetFavoritesUseCase getFavoritesUseCase,
                         SetFavoritesUseCase setFavoritesUseCase, LaunchAppUseCase launchAppUseCase) {
        super(application);
        this.getShortcutsUseCase = getShortcutsUseCase;
        this.addShortcutUseCase = addShortcutUseCase;
        this.moveShortcutUseCase = moveShortcutUseCase;
        this.setShortcutsUseCase = setShortcutsUseCase;
        this.removeShortcutUseCase = removeShortcutUseCase;
        this.getFavoritesUseCase = getFavoritesUseCase;
        this.setFavoritesUseCase = setFavoritesUseCase;
        this.launchAppUseCase = launchAppUseCase;
    }


    public void loadShortcuts() {
        state.postValue(HomeScreenState.Loading.INSTANCE);
        executor.execute(() -> {
            try {
                List<List<List<AppShortcut>>> loaded = getShortcutsUseCase.invoke();
                currentShortcuts.clear();
                currentShortcuts.addAll(loaded);
                state.postValue(new HomeScreenState.Content(currentShortcuts));
            } catch (Exception e) {
                Log.e("Home Screen", "Ошибка загрузки рабочего стола" + e.getMessage());
                state.postValue(new HomeScreenState.Error("Ошибка загрузки рабочего стола" + e.getMessage(), this::loadShortcuts, "Повторить попытку"));
            }
        });
    }

    public void addShortcut(AppShortcut shortcut) {
        executor.execute(() -> {
            addShortcutUseCase.invoke(shortcut);
            loadShortcuts(); // обновляем список
        });
    }

    public void addShortcut(AppShortcut app, int pageIndex, int row, int col) {
        executor.execute(() -> {
            addShortcutUseCase
                    .invoke(pageIndex, row, col, app);
            loadShortcuts();
        });
    }


    public void addToFavoritesAtPosition(AppShortcut shortcut, int pageIndex, int positionInPage) {
        executor.execute(() -> {
            List<List<AppShortcut>> currentPages = favoritesPagesLiveData.getValue();
            assert currentPages != null;
            currentPages.get(pageIndex).set(positionInPage, shortcut);
            setFavoritesUseCase.invoke(currentPages);
            loadFavorites();
        });
    }

    public void moveShortcut(int fromPage, int fromRow, int fromCol, int toPage, int toRow, int toCol) {
        // Проверки границ перед выполнением
        final List<List<List<AppShortcut>>> snapshot = deepCopy(currentShortcuts);
        if (fromPage < 0 || toPage < 0 || fromPage >= snapshot.size() || toPage >= snapshot.size()) return;
        if (fromRow < 0 || toRow < 0 || fromRow >= snapshot.get(fromPage).size() || toRow >= snapshot.get(toPage).size()) return;
        if (fromCol < 0 || toCol < 0 || fromCol >= snapshot.get(fromPage).get(fromRow).size() || toCol >= snapshot.get(toPage).get(toRow).size()) return;

        executor.execute(() -> {
            // Глубокая копия
            List<List<List<AppShortcut>>> updated = deepCopy(snapshot);

            AppShortcut fromItem = updated.get(fromPage).get(fromRow).get(fromCol);
            AppShortcut toItem = updated.get(toPage).get(toRow).get(toCol);
            updated.get(fromPage).get(fromRow).set(fromCol, toItem);
            updated.get(toPage).get(toRow).set(toCol, fromItem);

            // Замена in‑memory
            currentShortcuts.clear();
            currentShortcuts.addAll(updated);

            // Оповещение UI
            state.postValue(new HomeScreenState.Content(updated));
            scheduleSave(updated);
        });
    }

    private List<List<List<AppShortcut>>> deepCopy(List<List<List<AppShortcut>>> original) {
        List<List<List<AppShortcut>>> copy = new ArrayList<>();
        for (List<List<AppShortcut>> page : original) {
            List<List<AppShortcut>> pageCopy = new ArrayList<>();
            for (List<AppShortcut> row : page) {
                pageCopy.add(new ArrayList<>(row)); // копия строки
            }
            copy.add(pageCopy);
        }
        return copy;
    }

    private void scheduleSave(List<List<List<AppShortcut>>> shortcuts) {
        executor.execute(() -> setShortcutsUseCase.invoke(shortcuts));
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
        setShortcutsUseCase.invoke(currentShortcuts);
    }

    public void removeShortcut(int page, int row, int col) {
        final List<List<List<AppShortcut>>> snapshot = currentShortcuts;
        if (page < 0 || page >= snapshot.size()) return;
        if (row < 0 || row >= snapshot.get(page).size()) return;
        if (col < 0 || col >= snapshot.get(page).get(row).size()) return;

        executor.execute(() -> {
            List<List<List<AppShortcut>>> updated = deepCopy(snapshot);
            updated.get(page).get(row).set(col, null);

            currentShortcuts.clear();
            currentShortcuts.addAll(updated);
            state.postValue(new HomeScreenState.Content(updated));
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


    public void setMultiTouch(boolean multiTouch) {
        isMultiTouch.postValue(multiTouch);
    }

    public LiveData<Boolean> getIsMultiTouch() {
        return isMultiTouch;
    }



    // Метод загрузки избранного:
//    public void loadFavorites() {
//        executor.execute(() -> {
//            List<List<AppShortcutDTO>> rawFavorites = getFavoritesUseCase.invoke();
//            // Преобразуем List<List<AppShortcutDTO>> в плоский список (если у вас сетка избранного, но для простоты храним один ряд)
//            List<AppShortcutDTO> flat = new ArrayList<>();
//            for (List<AppShortcutDTO> row : rawFavorites) {
//                flat.addAll(row);
//            }
//            favoritesLiveData.postValue(flat);
//        });
//    }

    // Добавить в избранное (в конец первого свободного места)
//    public void addToFavorites(AppShortcutDTO shortcut) {
//        executor.execute(() -> {
//            List<List<AppShortcutDTO>> raw = getFavoritesUseCase.invoke();
//            // Ищем первую пустую ячейку
//            boolean added = false;
//            for (int i = 0; i < raw.size() && !added; i++) {
//                for (int j = 0; j < raw.get(i).size(); j++) {
//                    if (raw.get(i).get(j) == null) {
//                        raw.get(i).set(j, shortcut);
//                        added = true;
//                        break;
//                    }
//                }
//            }
//            // Если все ячейки заняты, можно добавить новую строку (или проигнорировать)
//            if (!added) {
//                // Пример: создаём новую строку с размером 5 и кладём в неё
//                List<AppShortcutDTO> newRow = new ArrayList<>();
//                for (int k = 0; k < raw.get(0).size(); k++) newRow.add(null);
//                newRow.set(0, shortcut);
//                raw.add(newRow);
//            }
//            setFavoritesUseCase.invoke(raw);
//            loadFavorites(); // перезагрузить
//        });
//    }

    // Удалить из избранного
    public void removeFromFavorites(AppShortcut shortcut) {
        executor.execute(() -> {
            List<List<AppShortcut>> raw = getFavoritesUseCase.invoke();
            boolean removed = false;
            for (List<AppShortcut> row : raw) {
                for (int i = 0; i < row.size(); i++) {
                    if (shortcut.equals(row.get(i))) { // нужно переопределить equals в AppShortcutDTO по packageName
                        row.set(i, null);
                        removed = true;
                        break;
                    }
                }
                if (removed) break;
            }
            if (removed) {
                setFavoritesUseCase.invoke(raw);
                loadFavorites();
            }
        });
    }

    public LiveData<List<AppShortcut>> getFavorites() {
        return favoritesLiveData;
    }

    public void launchApp(String packageName) {
        launchAppUseCase.invoke(packageName);
    }

    public void loadFavorites() {
        executor.execute(() -> {
            List<List<AppShortcut>> rawFavorites = getFavoritesUseCase.invoke(); // изначально List<List<AppShortcutDTO>> (строки, колонки)
            // Преобразуем в список страниц: каждая страница = плоский список из FAVORITE_COLUMNS элементов (может быть меньше на последней)
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
            favoritesPagesLiveData.postValue(pages);
        });
    }

    // Добавление в избранное (в первую пустую ячейку)
    public void addToFavorites(AppShortcut shortcut) {
        executor.execute(() -> {
            List<List<AppShortcut>> raw = getFavoritesUseCase.invoke();
            boolean added = false;
            for (int i = 0; i < raw.size(); i++) {
                for (int j = 0; j < raw.get(i).size(); j++) {
                    if (raw.get(i).get(j) == null) {
                        raw.get(i).set(j, shortcut);
                        added = true;
                        break;
                    }
                }
                if (added) break;
            }
            if (!added) {
                // Добавить новую строку с null и поместить shortcut в первую ячейку
                List<AppShortcut> newRow = new ArrayList<>();
                for (int i = 0; i < FAVORITE_COLUMNS; i++) newRow.add(null);
                newRow.set(0, shortcut);
                raw.add(newRow);
            }
            setFavoritesUseCase.invoke(raw);
            loadFavorites();
        });
    }

    // Удаление из избранного по позиции на странице
    public void removeFromFavorites(int pageIndex, int positionInPage) {
        executor.execute(() -> {
            List<List<AppShortcut>> pages = favoritesPagesLiveData.getValue();
            if (pages == null || pageIndex >= pages.size()) return;
            List<AppShortcut> page = pages.get(pageIndex);
            if (positionInPage >= page.size()) return;
            AppShortcut shortcutToRemove = page.get(positionInPage);
            if (shortcutToRemove == null) return;

            // Обновляем raw структуру
            List<List<AppShortcut>> raw = getFavoritesUseCase.invoke();
            for (List<AppShortcut> row : raw) {
                for (int i = 0; i < row.size(); i++) {
                    if (shortcutToRemove.equals(row.get(i))) {
                        row.set(i, null);
                        break;
                    }
                }
            }
            setFavoritesUseCase.invoke(raw);
            loadFavorites();
        });
    }

    public LiveData<List<List<AppShortcut>>> getFavoritesPages() {
        return favoritesPagesLiveData;
    }

    public void swapDesktopWithFavorites(int desktopPage, int desktopRow, int desktopCol,
                                         int favPageIndex, int favPositionInPage) {
        executor.execute(() -> {
            // Глубокие копии текущих данных
            List<List<List<AppShortcut>>> desktopCopy = deepCopy(currentShortcuts);
            List<List<AppShortcut>> favPages = favoritesPagesLiveData.getValue();
            if (favPages == null || favPageIndex >= favPages.size()) return;
            List<List<AppShortcut>> favCopy = new ArrayList<>();
            for (List<AppShortcut> page : favPages) {
                favCopy.add(new ArrayList<>(page));
            }

            // Проверки границ
            if (desktopPage < 0 || desktopPage >= desktopCopy.size()) return;
            List<List<AppShortcut>> desktopPageData = desktopCopy.get(desktopPage);
            if (desktopRow < 0 || desktopRow >= desktopPageData.size()) return;
            List<AppShortcut> desktopRowData = desktopPageData.get(desktopRow);
            if (desktopCol < 0 || desktopCol >= desktopRowData.size()) return;

            List<AppShortcut> favPageData = favCopy.get(favPageIndex);
            if (favPositionInPage < 0 || favPositionInPage >= favPageData.size()) return;

            AppShortcut desktopItem = desktopRowData.get(desktopCol);
            AppShortcut favItem = favPageData.get(favPositionInPage);

            // Меняем местами
            desktopRowData.set(desktopCol, favItem);
            favPageData.set(favPositionInPage, desktopItem);

            // Обновляем LiveData
            currentShortcuts.clear();
            currentShortcuts.addAll(desktopCopy);
            favoritesPagesLiveData.postValue(favCopy);

            // Уведомляем UI
            state.postValue(new HomeScreenState.Content(desktopCopy));

            // Сохраняем на диск
            scheduleSave(desktopCopy);
            // Также нужно сохранить избранное (преобразовать обратно в raw структуру)
            List<List<AppShortcut>> rawFav = new ArrayList<>();
            for (List<AppShortcut> page : favCopy) {
                // Разбиваем плоский список обратно на строки по 5 элементов
                for (int i = 0; i < page.size(); i += FAVORITE_COLUMNS) {
                    int end = Math.min(i + FAVORITE_COLUMNS, page.size());
                    rawFav.add(new ArrayList<>(page.subList(i, end)));
                }
            }
            setFavoritesUseCase.invoke(rawFav);
        });
    }

    public void swapFavorites(int srcPage, int srcPos, int dstPage, int dstPos) {
        executor.execute(() -> {
            List<List<AppShortcut>> pages = favoritesPagesLiveData.getValue();
            if (pages == null) return;
            // Создаём копию
            List<List<AppShortcut>> copy = new ArrayList<>();
            for (List<AppShortcut> page : pages) {
                copy.add(new ArrayList<>(page));
            }

            // Проверки границ
            if (srcPage < 0 || srcPage >= copy.size() || dstPage < 0 || dstPage >= copy.size()) return;
            List<AppShortcut> srcPageList = copy.get(srcPage);
            List<AppShortcut> dstPageList = copy.get(dstPage);
            if (srcPos < 0 || srcPos >= srcPageList.size() || dstPos < 0 || dstPos >= dstPageList.size()) return;

            // Меняем местами
            AppShortcut temp = srcPageList.get(srcPos);
            srcPageList.set(srcPos, dstPageList.get(dstPos));
            dstPageList.set(dstPos, temp);

            // Обновляем LiveData
            favoritesPagesLiveData.postValue(copy);

            // Сохраняем на диск (преобразуем обратно в raw-структуру)
            List<List<AppShortcut>> rawFav = new ArrayList<>();
            for (List<AppShortcut> page : copy) {
                for (int i = 0; i < page.size(); i += FAVORITE_COLUMNS) {
                    int end = Math.min(i + FAVORITE_COLUMNS, page.size());
                    rawFav.add(new ArrayList<>(page.subList(i, end)));
                }
            }
            setFavoritesUseCase.invoke(rawFav);
        });
    }


}