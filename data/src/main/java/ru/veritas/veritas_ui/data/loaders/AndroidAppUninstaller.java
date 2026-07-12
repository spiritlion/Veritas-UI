package ru.veritas.veritas_ui.data.loaders;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import ru.veritas.veritas_ui.core.exceptions.AppLaunchException;
import ru.veritas.veritas_ui.core.exceptions.AppUninstallException;
import ru.veritas.veritas_ui.core.loaders.AppUninstaller;

public class AndroidAppUninstaller implements AppUninstaller {
    private final Context context;

    public AndroidAppUninstaller(Context context) {
        this.context = context;
    }

    @Override
    public void delete(String packageName) throws AppUninstallException {
        Log.d("delete app", "1");
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            Uri uri = Uri.parse("package:" + packageName);
            Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Log.d("delete app", "2");
            context.startActivity(intent);
            Log.d("delete app", "3");
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("delete app", "e1");
            throw new AppUninstallException("Приложение не найдено: " + packageName);
        } catch (ActivityNotFoundException e) {
            Log.d("delete app", "e2");
            throw new AppUninstallException("Не удалось запустить удаление");
        }
    }
}
