package ru.veritas.veritas_ui.di;

import android.content.Context;
import android.content.Intent;

import ru.veritas.veritas_ui.activity.settings.SettingsActivity;
import ru.veritas.veritas_ui.core.navigators.Navigator;

public class NavigatorImpl implements Navigator {
    private final Context context;


    public NavigatorImpl(Context context) {
        this.context = context;
    }

    @Override
    public void openSettings() {
        Intent intent = new Intent(context, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
