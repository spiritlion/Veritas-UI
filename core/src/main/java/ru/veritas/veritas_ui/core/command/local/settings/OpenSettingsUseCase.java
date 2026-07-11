package ru.veritas.veritas_ui.core.command.local.settings;

import ru.veritas.veritas_ui.core.navigators.Navigator;

public class OpenSettingsUseCase {
    private final Navigator navigator;
    public OpenSettingsUseCase(Navigator navigator) {
        this.navigator = navigator;
    }
    public void invoke() {
        navigator.openSettings();
    }
}