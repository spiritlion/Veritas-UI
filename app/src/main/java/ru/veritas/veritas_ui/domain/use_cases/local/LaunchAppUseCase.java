package ru.veritas.veritas_ui.domain.use_cases.local;

import ru.veritas.veritas_ui.domain.loaders.AppLauncher;

public class LaunchAppUseCase {
    private final AppLauncher launcher;
    public LaunchAppUseCase(AppLauncher launcher) {
        this.launcher = launcher;
    }
    public void invoke(String packageName) {
        launcher.launch(packageName);
    }
}