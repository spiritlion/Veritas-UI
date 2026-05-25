package ru.veritas.veritas_ui.domain.command.local.home.favorites;

import java.util.ArrayList;

import ru.veritas.veritas_ui.domain.command.NonUndoableCommand;
import ru.veritas.veritas_ui.domain.entities.AppShortcut;
import ru.veritas.veritas_ui.domain.repositories.FavoritesRepository;

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
        var pages = repository.getFavorites();
        if (page >= pages.size()) {
            // если такой страницы нет – создаём
            while (pages.size() <= page) pages.add(new ArrayList<>());
        }
        var targetPage = pages.get(page);
        if (position >= targetPage.size()) {
            while (targetPage.size() <= position) targetPage.add(null);
        }
        targetPage.set(position, shortcut);
        repository.saveFavorites(pages);
        return null;
    }
}