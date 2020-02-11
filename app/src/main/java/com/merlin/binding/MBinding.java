package com.merlin.binding;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;
import androidx.databinding.BindingMethod;
import androidx.databinding.BindingMethods;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.merlin.adapter.LinearItemDecoration;
import com.merlin.adapter.LoadMoreInterceptor;
import com.merlin.adapter.MultiPageAdapter;
import com.merlin.adapter.OnMoreLoadable;
import com.merlin.adapter.OnRecyclerScroll;
import com.merlin.adapter.OnRecyclerScrollStateChange;
import com.merlin.api.Address;
import com.merlin.client.R;
import com.merlin.debug.Debug;
import com.merlin.model.Callback;
import com.merlin.model.OnAfterTextChange;
import com.merlin.model.OnBeforeTextChange;
import com.merlin.model.OnTextChange;
import com.merlin.util.Layout;
import com.merlin.view.Clicker;
import com.merlin.view.MultiPageAdapterRefreshBridge;
import com.merlin.view.OnTextChanged;
import com.merlin.view.Res;

@BindingMethods({
        @BindingMethod(type = RecyclerView.class,attribute = "itemDecoration",method ="addItemDecoration" )
})

public class MBinding {

    @BindingAdapter(value = {"enableBarPadding"})
    public static void enableBarPadding(View view, int enable) {
        Context context=null!=view?view.getContext():null;
        int height=null!=context? StatusBar.height(context):-1;
        if (height>0){
            view.setPadding(view.getPaddingLeft(),view.getPaddingTop()+height,
                    view.getPaddingRight(),view.getPaddingBottom());
        }
    }


    @BindingAdapter("android:text")
    public static void setText(TextView view, Object resId) {
        if (null!=view) {
            resId = null == resId ? "" : resId;
            if (resId instanceof String) {
                view.setText((String)resId);
            } else if (resId instanceof Integer) {
                view.setText((Integer)resId);
                Clicker.putRes(view,new Res((Integer)resId,null));
            }
        }
    }

    @BindingAdapter("android:src")
    public static void setSrc(ImageView view, Object img) {
        if (null!=img){
            if (img instanceof Integer){
                view.setImageResource((Integer)img);
                Clicker.putRes(view,new Res((Integer)img,null));
            }else if (img instanceof String){
                String path=(String)img;
                RoundedCorners roundedCorners = new RoundedCorners(10);
                RequestOptions options = RequestOptions.bitmapTransform(roundedCorners).override(view.getWidth(), view.getHeight());
//        Debug.D(MBinding.class,"AAAAAAAAAa "+Address.URL+path);
//         String ddd="https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1579668574979&di=2c09057e986a070149d31ba342ec5985&imgtype=0&src=http%3A%2F%2Farticle.fd.zol-img.com.cn%2Ft_s640x2000%2Fg3%2FM04%2F0C%2F03%2FCg-4V1RjLO2IIKzYAATUS9gV0gUAARNqwD3bwkABNRj460.jpg";
                if (null!=path){
                    if (path.startsWith("/")){
                        path= Address.URL+Address.PREFIX_THUMB+"?path="+path;
                    }
                }
                Debug.D(MBinding.class," "+path);
                Glide.with(view.getContext())
                        .load(path)
                        .centerCrop()
                        .apply(options)
                        .thumbnail(1f)
                        .placeholder(R.drawable.ic_picture_default)
//                .error(R.drawable.ic_default_pic)
                        .into(view);
            }
        }
    }

