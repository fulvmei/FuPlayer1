package com.chengfu.fuplayer.player;

import android.net.Uri;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

import com.chengfu.fuplayer.FuPlayerError;
import com.chengfu.fuplayer.MediaSource;
import com.chengfu.fuplayer.text.TextOutput;
import com.chengfu.fuplayer.video.VideoListener;

import java.util.Map;

public interface IPlayer {

    interface VideoComponent {

        /**
         * Adds a listener to receive video events.
         *
         * @param listener The listener to register.
         */
        void addVideoListener(VideoListener listener);

        /**
         * Removes a listener of video events.
         *
         * @param listener The listener to unregister.
         */
        void removeVideoListener(VideoListener listener);

        /**
         * Sets the {@link Surface} onto which video will be rendered. The caller is responsible for
         * tracking the lifecycle of the surface, and must clear the surface by calling {@code
         * setVideoSurface(null)} if the surface is destroyed.
         * <p>
         * <p>If the surface is held by a {@link SurfaceView}, {@link TextureView} or {@link
         * SurfaceHolder} then it's recommended to use {@link #setVideoSurfaceView(SurfaceView)}, {@link
         * #setVideoTextureView(TextureView)} or {@link #setVideoSurfaceHolder(SurfaceHolder)} rather
         * than this method, since passing the holder allows the player to track the lifecycle of the
         * surface automatically.
         *
         * @param surface The {@link Surface}.
         */
        void setVideoSurface(Surface surface);

        /**
         * Sets the {@link SurfaceHolder} that holds the {@link Surface} onto which video will be
         * rendered. The player will track the lifecycle of the surface automatically.
         *
         * @param surfaceHolder The surface holder.
         */
        void setVideoSurfaceHolder(SurfaceHolder surfaceHolder);

        /**
         * Sets the {@link SurfaceView} onto which video will be rendered. The player will track the
         * lifecycle of the surface automatically.
         *
         * @param surfaceView The surface view.
         */
        void setVideoSurfaceView(SurfaceView surfaceView);

        /**
         * Sets the {@link TextureView} onto which video will be rendered. The player will track the
         * lifecycle of the surface automatically.
         *
         * @param textureView The texture view.
         */
        void setVideoTextureView(TextureView textureView);

    }

    interface TextComponent {

        /**
         * Registers an output to receive text events.
         *
         * @param listener The output to register.
         */
        void addTextOutput(TextOutput listener);

        /**
         * Removes a text output.
         *
         * @param listener The output to remove.
         */
        void removeTextOutput(TextOutput listener);
    }

    int STATE_IDLE = 1;
    int STATE_BUFFERING = 2;
    int STATE_READY = 3;
    int STATE_ENDED = 4;

    int VIDEO_SCALING_MODE_SCALE_TO_FIT = 0;
    int VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING = 1;


    interface EventListener {

        void onStateChanged(int state);

        void onBufferingUpdate(int percent);

        void onLoadingChanged(boolean isLoading);

        void onSeekComplete();

        void onError(FuPlayerError error);
    }

    abstract class DefaultEventListener implements EventListener {


        @Override
        public void onStateChanged(int state) {

        }

        @Override
        public void onBufferingUpdate(int percent) {

        }

        @Override
        public void onLoadingChanged(boolean isLoading) {

        }

        @Override
        public void onSeekComplete() {

        }

        @Override
        public void onError(FuPlayerError error) {

        }
    }

    VideoComponent getVideoComponent();

    TextComponent getTextComponent();

    void addEventListener(EventListener listener);

    void removeEventListener(EventListener listener);

    int getPlayerState();

    int getAudioSessionId();

    void setMediaSource(MediaSource mediaSource);

    void setPlayWhenReady(boolean playWhenReady);

    boolean getPlayWhenReady();

    void setLooping(boolean looping);

    boolean isLooping();

    void setVolume(float volume);

    float getVolume();

    int getBufferPercentage();

    long getCurrentPosition();

    long getDuration();

    boolean isPlaying();

    boolean isSeekable();

    void seekTo(long msec);

    void resume();

    void stop();

    void release();
}
