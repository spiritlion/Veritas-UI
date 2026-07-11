package ru.veritas.veritas_ui.core.command.local.home;

import ru.veritas.veritas_ui.core.entities.AppIcon;
import ru.veritas.veritas_ui.core.entities.AppShortcut;
import ru.veritas.veritas_ui.core.loaders.IconLoader;

public class GetAppIconUseCase {
    private final IconLoader iconLoader;
    public GetAppIconUseCase(IconLoader iconLoader) {
        this.iconLoader = iconLoader;
    }
    public AppIcon invoke(AppShortcut shortcut) {
        return iconLoader.loadIcon(shortcut.getPackageName(), shortcut.getIconSpec());
    }
}