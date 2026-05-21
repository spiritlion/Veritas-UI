package ru.veritas.veritas_ui.ui.classic.main.home.favorites;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import java.util.ArrayList;
import java.util.List;

public class RadialMenuView extends View {

    public static class MenuItem {
        public final String label;
        public final int iconType;
        public MenuItem(String label, int iconType) {
            this.label = label;
            this.iconType = iconType;
        }
    }

    public interface Callback {
        void onItemSelected(int index, String label);
        void onDismiss();
    }

    private static class PositionedItem {
        final String label;
        final int iconType;
        final float angleDeg; // от -90 до +90
        float x, y;
        boolean highlighted;
        PositionedItem(MenuItem item, float angleDeg) {
            this.label = item.label;
            this.iconType = item.iconType;
            this.angleDeg = angleDeg;
        }
    }

    private float cx, cy;
    private float radius;
    private final float iconR;
    private final List<PositionedItem> items = new ArrayList<>();
    private int activeIndex = -1;
    private final Callback callback;

    private final Paint dimPaint       = new Paint();
    private final Paint circlePaint    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint      = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint       = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint     = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint iconPaint      = new Paint(Paint.ANTI_ALIAS_FLAG);

    public RadialMenuView(Context context, float touchX, float touchY,
                          List<MenuItem> menuItems, Callback callback) {
        super(context);
        this.callback = callback;

        float dp = context.getResources().getDisplayMetrics().density;
        iconR = 31 * dp;
        float desiredRadius = 130 * dp;

        // ===== 1. Настройка красок (особенно labelPaint) =====
        labelPaint.setColor(0xEEFFFFFF);
        labelPaint.setTextSize(11 * dp);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setShadowLayer(5, 0, 1, Color.BLACK);

        dimPaint.setColor(0xBB111111);
        circlePaint.setColor(0xFF2A2A2E);
        circlePaint.setShadowLayer(12 * dp, 0, 3 * dp, 0xAA000000);
        highlightPaint.setColor(0xFF5B21B6);
        highlightPaint.setShadowLayer(18 * dp, 0, 4 * dp, 0x99330066);
        linePaint.setColor(0x44FFFFFF);
        linePaint.setStrokeWidth(1.2f * dp);
        linePaint.setStyle(Paint.Style.STROKE);
        dotPaint.setColor(0xFFFFFFFF);
        dotPaint.setShadowLayer(6 * dp, 0, 0, 0x99000000);
        dotBorderPaint.setColor(0x55FFFFFF);
        dotBorderPaint.setStrokeWidth(1.5f * dp);
        dotBorderPaint.setStyle(Paint.Style.STROKE);

        iconPaint.setColor(Color.WHITE);
        iconPaint.setAntiAlias(true);
        iconPaint.setStyle(Paint.Style.STROKE);
        iconPaint.setStrokeWidth(1.8f * dp);
        iconPaint.setStrokeCap(Paint.Cap.ROUND);
        iconPaint.setStrokeJoin(Paint.Join.ROUND);

        // ===== 2. Распределение углов только в верхней полуокружности =====
        int count = menuItems.size();
        // Диапазон углов: от -90° (вверх) до +90° (вправо) — это 180°
        float angleStart = -180f;
        float angleEnd = 0f;
        float angleRange = angleEnd - angleStart; // 180°
        float step = (count > 1) ? angleRange / (count - 1) : 0f;
        // Если всего один пункт — поставим его строго вверх (угол -90°)
        if (count == 1) step = 0;

        for (int i = 0; i < count; i++) {
            float angle = angleStart + i * step;
            items.add(new PositionedItem(menuItems.get(i), angle));
        }

        // ===== 3. Проверка, помещается ли пункт (только для верхней половины) =====
        java.util.function.BiFunction<Float, Float, Boolean> fits = (centerX, centerY) -> {
            Rect screenRect = new Rect(0, 0,
                    getResources().getDisplayMetrics().widthPixels,
                    getResources().getDisplayMetrics().heightPixels);
            for (PositionedItem it : items) {
                double rad = Math.toRadians(it.angleDeg);
                float x = centerX + (float) (radius * Math.cos(rad));
                float y = centerY + (float) (radius * Math.sin(rad));

                // Горизонтальные границы
                if (x - iconR < 0 || x + iconR > screenRect.right) return false;

                // Верхняя граница (иконка не должна вылезать за верх)
                if (y - iconR < 0) return false;

                // Нижняя граница: учитываем текст под иконкой
                float textHeight = labelPaint.getTextSize();
                float labelBottom = y + iconR + textHeight + 5 * dp;
                if (labelBottom > screenRect.bottom) return false;

                // Горизонтальный вылет текста
                float textWidth = labelPaint.measureText(it.label);
                if (x - textWidth / 2 < 0 || x + textWidth / 2 > screenRect.right) return false;
            }
            return true;
        };

        // ===== 4. Подбор оптимального центра и радиуса =====
        float bestCx = touchX, bestCy = touchY;
        float finalRadius = desiredRadius;

        // Ограничим минимальный угловой шаг, чтобы иконки не слипались (опционально)
        float minStepDeg = 30f; // минимальный угол между соседними пунктами
        if (count > 1 && (angleRange / (count - 1)) < minStepDeg) {
            // Уменьшаем радиус, чтобы вместить больше пунктов? Или просто ничего не делаем,
            // пусть будет тесно. Лучше уменьшим радиус.
            float factor = (angleRange / (count - 1)) / minStepDeg;
            desiredRadius = desiredRadius * factor;
            if (desiredRadius < 60 * dp) desiredRadius = 60 * dp;
        }

        radius = desiredRadius;
        if (!fits.apply(touchX, touchY)) {
            // Пробуем сместить центр
            float stepShift = 20 * dp;
            float bestDist = Float.MAX_VALUE;
            boolean found = false;
            for (float dx = -200; dx <= 200; dx += stepShift) {
                for (float dy = -200; dy <= 200; dy += stepShift) {
                    float testX = touchX + dx;
                    float testY = touchY + dy;
                    // Предварительная грубая проверка: центр не должен быть слишком близко к краю
                    if (testX - desiredRadius < 0 || testX + desiredRadius > getResources().getDisplayMetrics().widthPixels) continue;
                    if (testY - desiredRadius < 0) continue; // верхняя граница для центра
                    if (testY + desiredRadius > getResources().getDisplayMetrics().heightPixels) continue;
                    if (fits.apply(testX, testY)) {
                        float dist = Math.abs(dx) + Math.abs(dy);
                        if (dist < bestDist) {
                            bestDist = dist;
                            bestCx = testX;
                            bestCy = testY;
                            found = true;
                        }
                    }
                }
            }
            if (!found) {
                // Уменьшаем радиус
                float minRadius = 60 * dp;
                float currentRadius = desiredRadius;
                while (currentRadius >= minRadius) {
                    radius = currentRadius;
                    if (fits.apply(touchX, touchY)) {
                        finalRadius = currentRadius;
                        bestCx = touchX;
                        bestCy = touchY;
                        break;
                    }
                    currentRadius -= 10 * dp;
                }
            } else {
                finalRadius = desiredRadius;
            }
        } else {
            finalRadius = desiredRadius;
        }

        this.cx = bestCx;
        this.cy = bestCy;
        this.radius = finalRadius;

        // ===== 5. Финальный пересчёт координат =====
        for (PositionedItem it : items) {
            double rad = Math.toRadians(it.angleDeg);
            it.x = cx + (float) (radius * Math.cos(rad));
            it.y = cy + (float) (radius * Math.sin(rad));
        }

        setLayerType(LAYER_TYPE_SOFTWARE, null);

        setAlpha(0f);
        setScaleX(0f);
        setScaleY(0f);
    }

