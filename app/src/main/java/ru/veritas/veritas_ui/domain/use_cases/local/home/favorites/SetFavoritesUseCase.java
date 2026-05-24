package ru.veritas.veritas_ui.domain.use_cases.local.home.favorites;

import java.util.List;

import ru.veritas.veritas_ui.domain.entities.AppShortcut;
import ru.veritas.veritas_ui.domain.repositories.FavoritesRepository;


public class SetFavoritesUseCase {
    private final FavoritesRepository repository;

    public SetFavoritesUseCase(FavoritesRepository repository) {
        this.repository = repository;
    }

    public void invoke(List<List<AppShortcut>> favorites) {
        repository.saveFavorites(favorites);
    }
}