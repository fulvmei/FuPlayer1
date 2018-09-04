package com.chengfu.fuplayer.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.FrameLayout;

import com.chengfu.fuplayer.player.IPlayer;

public class PlayerControlView extends FrameLayout {

    private IPlayer mPlayer;
    private final ComponentListener mComponentListener = new ComponentListener();

    public PlayerControlView(@NonNull Context context) {
        this(context, null);
    }

    public PlayerControlView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayerControlView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return false;
    }

    public void setPlayer(IPlayer player) {
        if (mPlayer == player) {
            return;
        }
        if (mPlayer != null) {
            mPlayer.removeEventListener(mComponentListener);
        }
        mPlayer = player;
        if (player != null) {
            player.addEventListener(mComponentListener);
        }
        updateAll();
    }

    private void updateAll() {

    }

    private final class ComponentListener extends IPlayer.DefaultEventListener {


        @Override
        public void onStateChanged(boolean playWhenReady, int playbackState) {

        }

        @Override
        public void onBufferingUpdate(int percent) {

        }
    }
}
