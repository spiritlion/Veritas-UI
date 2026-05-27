package ru.veritas.veritas_ui.ui.classic.home;// ViewPagerPagesAdapter.java

import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

import ru.veritas.veritas_ui.core.command.local.LaunchAppUseCase;
import ru.veritas.veritas_ui.core.command.local.home.GetAppIconUseCase;
import ru.veritas.veritas_ui.core.entities.AppShortcut;
import ru.veritas.veritas_ui.ui.classic.apps.AppsAdapter;
import ru.veritas.veritas_ui.ui.common.utils.ToDoubleListUtils;

public class ViewPagerPagesAdapter extends FragmentStateAdapter {
    private final OnItemClickListener listener;
    private final GetAppIconUseCase getAppIconUseCase;
    private final LaunchAppUseCase launchAppUseCase;
    private final int columnCount;
    private List<List<AppShortcut>> pagesData;

    public interface OnItemClickListener {
        void onItemClick(AppShortcut shortcut);
        void onItemLongClick(int page, int row, int col, View v);
    }

    public ViewPagerPagesAdapter(OnItemClickListener listener,
                                 @NonNull FragmentActivity fragmentActivity,
                                 GetAppIconUseCase getAppIconUseCase,
                                 LaunchAppUseCase launchAppUseCase,
                                 int columnCount) {
        super(fragmentActivity);
        this.listener = listener;
        this.getAppIconUseCase = getAppIconUseCase;
        this.launchAppUseCase = launchAppUseCase;
        this.columnCount = columnCount;
    }

    public void setPagesData(List<List<List<AppShortcut>>> triplePages) {
        this.pagesData = ToDoubleListUtils.invoke(triplePages);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        HomePageFragment fragment = HomePageFragment.newInstance(position, columnCount,
                getAppIconUseCase, launchAppUseCase);
        fragment.setOnItemClickListener(listener);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return pagesData != null ? pagesData.size() : 0;
    }
}