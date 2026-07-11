package ru.veritas.veritas_ui.di;

import android.content.Context;
import android.content.pm.PackageManager;

import ru.veritas.veritas_ui.core.navigators.Navigator;
import ru.veritas.veritas_ui.core.repositories.SettingsRepository;
import ru.veritas.veritas_ui.data.command.CommandFactoryImpl;
import ru.veritas.veritas_ui.data.dto.AppInfoDto;
import ru.veritas.veritas_ui.data.loaders.AndroidAppLauncher;
import ru.veritas.veritas_ui.data.datasource.local.PackageManagerDataSource;
import ru.veritas.veritas_ui.data.mappers.AppShortcutMapperDto;
import ru.veritas.veritas_ui.data.repositories.AppRepositoryImpl;
import ru.veritas.veritas_ui.data.repositories.FavoritesRepositoryImpl;
import ru.veritas.veritas_ui.data.repositories.HomeRepositoryImpl;
import ru.veritas.veritas_ui.core.command.CommandFactory;
import ru.veritas.veritas_ui.core.mappers.AppShortcutMapper;
import ru.veritas.veritas_ui.core.repositories.AppRepository;
import ru.veritas.veritas_ui.core.repositories.FavoritesRepository;
import ru.veritas.veritas_ui.core.repositories.HomeRepository;
import ru.veritas.veritas_ui.data.repositories.SettingsRepositoryImpl;

public class DependencyContainer {
    private Context context;
    private static DependencyContainer instance;
    private final PackageManagerDataSource dataSource;
    private final Navigator navigator;
    private final AppRepository appRepository;
    private final HomeRepository homeRepository;
    private final FavoritesRepository favoritesRepository;
    private final SettingsRepository settingsRepository;  // новое поле
    private final CommandFactory commandFactory;

    public DependencyContainer(Context context) {
        this.context = context;
        PackageManager pm = context.getPackageManager();

        dataSource = new PackageManagerDataSource(context);
        AppShortcutMapper<AppInfoDto> mapper = new AppShortcutMapperDto();

        appRepository = new AppRepositoryImpl(dataSource, mapper);
        homeRepository = new HomeRepositoryImpl(context);
        settingsRepository = new SettingsRepositoryImpl(context);
        favoritesRepository = new FavoritesRepositoryImpl(context);

        navigator = new NavigatorImpl(context);
        AndroidAppLauncher appLauncher = new AndroidAppLauncher(context);
        commandFactory = new CommandFactoryImpl(
                appRepository,
                homeRepository,
                favoritesRepository,
                navigator,
                appLauncher,
                pm
        );
    }

    public static synchronized DependencyContainer getInstance(Context context) {
        if (instance == null)
            instance = new DependencyContainer(context);
        return instance;
    }

    public Context getContext() {
        return context;
    }


    public HomeRepository getHomeRepository() {
        return homeRepository;
    }

    public FavoritesRepository getFavoritesRepository() {
        return favoritesRepository;
    }
    public SettingsRepository getSettingsRepository() {
        return settingsRepository;
    }

    public CommandFactory getCommandFactory() {
        return commandFactory;
    }

    public AppRepository getAppRepository() {
        return appRepository;
    }

    public Navigator getNavigator() {
        return navigator;
    }
}