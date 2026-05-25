package ru.veritas.veritas_ui.domain.command.local.home;

import ru.veritas.veritas_ui.domain.command.Command;
import ru.veritas.veritas_ui.domain.entities.AppShortcut;
import ru.veritas.veritas_ui.domain.repositories.FavoritesRepository;
import ru.veritas.veritas_ui.domain.repositories.HomeRepository;

public class SwapShortcutWithFavoriteCommand implements Command<Void> {
    private final HomeRepository homeRepo;
    private final FavoritesRepository favRepo;
    private final int desktopPage, desktopRow, desktopCol;
    private final int favPage, favPos;
    private AppShortcut desktopSnapshot;
    private AppShortcut favSnapshot;

    public SwapShortcutWithFavoriteCommand(HomeRepository homeRepo,
                                           FavoritesRepository favRepo,
                                           int desktopPage, int desktopRow, int desktopCol,
                                           int favPage, int favPos) {
        this.homeRepo = homeRepo;
        this.favRepo = favRepo;
        this.desktopPage = desktopPage; this.desktopRow = desktopRow; this.desktopCol = desktopCol;
        this.favPage = favPage; this.favPos = favPos;
    }

    @Override
    public Void execute() {
        desktopSnapshot = homeRepo.getShortcut(desktopPage, desktopRow, desktopCol);
        favSnapshot = getFavoriteAt(favPage, favPos);

        homeRepo.addShortcut(desktopPage, desktopRow, desktopCol, favSnapshot);
        setFavoriteAt(favPage, favPos, desktopSnapshot);
        return null;
    }

    @Override
    public void undo() {
        if (desktopSnapshot == null && favSnapshot == null) return;
        homeRepo.addShortcut(desktopPage, desktopRow, desktopCol, desktopSnapshot);
        setFavoriteAt(favPage, favPos, favSnapshot);
    }

    @Override
    public boolean isUndoable() { return true; }

    private AppShortcut getFavoriteAt(int page, int pos) {
        var pages = favRepo.getFavorites();
        if (page >= pages.size()) return null;
        var pageList = pages.get(page);
        if (pos >= pageList.size()) return null;
        return pageList.get(pos);
    }

    private void setFavoriteAt(int page, int pos, AppShortcut shortcut) {
        var pages = favRepo.getFavorites();
        if (page >= pages.size()) return;
        var pageList = pages.get(page);
        if (pos >= pageList.size()) return;
        pageList.set(pos, shortcut);
        favRepo.saveFavorites(pages);
    }
}