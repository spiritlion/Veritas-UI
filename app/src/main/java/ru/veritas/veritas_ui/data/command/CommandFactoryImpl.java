package ru.veritas.veritas_ui.data.command;

import java.util.List;

import ru.veritas.veritas_ui.domain.command.CommandFactory;
import ru.veritas.veritas_ui.domain.command.local.home.favorites.*;
import ru.veritas.veritas_ui.domain.command.local.home.*;
import ru.veritas.veritas_ui.domain.entities.AppShortcut;
import ru.veritas.veritas_ui.domain.repositories.FavoritesRepository;
import ru.veritas.veritas_ui.domain.repositories.HomeRepository;
import ru.veritas.veritas_ui.di.DependencyContainer;

public class CommandFactoryImpl implements CommandFactory {
    private final HomeRepository homeRepository;
    private final FavoritesRepository favoritesRepository;

    public CommandFactoryImpl(DependencyContainer container) {
        this.homeRepository = container.getHomeRepository();
        this.favoritesRepository = container.getFavoritesRepository();
    }

    // Рабочий стол
    @Override
    public AddShortcutToDesktopCommand createAddShortcutToDesktopCommand(AppShortcut shortcut,
                                                                         int page, int row, int col) {
        return new AddShortcutToDesktopCommand(homeRepository, shortcut, page, row, col);
    }

    @Override
    public AddShortcutToFirstFreeCellCommand createAddShortcutToFirstFreeCellCommand(AppShortcut shortcut) {
        return new AddShortcutToFirstFreeCellCommand(homeRepository, shortcut);
    }

    @Override
    public MoveShortcutCommand createMoveShortcutCommand(int fromPage, int fromRow, int fromCol,
                                                         int toPage, int toRow, int toCol) {
        return new MoveShortcutCommand(homeRepository, fromPage, fromRow, fromCol, toPage, toRow, toCol);
    }

    @Override
    public RemoveShortcutCommand createRemoveShortcutCommand(int page, int row, int col) {
        return new RemoveShortcutCommand(homeRepository, page, row, col);
    }

    @Override
    public SetShortcutsCommand createSetShortcutsCommand(List<List<List<AppShortcut>>> shortcuts) {
        return new SetShortcutsCommand(homeRepository, shortcuts);
    }

    @Override
    public SwapShortcutWithFavoriteCommand createSwapShortcutWithFavoriteCommand(
            int desktopPage, int desktopRow, int desktopCol, int favPage, int favPos) {
        return new SwapShortcutWithFavoriteCommand(
                homeRepository, favoritesRepository,
                desktopPage, desktopRow, desktopCol, favPage, favPos);
    }

    // Избранное
    @Override
    public AddToFavoritesCommand createAddToFavoritesCommand(AppShortcut shortcut, int page, int pos) {
        return new AddToFavoritesCommand(favoritesRepository, shortcut, page, pos);
    }

    @Override
    public AddToFirstFreeFavoritesCommand createAddToFirstFreeFavoritesCommand(AppShortcut shortcut) {
        return new AddToFirstFreeFavoritesCommand(favoritesRepository, shortcut);
    }

    @Override
    public RemoveFromFavoritesCommand createRemoveFromFavoritesCommand(int page, int pos) {
        return new RemoveFromFavoritesCommand(favoritesRepository, page, pos);
    }

    @Override
    public SwapFavoritesCommand createSwapFavoritesCommand(int srcPage, int srcPos,
                                                           int dstPage, int dstPos) {
        return new SwapFavoritesCommand(favoritesRepository, srcPage, srcPos, dstPage, dstPos);
    }
}