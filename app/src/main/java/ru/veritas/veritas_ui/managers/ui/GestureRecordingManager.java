package ru.veritas.veritas_ui.managers.ui;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class GestureRecordingManager {

    public interface OnSwipeListener {
        void onSwipeDown();
        void onSwipeUp();
    }

    public static void setupVerticalSwipe(View view, OnSwipeListener listener) {
        GestureDetector gestureDetector = new GestureDetector(view.getContext(),
                new GestureDetector.SimpleOnGestureListener() {

                    private static final int SWIPE_THRESHOLD = 100;
                    private static final int SWIPE_VELOCITY_THRESHOLD = 100;

                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2,
                                           float velocityX, float velocityY) {
                        if (e1 == null || e2 == null) return false;

                        float diffY = e2.getY() - e1.getY();
                        float diffX = e2.getX() - e1.getX();

                        // Вертикальный свайп
                        if (Math.abs(diffY) > Math.abs(diffX)) {
                            if (Math.abs(diffY) > SWIPE_THRESHOLD &&
                                    Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {

                                if (diffY > 0) {
                                    // Свайп вниз
                                    if (listener != null) {
                                        listener.onSwipeDown();
                                    }
                                } else {
                                    // Свайп вверх
                                    if (listener != null) {
                                        listener.onSwipeUp();
                                    }
                                }
                                return true;
                            }
                        }
                        return false;
                    }
                });

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
    }
}