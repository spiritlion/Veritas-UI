package ru.veritas.veritas_ui.ui.common.utils;

import android.content.ClipData;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * Обрабатывает жест на карточке:
 *  - обычный тап -> onClick()
 *  - долгое нажатие без движения -> onLongPress() (например, показ контекстного меню)
 *  - долгое нажатие + движение пальца -> запуск drag-and-drop (onDragStart() + createDragData())
 *
 * Раньше этот код жил как анонимный класс внутри AppAdapter.onBindViewHolder
 * и создавался заново на каждый bind.
 */
public class LongPressDragTouchListener implements View.OnTouchListener {

    public interface Callback {
        void onClick();
        void onLongPress();
        /** Вызывается непосредственно перед стартом drag (например, чтобы закрыть уже показанное меню). */
        void onDragStart();
        ClipData createDragData();
    }

    private static final long LONG_PRESS_DELAY_MS = 500;

    private final Callback callback;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable longPressRunnable;

    private boolean isMenuShown = false;
    private boolean dragLaunched = false;
    private boolean isLongPressTriggered = false;
    private boolean hasMoved = false;
    private float startX, startY;
    private float touchSlop;

    public LongPressDragTouchListener(Callback callback) {
        this.callback = callback;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (touchSlop == 0) {
            touchSlop = ViewConfiguration.get(v.getContext()).getScaledTouchSlop();
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getRawX();
                startY = event.getRawY();
                isMenuShown = false;
                dragLaunched = false;
                isLongPressTriggered = false;
                hasMoved = false;
                v.getParent().requestDisallowInterceptTouchEvent(true);

                longPressRunnable = () -> {
                    isLongPressTriggered = true;
                    if (!dragLaunched) {
                        callback.onLongPress();
                        isMenuShown = true;
                    }
                };
                handler.postDelayed(longPressRunnable, LONG_PRESS_DELAY_MS);
                return true;

            case MotionEvent.ACTION_MOVE:
                if (dragLaunched) return true;

                float deltaX = Math.abs(event.getRawX() - startX);
                float deltaY = Math.abs(event.getRawY() - startY);

                if (deltaX > touchSlop || deltaY > touchSlop) {
                    if (!hasMoved) {
                        hasMoved = true;
                        handler.removeCallbacks(longPressRunnable);
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                    }

                    if (isLongPressTriggered) {
                        dragLaunched = true;
                        handler.removeCallbacks(longPressRunnable);
                        if (isMenuShown) {
                            callback.onDragStart();
                            isMenuShown = false;
                        }
                        ClipData dragData = callback.createDragData();
                        v.startDragAndDrop(dragData, new View.DragShadowBuilder(v), null, 0);
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                    }
                }
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                handler.removeCallbacks(longPressRunnable);
                v.getParent().requestDisallowInterceptTouchEvent(false);
                if (!dragLaunched && !isLongPressTriggered && !hasMoved) {
                    callback.onClick();
                }
                return true;

            default:
                return false;
        }
    }
}