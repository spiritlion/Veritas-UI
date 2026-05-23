package ru.veritas.veritas_ui.domain.repositories;

import java.util.List;

import ru.veritas.veritas_ui.data.dto.AppInfoDto;
import ru.veritas.veritas_ui.domain.entities.AppShortcut;


public interface AppRepository {
    /**
     * Переделывает {@link AppInfoDto} в {@link AppInfoEntity}
     */
    List<AppShortcut> getInstalledApps();
}