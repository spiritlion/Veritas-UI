package ru.veritas.veritas_ui.domain.command;

import ru.veritas.veritas_ui.domain.command.local.home.favorites.*;
import ru.veritas.veritas_ui.domain.command.local.home.*;
import ru.veritas.veritas_ui.domain.entities.AppShortcut;
import java.util.List;

public interface CommandFactory {
    // рабочий стол
    AddShortcutToDesktopCommand createAddShortcutToDesktopCommand(AppShortcut shortcut, int page, int row, int col);
    AddShortcutToFirstFreeCellCommand createAddShortcutToFirstFreeCellCommand(AppShortcut shortcut);
    MoveShortcutCommand createMoveShortcutCommand(int fromPage, int fromRow, int fromCol, int toPage, int toRow, int toCol);
    RemoveShortcutCommand createRemoveShortcutCommand(int page, int row, int col);
    SetShortcutsCommand createSetShortcutsCommand(List<List<List<AppShortcut>>> shortcuts);
    SwapShortcutWithFavoriteCommand createSwapShortcutWithFavoriteCommand(int desktopPage, int desktopRow, int desktopCol, int favPage, int favPos);

    // избранное
    AddToFavoritesCommand createAddToFavoritesCommand(AppShortcut shortcut, int page, int pos);
    AddToFirstFreeFavoritesCommand createAddToFirstFreeFavoritesCommand(AppShortcut shortcut);
    RemoveFromFavoritesCommand createRemoveFromFavoritesCommand(int page, int pos);
    SwapFavoritesCommand createSwapFavoritesCommand(int srcPage, int srcPos, int dstPage, int dstPos);
}