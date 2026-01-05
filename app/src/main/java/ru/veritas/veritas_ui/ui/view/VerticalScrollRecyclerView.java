package ru.veritas.veritas_ui.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class VerticalScrollRecyclerView extends RecyclerView {

    private float startY = 0;
    private boolean isAtTop = true;
    private boolean isAtBottom = false;

    // Флаг для оптимизации - не проверять каждый раз canScrollVertically
    private boolean shouldCheckScroll = true;

    public VerticalScrollRecyclerView(@NonNull Context context) {
        super(context);
        init();
    }

    public VerticalScrollRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VerticalScrollRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Предварительная настройка для лучшей производительности
        setHasFixedSize(true);
        setItemViewCacheSize(20);
        setDrawingCacheEnabled(true);
        setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        // Оптимизация скролла
        addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                // Включаем проверку только при начале скролла
                shouldCheckScroll = (newState == RecyclerView.SCROLL_STATE_IDLE);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (shouldCheckScroll) {
                    isAtTop = !canScrollVertically(-1);
                    isAtBottom = !canScrollVertically(1);
                }
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        // Быстрая проверка - если нет адаптера или элементов, пропускаем обработку
        if (getAdapter() == null || getAdapter().getItemCount() == 0) {
            return super.onInterceptTouchEvent(e);
        }

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startY = e.getY();
                isAtTop = !canScrollVertically(-1);
                isAtBottom = !canScrollVertically(1);
                // Разрешаем RecyclerView обрабатывать касание
                getParent().requestDisallowInterceptTouchEvent(true);
                break;

            case MotionEvent.ACTION_MOVE:
                float currentY = e.getY();
                float deltaY = currentY - startY;

                // Если движение значительное (>5px)
                if (Math.abs(deltaY) > 5) {
                    // Если скроллим вниз и достигли верха списка
                    if (deltaY > 0 && isAtTop) {
                        // Достигли верха - разрешаем ViewPager2 перехватить событие
                        getParent().requestDisallowInterceptTouchEvent(false);
                        return false;
                    }
                    // Если скроллим вверх и достигли низа списка
                    else if (deltaY < 0 && isAtBottom) {
                        // Достигли низа - разрешаем ViewPager2 перехватить событие
                        getParent().requestDisallowInterceptTouchEvent(false);
                        return false;
                    }
                }
                break;
        }

        return super.onInterceptTouchEvent(e);
    }

    /**
     * Оптимизированная проверка скролла
     */
    @Override
    public boolean canScrollVertically(int direction) {
        // Быстрая проверка
        if (getLayoutManager() == null || getAdapter() == null || getAdapter().getItemCount() == 0) {
            return false;
        }
        return super.canScrollVertically(direction);
    }
}