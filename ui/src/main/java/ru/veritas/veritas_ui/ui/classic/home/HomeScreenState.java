package ru.veritas.veritas_ui.ui.classic.home;

import java.util.List;

import ru.veritas.veritas_ui.core.entities.AppShortcut;

public abstract class HomeScreenState {
    private HomeScreenState() {}
    public static final class Loading extends HomeScreenState {
        public static final Loading INSTANCE = new Loading();
        private Loading() {}
    }

    public static final class Content extends HomeScreenState {
        private final List<List<List<AppShortcut>>> apps;

        public Content(List<List<List<AppShortcut>>> apps) {
            this.apps = apps;
        }

        public List<List<List<AppShortcut>>> getApps() {
            return apps;
        }
    }

    public static final class Error extends HomeScreenState {
        private final String message;
        private final Runnable retryAction;
        private final String buttonText;

        public Error(String message, Runnable retryAction, String buttonText) {
            this.message = message;
            this.retryAction = retryAction;
            this.buttonText = buttonText;
        }

        public Runnable getRetryAction() {
            return retryAction;
        }

        public String getMessage() {
            return message;
        }

        public String getButtonText() {
            return buttonText;
        }
    }
}
