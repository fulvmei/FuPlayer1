package com.chengfu.fuplayer.player.exo;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.view.Surface;

import com.chengfu.fuplayer.FuLog;
import com.chengfu.fuplayer.FuPlayerError;
import com.chengfu.fuplayer.player.AbsPlayer;
import com.chengfu.fuplayer.player.IPlayer;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.IOException;
import java.util.Map;

public class ExoPlayer extends AbsPlayer {

    public static final String TAG = "SysPlayer";

    private final Context mContext;
    private SimpleExoPlayer mMediaPlayer;
    private final DefaultBandwidthMeter mBandwidthMeter;

    private Surface mSurface;
    private int mAudioSession;
    private boolean mLooping;
    private float mVolume = 1.0f;

    private com.chengfu.fuplayer.MediaSource mMediaSource;

    private boolean mSeekable;
    private boolean mPlayWhenReady;
//    private boolean mPreparing;

    private int mCurrentState = -1;

    private int mCurrentBufferPercentage;
    private long mSeekWhenPrepared;  // recording the seek position while preparing

    public ExoPlayer(Context context) {
        this(context, null);
    }

    public ExoPlayer(Context context, ExoPlayerOption option) {
        mContext = context;
        mBandwidthMeter = new DefaultBandwidthMeter();
        setPlayerState(mPlayWhenReady, STATE_IDLE);
    }

    private SimpleExoPlayer createPlayer() {
        RenderersFactory renderersFactory = new DefaultRenderersFactory(mContext);
        DefaultTrackSelector trackSelector =
                new DefaultTrackSelector();
        SimpleExoPlayer simpleExoPlayer = com.google.android.exoplayer2.ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector);

        simpleExoPlayer.setPlayWhenReady(mPlayWhenReady);

        simpleExoPlayer.addListener(mEventListener);
        simpleExoPlayer.setVideoListener(mVideoListener);

