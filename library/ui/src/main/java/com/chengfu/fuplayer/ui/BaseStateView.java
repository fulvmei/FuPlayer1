package com.chengfu.fuplayer.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.chengfu.fuplayer.FuPlayerError;
import com.chengfu.fuplayer.player.IPlayer;

public class BaseStateView extends FrameLayout implements IPlayer.EventListener{

    public BaseStateView(@NonNull Context context) {
        super(context);
    }

    public BaseStateView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseStateView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {

    }

    @Override
    public void onBufferingUpdate(int percent) {

    }

    @Override
    public void onSeekComplete() {

    }

    @Override
    public void onError(FuPlayerError error) {

    }
}
