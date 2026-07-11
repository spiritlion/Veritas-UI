package ru.veritas.veritas_ui.data.loaders.icon;

import java.util.ArrayList;
import java.util.List;

import ru.veritas.veritas_ui.core.entities.AppIcon;
import ru.veritas.veritas_ui.core.loaders.IconLoader;

public class CompositeIconLoader implements IconLoader {
    private final List<IconLoader> loaders = new ArrayList<>();

    public void addLoader(IconLoader loader) { loaders.add(loader); }

    @Override
    public AppIcon loadIcon(String packageName, String iconSpec) {
        for (IconLoader loader : loaders) {
            AppIcon icon = loader.loadIcon(packageName, iconSpec);
            if (icon != null) return icon;
        }
        return null;
    }
}