package ru.veritas.veritas_ui.data.repositories;

import ru.veritas.veritas_ui.data.dto.AppInfoDto;
import ru.veritas.veritas_ui.data.source.local.PackageManagerDataSource;
import ru.veritas.veritas_ui.domain.entities.AppShortcut;
import ru.veritas.veritas_ui.domain.repositories.AppRepository;

import java.util.ArrayList;
import java.util.List;


public class AppRepositoryImpl implements AppRepository {
    private final PackageManagerDataSource dataSource;

    public AppRepositoryImpl(PackageManagerDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<AppShortcut> getInstalledApps() {
        List<AppInfoDto> dtos = dataSource.getInstalledApps();
        List<AppShortcut> apps = new ArrayList<>();
        for (AppInfoDto dto : dtos) {
            apps.add(new AppShortcut(dto.getPackageName(), dto.getAppName(), null));
        }
        return apps;
    }
}