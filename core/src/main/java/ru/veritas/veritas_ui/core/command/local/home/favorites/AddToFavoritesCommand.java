package ru.veritas.veritas_ui.core.command.local.home.favorites;

import java.util.ArrayList;

import ru.veritas.veritas_ui.core.command.NonUndoableCommand;
import ru.veritas.veritas_ui.core.entities.AppShortcut;
import ru.veritas.veritas_ui.core.repositories.FavoritesRepository;

public class AddToFavoritesCommand extends NonUndoableCommand<Void> {
    private final FavoritesRepository repository;
    private final AppShortcut shortcut;
    private final int page;
    private final int position;

    public AddToFavoritesCommand(FavoritesRepository repository,
                                 AppShortcut shortcut,
                                 int page, int position) {
        this.repository = repository;
        this.shortcut = shortcut;
        this.page = page;
        this.position = position;
    }

    @Override
    public Void execute() {
        repository.addFavorite(page, position, shortcut);
        return null;
    }
}