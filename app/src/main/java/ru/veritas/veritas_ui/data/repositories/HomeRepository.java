package ru.veritas.veritas_ui.data.repositories;

import java.util.List;

import ru.veritas.veritas_ui.domain.entities.AppShortcutDTO;

public interface HomeRepository {
    List<List<List<AppShortcutDTO>>> getShortcuts();
    void addShortcut(AppShortcutDTO shortcut);

    void addShortcut(int i, int j, int k, AppShortcutDTO shortcut);

    void removeShortcut(int i, int j, int k);
    boolean isShortcutExists(String packageName);
    AppShortcutDTO getShortcut(int i, int j, int k);
    void saveShortcuts(List<List<List<AppShortcutDTO>>> shortcuts);

    interface Favorites {
        List<List<AppShortcutDTO>> getFavorites();

        void saveFavorites(List<List<AppShortcutDTO>> favorites);
    }
}