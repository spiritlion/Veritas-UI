package ru.veritas.veritas_ui.core.command.local;


import ru.veritas.veritas_ui.core.exceptions.AppUninstallException;
import ru.veritas.veritas_ui.core.loaders.AppUninstaller;

public class UninstallAppUseCase {
    private final AppUninstaller uninstaller;

    public UninstallAppUseCase(AppUninstaller uninstaller) {
        this.uninstaller = uninstaller;
    }

    public void invoke(String packageName) throws AppUninstallException {
        uninstaller.delete(packageName);
    }
}
