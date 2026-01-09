// [file name]: DesktopDragShadowBuilder.java
package ru.veritas.veritas_ui.managers.ui.desktop;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import ru.veritas.veritas_ui.R;

public class DesktopDragShadowBuilder extends View.DragShadowBuilder {

    private final Drawable shadowDrawable;
    private final String shadowText;
    private final float scale;

    public DesktopDragShadowBuilder(View view, float scale) {
        super(view);
        this.scale = scale;

        // Получаем иконку и текст из view
        Drawable icon = null;
        String text = "";

        if (view instanceof View) {
            View itemView = view;
            ImageView iconView = itemView.findViewById(R.id.desktopAppIcon);
            TextView textView = itemView.findViewById(R.id.desktopAppName);

            if (iconView != null) {
                icon = iconView.getDrawable();
            }

            if (textView != null) {
                text = textView.getText().toString();
            }
        }

        shadowDrawable = icon;
        shadowText = text;
    }

    @Override
    public void onProvideShadowMetrics(Point shadowSize, Point shadowTouchPoint) {
        // Размер тени
        int width = (int) (getView().getWidth() * scale);
        int height = (int) (getView().getHeight() * scale);
        shadowSize.set(width, height);

        // Точка касания (центр тени)
        shadowTouchPoint.set(width / 2, height / 2);
    }

    @Override
    public void onDrawShadow(Canvas canvas) {
        if (shadowDrawable != null) {
            // Сохраняем состояние canvas
            canvas.save();

            // Применяем масштаб
            canvas.scale(scale, scale);

            // Рисуем фон
            Paint backgroundPaint = new Paint();
            backgroundPaint.setColor(Color.WHITE);
            backgroundPaint.setStyle(Paint.Style.FILL);
            canvas.drawRoundRect(0, 0, getView().getWidth(), getView().getHeight(), 20, 20, backgroundPaint);

            // Рисуем границу
            Paint borderPaint = new Paint();
            borderPaint.setColor(Color.LTGRAY);
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setStrokeWidth(2);
            canvas.drawRoundRect(0, 0, getView().getWidth(), getView().getHeight(), 20, 20, borderPaint);

            // Рисуем иконку (с эффектом тени)
            shadowDrawable.setBounds(10, 10, getView().getWidth() - 10, getView().getHeight() - 30);
            shadowDrawable.draw(canvas);

            // Рисуем текст (упрощенно)
            if (!shadowText.isEmpty()) {
                Paint textPaint = new Paint();
                textPaint.setColor(Color.BLACK);
                textPaint.setTextSize(20);
                textPaint.setTextAlign(Paint.Align.CENTER);

                float textWidth = textPaint.measureText(shadowText);
                if (textWidth > getView().getWidth() - 20) {
                    // Обрезаем текст если слишком длинный
                    String shortened = shadowText.substring(0, Math.min(8, shadowText.length())) + "...";
                    canvas.drawText(shortened, getView().getWidth() / 2, getView().getHeight() - 5, textPaint);
                } else {
                    canvas.drawText(shadowText, getView().getWidth() / 2, getView().getHeight() - 5, textPaint);
                }
            }

            // Восстанавливаем состояние canvas
            canvas.restore();
        } else {
            // Если нет иконки, рисуем простой прямоугольник
            super.onDrawShadow(canvas);
        }
    }
}