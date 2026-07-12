package ru.veritas.veritas_ui.core.exceptions;

public class AppUninstallException extends RuntimeException {
    String message;
    public AppUninstallException(String message) {
        super(message);
        this.message = message;
    }

    public AppUninstallException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }

    public String getMessage() { return message; }
}
