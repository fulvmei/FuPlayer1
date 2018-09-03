package com.chengfu.fuplayer.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.chengfu.fuplayer.FuPlayerError;
import com.chengfu.fuplayer.player.IPlayer;

public class DefaultErrorView extends BaseStateView {

    private FuPlayerError mPlayerError;

    public DefaultErrorView(@NonNull Context context) {
        this(context, null);
    }

    public DefaultErrorView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DefaultErrorView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.default_error_view, this);
    }

    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == IPlayer.STATE_ERROR) {
            setVisibility(View.VISIBLE);
        } else {
            setVisibility(View.GONE);
        }
    }

    @Override
    public void onError(FuPlayerError error) {
        mPlayerError = error;
    }
}
