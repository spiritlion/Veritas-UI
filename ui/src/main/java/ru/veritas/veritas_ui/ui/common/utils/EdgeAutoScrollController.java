package ru.veritas.veritas_ui.ui.common.utils;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.viewpager2.widget.ViewPager2;

/**
 * Универсальный контроллер «drag у края контейнера -> перелистывание ViewPager2».
 *
 * Раньше этот код был продублирован дважды в HomeScreenFragment: один раз для
 * основного ViewPager (schedulePageFlip/cancelPageFlip/updateEdgeIndicators/...),
 * и почти дословно ещё раз для избранного (scheduleFavPageFlip/cancelFavPageFlip/...).
 * Теперь это один класс, который создаётся отдельно под каждый ViewPager2.
 */
public class EdgeAutoScrollController {

    /** Порог края по умолчанию, dp. Раньше в разных местах использовались разные значения (64dp и 50dp). */
    public static final int DEFAULT_EDGE_THRESHOLD_DP = 64;
    private static final long DEFAULT_FLIP_DELAY_MS = 300;
    private static final long INDICATOR_ANIM_MS = 80;

    public interface PageCountProvider {
        int getPageCount();
    }

    private final ViewPager2 viewPager;
    private final PageCountProvider pageCountProvider;
    private final View leftIndicator;
    private final View rightIndicator;
    private final long flipDelayMs;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private Runnable flipRunnable;
    private int scheduledTargetPage = -1;
    private int currentPageDuringDrag = -1;
    private int totalPagesDuringDrag = 0;
    private boolean dragging = false;

    public EdgeAutoScrollController(ViewPager2 viewPager,
                                    PageCountProvider pageCountProvider,
                                    View leftIndicator,
                                    View rightIndicator) {
        this(viewPager, pageCountProvider, leftIndicator, rightIndicator, DEFAULT_FLIP_DELAY_MS);
    }

    public EdgeAutoScrollController(ViewPager2 viewPager,
                                    PageCountProvider pageCountProvider,
                                    View leftIndicator,
                                    View rightIndicator,
                                    long flipDelayMs) {
        this.viewPager = viewPager;
        this.pageCountProvider = pageCountProvider;
        this.leftIndicator = leftIndicator;
        this.rightIndicator = rightIndicator;
        this.flipDelayMs = flipDelayMs;
    }

    /** Вызывать в ACTION_DRAG_STARTED. */
    public void onDragStarted() {
        dragging = true;
        currentPageDuringDrag = viewPager.getCurrentItem();
        totalPagesDuringDrag = pageCountProvider.getPageCount();
        updateIndicators();
    }

    /**
     * Вызывать в ACTION_DRAG_LOCATION, когда координаты известны напрямую.
     *
     * @param x               X-координата в системе координат контейнера той же ширины, что и containerWidth
     * @param containerWidth  ширина контейнера
     * @param edgeThresholdPx порог края в пикселях
     */
    public void onDragLocation(float x, float containerWidth, float edgeThresholdPx) {
        if (!dragging) return;
        applyDirection(computeDirection(x, containerWidth, edgeThresholdPx));
    }

    /** Вызывать, если направление уже вычислено снаружи (например, пришло из ViewModel). */
    public void onDragDirectionChanged(int direction) {
        if (!dragging) return;
        applyDirection(direction);
    }

    /** Вызывать в ACTION_DRAG_ENDED / ACTION_DRAG_EXITED. */
    public void onDragEnded() {
        dragging = false;
        cancelPendingFlip();
        hideIndicators();
    }

    /** Чистая функция вычисления направления: -1 (влево), 0 (нет), 1 (вправо). */
    public static int computeDirection(float x, float containerWidth, float edgeThresholdPx) {
        if (x < edgeThresholdPx) return -1;
        if (x > containerWidth - edgeThresholdPx) return 1;
        return 0;
    }

    private void applyDirection(int direction) {
        if (direction == -1 && currentPageDuringDrag > 0) {
            scheduleFlip(currentPageDuringDrag - 1);
        } else if (direction == 1 && currentPageDuringDrag < totalPagesDuringDrag - 1) {
            scheduleFlip(currentPageDuringDrag + 1);
        } else {
            cancelPendingFlip();
        }
    }

    private void scheduleFlip(int targetPage) {
        if (scheduledTargetPage == targetPage) return;
        cancelPendingFlip();
        scheduledTargetPage = targetPage;
        flipRunnable = () -> {
            viewPager.setCurrentItem(targetPage, true);
            currentPageDuringDrag = targetPage;
            scheduledTargetPage = -1;
            updateIndicators();
        };
        handler.postDelayed(flipRunnable, flipDelayMs);
    }

    private void cancelPendingFlip() {
        if (flipRunnable != null) {
            handler.removeCallbacks(flipRunnable);
            flipRunnable = null;
        }
        scheduledTargetPage = -1;
    }

    private void updateIndicators() {
        if (!dragging) {
            hideIndicators();
            return;
        }
        setIndicatorVisible(leftIndicator, currentPageDuringDrag > 0);
        setIndicatorVisible(rightIndicator, currentPageDuringDrag < totalPagesDuringDrag - 1);
    }

    private void hideIndicators() {
        setIndicatorVisible(leftIndicator, false);
        setIndicatorVisible(rightIndicator, false);
    }

    private void setIndicatorVisible(View indicator, boolean show) {
        if (indicator == null) return;
        if (show) {
            if (indicator.getVisibility() != View.VISIBLE) {
                indicator.setAlpha(0f);
                indicator.setVisibility(View.VISIBLE);
                indicator.animate().alpha(1f).setDuration(INDICATOR_ANIM_MS).start();
            }
        } else {
            if (indicator.getVisibility() == View.VISIBLE) {
                indicator.animate().alpha(0f).setDuration(INDICATOR_ANIM_MS)
                        .withEndAction(() -> indicator.setVisibility(View.GONE)).start();
            }
        }
    }
}