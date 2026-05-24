package ru.veritas.veritas_ui.data.loaders.icon;

import java.util.HashMap;
import java.util.Map;

import ru.veritas.veritas_ui.domain.entities.AppIcon;
import ru.veritas.veritas_ui.domain.loaders.IconLoader;

public class CachedIconLoader implements IconLoader {
    private final IconLoader delegate;
    private final Map<String, AppIcon> cache = new HashMap<>();

    public CachedIconLoader(IconLoader delegate) { this.delegate = delegate; }

    @Override
    public AppIcon loadIcon(String packageName, String iconSpec) {
        String key = packageName + "|" + (iconSpec != null ? iconSpec : "");
        if (cache.containsKey(key)) return cache.get(key);
        AppIcon icon = delegate.loadIcon(packageName, iconSpec);
        cache.put(key, icon);
        return icon;
    }
}