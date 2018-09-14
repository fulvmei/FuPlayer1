package com.chengfu.fuplayer.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.chengfu.fuplayer.PlayerError;
import com.chengfu.fuplayer.player.IPlayer;


public abstract class BaseStateView extends FrameLayout {

    private IPlayer mPlayer;
    private ComponentListener mComponentListener;
    protected PlayerError mPlayerError;

    public BaseStateView(@NonNull Context context) {
        this(context, null);
    }

    public BaseStateView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseStateView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mComponentListener = new ComponentListener();
        mPlayerError = null;
    }

    public void setPlayer(IPlayer player) {
        if (mPlayer == player) {
            return;
        }
        if (mPlayer != null) {
            mPlayer.removeEventListener(mComponentListener);
            mPlayerError = null;
            detachPlayer();
        }
        mPlayer = player;

        if (player != null) {
            mPlayerError = player.getPlayerError();
            onStateChanged(player.getPlayWhenReady(), player.getPlayerState());
            player.addEventListener(mComponentListener);
        }
    }

    abstract void onStateChanged(boolean playWhenReady, int playbackState);

    abstract void detachPlayer();

    private final class ComponentListener extends IPlayer.DefaultEventListener {
        @Override
        public void onError(PlayerError playerError) {
            mPlayerError = playerError;
        }

        @Override
        public void onStateChanged(boolean playWhenReady, int playbackState) {
            if (playbackState != IPlayer.STATE_IDLE || mPlayer.getPlayerError() == null) {
                mPlayerError = null;
            }
            BaseStateView.this.onStateChanged(playWhenReady, playbackState);
        }
    }


}
