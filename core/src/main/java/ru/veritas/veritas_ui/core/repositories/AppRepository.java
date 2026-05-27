package ru.veritas.veritas_ui.core.repositories;

import java.util.List;

import ru.veritas.veritas_ui.core.entities.AppShortcut;


public interface AppRepository {
    List<AppShortcut> getInstalledApps();
}