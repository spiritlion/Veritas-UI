package ru.veritas.veritas_ui.ui.common.utils;

import android.view.View;

/**
 * Общая логика подсветки View, над которым находится перетаскиваемый элемент.
 * Раньше этот код (поле highlightedView + два метода) был продублирован
 * в HomePageFragment и HomeScreenFragment.
 */
public class DragHighlightHelper {

    private final int highlightDrawableRes;
    private View highlightedView;

    public DragHighlightHelper(int highlightDrawableRes) {
        this.highlightDrawableRes = highlightDrawableRes;
    }

    /** Подсветить view. Если это тот же элемент, что и раньше — ничего не делает. */
    public void highlight(View view) {
        if (highlightedView == view) return;
        clear();
        if (view != null) {
            view.setBackgroundResource(highlightDrawableRes);
            highlightedView = view;
        }
    }

    /** Снять подсветку с текущего элемента (если есть). */
    public void clear() {
        if (highlightedView != null) {
            highlightedView.setBackground(null);
            highlightedView = null;
        }
    }
}