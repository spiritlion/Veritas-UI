package ru.veritas.veritas_ui.data.repositories;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ru.veritas.veritas_ui.domain.entities.AppShortcut;

public class HomeRepositoryImpl implements HomeRepository {
    private static final String PREFS_NAME = "launcher_prefs";
    private static final String KEY_SHORTCUTS = "shortcuts";
    private final SharedPreferences prefs;

    public HomeRepositoryImpl(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public List<AppShortcut> getShortcuts() {
        List<AppShortcut> shortcuts = new ArrayList<>();
        String json = prefs.getString(KEY_SHORTCUTS, "[]");
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String packageName = obj.getString("packageName");
                String appName = obj.getString("appName");
                // Иконку не сохраняем в JSON, она будет загружена позже из PackageManager
                shortcuts.add(new AppShortcut(packageName, appName, null));
            }
        } catch (JSONException e) {
            Log.e("HomeRepo", "Error parsing shortcuts", e);
        }
        return shortcuts;
    }

    @Override
    public void addShortcut(String packageName, String appName, Drawable icon) {
        List<AppShortcut> current = getShortcuts();
        // Проверяем, нет ли уже такого ярлыка
        for (AppShortcut shortcut : current) {
            if (shortcut.getPackageName().equals(packageName)) {
                return;
            }
        }
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
        JSONArray array = new JSONArray(); // TODO переделать на песочницу
        for (AppShortcut shortcut : shortcuts) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("packageName", shortcut.getPackageName());
                obj.put("appName", shortcut.getAppName());
                array.put(obj);
            } catch (JSONException e) {
                Log.e("HomeRepo", "Error saving shortcut", e);
            }
        }
        prefs.edit().putString(KEY_SHORTCUTS, array.toString()).apply();
    }
}