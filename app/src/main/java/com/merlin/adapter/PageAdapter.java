package com.merlin.adapter;

import androidx.databinding.ViewDataBinding;

import com.merlin.api.PageData;

import java.util.List;

/**
 * @deprecated
 * @param <T>
 * @param <V>
 */
public abstract class  PageAdapter<T,V extends ViewDataBinding> extends BaseAdapter<T,V> {

    public final boolean fillPage(PageData<T> page){
        List<T> list=null!=page?page.getData():null;
        if (null!=list&&list.size()>0){
            if (page.getPage()<=0){
                setData(list, true);
            }else{
                addAll(list, true);
            }
        }
        return false;
    }

}
