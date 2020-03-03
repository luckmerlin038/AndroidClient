package com.merlin.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.merlin.client.R;
import com.merlin.client.databinding.ItemTransportBinding;
import com.merlin.debug.Debug;
import com.merlin.transport.Transport;

import java.util.List;

public class TransportAdapter<T extends Transport> extends Adapter<T> {

    @Override
    protected Integer onResolveItemLayoutId(ViewGroup parent, int viewType) {
        return R.layout.item_transport;
    }

    @Override
    protected void onBindViewHolder(RecyclerView.ViewHolder holder, ViewDataBinding binding, int position, T data, @NonNull List<Object> payloads) {
        if (null!=binding&&binding instanceof ItemTransportBinding){
            Debug.D(getClass(),"AAAAAAAAAAAAA "+position+" "+binding.getRoot());
//             ((ItemTransportBinding)binding).setData(data);
         }
    }

    @Override
    public RecyclerView.LayoutManager onResolveLayoutManager(RecyclerView rv) {
        return new LinearLayoutManager(rv.getContext(), RecyclerView.VERTICAL,false);
    }
}
