package ru.veritas.veritas_ui.core.command.local.home.favorites;

import ru.veritas.veritas_ui.core.command.NonUndoableCommand;
import ru.veritas.veritas_ui.core.repositories.FavoritesRepository;

public class RemoveFromFavoritesCommand extends NonUndoableCommand<Void> {
    private final FavoritesRepository repository;
    private final int page, position;

    public RemoveFromFavoritesCommand(FavoritesRepository repository, int page, int position) {
        this.repository = repository;
        this.page = page;
        this.position = position;
    }

    @Override
    public Void execute() {
        repository.removeFavorite(page, position);
        return null;
    }
}