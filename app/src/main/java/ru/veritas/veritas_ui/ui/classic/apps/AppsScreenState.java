// AppsScreenState.java
package ru.veritas.veritas_ui.ui.classic.apps;

import java.util.List;
import ru.veritas.veritas_ui.domain.entities.AppInfoEntity;

public abstract class AppsScreenState {
    private AppsScreenState() {} // запрещаем создание извне

    public static final class Loading extends AppsScreenState {
        public static final Loading INSTANCE = new Loading();
        private Loading() {}
    }

    public static final class Content extends AppsScreenState {
        private final List<AppInfoEntity> apps;
        private final boolean showForMe; // или другие параметры, если нужны

        public Content(List<AppInfoEntity> apps) {
            this.apps = apps;
            this.showForMe = true; // по умолчанию или передавайте через конструктор
        }

        public List<AppInfoEntity> getApps() {
            return apps;
        }

        public boolean isShowForMe() {
            return showForMe;
        }
    }

    public static final class Error extends AppsScreenState {
        private final String message;
        private final Runnable retryAction;
        private final String buttonText;

        public Error(String message, Runnable retryAction, String buttonText) {
            this.message = message;
            this.retryAction = retryAction;
            this.buttonText = buttonText;
        }

        public String getMessage() {
            return message;
        }

        public Runnable getRetryAction() {
            return retryAction;
        }

        public String getButtonText() {
            return buttonText;
        }
    }
}