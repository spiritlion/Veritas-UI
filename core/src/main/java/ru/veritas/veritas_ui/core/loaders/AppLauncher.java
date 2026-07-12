package ru.veritas.veritas_ui.core.loaders;

import ru.veritas.veritas_ui.core.exceptions.AppLaunchException;

// domain/interfaces/AppLauncher.java
public interface AppLauncher {
    void launch(String packageName) throws AppLaunchException;
}