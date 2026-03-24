package ru.veritas.veritas_ui.data.repositories;

import java.util.List;

import ru.veritas.veritas_ui.domain.entities.AppInfoEntity;
import ru.veritas.veritas_ui.data.dto.AppInfoDto;


public interface AppRepository {
    /**
     * Переделывает {@link AppInfoDto} в {@link AppInfoEntity}
     */
    List<AppInfoEntity> getInstalledApps();
}