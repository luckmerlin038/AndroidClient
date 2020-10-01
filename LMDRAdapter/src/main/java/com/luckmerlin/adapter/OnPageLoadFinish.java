package com.luckmerlin.adapter;

import com.luckmerlin.core.proguard.PublishMethods;

public interface OnPageLoadFinish<T> extends PublishMethods {
    void onApiFinish(int what, String note, T data, Object arg);
}
