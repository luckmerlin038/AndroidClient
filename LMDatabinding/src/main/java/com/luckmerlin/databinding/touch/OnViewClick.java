package com.luckmerlin.databinding.touch;

import android.view.View;

import com.luckmerlin.core.proguard.PublishMethods;
import com.luckmerlin.databinding.BindingObject;

public interface OnViewClick extends TouchListener,PublishMethods, BindingObject {
    boolean onViewClick(View view,int resId,int count,Object tag);
}
