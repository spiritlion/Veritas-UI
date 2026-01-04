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

public class MainFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnShowApps = view.findViewById(R.id.btnShowApps);
        Button btnSettings = view.findViewById(R.id.btnSettings);

        btnShowApps.setOnClickListener(v -> {
            if (getActivity() instanceof LauncherActivity) {
                ((LauncherActivity) getActivity()).switchToPage(1);
            }
        });

        btnSettings.setOnClickListener(v -> {
            // TODO: Добавить переход на страницу настроек
            // ((LauncherActivity) getActivity()).switchToPage(2);
        });
    }
}