package ru.veritas.veritas_ui.ui.classic.main.home;// ViewPagerPagesAdapter.java

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import ru.veritas.veritas_ui.domain.entities.AppShortcutDTO;
import ru.veritas.veritas_ui.domain.use_cases.local.home.ToDoubleListUseCase;

import java.util.List;

public class ViewPagerPagesAdapter extends FragmentStateAdapter {
    private int columnCount = 4; // или передавайте извне

    public ViewPagerPagesAdapter(OnItemClickListener listener, FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        this.onItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(AppShortcutDTO shortcut);
        void onItemLongClick(int page, int row, int col, View v);
    }

    private OnItemClickListener onItemClickListener;

    private List<List<AppShortcutDTO>> pagesData; // список страниц, каждая страница — список приложений

    public ViewPagerPagesAdapter(OnItemClickListener onItemClickListener,
                                 @NonNull FragmentActivity fragmentActivity,
                                 List<List<List<AppShortcutDTO>>> pagesData,
                                 int columnCount) {
        super(fragmentActivity);
        this.onItemClickListener = onItemClickListener;
        this.columnCount = columnCount;
        this.pagesData = ToDoubleListUseCase.invoke(pagesData);
    }

    public ViewPagerPagesAdapter(OnItemClickListener onItemClickListener, @NonNull FragmentActivity fragmentActivity,
                                 List<List<List<AppShortcutDTO>>> pagesData) {
        super(fragmentActivity);
        this.onItemClickListener = onItemClickListener;
        this.pagesData = ToDoubleListUseCase.invoke(pagesData);
    }

    public void setPagesData( List<List<List<AppShortcutDTO>>> pagesData) {
        Log.d("s p d", (pagesData == null) + "");
        this.pagesData = ToDoubleListUseCase.invoke(pagesData); // TODO не отображается рабочий стол
    }


    @NonNull
    @Override
    public Fragment createFragment(int position) {
        List<AppShortcutDTO> appsList = pagesData.get(position);
        HomePageFragment fragment = HomePageFragment.newInstance(appsList, position, columnCount);
        fragment.setOnItemClickListener(onItemClickListener);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return pagesData != null ? pagesData.size() : 0;
    }
}