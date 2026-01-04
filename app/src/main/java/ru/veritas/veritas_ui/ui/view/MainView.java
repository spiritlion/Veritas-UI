package ru.veritas.veritas_ui.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import ru.veritas.veritas_ui.R;

/**
 * Кастомный View для главного экрана (если понадобится)
 */
public class MainView extends LinearLayout {

    public MainView(Context context) {
        super(context);
        init(context);
    }

    public MainView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MainView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        // Можно загрузить кастомную разметку если нужно
        // inflate(context, R.layout.main_view_custom, this);
    }
}