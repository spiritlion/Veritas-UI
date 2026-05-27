package ru.veritas.veritas_ui.core.loaders;

import ru.veritas.veritas_ui.core.entities.AppIcon;

public interface IconLoader {
    AppIcon loadIcon(String packageName, String iconSpec);
}
