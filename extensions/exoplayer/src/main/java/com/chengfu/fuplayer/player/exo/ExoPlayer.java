package com.chengfu.fuplayer.player.exo;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.view.Surface;

import com.chengfu.fuplayer.FuLog;

import com.chengfu.fuplayer.PlayerError;
import com.chengfu.fuplayer.player.AbsPlayer;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSourceFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.DefaultHlsDataSourceFactory;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoListener;


public class ExoPlayer extends AbsPlayer {

    public static final String TAG = "ExoPlayer";

    private final Context mContext;
    private SimpleExoPlayer mMediaPlayer;
    private final DefaultBandwidthMeter mBandwidthMeter;

    private Surface mSurface;
    private int mAudioSession;
    private boolean mLooping;
    private float mVolume = 1.0f;

    private com.chengfu.fuplayer.MediaSource mMediaSource;

    private PlayerError mPlayerError;
    private int mCurrentBufferPercentage;
    private int mVideoWidth;
    private int mVideoHeight;
    private boolean mRenderedFirstFrame;

    private boolean mSeekable;
    private boolean mPlayWhenReady;
//    private boolean mPreparing;

    private int mCurrentState = -1;

    private long mSeekWhenPrepared;  // recording the seek position while preparing

    public ExoPlayer(Context context) {
        this(context, null);
    }

    public ExoPlayer(Context context, ExoPlayerOption option) {
        mContext = context;
        mVideoWidth = 0;
        mVideoHeight = 0;
        mPlayerError = null;
        mRenderedFirstFrame = false;
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
        simpleExoPlayer.addVideoListener(mVideoListener);

        return simpleExoPlayer;
    }

    public boolean isInPlaybackState() {
        return (mMediaPlayer != null &&
                mCurrentState != STATE_IDLE
                && mCurrentState != STATE_PREPARING);
    }

    private MediaSource getMediaSource(com.chengfu.fuplayer.MediaSource mediaSource) {
        Uri uri;
        if (mediaSource.getPath() != null) {
            uri = Uri.parse(mMediaSource.getPath());
        } else {
            uri = mMediaSource.getUri();
        }
        DefaultDataSourceFactory dataSourceFactory =
                new DefaultDataSourceFactory(mContext,
                        Util.getUserAgent(mContext, mContext.getPackageName()), mBandwidthMeter);
        if (mediaSource.getType() == com.chengfu.fuplayer.MediaSource.MEDIA_TYPE_DASH) {
            return new DashMediaSource.Factory(new DefaultDashChunkSource.Factory(dataSourceFactory), dataSourceFactory).createMediaSource(uri);
        } else if (mediaSource.getType() == com.chengfu.fuplayer.MediaSource.MEDIA_TYPE_SS) {
            return new SsMediaSource.Factory(new DefaultSsChunkSource.Factory(dataSourceFactory), dataSourceFactory).createMediaSource(uri);
        } else if (mediaSource.getType() == com.chengfu.fuplayer.MediaSource.MEDIA_TYPE_HLS) {
            return new HlsMediaSource.Factory(new DefaultHlsDataSourceFactory(dataSourceFactory)).createMediaSource(uri);
        } else if (mediaSource.getType() == com.chengfu.fuplayer.MediaSource.MEDIA_TYPE_RTMP) {
            return new ExtractorMediaSource.Factory(new RtmpDataSourceFactory()).createMediaSource(uri);
        }

        String scheme = uri.getScheme();
        if (scheme != null && scheme.contains("rtmp")) {
            return new ExtractorMediaSource.Factory(new RtmpDataSourceFactory()).createMediaSource(uri);
        }

        int contentType = Util.inferContentType(uri);
        switch (contentType) {
            case C.TYPE_DASH:
                return new DashMediaSource.Factory(new DefaultDashChunkSource.Factory(dataSourceFactory), dataSourceFactory).createMediaSource(uri);
            case C.TYPE_SS:
                return new SsMediaSource.Factory(new DefaultSsChunkSource.Factory(dataSourceFactory), dataSourceFactory).createMediaSource(uri);
            case C.TYPE_HLS:
                return new HlsMediaSource.Factory(new DefaultHlsDataSourceFactory(dataSourceFactory)).createMediaSource(uri);
            case C.TYPE_OTHER:
            default:
                return new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
        }
    }

