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
    UseCase useCase = null;
    @Override
    public CommandFactory.UseCase getUseCaseFactory() {
        if(useCase ==null)
            useCase = this.new UseCases();
        return useCase;
    }
    class UseCases implements CommandFactory.UseCase {
        OpenSettingsUseCase openSettingUseCase = null;
        @Override
        public void setOpenSettingsUseCase(OpenSettingsUseCase useCase) {
            this.openSettingUseCase = useCase;
        }

        @Override
        public OpenSettingsUseCase getOpenSettingsUseCase() {
            if (openSettingUseCase == null)
                openSettingUseCase = new OpenSettingsUseCase(navigator);
            return openSettingUseCase;
        }

        GetInstalledAppsUseCase getInstalledAppUseCase = null;
        @Override
        public void setGetInstalledAppsUseCase(GetInstalledAppsUseCase useCase) {
            getInstalledAppUseCase = useCase;
        }

        @Override
        public GetInstalledAppsUseCase getGetInstalledAppUseCase() {
            if (getInstalledAppUseCase == null)
                getInstalledAppUseCase = new GetInstalledAppsUseCase(appRepository);
            return getInstalledAppUseCase;
        }
        LaunchAppUseCase launchAppUseCase = null;
        @Override
        public void setLaunchAppUseCase(LaunchAppUseCase launchAppUseCase) {
            this.launchAppUseCase = launchAppUseCase;
        }

        @Override
        public LaunchAppUseCase getLaunchAppUseCase() {
            if (launchAppUseCase == null)
                launchAppUseCase = new LaunchAppUseCase(appLauncher);
            return launchAppUseCase;
        }

        GetShortcutsUseCase getShortcutsUseCase = null;
        @Override
        public void setGetShortcutsUseCase(GetShortcutsUseCase useCase) {
            getShortcutsUseCase = useCase;
        }

        @Override
        public GetShortcutsUseCase getGetShortcutsUseCase() {
            if (getShortcutsUseCase == null)
                getShortcutsUseCase = new GetShortcutsUseCase(homeRepository);
            return getShortcutsUseCase;
        }

        GetFavoritesUseCase getFavoritesUseCase = null;
        @Override
        public void setGetFavoritesUseCase(GetFavoritesUseCase useCase) {
            getFavoritesUseCase = useCase;
        }

        @Override
        public GetFavoritesUseCase getGetFavoritesUseCase() {
            if (getFavoritesUseCase == null)
                getFavoritesUseCase = new GetFavoritesUseCase(favoritesRepository);
            return getFavoritesUseCase;
        }

        GetAppIconUseCase getAppIconUseCase = null;
        @Override
        public GetAppIconUseCase getGetAppIconUseCase() {
            if (getAppIconUseCase == null) {
                IconLoader defaultLoader = new DefaultIconLoader(pm);
                IconLoader iconPackLoader = new IconPackIconLoader(pm);
                IconLoader fileLoader = new FileIconLoader();

                CompositeIconLoader composite = new CompositeIconLoader();
                composite.addLoader(iconPackLoader);
                composite.addLoader(fileLoader);
                composite.addLoader(defaultLoader);

                IconLoader cachedLoader = new CachedIconLoader(composite);
                getAppIconUseCase = new GetAppIconUseCase(cachedLoader);
            }
            return getAppIconUseCase;
        }

        @Override
        public void setGetAppIconUseCase(GetAppIconUseCase useCase) {
            getAppIconUseCase = useCase;
        }
    }
}