    // ========== Отрисовка (без изменений) ==========
    @Override
    protected void onDraw(Canvas canvas) {
        float dp = getResources().getDisplayMetrics().density;
        canvas.drawRect(0, 0, getWidth(), getHeight(), dimPaint);

        for (PositionedItem it : items) {
            canvas.drawLine(cx, cy, it.x, it.y, linePaint);
        }

        for (PositionedItem it : items) {
            float r = it.highlighted ? iconR * 1.20f : iconR;
            Paint bg = it.highlighted ? highlightPaint : circlePaint;
            canvas.drawCircle(it.x, it.y, r, bg);
            drawIcon(canvas, it.iconType, it.x, it.y, r, dp);
            float ly = it.y + r + labelPaint.getTextSize() + 5 * dp;
            canvas.drawText(it.label, it.x, ly, labelPaint);
        }

        canvas.drawCircle(cx, cy, 7 * dp, dotPaint);
        canvas.drawCircle(cx, cy, 13 * dp, dotBorderPaint);
    }

    private void drawIcon(Canvas canvas, int iconType, float ix, float iy, float r, float dp) {
        float s = r * 0.42f;
        iconPaint.setStyle(Paint.Style.STROKE);
        switch (iconType) {
            case 0: drawSettingsIcon(canvas, ix, iy, s); break;
            case 1: drawWidgetIcon(canvas, ix, iy, s, dp); break;
            case 2: drawFolderIcon(canvas, ix, iy, s, dp); break;
            case 3: drawWallpaperIcon(canvas, ix, iy, s, dp); break;
            case 4: drawPlusIcon(canvas, ix, iy, s, dp); break;
            default: canvas.drawCircle(ix, iy, s * 0.6f, iconPaint);
        }
    }

