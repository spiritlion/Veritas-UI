package ru.veritas.veritas_ui.core.command.local;

import ru.veritas.veritas_ui.core.exceptions.AppLaunchException;
import ru.veritas.veritas_ui.core.loaders.AppLauncher;

public class LaunchAppUseCase {
    private final AppLauncher launcher;
    public LaunchAppUseCase(AppLauncher launcher) {
        this.launcher = launcher;
    }
    public void invoke(String packageName) throws AppLaunchException {
        launcher.launch(packageName);
    }
}