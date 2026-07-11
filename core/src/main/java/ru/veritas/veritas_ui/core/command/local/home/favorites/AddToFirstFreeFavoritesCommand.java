package ru.veritas.veritas_ui.core.command.local.home.favorites;

import ru.veritas.veritas_ui.core.command.Command;
import ru.veritas.veritas_ui.core.entities.AppShortcut;
import ru.veritas.veritas_ui.core.repositories.FavoritesRepository;

import java.util.ArrayList;
import java.util.List;

public class AddToFirstFreeFavoritesCommand implements Command<Void> {
    private final FavoritesRepository repository;
    private final AppShortcut shortcut;
    private int addedPage = -1;
    private int addedPos = -1;

    public AddToFirstFreeFavoritesCommand(FavoritesRepository repository, AppShortcut shortcut) {
        this.repository = repository;
        this.shortcut = shortcut;
    }

    @Override
    public Void execute() {
        var pages = repository.getFavorites();
        for (int p = 0; p < pages.size(); p++) {
            List<AppShortcut> page = pages.get(p);
            for (int i = 0; i < page.size(); i++) {
                if (page.get(i) == null) {
                    page.set(i, shortcut);
                    addedPage = p;
                    addedPos = i;
                    repository.saveFavorites(pages);
                    return null;
                }
            }
        }
        // если свободного места нет – создаём новую страницу
        List<AppShortcut> newPage = new ArrayList<>();
        newPage.add(shortcut);
        pages.add(newPage);
        addedPage = pages.size() - 1;
        addedPos = 0;
        repository.saveFavorites(pages);
        return null;
    }

    @Override
    public void undo() {
        if (addedPage >= 0 && addedPos >= 0) {
            var pages = repository.getFavorites();
            if (addedPage < pages.size()) {
                var page = pages.get(addedPage);
                if (addedPos < page.size()) page.set(addedPos, null);
                repository.saveFavorites(pages);
            }
        }
    }

    @Override
    public boolean isUndoable() { return true; }
}