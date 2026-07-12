package ru.veritas.veritas_ui.core.loaders;

import ru.veritas.veritas_ui.core.exceptions.AppUninstallException;

public interface AppUninstaller {
    void delete(String packageName) throws AppUninstallException;
}