    @BindingAdapter(value = {"textWatcher"})
    public static void setEditTextListener(EditText view, Callback callback) {
        if (null!=view&&null!=callback){
            view.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    if (callback instanceof OnBeforeTextChange){
                        ((OnBeforeTextChange)callback).beforeTextChanged(view,s,start,count,after);
                    }
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (callback instanceof OnTextChange){
                            ((OnTextChange)callback).onTextChanged(view,s,start,before,count);
                        }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (callback instanceof OnAfterTextChange){
                        ((OnAfterTextChange)callback).onAfterChanged(view,s);
                    }
                }
            });
        }
    }

    @BindingAdapter(value = {"createModel"})
    public static void createModel(View view, Object modeClass) {
        new ModelBinder().bind(view,modeClass);
    }

    @BindingAdapter(value = {"enableMultiClick"})
    public static void enableMultiClick(View view, Object obj) {

    }

    @BindingAdapter(value = {"enableClick"})
    public static void enableClick(View view, Object obj) {
        if (null!=view&&null!=obj){
            if (obj instanceof Boolean){
                new Clicker().attach(view,(Boolean)obj);
            }else if (obj instanceof Clicker.Click){
                new Clicker().attach(view,(Clicker.Click)obj);
            }
        }
    }

    @BindingAdapter(value = {"enableModelClick"})
    public static void enableModelClick(View view, Object object) {
        if (null!=object){
            ClickBinding binding = null;
            if (object instanceof Boolean){
                binding=new ClickBinding((Boolean)object,null);
            }else if (object instanceof ClickBinding){
                binding=(ClickBinding)object;
            }
            if (null!=binding){
                binding.bind(view);
            }
        }
    }

    @BindingAdapter(value = {"statusBar"})
    public static void statusBar(View view, StatusBar statusBar) {
        if (null!=view &&null!=statusBar){
            statusBar.inflate(view);
        }
    }

    @BindingAdapter("onEditorActionChange")
    public static void onEditorActionChange(EditText view, TextView.OnEditorActionListener listener) {
        if (null != view && null != listener) {
            view.setOnEditorActionListener(listener);
        }
    }

    @BindingAdapter("onTextChange")
    public static void onTextChange(EditText view, OnTextChanged listener) {
        if (null!=view&&null!=listener){
            view.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    listener.onTextChanged(view,OnTextChanged.TEXT_BEFORE,s);
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    listener.onTextChanged(view,OnTextChanged.TEXT_CHANGED,s);
                }

                @Override
                public void afterTextChanged(Editable s) {
                    listener.onTextChanged(view,OnTextChanged.TEXT_CHANGING,s);
                }
            });
        }
    }

    @BindingAdapter("onFocusChange")
    public static void onFocusChange(View view, View.OnFocusChangeListener listener) {
        if (null!=view){
            view.setOnFocusChangeListener(listener);
        }
    }


    @BindingAdapter("selected")
    public static void selected(View view, boolean selected) {
        if (null!=view){
            view.setSelected(selected);
        }
    }

    @BindingAdapter("adapter")
    public static void adapter(RecyclerView view, com.merlin.adapter.Adapter adapter) {
        if (null!=view&&null!=adapter){
            RecyclerView.LayoutManager manager=adapter.onResolveLayoutManager(view);
            if (null!=manager){
                view.setLayoutManager(manager);
            }
            if (adapter instanceof OnMoreLoadable || adapter instanceof OnRecyclerScroll||adapter instanceof OnRecyclerScrollStateChange){
                view.addOnScrollListener(new LoadMoreInterceptor(){
                    @Override
                    protected void onLoadMore(RecyclerView recyclerView,int state, String debug) {
                        if (adapter instanceof OnMoreLoadable) {
                            ((OnMoreLoadable) adapter).onLoadMore(recyclerView, state, debug);
                        }
                    }

                    @Override
                    protected void onRecyclerScrolled(RecyclerView recyclerView, int dx, int dy) {
                        if (adapter instanceof OnRecyclerScroll){
                            ((OnRecyclerScroll)adapter).onRecyclerScrolled(recyclerView,dx,dy);
                        }
                    }

                    @Override
                    protected void onRecyclerScrollStateChanged(RecyclerView recyclerView, int newState) {
                        if (adapter instanceof OnRecyclerScrollStateChange){
                            ((OnRecyclerScrollStateChange)adapter).onRecyclerScrollStateChanged(recyclerView,newState);
                        }
                    }
                });
            }
            final ViewParent parent=adapter instanceof MultiPageAdapter?view.getParent():null;
            if (null!=parent&&parent instanceof SwipeRefreshLayout){
                final SwipeRefreshLayout refreshLayout=(SwipeRefreshLayout)parent;
                final MultiPageAdapter multiPageAdapter=(MultiPageAdapter)adapter;
                final MultiPageAdapterRefreshBridge refresh=new MultiPageAdapterRefreshBridge(){
                    @Override
                    public void onPageLoadUpdate(int state, boolean idle, MultiPageAdapter.Page page) {
                        switch (state){
                            case UPDATE_PAGE_START:
                                refreshLayout.setRefreshing(true);
                                break;
                            case UPDATE_PAGE_END:
                                if (idle){
                                    refreshLayout.setRefreshing(false);
                                }
                                break;
                        }
                    }

                    @Override
                    public void onRefresh() {
                        if (!multiPageAdapter.resetLoad()&&!multiPageAdapter.isLoading()){
                            refreshLayout.setRefreshing(false);
                        }
                    }
                };
                ((SwipeRefreshLayout)parent).setOnRefreshListener(refresh);
                multiPageAdapter.add(refresh);
            }
            view.setAdapter(adapter);
        }
    }

    @BindingAdapter("layoutManager")
    public static void linear(RecyclerView view, Layout layout) {
        if (null!=layout){
            Context context=view.getContext();
            int spanCount=layout.getSpanCount();
            int orientation=layout.getOrientation();
            boolean isReverseLayout=layout.isReverseLayout();
            switch (layout.getLayout()){
                case Layout.STAGGERED_GRID_LAYOUT:
                    StaggeredGridLayoutManager sgm=new StaggeredGridLayoutManager(spanCount,orientation);
                    sgm.setReverseLayout(isReverseLayout);
                    view.setLayoutManager(sgm);
                    break;
                case Layout.LINEAR_LAYOUT:
                    view.addItemDecoration(new LinearItemDecoration(3));
                    LinearLayoutManager llm=new LinearLayoutManager(context, orientation,isReverseLayout);
                    llm.setSmoothScrollbarEnabled(true);
                    view.setLayoutManager(llm);
                    break;
                case Layout.GRID_LAYOUT:
                    GridLayoutManager glm=new GridLayoutManager(context, spanCount,orientation,isReverseLayout);
                    glm.setSmoothScrollbarEnabled(true);
                    view.setLayoutManager(glm);
                    break;
            }
        }

    }

}
