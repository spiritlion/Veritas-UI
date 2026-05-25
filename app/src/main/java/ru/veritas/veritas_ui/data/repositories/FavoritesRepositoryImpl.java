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

import ru.veritas.veritas_ui.domain.entities.AppShortcut;
import ru.veritas.veritas_ui.domain.repositories.FavoritesRepository;

public class FavoritesRepositoryImpl implements FavoritesRepository {
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
            return createFavorites();
        } catch (IOException ex) {
            Log.e("get f", "Ошибка ввода-вывода", ex);
            return new ArrayList<>(); // или брось RuntimeException
        }
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

    private List<List<AppShortcut>> createFavorites() {
        List<List<AppShortcut>> favorites = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            favorites.add(new ArrayList<>());
            for(int j=0;j<4;++j) {
                favorites.get(i).add(null);
            }
        }
        saveFavorites(favorites);
        return favorites;
    }
}