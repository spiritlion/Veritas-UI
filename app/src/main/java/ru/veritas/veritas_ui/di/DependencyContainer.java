package ru.veritas.veritas_ui.di;

import android.content.Context;
import android.content.pm.PackageManager;

import ru.veritas.veritas_ui.data.dto.AppInfoDto;
import ru.veritas.veritas_ui.data.loaders.AndroidAppLauncher;
import ru.veritas.veritas_ui.data.loaders.icon.CachedIconLoader;
import ru.veritas.veritas_ui.data.loaders.icon.CompositeIconLoader;
import ru.veritas.veritas_ui.data.loaders.icon.DefaultIconLoader;
import ru.veritas.veritas_ui.data.loaders.icon.FileIconLoader;
import ru.veritas.veritas_ui.data.loaders.icon.IconPackIconLoader;
import ru.veritas.veritas_ui.data.mappers.AppShortcutMapperDto;
import ru.veritas.veritas_ui.data.repositories.AppRepositoryImpl;
import ru.veritas.veritas_ui.data.source.local.PackageManagerDataSource;
import ru.veritas.veritas_ui.domain.loaders.IconLoader;
import ru.veritas.veritas_ui.domain.mappers.AppShortcutMapper;
import ru.veritas.veritas_ui.domain.repositories.AppRepository;
import ru.veritas.veritas_ui.domain.use_cases.local.GetInstalledAppsUseCase;
import ru.veritas.veritas_ui.domain.use_cases.local.LaunchAppUseCase;
import ru.veritas.veritas_ui.domain.use_cases.local.home.GetAppIconUseCase;

public class DependencyContainer {
    private static DependencyContainer instance;
    private final PackageManagerDataSource dataSource;

    private final AppRepository repository;
    private final GetInstalledAppsUseCase getInstalledAppsUseCase;
    private final GetAppIconUseCase getAppIconUseCase;
    private final LaunchAppUseCase launchAppUseCase;
    private Context context;

    public DependencyContainer(Context context) {
        this.context = context;
        PackageManager pm = context.getPackageManager();

        dataSource = new PackageManagerDataSource(context);
        AppShortcutMapper<AppInfoDto > mapper = new AppShortcutMapperDto();
        repository = new AppRepositoryImpl(dataSource, mapper);
        getInstalledAppsUseCase = new GetInstalledAppsUseCase(repository);

        // region -- icon loader --

        IconLoader defaultLoader = new DefaultIconLoader(pm);
        IconLoader iconPackLoader = new IconPackIconLoader(pm);
        IconLoader fileLoader = new FileIconLoader();

        CompositeIconLoader composite = new CompositeIconLoader();
        composite.addLoader(iconPackLoader);
        composite.addLoader(fileLoader);
        composite.addLoader(defaultLoader);

        IconLoader cashedLoader = new CachedIconLoader(composite);

        getAppIconUseCase = new GetAppIconUseCase(cashedLoader);
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
}
