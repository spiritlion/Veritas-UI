package ru.veritas.veritas_ui.ui.classic.home;

import static android.view.View.INVISIBLE;

import android.content.ClipData;
import android.content.ClipDescription;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ru.veritas.veritas_ui.R;
import ru.veritas.veritas_ui.domain.entities.AppShortcut;
import ru.veritas.veritas_ui.domain.use_cases.local.LaunchAppUseCase;
import ru.veritas.veritas_ui.domain.use_cases.local.home.GetAppIconUseCase;
import ru.veritas.veritas_ui.ui.common.utils.IconUtils;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {
    private List<AppShortcut> appsList;
    private ViewPagerPagesAdapter.OnItemClickListener listener;
    private final GetAppIconUseCase getAppIconUseCase;
    private final LaunchAppUseCase launchAppUseCase;
    private int pageIndex;
    private int columnCount;
    private PopupMenu currentPopup;
    private DragDropListener dragDropListener;
    private DragEdgeListener dragEdgeListener;
    private int itemHeight = RecyclerView.LayoutParams.WRAP_CONTENT;

    public interface DragEdgeListener {
        void onDragEdge(int direction);
    }
    public interface DragDropListener {
        void onDrop(int fromPage, int fromRow, int fromCol,
                    int targetPage, int targetRow, int targetCol);
    }

    public AppAdapter(List<AppShortcut> appsList, GetAppIconUseCase getAppIconUseCase,
                      LaunchAppUseCase launchAppUseCase,
                      ViewPagerPagesAdapter.OnItemClickListener listener,
                      int pageIndex, int columnCount) {
        this.appsList = appsList;
        this.getAppIconUseCase = getAppIconUseCase;
        this.launchAppUseCase = launchAppUseCase;
        this.listener = listener;
        this.pageIndex = pageIndex;
        this.columnCount = columnCount;
    }

    public void setItemHeight(int height) {
        if (this.itemHeight != height) {
            this.itemHeight = height;
            notifyDataSetChanged();
        }
    }

    public void setDragEdgeListener(DragEdgeListener listener) {
        this.dragEdgeListener = listener;
    }

    public void setDragDropListener(DragDropListener listener) {
        this.dragDropListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppShortcut app = appsList.get(position);
        if (app == null) {
            holder.setVisibility(INVISIBLE);
            return;
        }
        holder.setVisibility(View.VISIBLE);
        holder.appName.setText(app.getAppName());
        holder.appIcon.setImageDrawable(IconUtils.toDrawable(
                getAppIconUseCase.invoke(app),
                holder.itemView.getResources()
        ));

        holder.app.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(app);
        });

        holder.app.setOnTouchListener(new View.OnTouchListener() {
            private Handler handler = new Handler(Looper.getMainLooper());
            private Runnable longPressRunnable;
            private boolean isMenuShown = false;
            private boolean dragLaunched = false;
            private boolean isLongPressTriggered = false;
            private boolean hasMoved = false;
            private float startX, startY;
            private float touchSlop;

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
                                showAppMenu(holder.app, app);
                                isMenuShown = true;
                            }
                        };
                        handler.postDelayed(longPressRunnable, 500);
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
                                if (isMenuShown && currentPopup != null) {
                                    currentPopup.dismiss();
                                    currentPopup = null;
                                    isMenuShown = false;
                                }
                                int row = position / columnCount;
                                int col = position % columnCount;
                                ClipData.Item item = new ClipData.Item("home:" + pageIndex + ":" + row + ":" + col);
                                ClipData dragData = new ClipData("shortcuts",
                                        new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN}, item);
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
                            v.performClick();
                        }
                        return true;

                    default:
                        return false;
                }
            }
        });

        ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
        if (params != null) {
            params.height = itemHeight;
            holder.itemView.setLayoutParams(params);
        }
    }

    @Override
    public int getItemCount() {
        return appsList == null ? 0 : appsList.size();
    }

    public void updateData(List<AppShortcut> newList) {
        this.appsList = newList;
        notifyDataSetChanged();
    }

    public void setListener(ViewPagerPagesAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    private void showAppMenu(View view, AppShortcut app) {
        if (currentPopup != null) {
            currentPopup.dismiss();
        }
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        popup.getMenuInflater().inflate(R.menu.popup_app_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_item_uninstall) {
                // TODO: удаление
                return true;
            } else if (id == R.id.menu_item_about) {
                launchAppUseCase.invoke(app.getPackageName() + " ?info");
                return true;
            }
            return false;
        });
        popup.setOnDismissListener(menu -> currentPopup = null);
        popup.show();
        currentPopup = popup;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView appIcon;
        TextView appName;
        LinearLayout app;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            app = itemView.findViewById(R.id.inside_app);
            appIcon = itemView.findViewById(R.id.app_icon);
            appName = itemView.findViewById(R.id.app_name);
        }

        void setVisibility(int visibility) {
            app.setVisibility(visibility);
        }
    }
}