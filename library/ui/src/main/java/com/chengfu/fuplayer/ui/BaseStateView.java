package com.chengfu.fuplayer.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.chengfu.fuplayer.PlayerError;


public abstract class BaseStateView extends FrameLayout {

    public BaseStateView(@NonNull Context context) {
        super(context);
    }

    public BaseStateView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseStateView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    abstract void onStateChanged(boolean playWhenReady, int playbackState);

    abstract void onError(PlayerError error);

    abstract void removed();


}
