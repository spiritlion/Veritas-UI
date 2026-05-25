package ru.veritas.veritas_ui.domain.command.local.home.favorites;

import java.util.List;

import ru.veritas.veritas_ui.domain.command.NonUndoableCommand;
import ru.veritas.veritas_ui.domain.entities.AppShortcut;
import ru.veritas.veritas_ui.domain.repositories.FavoritesRepository;

public class SetFavoritesCommand extends NonUndoableCommand<Void> {
    private final FavoritesRepository repository;
    private final List<List<AppShortcut>> favorites;

    public SetFavoritesCommand(FavoritesRepository repository,
                               List<List<AppShortcut>> favorites) {
        this.repository = repository;
        this.favorites = favorites;
    }

    @Override
    public Void execute() {
        repository.saveFavorites(favorites);
        return null;
    }
}