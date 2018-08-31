package com.chengfu.fuplayer.player;

import android.os.Bundle;

public abstract class BaseInternalPlayer implements IPlayer {

    protected int mCurrentState = STATE_IDLE;

    protected float mCurrentVolume = 1.0f;

    private OnPlayerEventListener mOnPlayerEventListener;
    private OnErrorEventListener mOnErrorEventListener;
    private OnBufferingListener mOnBufferingListener;

    protected int mBufferPercentage;

    @Override
    public void option(int code, Bundle bundle) {
        //not handle
    }

    @Override
    public final void setOnBufferingListener(OnBufferingListener onBufferingListener) {
        this.mOnBufferingListener = onBufferingListener;
    }

    @Override
    public final void setOnPlayerEventListener(OnPlayerEventListener onPlayerEventListener) {
        this.mOnPlayerEventListener = onPlayerEventListener;
    }

    @Override
    public final void setOnErrorEventListener(OnErrorEventListener onErrorEventListener) {
        this.mOnErrorEventListener = onErrorEventListener;
    }

    protected final void submitPlayerEvent(int eventCode, Bundle bundle) {
        if (mOnPlayerEventListener != null)
            mOnPlayerEventListener.onPlayerEvent(eventCode, bundle);
    }

    protected final void submitErrorEvent(int eventCode, Bundle bundle) {
        if (mOnErrorEventListener != null)
            mOnErrorEventListener.onErrorEvent(eventCode, bundle);
    }

    protected final void submitBufferingUpdate(int bufferPercentage, Bundle extra) {
        mBufferPercentage = bufferPercentage;
        if (mOnBufferingListener != null)
            mOnBufferingListener.onBufferingUpdate(bufferPercentage, extra);
    }

    protected final void updateStatus(int status) {
        this.mCurrentState = status;
        Bundle bundle = BundlePool.obtain();
        bundle.putInt(EventKey.INT_DATA, status);
        submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_STATUS_CHANGE, bundle);
    }

    @Override
    public float getVolume() {
        return mCurrentVolume;
    }

    @Override
    public int getBufferPercentage() {
        return mBufferPercentage;
    }

    @Override
    public final int getState() {
        return mCurrentState;
    }
}
