package ru.veritas.veritas_ui.ui.classic.home.favorites;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.content.ClipData;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ru.veritas.veritas_ui.ui.R;
import ru.veritas.veritas_ui.core.entities.AppShortcut;
import ru.veritas.veritas_ui.core.command.local.home.GetAppIconUseCase;
import ru.veritas.veritas_ui.ui.common.utils.DragDataHelper;
import ru.veritas.veritas_ui.ui.common.utils.IconUtils;
import ru.veritas.veritas_ui.ui.common.utils.LongPressDragTouchListener;

/**
 * Адаптер для списка избранных
 */
public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.ViewHolder> {
    // region Parameters
    private List<AppShortcut> apps;

    private OnItemClickListener listener;
    private OnItemMenuClickListener menuListener;
    private OnSpecificItemClickListener specificListener;

    private final int pageIndex;
    private final int columnCount;

    private int itemHeight = RecyclerView.LayoutParams.WRAP_CONTENT;

    private final GetAppIconUseCase getAppIconUseCase;

    private PopupMenu currentPopupMenu;
    // endregion

    // region Listeners
    public interface OnItemClickListener {
        void OnItemClick(String packageName);
    }
    public interface OnItemMenuClickListener {
        void onRemoveClick(int page, int position);
        void onInfoClick(String packageName);
    }
    public interface OnSpecificItemClickListener {
        void ItemClick();
    }



    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setMenuListener(OnItemMenuClickListener menuListener) {
        this.menuListener = menuListener;
    }

    public void setSpecificListener(OnSpecificItemClickListener specificListener) {
        this.specificListener = specificListener;
    }
    // endregion

    public FavoritesAdapter(List<AppShortcut> apps, int pageIndex, int columnCount, GetAppIconUseCase getAppIconUseCase) {
        this.apps = apps;
        this.pageIndex = pageIndex;
        this.columnCount = columnCount;
        this.getAppIconUseCase = getAppIconUseCase;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoritesAdapter.ViewHolder holder, int position) {
        AppShortcut currentApp = apps.get(position);
        if (currentApp == null) {
            holder.setVisibility(INVISIBLE);
            return;
        }
        holder.setVisibility(VISIBLE);
        holder.imageView.setImageDrawable(IconUtils.toDrawable(
                getAppIconUseCase.invoke(currentApp),
                holder.itemView.getResources()
        ));


        holder.app.setOnTouchListener(new LongPressDragTouchListener(new LongPressDragTouchListener.Callback() {
            @Override
            public void onClick() {
                if (listener != null)
                    listener.OnItemClick(currentApp.getPackageName());
            }

            @Override
            public void onLongPress() {
                showPopupMenu(holder.app, currentApp, position);
            }


            @Override
            public void onDragStart() {
                if (currentPopupMenu != null) {
                    currentPopupMenu.dismiss();
                    currentPopupMenu = null;
                }
            }

            @Override
            public ClipData createDragData() {
                return DragDataHelper.createFavoriteDragData(pageIndex, position);
            }
        }));

        ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
        if (params != null) {
            params.height = itemHeight;
            holder.itemView.setLayoutParams(params);
        }
    }

    private void showPopupMenu(View view, AppShortcut app, int position) {
        if (currentPopupMenu != null) currentPopupMenu.dismiss();
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        popup.getMenuInflater().inflate(R.menu.popup_fav_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.fav_menu_item_about) {
                if (menuListener != null) menuListener.onInfoClick(app.getPackageName());
                return true;
            } else if (id == R.id.fav_menu_item_remove) {
                if (menuListener != null) menuListener.onRemoveClick(pageIndex, position);
            }
            return false;
        });
        popup.show();
        currentPopupMenu = popup;
    }

    @Override
    public int getItemCount() {
        return apps == null ? 0 : apps.size();
    }

    public void updateData(List<AppShortcut> appShortcuts) {
        this.apps = appShortcuts;
        notifyDataSetChanged();
    }

    public void setItemHeight(int height) {
        if (this.itemHeight != height) {
            this.itemHeight = height;
            notifyDataSetChanged();
        }
    }

    /**
     * ViewHolder для списка избранных
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        LinearLayout app;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            app = itemView.findViewById(R.id.inside_fav);
            imageView = itemView.findViewById(R.id.fav_icon);
        }

        void setVisibility(int visibility) {
            app.setVisibility(visibility);
        }
    }
}