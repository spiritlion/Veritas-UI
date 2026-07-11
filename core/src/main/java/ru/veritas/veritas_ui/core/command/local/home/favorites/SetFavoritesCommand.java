package ru.veritas.veritas_ui.core.command.local.home.favorites;

import java.util.List;

import ru.veritas.veritas_ui.core.command.NonUndoableCommand;
import ru.veritas.veritas_ui.core.entities.AppShortcut;
import ru.veritas.veritas_ui.core.repositories.FavoritesRepository;

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