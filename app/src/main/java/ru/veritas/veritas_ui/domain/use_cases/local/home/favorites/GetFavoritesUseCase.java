package ru.veritas.veritas_ui.domain.use_cases.local.home.favorites;

import java.util.List;

import ru.veritas.veritas_ui.domain.repositories.FavoritesRepository;
import ru.veritas.veritas_ui.domain.repositories.HomeRepository;
import ru.veritas.veritas_ui.domain.entities.AppShortcut;


public class GetFavoritesUseCase {
    private final FavoritesRepository repository;

    public GetFavoritesUseCase(FavoritesRepository repository) {
        this.repository = repository;
    }

    public List<List<AppShortcut>> invoke() {
        return repository.getFavorites();
    }
}