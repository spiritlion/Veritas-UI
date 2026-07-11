package ru.veritas.veritas_ui.core.repositories;

import java.util.List;

import ru.veritas.veritas_ui.core.entities.AppShortcut;

public interface FavoritesRepository {
    List<List<AppShortcut>> getFavorites();

    void saveFavorites(List<List<AppShortcut>> favorites);
}