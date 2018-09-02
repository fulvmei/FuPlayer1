package com.chengfu.fuplayer.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import com.chengfu.fuplayer.player.IPlayer;

public class DefaultLoadingView extends BaseStateView {

    public DefaultLoadingView(@NonNull Context context) {
        this(context, null);
    }

    public DefaultLoadingView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DefaultLoadingView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.default_lodaing_view, this);
    }

    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {
        System.out.println("aaaaaaaaaaaaaaaa   playWhenReady=" + playWhenReady + "，playbackState=" + playbackState);
        if (playbackState == IPlayer.STATE_PREPARING || (playWhenReady == true && playbackState == IPlayer.STATE_BUFFERING)) {
            setVisibility(View.VISIBLE);
        } else {
            setVisibility(View.INVISIBLE);
        }
    }
}
