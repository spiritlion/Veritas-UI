package ru.veritas.veritas_ui.ui.classic.home;

import static android.view.View.INVISIBLE;

import android.content.ClipData;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ru.veritas.veritas_ui.ui.R;
import ru.veritas.veritas_ui.core.entities.AppShortcut;
import ru.veritas.veritas_ui.core.command.local.home.GetAppIconUseCase;
import ru.veritas.veritas_ui.ui.common.utils.DragDataHelper;
import ru.veritas.veritas_ui.ui.common.utils.IconUtils;
import ru.veritas.veritas_ui.ui.common.utils.LongPressDragTouchListener;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {
    private List<AppShortcut> appsList;
    private ViewPagerPagesAdapter.OnItemClickListener listener;
    private ViewPagerPagesAdapter.OnItemMenuClickListener menuListener;
    private final GetAppIconUseCase getAppIconUseCase;
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
                      ViewPagerPagesAdapter.OnItemClickListener listener,
                      ViewPagerPagesAdapter.OnItemMenuClickListener menuListener,
                      int pageIndex, int columnCount) {
        this.appsList = appsList;
        this.getAppIconUseCase = getAppIconUseCase;
        this.listener = listener;
        this.menuListener = menuListener;
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

        // Клик, долгое нажатие (меню) и drag теперь обрабатывает один переиспользуемый
        // LongPressDragTouchListener — отдельный setOnClickListener больше не нужен,
        // т.к. обычный тап тоже приходит через onClick() колбэка.
        int row = position / columnCount;
        int col = position % columnCount;
        holder.app.setOnTouchListener(new LongPressDragTouchListener(new LongPressDragTouchListener.Callback() {
            @Override
            public void onClick() {
                if (listener != null) listener.onItemClick(app);
            }

            @Override
            public void onLongPress() {
                showAppMenu(holder.app, app, row, col);
            }

            @Override
            public void onDragStart() {
                if (currentPopup != null) {
                    currentPopup.dismiss();
                    currentPopup = null;
                }
            }

            @Override
            public ClipData createDragData() {
                return DragDataHelper.createHomeShortcutDragData(pageIndex, row, col);
            }
        }));

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

    private void showAppMenu(View view, AppShortcut app, int row, int col) {
        if (currentPopup != null) {
            currentPopup.dismiss();
        }
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        popup.getMenuInflater().inflate(R.menu.popup_app_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_item_uninstall) {
                if (menuListener != null) menuListener.onUninstallClick(app.getPackageName());
            } else if (id == R.id.menu_item_about) {
                if (menuListener != null) menuListener.onInfoClick(app.getPackageName());
                return true;
            } else if (id == R.id.menu_item_delete) {
                if (menuListener != null) {
                    menuListener.onDeleteClick(pageIndex, row, col);
                }
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