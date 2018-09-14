package com.chengfu.fuplayer.player;

import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

import com.chengfu.fuplayer.MediaSource;
import com.chengfu.fuplayer.PlayerError;
import com.chengfu.fuplayer.text.TextOutput;
import com.chengfu.fuplayer.video.VideoListener;

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

        boolean hasRenderedFirstFrame();

        int getVideoWidth();

        int getVideoHeight();

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

    interface EventListener {

        void onStateChanged(boolean playWhenReady, int playbackState);

        void onSeekableChanged(boolean seekable);

        void onBufferingUpdate(int percent);

        void onSeekComplete();

        void onError(PlayerError playerError);
    }

    abstract class DefaultEventListener implements EventListener {

        @Override
        public void onStateChanged(boolean playWhenReady, int playbackState) {

        }

        @Override
        public void onSeekableChanged(boolean seekable) {

        }

        @Override
        public void onBufferingUpdate(int percent) {

        }

        @Override
        public void onSeekComplete() {

        }

        @Override
        public void onError(PlayerError playerError) {

        }
    }

    int STATE_IDLE = 1;
    //    int STATE_PREPARING = 2;
    int STATE_READY = 3;
    int STATE_BUFFERING = 4;
    int STATE_ENDED = 5;
//    int STATE_ERROR = 6;


    VideoComponent getVideoComponent();

    TextComponent getTextComponent();

    void addEventListener(EventListener listener);

    void removeEventListener(EventListener listener);

    PlayerError getPlayerError();

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
