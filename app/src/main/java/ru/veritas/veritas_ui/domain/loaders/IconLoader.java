package ru.veritas.veritas_ui.domain.loaders;

import androidx.annotation.Nullable;

import ru.veritas.veritas_ui.domain.entities.AppIcon;

public interface IconLoader {
    @Nullable
    AppIcon loadIcon(String packageName, @Nullable String iconSpec);
}
