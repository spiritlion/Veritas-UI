package ru.veritas.veritas_ui.domain.command.local.home.favorites;

import ru.veritas.veritas_ui.domain.command.Command;
import ru.veritas.veritas_ui.domain.entities.AppShortcut;
import ru.veritas.veritas_ui.domain.repositories.FavoritesRepository;

public class SwapFavoritesCommand implements Command<Void> {
    private final FavoritesRepository repository;
    private final int srcPage, srcPos;
    private final int dstPage, dstPos;
    private AppShortcut srcSnapshot, dstSnapshot;

    public SwapFavoritesCommand(FavoritesRepository repository,
                                int srcPage, int srcPos,
                                int dstPage, int dstPos) {
        this.repository = repository;
        this.srcPage = srcPage; this.srcPos = srcPos;
        this.dstPage = dstPage; this.dstPos = dstPos;
    }

    @Override
    public Void execute() {
        var pages = repository.getFavorites();
        if (srcPage >= pages.size() || dstPage >= pages.size()) return null;
        var srcList = pages.get(srcPage);
        var dstList = pages.get(dstPage);
        if (srcPos >= srcList.size() || dstPos >= dstList.size()) return null;

        srcSnapshot = srcList.get(srcPos);
        dstSnapshot = dstList.get(dstPos);
        srcList.set(srcPos, dstSnapshot);
        dstList.set(dstPos, srcSnapshot);
        repository.saveFavorites(pages);
        return null;
    }

    @Override
    public void undo() {
        if (srcSnapshot != null && dstSnapshot != null) {
            var pages = repository.getFavorites();
            if (srcPage < pages.size() && dstPage < pages.size()) {
                var srcList = pages.get(srcPage);
                var dstList = pages.get(dstPage);
                if (srcPos < srcList.size() && dstPos < dstList.size()) {
                    srcList.set(srcPos, srcSnapshot);
                    dstList.set(dstPos, dstSnapshot);
                    repository.saveFavorites(pages);
                }
            }
        }
    }

    @Override
    public boolean isUndoable() { return true; }
}