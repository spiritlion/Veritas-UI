package ru.veritas.veritas_ui.ui.classic.main.home;// HomeScreenFragment.java (который использует fragment_home_screen.xml)

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import ru.veritas.veritas_ui.R;
import ru.veritas.veritas_ui.domain.entities.AppShortcutDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SampleScreenFragment extends Fragment {


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_screen, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewPager2 viewPager = view.findViewById(R.id.viewPager);

        // Подготовка данных: каждая страница — список AppShortcutDTO
        List<List<AppShortcutDTO>> pagesData = prepareData();

//        ViewPagerPagesAdapter adapter = new ViewPagerPagesAdapter(requireActivity(), pagesData);
//        viewPager.setAdapter(adapter);
    }

    private List<List<AppShortcutDTO>> prepareData() {
        List<List<AppShortcutDTO>> pages = new ArrayList<>();

        // Страница 1
        List<AppShortcutDTO> page1 = Arrays.asList(
                new AppShortcutDTO("com.example.app1", "App 1", "ic_app1"),
                new AppShortcutDTO("com.example.app2", "App 2", "ic_app2"),
                new AppShortcutDTO("com.example.app3", "App 3", "ic_app3"),
                null,
                new AppShortcutDTO("com.example.app4", "App 4", "ic_app4")
        );

        // Страница 2
        List<AppShortcutDTO> page2 = Arrays.asList(
                new AppShortcutDTO("com.example.app4", "App 4", "ic_app4"),
                new AppShortcutDTO("com.example.app5", "App 5", "ic_app5")
        );

        pages.add(page1);
        pages.add(page2);
        return pages;
    }
}