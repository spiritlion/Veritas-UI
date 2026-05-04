package ru.veritas.veritas_ui.domain.use_cases.local.home.favorites;

import java.util.List;

import ru.veritas.veritas_ui.data.repositories.HomeRepository;
import ru.veritas.veritas_ui.domain.entities.AppShortcutDTO;

@FunctionalInterface
public interface SetFavoritesUseCase {
    void invoke(List<List<AppShortcutDTO>> favorites);

    static SetFavoritesUseCase create(HomeRepository.Favorites favoritesRep) {
        return favoritesRep::saveFavorites;
    }
}