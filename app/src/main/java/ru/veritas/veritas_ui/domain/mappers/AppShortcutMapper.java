package ru.veritas.veritas_ui.domain.mappers;

import ru.veritas.veritas_ui.domain.entities.AppShortcut;

public interface AppShortcutMapper<F> {
    AppShortcut map(F from);
}