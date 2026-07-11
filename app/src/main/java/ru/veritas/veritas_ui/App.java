package ru.veritas.veritas_ui;

import android.app.Application;

import ru.veritas.veritas_ui.di.DependencyContainer;

public class App extends Application {
    private DependencyContainer dependencyContainer;

    @Override
    public void onCreate() {
        super.onCreate();
        dependencyContainer = new DependencyContainer(this);
    }

    public DependencyContainer getDependencyContainer() {
        return dependencyContainer;
    }
}

