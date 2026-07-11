package ru.veritas.veritas_ui.data.command;

import android.content.pm.PackageManager;

import java.util.List;

import ru.veritas.veritas_ui.core.command.CommandFactory;
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
import ru.veritas.veritas_ui.core.loaders.AppLauncher;
import ru.veritas.veritas_ui.core.loaders.IconLoader;
import ru.veritas.veritas_ui.core.navigators.Navigator;
import ru.veritas.veritas_ui.core.repositories.AppRepository;
import ru.veritas.veritas_ui.core.repositories.FavoritesRepository;
import ru.veritas.veritas_ui.core.repositories.HomeRepository;
import ru.veritas.veritas_ui.data.loaders.icon.CachedIconLoader;
import ru.veritas.veritas_ui.data.loaders.icon.CompositeIconLoader;
import ru.veritas.veritas_ui.data.loaders.icon.DefaultIconLoader;
import ru.veritas.veritas_ui.data.loaders.icon.FileIconLoader;
import ru.veritas.veritas_ui.data.loaders.icon.IconPackIconLoader;

public class CommandFactoryImpl implements CommandFactory {
    private final AppRepository appRepository;
    private final HomeRepository homeRepository;
    private final FavoritesRepository favoritesRepository;
    private final UseCase useCase;   // один экземпляр, создаётся в конструкторе
    public final Navigator navigator;
    public final AppLauncher appLauncher;
    private final PackageManager pm;


    public CommandFactoryImpl(AppRepository appRepository,
                              HomeRepository homeRepository,
                              FavoritesRepository favoritesRepository,
                              Navigator navigator, AppLauncher appLauncher,
                              PackageManager pm) {
        this.appRepository = appRepository;
        this.homeRepository = homeRepository;
        this.favoritesRepository = favoritesRepository;
        this.navigator = navigator;
        this.appLauncher = appLauncher;
        this.pm = pm;
        this.useCase = new UseCases();
    }

    CommandFactory.HomeScreen homeScreen = null;
    @Override
    public CommandFactory.HomeScreen getHomeScreenFactory() {
        if (homeScreen == null)
            homeScreen = this.new HomeScreen();
        return homeScreen;
    }
    class HomeScreen implements CommandFactory.HomeScreen {

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
    }
    CommandFactory.Favorites favorites = null;
    @Override
    public CommandFactory.Favorites getFavoritesFactory() {
        if (favorites == null)
            favorites = this.new Favorites();
        return favorites;
    }
    class Favorites implements CommandFactory.Favorites {
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
    Settings settings = null;
    @Override
    public CommandFactory.Settings getSettingsFactory() {
        if(settings==null)
            settings = this.new Settings();
        return settings;
    }
    class Settings implements CommandFactory.Settings { }
    @Override
    public CommandFactory.UseCase getUseCaseFactory() {
        return useCase;
    }
    // Вложенный класс – теперь неизменяемый
    private class UseCases implements CommandFactory.UseCase {
        private final OpenSettingsUseCase openSettingsUseCase;
        private final GetInstalledAppsUseCase getInstalledAppsUseCase;
        private final LaunchAppUseCase launchAppUseCase;
        private final GetShortcutsUseCase getShortcutsUseCase;
        private final GetFavoritesUseCase getFavoritesUseCase;
        private final GetAppIconUseCase getAppIconUseCase;

        public UseCases() {
            // 1. Простые use case
            this.openSettingsUseCase = new OpenSettingsUseCase(navigator);
            this.getInstalledAppsUseCase = new GetInstalledAppsUseCase(appRepository);
            this.launchAppUseCase = new LaunchAppUseCase(appLauncher);
            this.getShortcutsUseCase = new GetShortcutsUseCase(homeRepository);
            this.getFavoritesUseCase = new GetFavoritesUseCase(favoritesRepository);

            // 2. Сложный GetAppIconUseCase (логика построения загрузчиков вынесена сюда же)
            IconLoader defaultLoader = new DefaultIconLoader(pm);
            IconLoader iconPackLoader = new IconPackIconLoader(pm);
            IconLoader fileLoader = new FileIconLoader();

            CompositeIconLoader composite = new CompositeIconLoader();
            composite.addLoader(iconPackLoader);
            composite.addLoader(fileLoader);
            composite.addLoader(defaultLoader);

            IconLoader cachedLoader = new CachedIconLoader(composite);
            this.getAppIconUseCase = new GetAppIconUseCase(cachedLoader);
        }

        @Override
        public OpenSettingsUseCase getOpenSettingsUseCase() {
            return openSettingsUseCase;
        }

        @Override
        public GetInstalledAppsUseCase getGetInstalledAppUseCase() {
            return getInstalledAppsUseCase;
        }

        @Override
        public LaunchAppUseCase getLaunchAppUseCase() {
            return launchAppUseCase;
        }

        @Override
        public GetShortcutsUseCase getGetShortcutsUseCase() {
            return getShortcutsUseCase;
        }

        @Override
        public GetFavoritesUseCase getGetFavoritesUseCase() {
            return getFavoritesUseCase;
        }

        @Override
        public GetAppIconUseCase getGetAppIconUseCase() {
            return getAppIconUseCase;
        }
    }
}