    private void drawSettingsIcon(Canvas canvas, float ix, float iy, float s) {
        float innerR = s * 0.52f;
        canvas.drawCircle(ix, iy, innerR, iconPaint);
        int teeth = 6;
        for (int i = 0; i < teeth; i++) {
            double a = Math.toRadians(360.0 / teeth * i);
            float ax = ix + (float) (Math.cos(a) * innerR);
            float ay = iy + (float) (Math.sin(a) * innerR);
            float bx = ix + (float) (Math.cos(a) * s);
            float by = iy + (float) (Math.sin(a) * s);
            canvas.drawLine(ax, ay, bx, by, iconPaint);
        }
    }

    private void drawWidgetIcon(Canvas canvas, float ix, float iy, float s, float dp) {
        float half = s * 0.42f;
        float gap = s * 0.15f;
        iconPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        float[][] offsets = {{-half - gap, -half - gap}, {gap, -half - gap},
                {-half - gap, gap}, {gap, gap}};
        for (float[] o : offsets) {
            RectF rf = new RectF(ix + o[0], iy + o[1],
                    ix + o[0] + half, iy + o[1] + half);
            canvas.drawRoundRect(rf, 2 * dp, 2 * dp, iconPaint);
        }
        iconPaint.setStyle(Paint.Style.STROKE);
    }

    private void drawFolderIcon(Canvas canvas, float ix, float iy, float s, float dp) {
        float fw = s * 1.5f;
        float fh = s;
        float fl = ix - fw / 2f;
        float ft = iy - fh / 2f + s * 0.15f;
        RectF body = new RectF(fl, ft, fl + fw, ft + fh);
        canvas.drawRoundRect(body, 3 * dp, 3 * dp, iconPaint);
        float tabW = fw * 0.42f;
        float tabH = s * 0.32f;
        Path tab = new Path();
        tab.moveTo(fl, ft);
        tab.lineTo(fl, ft - tabH);
        tab.lineTo(fl + tabW, ft - tabH);
        tab.lineTo(fl + tabW + tabH * 0.6f, ft);
        canvas.drawPath(tab, iconPaint);
    }

    private void drawWallpaperIcon(Canvas canvas, float ix, float iy, float s, float dp) {
        float fw = s * 1.5f;
        float fh = s;
        float fl = ix - fw / 2f;
        float ft = iy - fh / 2f;
        RectF frame = new RectF(fl, ft, fl + fw, ft + fh);
        canvas.drawRoundRect(frame, 3 * dp, 3 * dp, iconPaint);
        Path mountains = new Path();
        mountains.moveTo(fl, ft + fh);
        mountains.lineTo(fl + fw * 0.30f, ft + fh * 0.35f);
        mountains.lineTo(fl + fw * 0.55f, ft + fh * 0.65f);
        mountains.lineTo(fl + fw * 0.72f, ft + fh * 0.28f);
        mountains.lineTo(fl + fw, ft + fh);
        canvas.drawPath(mountains, iconPaint);
        canvas.drawCircle(fl + fw * 0.20f, ft + fh * 0.28f, s * 0.18f, iconPaint);
    }

    private void drawPlusIcon(Canvas canvas, float ix, float iy, float s, float dp) {
        float len = s * 0.8f;
        float thick = s * 0.2f;
        iconPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(ix - len / 2, iy - thick / 2, ix + len / 2, iy + thick / 2, iconPaint);
        canvas.drawRect(ix - thick / 2, iy - len / 2, ix + thick / 2, iy + len / 2, iconPaint);
        iconPaint.setStyle(Paint.Style.STROKE);
    }

    // ========== Обработка касаний ==========
    public void onFingerMoved(float x, float y) {
        int idx = hitTest(x, y);
        if (idx == activeIndex) return;
        if (activeIndex >= 0) items.get(activeIndex).highlighted = false;
        activeIndex = idx;
        if (activeIndex >= 0) items.get(activeIndex).highlighted = true;
        invalidate();
    }

    public void onFingerUp(float x, float y) {
        onFingerMoved(x, y);
        if (activeIndex >= 0) {
            callback.onItemSelected(activeIndex, items.get(activeIndex).label);
        } else {
            callback.onDismiss();
        }
    }

    private int hitTest(float x, float y) {
        float threshold = iconR * iconR * 2.2f;
        for (int i = 0; i < items.size(); i++) {
            float dx = x - items.get(i).x;
            float dy = y - items.get(i).y;
            if (dx * dx + dy * dy <= threshold) return i;
        }
        return -1;
    }

    public void animateShow() {
        animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(200)
                .setInterpolator(new OvershootInterpolator())
                .start();

    }

    // Метод для плавного скрытия с последующим удалением
    public void animateHide(Runnable onEnd) {
        animate()
                .alpha(0f)
                .scaleX(0f)
                .scaleY(0f)
                .setDuration(150)
                .setInterpolator(new AccelerateInterpolator())
                .withEndAction(onEnd)
                .start();
    }
}