package ru.veritas.veritas_ui.data.repositories;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import ru.veritas.veritas_ui.core.entities.AppShortcut;
import ru.veritas.veritas_ui.core.repositories.FavoritesRepository;

public class FavoritesRepositoryImpl implements FavoritesRepository {
    private static final int PAGE_COUNT = 3;
    private static final int ITEMS_PER_PAGE = 4;

    private final Context context;

    public FavoritesRepositoryImpl(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public List<List<AppShortcut>> getFavorites() {
        try (FileInputStream fileInputStream = context.openFileInput("favor_arh.json");
             InputStreamReader streamReader = new InputStreamReader(fileInputStream)) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<List<AppShortcut>>>(){}.getType();
            return gson.fromJson(streamReader, type);
        } catch (FileNotFoundException e) {
            Log.e("get f", "Файл не найден, создаю новый");
            List<List<AppShortcut>> newFavorites = createFavorites();
            saveFavorites(newFavorites);
            return newFavorites;
        } catch (IOException ex) {
            Log.e("get f", "Ошибка ввода-вывода", ex);
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void addFavorite(int i, int j, AppShortcut shortcut) {
        List<List<AppShortcut>> favorites = getFavorites();
        if (favorites == null || i < 0 || i >= favorites.size() || j < 0 || j >= favorites.get(i).size()) {
            throw new IndexOutOfBoundsException("Invalid index: page=" + i + ", position=" + j);
        }
        favorites.get(i).set(j, shortcut);
        saveFavorites(favorites);
    }

    @Override
    public void removeFavorite(int i, int j) {
        addFavorite(i, j, null);
    }

    @Override
    public boolean isFavoriteExists(String packageName) {
        if (packageName == null) return false;
        List<List<AppShortcut>> favorites = getFavorites();
        if (favorites == null) return false;
        for (List<AppShortcut> page : favorites) {
            for (AppShortcut shortcut : page) {
                if (shortcut != null && packageName.equals(shortcut.getPackageName())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public AppShortcut getFavorite(int i, int j) {
        List<List<AppShortcut>> favorites = getFavorites();
        if (favorites == null || i < 0 || i >= favorites.size() || j < 0 || j >= favorites.get(i).size()) {
            throw new IndexOutOfBoundsException("Invalid index: page=" + i + ", position=" + j);
        }
        return favorites.get(i).get(j);
    }

    private List<List<AppShortcut>> createFavorites() {
        List<List<AppShortcut>> favorites = new ArrayList<>();
        for (int i = 0; i < PAGE_COUNT; i++) {
            List<AppShortcut> page = new ArrayList<>();
            for (int j = 0; j < ITEMS_PER_PAGE; j++) {
                page.add(null);
            }
            favorites.add(page);
        }
        return favorites;
    }

    @Override
    public void saveFavorites(List<List<AppShortcut>> favorites) {
        Gson gson = new Gson();
        String data = gson.toJson(favorites);
        Log.d("json", data);
        try (FileOutputStream fileOutputStream = context.openFileOutput("favor_arh.json", Context.MODE_PRIVATE)) {
            fileOutputStream.write(data.getBytes());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}