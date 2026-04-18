package ru.veritas.veritas_ui.domain.use_cases.local.home;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

import ru.veritas.veritas_ui.domain.entities.AppShortcutDTO;

public class GetImageUseCase {
    private final PackageManager packageManager;

    public GetImageUseCase(PackageManager packageManager) {
        this.packageManager = packageManager;
    }

    @Nullable
    public Drawable invoke(AppShortcutDTO dto) {
        try {
            return packageManager.getApplicationIcon(dto.getPackageName());
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }
}
