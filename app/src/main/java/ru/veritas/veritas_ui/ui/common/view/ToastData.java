package ru.veritas.veritas_ui.ui.common.view;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

public class ToastData {
    private final String message;
    private final boolean durationIsShort;
    private final ToastType type;

    public ToastData(String message, boolean durationIsShort, ToastType type) {
        this.message = message;
        this.durationIsShort = durationIsShort;
        this.type = type;
    }

    public ToastData(String message, ToastType type) {
        this.message = message;
        this.durationIsShort = true;
        this.type = type;
    }

    public ToastData(String message, boolean durationIsShort) {
        this.message = message;
        this.durationIsShort = durationIsShort;
        this.type = ToastType.Info;
    }

    public ToastData(String message) {
        this.message = message;
        this.durationIsShort = true;
        this.type = ToastType.Info;
    }

    @NonNull
    @Contract(value = "_ -> new", pure = true)
    public static ToastData info(String message) {
        return new ToastData(message, true, ToastType.Info);
    }

    @NonNull
    @Contract(value = "_ -> new", pure = true)
    public static ToastData error(String message) {
        return new ToastData(message, true, ToastType.Error);
    }

    public String getMessage() {
        return message;
    }

    public boolean getDurationIsShort() {
        return durationIsShort;
    }

    public ToastType getType() {
        return type;
    }

    public enum ToastType{
        Info,
        Error;
    }
}
