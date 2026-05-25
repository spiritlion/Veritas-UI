package ru.veritas.veritas_ui.di;

import android.content.Context;
import android.content.pm.PackageManager;

import ru.veritas.veritas_ui.data.command.CommandFactoryImpl;
import ru.veritas.veritas_ui.data.dto.AppInfoDto;
import ru.veritas.veritas_ui.data.loaders.AndroidAppLauncher;
import ru.veritas.veritas_ui.data.loaders.icon.CachedIconLoader;
import ru.veritas.veritas_ui.data.loaders.icon.CompositeIconLoader;
import ru.veritas.veritas_ui.data.loaders.icon.DefaultIconLoader;
import ru.veritas.veritas_ui.data.loaders.icon.FileIconLoader;
import ru.veritas.veritas_ui.data.loaders.icon.IconPackIconLoader;
import ru.veritas.veritas_ui.data.mappers.AppShortcutMapperDto;
import ru.veritas.veritas_ui.data.repositories.AppRepositoryImpl;
import ru.veritas.veritas_ui.data.repositories.FavoritesRepositoryImpl;
import ru.veritas.veritas_ui.data.repositories.HomeRepositoryImpl;
import ru.veritas.veritas_ui.data.source.local.PackageManagerDataSource;
import ru.veritas.veritas_ui.domain.command.CommandFactory;
import ru.veritas.veritas_ui.domain.command.local.home.SetShortcutsCommand;
import ru.veritas.veritas_ui.domain.command.local.home.SwapShortcutWithFavoriteCommand;
import ru.veritas.veritas_ui.domain.command.local.home.favorites.SetFavoritesCommand; // если создали
import ru.veritas.veritas_ui.domain.loaders.IconLoader;
import ru.veritas.veritas_ui.domain.mappers.AppShortcutMapper;
import ru.veritas.veritas_ui.domain.repositories.AppRepository;
import ru.veritas.veritas_ui.domain.repositories.FavoritesRepository;
import ru.veritas.veritas_ui.domain.repositories.HomeRepository;
import ru.veritas.veritas_ui.domain.command.local.GetInstalledAppsUseCase;
import ru.veritas.veritas_ui.domain.command.local.LaunchAppUseCase;
import ru.veritas.veritas_ui.domain.command.local.home.GetAppIconUseCase;
import ru.veritas.veritas_ui.domain.command.local.home.GetShortcutsUseCase;
import ru.veritas.veritas_ui.domain.command.local.home.favorites.GetFavoritesUseCase;

public class DependencyContainer {
    private static DependencyContainer instance;
    private final PackageManagerDataSource dataSource;

    private final AppRepository appRepository;
    private final HomeRepository homeRepository;
    private final FavoritesRepository favoritesRepository;
    private final GetInstalledAppsUseCase getInstalledAppsUseCase;
    private final GetAppIconUseCase getAppIconUseCase;
    private final LaunchAppUseCase launchAppUseCase;
    private Context context;
    private GetShortcutsUseCase getShortcutsUseCase;
    private GetFavoritesUseCase getFavoritesUseCase;
    private CommandFactory commandFactory;

    public DependencyContainer(Context context) {
        this.context = context;
        PackageManager pm = context.getPackageManager();

        dataSource = new PackageManagerDataSource(context);
        AppShortcutMapper<AppInfoDto> mapper = new AppShortcutMapperDto();

        appRepository = new AppRepositoryImpl(dataSource, mapper);
        getInstalledAppsUseCase = new GetInstalledAppsUseCase(appRepository);

        homeRepository = new HomeRepositoryImpl(context);
        getShortcutsUseCase = new GetShortcutsUseCase(homeRepository);

        favoritesRepository = new FavoritesRepositoryImpl(context);
        getFavoritesUseCase = new GetFavoritesUseCase(favoritesRepository);

        // region -- icon loader --
        IconLoader defaultLoader = new DefaultIconLoader(pm);
        IconLoader iconPackLoader = new IconPackIconLoader(pm);
        IconLoader fileLoader = new FileIconLoader();

        CompositeIconLoader composite = new CompositeIconLoader();
        composite.addLoader(iconPackLoader);
        composite.addLoader(fileLoader);
        composite.addLoader(defaultLoader);

        IconLoader cachedLoader = new CachedIconLoader(composite);
        getAppIconUseCase = new GetAppIconUseCase(cachedLoader);
        // endregion

        AndroidAppLauncher appLauncher = new AndroidAppLauncher(context);
        launchAppUseCase = new LaunchAppUseCase(appLauncher);
    }

    public static synchronized DependencyContainer getInstance(Context context) {
        if (instance == null)
            instance = new DependencyContainer(context);
        return instance;
    }

    public GetAppIconUseCase getGetAppIconUseCase() {
        return getAppIconUseCase;
    }

    public LaunchAppUseCase getLaunchAppUseCase() {
        return launchAppUseCase;
    }

    public GetInstalledAppsUseCase getGetInstalledAppsUseCase() {
        return getInstalledAppsUseCase;
    }

    public Context getContext() {
        return context;
    }

    public GetShortcutsUseCase getGetShortcutsUseCase() {
        return getShortcutsUseCase;
    }

    public GetFavoritesUseCase getGetFavoritesUseCase() {
        return getFavoritesUseCase;
    }

    public HomeRepository getHomeRepository() {
        return homeRepository;
    }

    public FavoritesRepository getFavoritesRepository() {
        return favoritesRepository;
    }

    public CommandFactory getCommandFactory() {
        if (commandFactory == null) {
            commandFactory = new CommandFactoryImpl(this);
        }
        return commandFactory;
    }
}