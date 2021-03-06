package com.merlin.adapter;

import androidx.recyclerview.widget.RecyclerView;

public interface OnItemSlideRemove {
    interface Remover{
        void remove(boolean remove);
    }

    void onItemSlideRemove(int position, Object data, int direction, RecyclerView.ViewHolder viewHolder, Remover remover);
}
