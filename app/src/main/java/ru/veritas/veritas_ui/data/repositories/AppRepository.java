package ru.veritas.veritas_ui.data.repositories;

import java.util.List;

import ru.veritas.veritas_ui.data.dto.AppInfoDto;
import ru.veritas.veritas_ui.domain.entities.AppShortcutDTO;


public interface AppRepository {
    /**
     * Переделывает {@link AppInfoDto} в {@link AppInfoEntity}
     */
    List<AppShortcutDTO> getInstalledApps();
}