package ru.veritas.veritas_ui.ui.classic.main.home.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

public class ScalableContainer extends FrameLayout {
    private ScaleGestureDetector scaleGestureDetector;
    private ViewPager2 viewPager;
    private boolean isScaling = false;
    private OnScaleListener onScaleListener;
    private OnMultiTouchListener multiTouchListener;
    private int lastPointerCount = 0;
    private boolean wasPageInputEnabled = true;

    public interface OnScaleListener {
        void onScale(float scaleFactor);
        void onScaleBegin();
        void onScaleEnd();
    }

    public interface OnMultiTouchListener {
        void onMultiTouchChanged(boolean isMultiTouch);
    }

    public ScalableContainer(@NonNull Context context) {
        super(context);
        init();
    }

    public ScalableContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(@NonNull ScaleGestureDetector detector) {
                if (onScaleListener != null) {
                    onScaleListener.onScale(detector.getScaleFactor());
                }
                return true;
            }

            @Override
            public boolean onScaleBegin(@NonNull ScaleGestureDetector detector) {
                isScaling = true;
                if (viewPager != null) {
                    viewPager.setUserInputEnabled(false);
                }
                if (onScaleListener != null) {
                    onScaleListener.onScaleBegin();
                }
                return true;
            }

            @Override
            public void onScaleEnd(@NonNull ScaleGestureDetector detector) {
                isScaling = false;
                if (viewPager != null) {
                    // Восстанавливаем состояние прокрутки страниц с учётом мультитач
                    updatePageInputEnabled();
                }
                if (onScaleListener != null) {
                    onScaleListener.onScaleEnd();
                }
            }
        });
    }

    public void setViewPager(ViewPager2 viewPager) {
        this.viewPager = viewPager;
        if (viewPager != null) {
            wasPageInputEnabled = viewPager.isUserInputEnabled();
        }
    }

    public void setOnScaleListener(OnScaleListener listener) {
        this.onScaleListener = listener;
    }

    public void setOnMultiTouchListener(OnMultiTouchListener listener) {
        this.multiTouchListener = listener;
    }

    private void updatePageInputEnabled() {
        if (viewPager == null) return;
        boolean multiTouch = lastPointerCount >= 2;
        if (multiTouch) {
            viewPager.setUserInputEnabled(false);
        } else {
            viewPager.setUserInputEnabled(wasPageInputEnabled);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        scaleGestureDetector.onTouchEvent(ev);
        int pointerCount = ev.getPointerCount();
        if (pointerCount != lastPointerCount) {
            lastPointerCount = pointerCount;
            boolean multiTouch = pointerCount >= 2;
            if (multiTouchListener != null) {
                multiTouchListener.onMultiTouchChanged(multiTouch);
            }
            // Отключаем прокрутку страниц при мультитач
            if (viewPager != null) {
                if (multiTouch) {
                    wasPageInputEnabled = viewPager.isUserInputEnabled();
                    viewPager.setUserInputEnabled(false);
                } else {
                    viewPager.setUserInputEnabled(wasPageInputEnabled);
                }
            }
        }
        return isScaling || super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        if (isScaling) {
            return true;
        }
        return super.onTouchEvent(event);
    }
}