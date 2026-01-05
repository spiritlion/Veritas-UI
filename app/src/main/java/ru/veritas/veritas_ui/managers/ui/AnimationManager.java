package ru.veritas.veritas_ui.managers.ui;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

/**
 * Менаджер, отвечающий за анимации
 */
public abstract class AnimationManager implements ViewPager2.PageTransformer {
    // Наверное анимация делается по другому, но я пока не особо разбирался в этой теме
    public static class DepthPageTransformer extends AnimationManager{
        private static final float MIN_SCALE = 0.85f;

        @Override
        public void transformPage(@NonNull View view, float position) {
            int pageWidth = view.getWidth();

            if (position < -1) {
                view.setAlpha(0f);
            } else if (position <= 0) {
                view.setAlpha(1f);
                view.setTranslationX(0f);
                view.setScaleX(1f);
                view.setScaleY(1f);
            } else if (position <= 1) {
                view.setAlpha(1 - position);
                view.setTranslationX(pageWidth * -position);
                float scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);
            } else {
                view.setAlpha(0f);
            }
        }
    }

    public static class VerticalDepthPageTransformer extends AnimationManager {

        private static final float MIN_SCALE = 0.85f;
        private static final float MIN_ALPHA = 0.5f;

        @Override
        public void transformPage(@NonNull View view, float position) {
            int pageHeight = view.getHeight();

            if (position < -1) { // [-Infinity,-1)
                // Эта страница находится слева от экрана
                view.setAlpha(0f);
            } else if (position <= 1) { // [-1,1]
                // Модифицируем стандартное слайд-перемещение для вертикального скролла
                view.setTranslationY(-position * pageHeight);

                // Эффект глубины (уменьшение масштаба)
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

                // Эффект прозрачности
                float alphaFactor = Math.max(MIN_ALPHA, 1 - Math.abs(position));
                view.setAlpha(alphaFactor);
            } else { // (1,+Infinity]
                // Эта страница находится справа от экрана
                view.setAlpha(0f);
            }
        }
    }

    public static class VerticalPageTransformer extends AnimationManager {

        private static final float MIN_SCALE = 0.85f;

        @Override
        public void transformPage(@NonNull View view, float position) {
            int pageHeight = view.getHeight();

            if (position < -1) { // [-Infinity,-1)
                // Эта страница находится выше экрана
                view.setAlpha(0f);
            } else if (position <= 1) { // [-1,1]
                // Модифицируем стандартное слайд-перемещение для вертикального скролла
                view.setTranslationY(-position * pageHeight);

                // Эффект глубины (уменьшение масштаба)
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

                // Эффект прозрачности
                view.setAlpha(Math.max(0.5f, 1 - Math.abs(position)));
            } else { // (1,+Infinity]
                // Эта страница находится ниже экрана
                view.setAlpha(0f);
            }
        }
    }
}
