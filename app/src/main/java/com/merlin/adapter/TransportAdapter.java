package com.merlin.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.merlin.client.R;
import com.merlin.client.databinding.ItemTransportBinding;
import com.merlin.transport.AbsTransport;

import java.util.List;

public class TransportAdapter<T extends AbsTransport> extends Adapter<T> implements OnItemTouchResolver {

    @Override
    protected Integer onResolveItemLayoutId(ViewGroup parent, int viewType) {
        return R.layout.item_transport;
    }

    public final boolean updateErrorTextId(T data,String error,String debug){
        if (null!=data){
            data.setError(error);// Get through
            return update(debug,data);
        }
        return false;
    }

    @Override
    protected void onBindViewHolder(RecyclerView.ViewHolder holder, ViewDataBinding binding, int position, T data, @NonNull List<Object> payloads) {
        if (null!=binding&&binding instanceof ItemTransportBinding){
            ItemTransportBinding itb=(ItemTransportBinding)binding;
            itb.setPosition(position+1);
            itb.setData(data);
         }
    }

    @Override
    public RecyclerView.LayoutManager onResolveLayoutManager(RecyclerView rv) {
        return new LinearLayoutManager(rv.getContext(), RecyclerView.VERTICAL,false);
    }

    @Override
    public Object onResolveItemTouch(RecyclerView recyclerView) {
        return new ItemSlideRemover();
    }


}
