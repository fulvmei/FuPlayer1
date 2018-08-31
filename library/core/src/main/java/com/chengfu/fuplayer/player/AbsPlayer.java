package com.chengfu.fuplayer.player;

import android.graphics.SurfaceTexture;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

import com.chengfu.fuplayer.FuLog;
import com.chengfu.fuplayer.FuPlayerError;
import com.chengfu.fuplayer.text.TextOutput;
import com.chengfu.fuplayer.video.VideoListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class AbsPlayer implements IPlayer, IPlayer.VideoComponent, IPlayer.TextComponent {

    private static final String TAG = "AbsPlayer";

    private final ComponentListener componentListener = new ComponentListener();

    private SurfaceHolder mSurfaceHolder;
    private TextureView mTextureView;

    protected final CopyOnWriteArraySet<EventListener> eventListeners = new CopyOnWriteArraySet<>();
    protected final CopyOnWriteArraySet<VideoListener> videoListeners = new CopyOnWriteArraySet<>();
    protected final CopyOnWriteArraySet<TextOutput> textListeners = new CopyOnWriteArraySet<>();

    @Override
    public void addEventListener(EventListener listener) {
        eventListeners.add(listener);
    }

    @Override
    public void removeEventListener(EventListener listener) {
        eventListeners.remove(listener);
    }

    @Override
    public void addVideoListener(VideoListener listener) {
        videoListeners.add(listener);
    }

    @Override
    public void removeVideoListener(VideoListener listener) {
        videoListeners.remove(listener);
    }


    @Override
    public void addTextOutput(TextOutput listener) {
        textListeners.add(listener);
    }

    @Override
    public void removeTextOutput(TextOutput listener) {
        textListeners.remove(listener);
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
            surfaceHolder.addCallback(componentListener);
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
            textureView.setSurfaceTextureListener(componentListener);
            SurfaceTexture surfaceTexture = textureView.isAvailable() ? textureView.getSurfaceTexture()
                    : null;
            setVideoSurface(surfaceTexture == null ? null : new Surface(surfaceTexture));
        }
    }

    private void removeSurfaceCallbacks() {
        if (mTextureView != null) {
            if (mTextureView.getSurfaceTextureListener() != componentListener) {
                FuLog.w(TAG, "SurfaceTextureListener already unset or replaced.");
            } else {
                mTextureView.setSurfaceTextureListener(null);
            }
            mTextureView = null;
        }
        if (mSurfaceHolder != null) {
            mSurfaceHolder.removeCallback(componentListener);
            mSurfaceHolder = null;
        }
    }

    protected final void submitStateChanged(int state) {
        for (EventListener listener : eventListeners) {
            listener.onStateChanged(state);
        }
    }

    protected final void submitBufferingUpdate(int percent) {
        for (EventListener listener : eventListeners) {
            listener.onBufferingUpdate(percent);
        }
    }

    protected final void submitLoadingChanged(boolean isLoading) {
        for (EventListener listener : eventListeners) {
            listener.onLoadingChanged(isLoading);
        }
    }


    protected final void submitSeekComplete() {
        for (EventListener listener : eventListeners) {
            listener.onSeekComplete();
        }
    }

    protected final void submitError(FuPlayerError error) {
        for (EventListener listener : eventListeners) {
            listener.onError(error);
        }
    }

    protected final void submitVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
        for (VideoListener listener : videoListeners) {
            listener.onVideoSizeChanged(width, height, unappliedRotationDegrees, pixelWidthHeightRatio);
        }
    }

    private final class ComponentListener implements SurfaceHolder.Callback,
            TextureView.SurfaceTextureListener {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            setVideoSurface(holder.getSurface());
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            setVideoSurface(null);
        }

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            setVideoSurface(new Surface(surfaceTexture));
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            setVideoSurface(null);
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    }

}
