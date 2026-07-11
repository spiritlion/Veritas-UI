package ru.veritas.veritas_ui.core.command;

import ru.veritas.veritas_ui.core.command.local.GetInstalledAppsUseCase;
import ru.veritas.veritas_ui.core.command.local.LaunchAppUseCase;
import ru.veritas.veritas_ui.core.command.local.home.AddShortcutToDesktopCommand;
import ru.veritas.veritas_ui.core.command.local.home.AddShortcutToFirstFreeCellCommand;
import ru.veritas.veritas_ui.core.command.local.home.GetAppIconUseCase;
import ru.veritas.veritas_ui.core.command.local.home.GetShortcutsUseCase;
import ru.veritas.veritas_ui.core.command.local.home.MoveShortcutCommand;
import ru.veritas.veritas_ui.core.command.local.home.RemoveShortcutCommand;
import ru.veritas.veritas_ui.core.command.local.home.SetShortcutsCommand;
import ru.veritas.veritas_ui.core.command.local.home.SwapShortcutWithFavoriteCommand;
import ru.veritas.veritas_ui.core.command.local.home.favorites.AddToFavoritesCommand;
import ru.veritas.veritas_ui.core.command.local.home.favorites.AddToFirstFreeFavoritesCommand;
import ru.veritas.veritas_ui.core.command.local.home.favorites.GetFavoritesUseCase;
import ru.veritas.veritas_ui.core.command.local.home.favorites.RemoveFromFavoritesCommand;
import ru.veritas.veritas_ui.core.command.local.home.favorites.SwapFavoritesCommand;
import ru.veritas.veritas_ui.core.command.local.settings.OpenSettingsUseCase;
import ru.veritas.veritas_ui.core.entities.AppShortcut;

import java.util.List;

public interface CommandFactory {
    HomeScreen getHomeScreenFactory();
    Favorites getFavoritesFactory();
    Settings getSettingsFactory();
    UseCase getUseCaseFactory();


    interface HomeScreen {
        AddShortcutToDesktopCommand createAddShortcutToDesktopCommand(AppShortcut shortcut, int page, int row, int col);
        AddShortcutToFirstFreeCellCommand createAddShortcutToFirstFreeCellCommand(AppShortcut shortcut);
        MoveShortcutCommand createMoveShortcutCommand(int fromPage, int fromRow, int fromCol, int toPage, int toRow, int toCol);
        RemoveShortcutCommand createRemoveShortcutCommand(int page, int row, int col);
        SetShortcutsCommand createSetShortcutsCommand(List<List<List<AppShortcut>>> shortcuts);
        SwapShortcutWithFavoriteCommand createSwapShortcutWithFavoriteCommand(int desktopPage, int desktopRow, int desktopCol, int favPage, int favPos);
    }
    interface Favorites {
        AddToFavoritesCommand createAddToFavoritesCommand(AppShortcut shortcut, int page, int pos);
        AddToFirstFreeFavoritesCommand createAddToFirstFreeFavoritesCommand(AppShortcut shortcut);
        RemoveFromFavoritesCommand createRemoveFromFavoritesCommand(int page, int pos);
        SwapFavoritesCommand createSwapFavoritesCommand(int srcPage, int srcPos, int dstPage, int dstPos);

    }
    interface Settings {

    }
    interface UseCase {
        OpenSettingsUseCase getOpenSettingsUseCase();

        GetInstalledAppsUseCase getGetInstalledAppUseCase();

        LaunchAppUseCase getLaunchAppUseCase();

        GetShortcutsUseCase getGetShortcutsUseCase();

        GetFavoritesUseCase getGetFavoritesUseCase();

        GetAppIconUseCase getGetAppIconUseCase();
    }
}