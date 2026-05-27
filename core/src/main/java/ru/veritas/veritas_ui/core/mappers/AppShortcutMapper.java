package ru.veritas.veritas_ui.core.mappers;

import ru.veritas.veritas_ui.core.entities.AppShortcut;

public interface AppShortcutMapper<F> {
    AppShortcut map(F from);
}