        return simpleExoPlayer;
    }

    public boolean isInPlaybackState() {
        return (mMediaPlayer != null &&
                mCurrentState != STATE_IDLE);
    }

    private MediaSource getMediaSource(Uri uri) {
        int contentType = Util.inferContentType(uri);
        DefaultDataSourceFactory dataSourceFactory =
                new DefaultDataSourceFactory(mContext,
                        Util.getUserAgent(mContext, mContext.getPackageName()), mBandwidthMeter);
        switch (contentType) {
            case C.TYPE_DASH:
                DefaultDashChunkSource.Factory factory = new DefaultDashChunkSource.Factory(dataSourceFactory);
                return new DashMediaSource(uri, dataSourceFactory, factory, null, null);
            case C.TYPE_SS:
                DefaultSsChunkSource.Factory ssFactory = new DefaultSsChunkSource.Factory(dataSourceFactory);
                return new SsMediaSource(uri, dataSourceFactory, ssFactory, null, null);
            case C.TYPE_HLS:
                return new HlsMediaSource(uri, dataSourceFactory, null, null);

            case C.TYPE_OTHER:
            default:
                // This is the MediaSource representing the media to be played.
                ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
                return new ExtractorMediaSource(uri,
                        dataSourceFactory, extractorsFactory, null, null);
        }
    }

    private void openMedia() {
        if (mMediaSource == null || (mMediaSource.getPath() == null && mMediaSource.getUri() == null)) {
            FuLog.w(TAG, "this mediaSource is null or path and uri both are empty", new NullPointerException("mediaSource is null"));
            setPlayerState(mPlayWhenReady, STATE_IDLE);
            submitError(FuPlayerError.create(FuPlayerError.MEDIA_ERROR_IO));
            return;
        }

        mMediaPlayer = createPlayer();
        if (mMediaSource.getPath() != null) {
            mMediaPlayer.prepare(getMediaSource(Uri.parse(mMediaSource.getPath())));
        } else {
            mMediaPlayer.prepare(getMediaSource(mMediaSource.getUri()));
        }
//        mPreparing = true;

        setPlayerState(mPlayWhenReady, STATE_BUFFERING);
        FuLog.w(TAG, "Set media source for the player: source=" + mMediaSource.toString());
    }

    private void setPlayerState(boolean playWhenReady, int state) {
        if (mPlayWhenReady == playWhenReady && mCurrentState == state) {
            return;
        }
        mPlayWhenReady = playWhenReady;
        mCurrentState = state;
        submitStateChanged(state);
    }

    @Override
    public void setVideoSurface(Surface surface) {
        if (mSurface == surface) {
            return;
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.setVideoSurface(surface);
        }
        mSurface = surface;
    }

    @Override
    public int getPlayerState() {
        return mCurrentState;
    }

    @Override
    public int getAudioSessionId() {
        if (mMediaPlayer != null) {
            mMediaPlayer.getAudioSessionId();
        }
        return 0;
    }

    @Override
    public void setMediaSource(com.chengfu.fuplayer.MediaSource mediaSource) {
        stop();
        mMediaSource = mediaSource;
        mSeekable = false;
        mSeekWhenPrepared = 0;
        mCurrentBufferPercentage = 0;
        openMedia();
    }

    @Override
    public void setPlayWhenReady(boolean playWhenReady) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setPlayWhenReady(playWhenReady);
        }
        mPlayWhenReady = playWhenReady;
    }

    @Override
    public boolean getPlayWhenReady() {
        if (mMediaPlayer != null) {
            mMediaPlayer.getPlayWhenReady();
        }
        return false;
    }

    @Override
    public void setLooping(boolean looping) {
        if (mLooping == looping) {
            return;
        }
        if (mMediaPlayer != null) {
            if (looping) {
                mMediaPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);
            } else {
                mMediaPlayer.setRepeatMode(Player.REPEAT_MODE_OFF);
            }
        }
        mLooping = looping;
    }

    @Override
    public boolean isLooping() {
        return mLooping;
    }

    @Override
    public void setVolume(float volume) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setVolume(volume);
        }
    }

    @Override
    public float getVolume() {
        if (mMediaPlayer != null) {
            mMediaPlayer.getVolume();
        }
        return 0;
    }

    @Override
    public int getBufferPercentage() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getBufferedPercentage();
        }
        return 0;
    }

    @Override
    public long getCurrentPosition() {
        if (mMediaPlayer != null) {
            mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public long getDuration() {
        if (mMediaPlayer != null) {
            mMediaPlayer.getDuration();
        }
        return 0;
    }

    @Override
    public boolean isPlaying() {
        if (mMediaPlayer != null) {
            return isInPlaybackState() && mMediaPlayer.getPlayWhenReady();
        }
        return false;
    }

    @Override
    public boolean isSeekable() {
        return mSeekable;
    }

    @Override
    public void seekTo(long msec) {
        if (isInPlaybackState()) {
            mMediaPlayer.seekTo(msec);
        }
    }

    @Override
    public void resume() {
        openMedia();
    }

    @Override
    public void stop() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            setPlayerState(mPlayWhenReady, STATE_IDLE);
        }
    }

    @Override
    public void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mMediaSource = null;
            mPlayWhenReady = false;
            mSurface = null;
            setPlayerState(mPlayWhenReady, STATE_IDLE);
        }
    }

    private final SimpleExoPlayer.VideoListener mVideoListener = new SimpleExoPlayer.VideoListener() {
        @Override
        public void onVideoSizeChanged(int width, int height,
                                       int unappliedRotationDegrees, float pixelWidthHeightRatio) {

            submitVideoSizeChanged(width, height, unappliedRotationDegrees, pixelWidthHeightRatio);
        }

        @Override
        public void onRenderedFirstFrame() {
            FuLog.d(TAG, "onRenderedFirstFrame");
        }
    };

    private final Player.EventListener mEventListener = new Player.EventListener() {
        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest) {
            FuLog.d(TAG, "onTimelineChanged...");
        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
            int bufferPercentage = mMediaPlayer.getBufferedPercentage();
            if (!isLoading) {
                submitBufferingUpdate(bufferPercentage);
            }
            FuLog.d(TAG, "onLoadingChanged : " + isLoading + ", bufferPercentage = " + bufferPercentage);
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            FuLog.d(TAG, "onPlayerStateChanged : playWhenReady = " + playWhenReady
                    + ", playbackState = " + playbackState);
            setPlayerState(playWhenReady, playbackState);
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {

        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            if (error == null) {
                submitError(FuPlayerError.create(FuPlayerError.MEDIA_ERROR_UNKNOWN));
                return;
            }
            FuLog.e(TAG, error.getMessage() == null ? "" : error.getMessage());
            int type = error.type;
            switch (type) {
                case ExoPlaybackException.TYPE_SOURCE:
                    submitError(FuPlayerError.create(FuPlayerError.MEDIA_ERROR_IO));
                    break;
                case ExoPlaybackException.TYPE_RENDERER:
                    submitError(FuPlayerError.create(FuPlayerError.MEDIA_ERROR_IO));
                    break;
                case ExoPlaybackException.TYPE_UNEXPECTED:
                    submitError(FuPlayerError.create(FuPlayerError.MEDIA_ERROR_UNKNOWN));
                    break;
            }
        }

        @Override
        public void onPositionDiscontinuity() {

        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
            FuLog.d(TAG, "onPlaybackParametersChanged : " + playbackParameters.toString());
        }
    };
}
