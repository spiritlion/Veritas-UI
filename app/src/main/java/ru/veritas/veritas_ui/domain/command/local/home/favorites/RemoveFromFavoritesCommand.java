package ru.veritas.veritas_ui.domain.command.local.home.favorites;

import ru.veritas.veritas_ui.domain.command.NonUndoableCommand;
import ru.veritas.veritas_ui.domain.repositories.FavoritesRepository;

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
        var pages = repository.getFavorites();
        if (page < pages.size()) {
            var pageList = pages.get(page);
            if (position < pageList.size()) pageList.set(position, null);
            repository.saveFavorites(pages);
        }
        return null;
    }
}