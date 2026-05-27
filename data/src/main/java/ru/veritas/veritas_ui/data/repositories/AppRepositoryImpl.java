package ru.veritas.veritas_ui.data.repositories;

import java.util.ArrayList;
import java.util.List;

import ru.veritas.veritas_ui.data.dto.AppInfoDto;
import ru.veritas.veritas_ui.core.entities.AppShortcut;
import ru.veritas.veritas_ui.core.mappers.AppShortcutMapper;
import ru.veritas.veritas_ui.core.repositories.AppRepository;
import ru.veritas.veritas_ui.data.datasource.local.PackageManagerDataSource;


public class AppRepositoryImpl implements AppRepository {
    private final PackageManagerDataSource dataSource;
    private final AppShortcutMapper<AppInfoDto> mapper;

    public AppRepositoryImpl(PackageManagerDataSource dataSource,
                             AppShortcutMapper<AppInfoDto> mapper) {
        this.dataSource = dataSource;
        this.mapper = mapper;
    }

    @Override
    public List<AppShortcut> getInstalledApps() {
        List<AppInfoDto> dtos = dataSource.getInstalledApps();
        List<AppShortcut> apps = new ArrayList<>();
        for (AppInfoDto dto : dtos) {
            apps.add(mapper.map(dto));
        }
        return apps;
    }
}