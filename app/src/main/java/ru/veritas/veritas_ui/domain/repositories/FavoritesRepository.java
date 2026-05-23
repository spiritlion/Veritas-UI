package ru.veritas.veritas_ui.domain.repositories;

import java.util.List;

import ru.veritas.veritas_ui.domain.entities.AppShortcut;

public interface FavoritesRepository {
    List<List<AppShortcut>> getFavorites();

    void saveFavorites(List<List<AppShortcut>> favorites);
}