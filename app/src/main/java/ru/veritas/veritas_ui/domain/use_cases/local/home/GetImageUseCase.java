package ru.veritas.veritas_ui.domain.use_cases.local.home;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import androidx.annotation.Nullable;
import ru.veritas.veritas_ui.domain.entities.AppShortcutDTO;

@FunctionalInterface
public interface GetImageUseCase {
    @Nullable Drawable invoke(AppShortcutDTO dto);

    static GetImageUseCase create(PackageManager packageManager) {
        return dto -> {
            try {
                return packageManager.getApplicationIcon(dto.getPackageName());
            } catch (PackageManager.NameNotFoundException e) {
                return null;
            }
        };
    }
}