    private void openMedia() {
        if (mMediaSource == null || (mMediaSource.getPath() == null && mMediaSource.getUri() == null)) {
            FuLog.w(TAG, "this mediaSource is null or path and uri both are empty", new NullPointerException("mediaSource is null"));
            mPlayerError = PlayerError.create(PlayerError.MEDIA_ERROR_IO);
            submitError(mPlayerError);
            setPlayerState(mPlayWhenReady, STATE_IDLE);
            return;
        }
        if (mMediaPlayer == null) {
            mMediaPlayer = createPlayer();
        }

        mMediaPlayer.prepare(getMediaSource(mMediaSource));

        setPlayerState(mPlayWhenReady, STATE_BUFFERING);
        FuLog.w(TAG, "Set media source for the player: source=" + mMediaSource.toString());
    }

    private void setPlayerState(boolean playWhenReady, int playbackState) {
        if (mPlayWhenReady == playWhenReady && mCurrentState == playbackState) {
            return;
        }
        if (mCurrentState == STATE_IDLE) {
            mVideoWidth = 0;
            mVideoHeight = 0;
            mRenderedFirstFrame = false;
        }
        mPlayWhenReady = playWhenReady;
        mCurrentState = playbackState;
        submitStateChanged(playWhenReady, playbackState);
    }

    @Override
    public PlayerError getPlayerError() {
        return mPlayerError;
    }

    @Override
    public boolean hasRenderedFirstFrame() {
        return mRenderedFirstFrame;
    }

    @Override
    public int getVideoWidth() {
        return mVideoWidth;
    }

    @Override
    public int getVideoHeight() {
        return mVideoHeight;
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
        mVideoWidth = 0;
        mVideoHeight = 0;
        mPlayerError = null;
        mRenderedFirstFrame = false;
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
        return mPlayWhenReady;
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
//            mMediaPlayer.release();
//            mMediaPlayer = null;
            mPlayerError = null;
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
            mPlayerError = null;
            setPlayerState(mPlayWhenReady, STATE_IDLE);
        }
    }

    private final VideoListener mVideoListener = new VideoListener() {
        @Override
        public void onVideoSizeChanged(int width, int height,
                                       int unappliedRotationDegrees, float pixelWidthHeightRatio) {
            mVideoWidth = width;
            mVideoHeight = height;
            submitVideoSizeChanged(width, height, unappliedRotationDegrees, pixelWidthHeightRatio);
        }

        @Override
        public void onRenderedFirstFrame() {
            FuLog.i(TAG, "onInfo : video_rendering_start");
            mRenderedFirstFrame = true;
            submitRenderedFirstFrame();
        }
    };

    private final Player.EventListener mEventListener = new Player.EventListener() {

        @Override
        public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {
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
            if (playbackState == Player.STATE_IDLE) {
                setPlayerState(playWhenReady, STATE_IDLE);
            } else if (playbackState == Player.STATE_BUFFERING) {
                setPlayerState(playWhenReady, STATE_BUFFERING);
            } else if (playbackState == Player.STATE_READY) {
                setPlayerState(playWhenReady, STATE_READY);
            } else if (playbackState == Player.STATE_ENDED) {
                setPlayerState(playWhenReady, STATE_ENDED);
            }
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {

        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            if (error == null) {
                mPlayerError = PlayerError.create(PlayerError.MEDIA_ERROR_UNKNOWN);
                submitError(mPlayerError);
                return;
            }
            int type = error.type;
            switch (type) {
                case ExoPlaybackException.TYPE_SOURCE:
                    mPlayerError = PlayerError.create(PlayerError.MEDIA_ERROR_IO);
                    submitError(mPlayerError);
                    break;
                case ExoPlaybackException.TYPE_RENDERER:
                    mPlayerError = PlayerError.create(PlayerError.MEDIA_ERROR_IO);
                    submitError(mPlayerError);
                    break;
                case ExoPlaybackException.TYPE_UNEXPECTED:
                    mPlayerError = PlayerError.create(PlayerError.MEDIA_ERROR_UNKNOWN);
                    submitError(mPlayerError);
                    break;
            }
        }

        @Override
        public void onPositionDiscontinuity(int reason) {
            FuLog.d(TAG, "onPositionDiscontinuity : reason=" + reason);
        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
            FuLog.d(TAG, "onPlaybackParametersChanged : " + playbackParameters.toString());
        }

        @Override
        public void onSeekProcessed() {

        }
    };
}
