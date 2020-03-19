package com.merlin.adapter;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.merlin.bean.Photo;
import com.merlin.client.R;
import com.merlin.client.databinding.ItemPhotoBinding;

import java.util.List;

public class PhotoAdapter extends ListAdapter<Photo> {
    private final int mSpanCount;
    private final boolean mEnableAdd;

    public PhotoAdapter(int spanCount,boolean add){
        mSpanCount=spanCount;
        this.mEnableAdd=add;
    }

    private boolean isAddPhotoViewType(int viewType){
        return mEnableAdd?viewType==TYPE_TAIL||viewType==TYPE_EMPTY:false;
    }

    @Override
    protected Integer onResolveViewTypeLayoutId(int viewType) {
        return viewType==TYPE_DATA||isAddPhotoViewType(viewType)?R.layout.item_photo:null;
    }

    @Override
    protected void onBindViewHolder(RecyclerView.ViewHolder holder,int viewType, ViewDataBinding binding, int position, Photo data, @NonNull List<Object> payloads) {
        if (null!=binding&&binding instanceof ItemPhotoBinding){
            ((ItemPhotoBinding)binding).setPhoto(isAddPhotoViewType(viewType)?new Photo(null,R.drawable.selector_photo_add):data);
        }
    }

    @Override
    public RecyclerView.LayoutManager onResolveLayoutManager(RecyclerView rv) {
        int spanCount=mSpanCount;
        GridLayoutManager gridLayoutManager = new GridLayoutManager(rv.getContext(), spanCount<=0?1:spanCount);
        gridLayoutManager.setOrientation(RecyclerView.VERTICAL);
        rv.addItemDecoration(new GridSpacingItemDecoration(spanCount, 10, true));
        return gridLayoutManager;
    }
}