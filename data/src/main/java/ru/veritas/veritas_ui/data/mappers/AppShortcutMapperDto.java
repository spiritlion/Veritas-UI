package ru.veritas.veritas_ui.data.mappers;

import ru.veritas.veritas_ui.data.dto.AppInfoDto;
import ru.veritas.veritas_ui.core.entities.AppShortcut;
import ru.veritas.veritas_ui.core.mappers.AppShortcutMapper;

public class AppShortcutMapperDto implements AppShortcutMapper<AppInfoDto> {
    @Override
    public AppShortcut map(AppInfoDto dto) {

        return new AppShortcut(
                dto.getPackageName(),
                dto.getAppName(),
                null // TODO: Реализовать логику кастомной иконки
        );
    }
}