// [file name]: DesktopDragListener.java
package ru.veritas.veritas_ui.managers.ui.desktop;

import android.content.ClipData;
import android.content.ClipDescription;
import android.graphics.Color;
import android.view.DragEvent;
import android.view.View;
import android.widget.GridLayout;

public class DesktopDragListener implements View.OnDragListener {

    private final GridLayout gridLayout;
    private final OnDragActionListener dragActionListener;
    private View draggedView;
    private int dragStartX, dragStartY;

    public interface OnDragActionListener {
        void onDragStart(View view, int gridX, int gridY);
        void onDragEnd(View view, int gridX, int gridY);
        void onDragCancel(View view);
    }

    public DesktopDragListener(GridLayout gridLayout, OnDragActionListener listener) {
        this.gridLayout = gridLayout;
        this.dragActionListener = listener;
    }

    @Override
    public boolean onDrag(View v, DragEvent event) {
        final int action = event.getAction();

        switch (action) {
            case DragEvent.ACTION_DRAG_STARTED:
                // Начало перетаскивания
                if (event.getClipDescription() != null &&
                        event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                    v.setBackgroundColor(Color.parseColor("#E0F7FA"));
                    return true;
                }
                return false;

            case DragEvent.ACTION_DRAG_ENTERED:
                // Вошли в зону перетаскивания
                v.setBackgroundColor(Color.parseColor("#B2EBF2"));
                return true;

            case DragEvent.ACTION_DRAG_LOCATION:
                // Перемещение внутри зоны
                handleDragLocation(event.getX(), event.getY());
                return true;

            case DragEvent.ACTION_DRAG_EXITED:
                // Вышли из зоны
                v.setBackgroundColor(Color.parseColor("#E0F7FA"));
                clearPositionHighlights();
                return true;

            case DragEvent.ACTION_DROP:
                // Отпустили элемент
                v.setBackgroundColor(Color.TRANSPARENT);

                if (event.getClipData() != null) {
                    ClipData.Item item = event.getClipData().getItemAt(0);
                    draggedView = (View) event.getLocalState();

                    // Получаем координаты отпускания
                    float dropX = event.getX();
                    float dropY = event.getY();

                    int gridX = (int) (dropX / getCellWidth());
                    int gridY = (int) (dropY / getCellHeight());

                    // Проверяем границы
                    gridX = Math.max(0, Math.min(gridX, gridLayout.getColumnCount() - 1));
                    gridY = Math.max(0, Math.min(gridY, gridLayout.getRowCount() - 1));

                    if (dragActionListener != null) {
                        dragActionListener.onDragEnd(draggedView, gridX, gridY);
                    }

                    clearPositionHighlights();
                }
                return true;

            case DragEvent.ACTION_DRAG_ENDED:
                // Завершение перетаскивания
                v.setBackgroundColor(Color.TRANSPARENT);
                if (dragActionListener != null && draggedView != null) {
                    dragActionListener.onDragCancel(draggedView);
                }
                clearPositionHighlights();
                return true;

            default:
                return false;
        }
    }

    private void handleDragLocation(float x, float y) {
        // Вычисляем текущую ячейку
        int currentX = (int) (x / getCellWidth());
        int currentY = (int) (y / getCellHeight());

        // Проверяем границы
        currentX = Math.max(0, Math.min(currentX, gridLayout.getColumnCount() - 1));
        currentY = Math.max(0, Math.min(currentY, gridLayout.getRowCount() - 1));

        // Подсвечиваем текущую позицию
        highlightPosition(currentX, currentY);
    }

    private void highlightPosition(int gridX, int gridY) {
        // Убираем подсветку со всех ячеек
        clearPositionHighlights();

        // Находим ячейку по координатам и подсвечиваем её
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            View child = gridLayout.getChildAt(i);
            GridLayout.LayoutParams params = (GridLayout.LayoutParams) child.getLayoutParams();

            if (params.columnSpec == GridLayout.spec(gridX) &&
                    params.rowSpec == GridLayout.spec(gridY)) {
                child.setBackgroundColor(Color.parseColor("#80E0E0E0"));
                break;
            }
        }
    }

    private void clearPositionHighlights() {
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            View child = gridLayout.getChildAt(i);
            child.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private float getCellWidth() {
        return gridLayout.getWidth() / (float) gridLayout.getColumnCount();
    }

    private float getCellHeight() {
        return gridLayout.getHeight() / (float) gridLayout.getRowCount();
    }

    public void setDragStartPosition(int gridX, int gridY) {
        this.dragStartX = gridX;
        this.dragStartY = gridY;
    }
}