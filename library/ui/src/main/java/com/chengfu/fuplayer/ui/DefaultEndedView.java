package com.chengfu.fuplayer.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.chengfu.fuplayer.FuPlayerError;
import com.chengfu.fuplayer.player.IPlayer;

public class DefaultEndedView extends BaseStateView {

    public DefaultEndedView(@NonNull Context context) {
        this(context, null);
    }

    public DefaultEndedView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DefaultEndedView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.default_ended_view, this);

        setVisibility(View.GONE);
    }

    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == IPlayer.STATE_ENDED) {
            setVisibility(View.VISIBLE);
        } else {
            setVisibility(View.GONE);
        }
    }

    @Override
    void onError(FuPlayerError error) {

    }

    @Override
    void removed() {
        setVisibility(View.GONE);
    }
}
