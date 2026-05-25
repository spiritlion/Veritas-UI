package ru.veritas.veritas_ui.domain.command.local.home.favorites;

import java.util.List;

import ru.veritas.veritas_ui.domain.entities.AppShortcut;
import ru.veritas.veritas_ui.domain.repositories.FavoritesRepository;


public class GetFavoritesUseCase {
    private final FavoritesRepository repository;

    public GetFavoritesUseCase(FavoritesRepository repository) {
        this.repository = repository;
    }

    public List<List<AppShortcut>> invoke() {
        return repository.getFavorites();
    }
}