package ru.veritas.veritas_ui.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ru.veritas.veritas_ui.LauncherActivity;
import ru.veritas.veritas_ui.R;
import ru.veritas.veritas_ui.managers.ui.GestureRecordingManager;
import ru.veritas.veritas_ui.ui.ViewType;

public class MainFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_view, container, false);

        Button btnShowApps = view.findViewById(R.id.btnShowApps);
        btnShowApps.setOnClickListener(v -> {
            if (getActivity() instanceof LauncherActivity) {
                ((LauncherActivity) getActivity()).switchToPage(ViewType.AppList);
            }
        });

        // Настраиваем жесты для всего фрагмента
        GestureRecordingManager.setupVerticalSwipe(view, new GestureRecordingManager.OnSwipeListener() {
            @Override
            public void onSwipeDown() {
                // Свайп вниз переходит к приложениям
                if (getActivity() instanceof LauncherActivity) {
                    ((LauncherActivity) getActivity()).switchToPage(ViewType.AppList);
                }
            }

            @Override
            public void onSwipeUp() {
                // Свайп вверх на главном экране - ничего не делаем
            }
        });

        return view;
    }
}