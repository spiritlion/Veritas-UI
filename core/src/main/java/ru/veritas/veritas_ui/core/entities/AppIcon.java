package ru.veritas.veritas_ui.core.entities;

public class AppIcon {
    private final byte[] pngData; // или Bitmap, но без android.graphics
    public AppIcon(byte[] pngData) {
        this.pngData = pngData;
    }

    public byte[] getPngData() {
        return pngData;
    }
}