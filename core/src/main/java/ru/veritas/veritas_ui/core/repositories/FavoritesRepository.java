package ru.veritas.veritas_ui.core.repositories;

import java.util.List;

import ru.veritas.veritas_ui.core.entities.AppShortcut;

public interface FavoritesRepository {
    List<List<AppShortcut>> getFavorites();

    void addFavorite(int i, int j, AppShortcut shortcut);

    void removeFavorite(int i, int j);
    boolean isFavoriteExists(String packageName);
    AppShortcut getFavorite(int i, int j);
    void saveFavorites(List<List<AppShortcut>> favorites);
}