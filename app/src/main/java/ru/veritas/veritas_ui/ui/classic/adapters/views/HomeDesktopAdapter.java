package ru.veritas.veritas_ui.ui.classic.adapters.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

/**
 * Кастомный View для главного экрана (если понадобится)
 */
public class HomeDesktopAdapter extends LinearLayout {

    public HomeDesktopAdapter(Context context) {
        super(context);
        init(context);
    }

    public HomeDesktopAdapter(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public HomeDesktopAdapter(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        // Можно загрузить кастомную разметку если нужно
        // inflate(context, R.layout.main_view_custom, this);
    }
}