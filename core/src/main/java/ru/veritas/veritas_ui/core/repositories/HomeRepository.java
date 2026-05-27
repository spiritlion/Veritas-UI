package ru.veritas.veritas_ui.core.repositories;

import java.util.List;

import ru.veritas.veritas_ui.core.entities.AppShortcut;

public interface HomeRepository {
    List<List<List<AppShortcut>>> getShortcuts();

    void addShortcut(int i, int j, int k, AppShortcut shortcut);

    void removeShortcut(int i, int j, int k);
    boolean isShortcutExists(String packageName);
    AppShortcut getShortcut(int i, int j, int k);
    void saveShortcuts(List<List<List<AppShortcut>>> shortcuts);


}