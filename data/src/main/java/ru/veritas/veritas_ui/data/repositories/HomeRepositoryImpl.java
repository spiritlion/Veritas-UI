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
import ru.veritas.veritas_ui.core.repositories.HomeRepository;

public class HomeRepositoryImpl implements HomeRepository {

    private final Context context;

    public HomeRepositoryImpl(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public List<List<List<AppShortcut>>> getShortcuts() {
        try (FileInputStream fileInputStream = context.openFileInput("home_arh.json");
             InputStreamReader streamReader = new InputStreamReader(fileInputStream)) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<List<List<AppShortcut>>>>(){}.getType();
            return gson.fromJson(streamReader, type);
        } catch (FileNotFoundException e) {
            Log.e("get s", "Файл не найден, создаю новый");
            createShortcuts();
            return getShortcuts();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

//    public List<List<List<AppShortcut>>> getShortcuts() {
//        List<List<List<AppShortcut>>> shortcuts = new ArrayList<>();
//        List<List<List<AppShortcutDTO>>> lll = getShortcutsDto();
//        assert lll != null;
//        for (int i = 0; i < lll.size(); i++) {
//            List<List<AppShortcutDTO>> ll = lll.get(i);
//            shortcuts.add(new ArrayList<>());
//            for (int j = 0; j < ll.size(); j++) {
//                List<AppShortcutDTO> l = ll.get(j);
//                shortcuts.get(i).add(new ArrayList<>());
//                for (int k = 0; k < l.size(); k++) {
//                    AppShortcut shortcut;
//                    try {
//                        shortcut = new AppShortcut(
//                                l.get(k).getPackageName(),
//                                l.get(k).getAppName(),
//                                packageManager.getApplicationIcon(l.get(i).getPackageName())
//                        );
//                    } catch (PackageManager.NameNotFoundException e) {
//                        shortcut = new AppShortcut(
//                                l.get(k).getPackageName(),
//                                l.get(k).getAppName(),
//                                null
//                        );
//                    }
//                    shortcuts.get(i).get(j).set(k, shortcut);
//                }
//            }
//        }
//        return null;
//    }

    @Override
    public void addShortcut(int i, int j, int k, AppShortcut shortcut) {
        Log.d("add s", "1");
        List<List<List<AppShortcut>>> shortcuts = getShortcuts();
        assert shortcuts != null;
        shortcuts.get(i).get(j).set(k, shortcut);
        saveShortcuts(shortcuts);
    }

    @Override
    public void removeShortcut(int i, int j, int k) {
        addShortcut(i, j, k, null);
    }

    @Override
    public boolean isShortcutExists(String packageName) {
        return getShortcuts().stream().anyMatch(ll ->
                ll.stream().anyMatch(l ->
                        l.stream().anyMatch(s ->
                                s.getPackageName().equals(packageName)
                        )
                )
        );
    }

    @Override
    public AppShortcut getShortcut(int i, int j, int k) {
        return getShortcuts().get(i).get(j).get(k);
    }

    private void createShortcuts() {
        List<List<List<AppShortcut>>> shortcuts = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            shortcuts.add(new ArrayList<>());
            for (int j = 0; j < 6; j++) {
                shortcuts.get(i).add(new ArrayList<>());
                for (int k = 0; k < 4; k++) {
                    shortcuts.get(i).get(j).add(null);
                }
            }
        }
        saveShortcuts(shortcuts);
    }

    public void saveShortcuts(List<List<List<AppShortcut>>> shortcuts) {
        Gson gson = new Gson();
        String data = gson.toJson(shortcuts);
        Log.d("json", data);
        try (FileOutputStream fileOutputStream = context.openFileOutput("home_arh.json", Context.MODE_PRIVATE)) {
            fileOutputStream.write(data.getBytes());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}