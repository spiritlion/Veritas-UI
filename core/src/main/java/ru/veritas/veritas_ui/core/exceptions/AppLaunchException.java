package ru.veritas.veritas_ui.core.exceptions;

public class AppLaunchException extends Exception {
    String message;
    public AppLaunchException(String message) {
        super(message);
        this.message = message;
    }

    public AppLaunchException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }

    public String getMessage() { return message; }
}
