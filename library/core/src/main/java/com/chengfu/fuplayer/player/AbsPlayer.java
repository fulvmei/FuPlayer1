package com.chengfu.fuplayer.player;

import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

import com.chengfu.fuplayer.FuLog;
import com.chengfu.fuplayer.PlayerError;
import com.chengfu.fuplayer.text.TextOutput;
import com.chengfu.fuplayer.video.VideoListener;

import java.util.concurrent.CopyOnWriteArraySet;

public abstract class AbsPlayer implements IPlayer, IPlayer.VideoComponent, IPlayer.TextComponent {

    private static final String TAG = "AbsPlayer";

    private final ComponentListener mComponentListener = new ComponentListener();

    private SurfaceHolder mSurfaceHolder;
    private TextureView mTextureView;

    protected final CopyOnWriteArraySet<EventListener> mEventListeners = new CopyOnWriteArraySet<>();
    protected final CopyOnWriteArraySet<VideoListener> mVideoListeners = new CopyOnWriteArraySet<>();
    protected final CopyOnWriteArraySet<TextOutput> mTextListeners = new CopyOnWriteArraySet<>();

    @Override
    public void addEventListener(EventListener listener) {
        mEventListeners.add(listener);
    }

    @Override
    public void removeEventListener(EventListener listener) {
        mEventListeners.remove(listener);
    }

    @Override
    public void addVideoListener(VideoListener listener) {
        mVideoListeners.add(listener);
    }

    @Override
    public void removeVideoListener(VideoListener listener) {
        mVideoListeners.remove(listener);
    }


    @Override
    public void addTextOutput(TextOutput listener) {
        mTextListeners.add(listener);
    }

    @Override
    public void removeTextOutput(TextOutput listener) {
        mTextListeners.remove(listener);
    }

    @Override
    public VideoComponent getVideoComponent() {
        return this;
    }


    @Override
    public TextComponent getTextComponent() {
        return this;
    }


    @Override
    public void setVideoSurfaceHolder(SurfaceHolder surfaceHolder) {
        if (mSurfaceHolder == surfaceHolder) {
            return;
        }
        removeSurfaceCallbacks();
        mSurfaceHolder = surfaceHolder;
        if (surfaceHolder == null) {
            setVideoSurface(null);
        } else {
            surfaceHolder.addCallback(mComponentListener);
            Surface surface = surfaceHolder.getSurface();
            setVideoSurface(surface != null && surface.isValid() ? surface : null);
        }
    }

    @Override
    public void setVideoSurfaceView(SurfaceView surfaceView) {
        setVideoSurfaceHolder(surfaceView == null ? null : surfaceView.getHolder());
    }


    @Override
    public void setVideoTextureView(TextureView textureView) {
        if (mTextureView == textureView) {
            return;
        }
        removeSurfaceCallbacks();
        mTextureView = textureView;
        if (textureView == null) {
            setVideoSurface(null);
        } else {
            if (textureView.getSurfaceTextureListener() != null) {
                Log.w(TAG, "Replacing existing SurfaceTextureListener.");
            }
            textureView.setSurfaceTextureListener(mComponentListener);
            SurfaceTexture surfaceTexture = textureView.isAvailable() ? textureView.getSurfaceTexture()
                    : null;
            setVideoSurface(surfaceTexture == null ? null : new Surface(surfaceTexture));
        }
    }

    private void removeSurfaceCallbacks() {
        if (mTextureView != null) {
            if (mTextureView.getSurfaceTextureListener() != mComponentListener) {
                FuLog.w(TAG, "SurfaceTextureListener already unset or replaced.");
            } else {
                mTextureView.setSurfaceTextureListener(null);
            }
            mTextureView = null;
        }
        if (mSurfaceHolder != null) {
            mSurfaceHolder.removeCallback(mComponentListener);
            mSurfaceHolder = null;
        }
    }

    protected final void submitStateChanged(boolean playWhenReady, int playbackState) {
        for (EventListener listener : mEventListeners) {
            listener.onStateChanged(playWhenReady, playbackState);
        }
    }

    protected final void submitBufferingUpdate(int percent) {
        for (EventListener listener : mEventListeners) {
            listener.onBufferingUpdate(percent);
        }
    }

    protected final void submitSeekComplete() {
        for (EventListener listener : mEventListeners) {
            listener.onSeekComplete();
        }
    }

    protected final void submitError(PlayerError playerError) {
        for (EventListener listener : mEventListeners) {
            listener.onError(playerError);
        }
    }

    protected final void submitVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
        for (VideoListener listener : mVideoListeners) {
            listener.onVideoSizeChanged(width, height, unappliedRotationDegrees, pixelWidthHeightRatio);
        }
    }

    protected final void submitRenderedFirstFrame() {
        for (VideoListener listener : mVideoListeners) {
            listener.onRenderedFirstFrame();
        }
    }

    private final class ComponentListener implements SurfaceHolder.Callback,
            TextureView.SurfaceTextureListener {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            FuLog.d(TAG,"surfaceCreated");
            setVideoSurface(holder.getSurface());
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            FuLog.d(TAG,"surfaceDestroyed");
            setVideoSurface(null);
        }

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            FuLog.d(TAG,"onSurfaceTextureAvailable");
            setVideoSurface(new Surface(surfaceTexture));
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            FuLog.d(TAG,"onSurfaceTextureDestroyed");
            setVideoSurface(null);
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    }

}
