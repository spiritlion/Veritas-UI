package ru.veritas.veritas_ui.data.repositories;

import java.util.ArrayList;
import java.util.List;

import ru.veritas.veritas_ui.data.dto.AppInfoDto;
import ru.veritas.veritas_ui.data.source.local.PackageManagerDataSource;
import ru.veritas.veritas_ui.domain.entities.AppShortcutDTO;


public class AppRepositoryImpl implements AppRepository {
    private final PackageManagerDataSource dataSource;

    public AppRepositoryImpl(PackageManagerDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<AppShortcutDTO> getInstalledApps() {
        List<AppInfoDto> dtos = dataSource.getInstalledApps();
        List<AppShortcutDTO> apps = new ArrayList<>();
        for (AppInfoDto dto : dtos) {
            apps.add(new AppShortcutDTO(dto.getPackageName(), dto.getAppName(), null));
        }
        return apps;
    }
}