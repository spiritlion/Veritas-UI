package ru.veritas.veritas_ui.data.repositories;

import android.graphics.drawable.Drawable;

import java.util.List;

import ru.veritas.veritas_ui.domain.entities.AppShortcut;

public interface HomeRepository {
    List<AppShortcut> getShortcuts();
    void addShortcut(String packageName, String appName, Drawable icon);
    void removeShortcut(String packageName);
    boolean isShortcutExists(String packageName);
}