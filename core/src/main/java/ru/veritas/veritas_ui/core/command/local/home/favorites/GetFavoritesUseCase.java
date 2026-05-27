package ru.veritas.veritas_ui.core.command.local.home.favorites;

import java.util.List;

import ru.veritas.veritas_ui.core.entities.AppShortcut;
import ru.veritas.veritas_ui.core.repositories.FavoritesRepository;


public class GetFavoritesUseCase {
    private final FavoritesRepository repository;

    public GetFavoritesUseCase(FavoritesRepository repository) {
        this.repository = repository;
    }

    public List<List<AppShortcut>> invoke() {
        return repository.getFavorites();
    }
}