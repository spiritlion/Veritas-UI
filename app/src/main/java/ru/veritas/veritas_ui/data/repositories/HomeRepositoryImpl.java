package ru.veritas.veritas_ui.data.repositories;

import static android.os.ParcelFileDescriptor.MODE_APPEND;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import ru.veritas.veritas_ui.domain.entities.AppShortcut;

public class HomeRepositoryImpl implements HomeRepository {
    private final Context context;
    private PackageManager packageManager;
    public HomeRepositoryImpl(Context context) {
        this.context = context.getApplicationContext();
        packageManager = context.getPackageManager();
    }

    @Override
    public List<AppShortcut> getShortcuts() {
        List<AppShortcut> shortcuts = new ArrayList<>();
        try (
            BufferedReader bf = new BufferedReader(
                new InputStreamReader(context.openFileInput("home_arh.txt"))
            )
        ) {
            String packageName;
            while ((packageName = bf.readLine()) != null) {
                if (packageName.isBlank()) continue;
                try {
                    Drawable icon = packageManager.getApplicationIcon(packageName); // TODO with AI
                    String appName = packageManager.getApplicationLabel(
                            packageManager.getApplicationInfo(packageName, 0)).toString();
                    shortcuts.add(new AppShortcut(packageName, appName, icon));
                } catch (PackageManager.NameNotFoundException e) {
                    // Приложение удалено — пропускаем
                }
            }
        } catch (FileNotFoundException e) {
            // TODO
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return shortcuts;
    }

    @Override
    public void addShortcut(String packageName, String appName, Drawable icon) {
        List<AppShortcut> current = getShortcuts();
        // Проверяем, нет ли уже такого ярлыка
//        for (AppShortcut shortcut : current) {
//            if (shortcut.getPackageName().equals(packageName)) {
//                return;
//            }
//        }
        current.add(new AppShortcut(packageName, appName, null));
        saveShortcuts(current);
    }

    @Override
    public void removeShortcut(String packageName) {
        List<AppShortcut> current = getShortcuts();
        current.removeIf(shortcut -> shortcut.getPackageName().equals(packageName));
        saveShortcuts(current);
    }

    @Override
    public boolean isShortcutExists(String packageName) {
        return getShortcuts().stream().anyMatch(s -> s.getPackageName().equals(packageName));
    }

    private void saveShortcuts(List<AppShortcut> shortcuts) {
        try {
            BufferedWriter bf = new BufferedWriter(
                    new OutputStreamWriter(
                            context.openFileOutput("home_arh.txt", Context.MODE_APPEND)));
            shortcuts.forEach(it -> {
                    try {
                        bf.append(it.getPackageName());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            bf.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}