package ru.veritas.veritas_ui.data.repositories;

import ru.veritas.veritas_ui.data.dto.AppInfoDto;
import ru.veritas.veritas_ui.data.source.local.PackageManagerDataSource;
import ru.veritas.veritas_ui.domain.entities.AppInfoEntity;

import java.util.ArrayList;
import java.util.List;


public class AppRepositoryImpl implements AppRepository {
    private final PackageManagerDataSource dataSource;

    public AppRepositoryImpl(PackageManagerDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<AppInfoEntity> getInstalledApps() {
        List<AppInfoDto> dtos = dataSource.getInstalledApps();
        List<AppInfoEntity> apps = new ArrayList<>();
        for (AppInfoDto dto : dtos) {
            apps.add(new AppInfoEntity(dto.getPackageName(), dto.getAppName(), dto.getIcon()));
        }
        return apps;
    }
}