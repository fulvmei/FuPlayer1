//package com.chengfu.fuplayer.player.exo;
//
//import android.content.Context;
//import android.net.Uri;
//import android.text.TextUtils;
//import android.view.Surface;
//
//
//import com.chengfu.fuplayer.FuLog;
//import com.chengfu.fuplayer.FuPlayerError;
//
//import com.chengfu.fuplayer.player.AbsPlayer;
//
//import com.chengfu.fuplayer.player.IPlayer;
//
//import com.google.android.exoplayer2.C;
//import com.google.android.exoplayer2.DefaultRenderersFactory;
//
//import com.google.android.exoplayer2.ExoPlaybackException;
//import com.google.android.exoplayer2.ExoPlayerFactory;
//
//import com.google.android.exoplayer2.PlaybackParameters;
//import com.google.android.exoplayer2.Player;
//import com.google.android.exoplayer2.RenderersFactory;
//import com.google.android.exoplayer2.SimpleExoPlayer;
//import com.google.android.exoplayer2.Timeline;
//import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
//import com.google.android.exoplayer2.extractor.ExtractorsFactory;
//import com.google.android.exoplayer2.source.ExtractorMediaSource;
//import com.google.android.exoplayer2.source.MediaSource;
//import com.google.android.exoplayer2.source.TrackGroupArray;
//import com.google.android.exoplayer2.source.dash.DashMediaSource;
//import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
//import com.google.android.exoplayer2.source.hls.HlsMediaSource;
//import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
//import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
//import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
//import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
//import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
//import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
//import com.google.android.exoplayer2.util.Util;
//
//import java.io.IOException;
//import java.util.Map;
//
//public class ExoPlayer extends AbsPlayer {
//
//    public static final String TAG = "ExoPlayer";
//
//    private final Context mContext;
//    private SimpleExoPlayer mMediaPlayer;
//
//
//    private Surface mSurface;
//    private int mAudioSession;
//    private boolean mLooping;
//    private float mVolume = 1.0f;
//    private int mVideoScalingMode;
//
//    private Uri mUri;
//    private Map<String, String> mHeaders;
//
//    private boolean mSeekable;
//
//    private int mCurrentState = STATE_IDLE;
//    private int mTargetState = STATE_IDLE;
//
//    private int mCurrentBufferPercentage;
//    private long mSeekWhenPrepared;  // recording the seek position while preparing
//
//    private boolean mPreparing = true;
//    private boolean mBuffering = false;
//    private boolean mPendingSeek = false;
//
//
//    private final DefaultBandwidthMeter mBandwidthMeter;
//
//    public ExoPlayer(Context context) {
//        this(context, null);
//    }
//
//    public ExoPlayer(Context context, ExoPlayerOption option) {
//        mContext = context;
//        mBandwidthMeter = new DefaultBandwidthMeter();
//
//        mMediaPlayer = createPlayer();
//    }
//
//    private SimpleExoPlayer createPlayer() {
//        RenderersFactory renderersFactory = new DefaultRenderersFactory(mContext);
//        DefaultTrackSelector trackSelector =
//                new DefaultTrackSelector();
//        SimpleExoPlayer simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector);
//
//        simpleExoPlayer.addListener(mEventListener);
//        simpleExoPlayer.setVideoListener(mVideoListener);
//
//        return simpleExoPlayer;
//    }
//
//    public boolean isInPlaybackState() {
//        return (mMediaPlayer != null &&
//                mCurrentState != STATE_IDLE &&
//                mCurrentState != STATE_PREPARING);
//    }
//
//    private MediaSource getMediaSource(Uri uri) {
//        int contentType = Util.inferContentType(uri);
//        DefaultDataSourceFactory dataSourceFactory =
//                new DefaultDataSourceFactory(mContext,
//                        Util.getUserAgent(mContext, mContext.getPackageName()), mBandwidthMeter);
//        switch (contentType) {
//            case C.TYPE_DASH:
//                DefaultDashChunkSource.Factory factory = new DefaultDashChunkSource.Factory(dataSourceFactory);
//                return new DashMediaSource(uri, dataSourceFactory, factory, null, null);
//            case C.TYPE_SS:
//                DefaultSsChunkSource.Factory ssFactory = new DefaultSsChunkSource.Factory(dataSourceFactory);
//                return new SsMediaSource(uri, dataSourceFactory, ssFactory, null, null);
//            case C.TYPE_HLS:
//                return new HlsMediaSource(uri, dataSourceFactory, null, null);
//
//            case C.TYPE_OTHER:
//            default:
//                // This is the MediaSource representing the media to be played.
//                ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
//                return new ExtractorMediaSource(uri,
//                        dataSourceFactory, extractorsFactory, null, null);
//        }
//    }
//
//    private void openMedia() {
//        if (mUri == null) {
//            FuLog.w(TAG, "this url is null", new NullPointerException("uri is null"));
//            submitError(FuPlayerError.create(FuPlayerError.MEDIA_ERROR_IO));
//            return;
//        }
//
//        try {
//            mMediaPlayer = createPlayer();
//            mMediaPlayer.setDataSource(mContext, mUri, mHeaders);
//            mMediaPlayer.prepareAsync();
//
//            mCurrentState = STATE_PREPARING;
//            FuLog.w(TAG, "set the player media uri : uri=" + mUri.getPath());
//        } catch (IOException e) {
//            e.printStackTrace();
//            FuLog.w(TAG, "Unable to open content: " + mUri, e);
//            mCurrentState = STATE_IDLE;
//            mTargetState = STATE_IDLE;
//            submitError(FuPlayerError.create(FuPlayerError.MEDIA_ERROR_IO));
//        } catch (IllegalArgumentException ex) {
//            FuLog.w(TAG, "Unable to open content: " + mUri, ex);
//            mCurrentState = STATE_IDLE;
//            mTargetState = STATE_IDLE;
//            submitError(FuPlayerError.create(FuPlayerError.MEDIA_ERROR_IO));
//            return;
//        }
//    }
//
//    @Override
//    public int getPlayerState() {
//        return mCurrentState;
//    }
//
//    @Override
//    public int getAudioSessionId() {
//        mMediaPlayer.getAudioSessionId();
//    }
//
//    @Override
//    public void setMediaPath(String path) {
//        if (TextUtils.isEmpty(path)) {
//            setMediaUri(null);
//        } else {
//            setMediaUri(Uri.parse(path));
//        }
//    }
//
//    @Override
//    public void setMediaUri(Uri uri) {
//        setMediaUri(uri, null);
//    }
//
//    @Override
//    public void setMediaUri(Uri uri, Map<String, String> headers) {
//        release(false);
//        mUri = uri;
//        mHeaders = headers;
//        mSeekable = false;
//        mSeekWhenPrepared = 0;
//        mCurrentBufferPercentage = 0;
//        openMedia();
//    }
//
//    @Override
//    public void setLooping(boolean looping) {
//        if (mLooping == looping) {
//            return;
//        }
//        if (mMediaPlayer != null) {
//            if (looping) {
//                mMediaPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);
//            } else {
//                mMediaPlayer.setRepeatMode(Player.REPEAT_MODE_OFF);
//            }
//        }
//        mLooping = looping;
//    }
//
//    @Override
//    public boolean isLooping() {
//        return mLooping;
//    }
//
//    @Override
//    public void setVolume(float volume) {
//        if (mVolume == volume) {
//            return;
//        }
//        if (mMediaPlayer != null) {
//            mMediaPlayer.setVolume(volume);
//        }
//        mVolume = volume;
//    }
//
//    @Override
//    public float getVolume() {
//        return mMediaPlayer.getVolume();
//    }
//
//    @Override
//    public int getBufferPercentage() {
//        return mCurrentBufferPercentage;
//    }
//
//    @Override
//    public void setVideoScalingMode(int videoScalingMode) {
//        if (mVideoScalingMode == videoScalingMode) {
//            return;
//        }
//        if (mMediaPlayer != null) {
//            if (videoScalingMode == VIDEO_SCALING_MODE_SCALE_TO_FIT) {
//                mMediaPlayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT);
//            } else {
//                mMediaPlayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
//            }
//        }
//        mVideoScalingMode = videoScalingMode;
//    }
//
//    @Override
//    public int getVideoScalingMode() {
//        return mVideoScalingMode;
//    }
//
//    @Override
//    public void setVideoSurface(Surface surface) {
//        if (mSurface == surface) {
//            return;
//        }
//        if (mMediaPlayer != null) {
//            mMediaPlayer.setVideoSurface(surface);
//        }
//        mSurface = surface;
//    }
//
//    @Override
//    public long getCurrentPosition() {
//        if (isInPlaybackState()) {
//            return mMediaPlayer.getCurrentPosition();
//        }
//        return 0;
//
//    }
//
//    @Override
//    public long getDuration() {
//        if (isInPlaybackState()) {
//            return mMediaPlayer.getDuration();
//        }
//        return -1;
//    }
//
//    @Override
//    public boolean isPlaying() {
//        return isInPlaybackState() && mMediaPlayer.isPlaying();
//    }
//
//    @Override
//    public boolean isSeekable() {
//        return mSeekable;
//    }
//
//    @Override
//    public void seekTo(long msec) {
//        if (isInPlaybackState()) {
//            mMediaPlayer.seekTo((int) msec);
//            mSeekWhenPrepared = 0;
//        } else {
//            mSeekWhenPrepared = msec;
//        }
//    }
//
//    @Override
//    public void start() {
//        if (isInPlaybackState()) {
//            mMediaPlayer.start();
//            mCurrentState = STATE_PLAYING;
//        }
//        mTargetState = STATE_PLAYING;
//    }
//
//    @Override
//    public void pause() {
//        if (isInPlaybackState()) {
//            if (mMediaPlayer.isPlaying()) {
//                mMediaPlayer.pause();
//                mCurrentState = STATE_PAUSED;
//            }
//        }
//        mTargetState = STATE_PAUSED;
//    }
//
//    @Override
//    public void suspend() {
//        release(false);
//    }
//
//    @Override
//    public void resume() {
//        openMedia();
//    }
//
//    @Override
//    public void stop() {
//        if (mMediaPlayer != null) {
//            mMediaPlayer.stop();
//            mMediaPlayer.release();
//            mMediaPlayer = null;
//            mCurrentState = STATE_IDLE;
//            mTargetState = STATE_IDLE;
//        }
//    }
//
//
//    private void release(boolean cleartargetstate) {
//        if (mMediaPlayer != null) {
//            mMediaPlayer.stop();
//            mMediaPlayer.release();
//            mMediaPlayer = null;
//            mCurrentState = STATE_IDLE;
//            if (cleartargetstate) {
//                mTargetState = STATE_IDLE;
//            }
//        }
//    }
//
//    private void updataState(int state) {
//        if (mCurrentState == state) {
//            return;
//        }
//        mCurrentState = state;
//        submitStateChanged(state);
//    }
//
//    private int getErrorCode(int code) {
//        return 0;
////        switch (code) {
////            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
////                return FuPlayerError.MEDIA_ERROR_UNKNOWN;
////            case MediaPlayer.MEDIA_ERROR_IO:
////                return FuPlayerError.MEDIA_ERROR_IO;
////            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
////                return FuPlayerError.MEDIA_ERROR_SERVER_DIED;
////            case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
////                return FuPlayerError.MEDIA_ERROR_TIMED_OUT;
////            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
////                return FuPlayerError.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK;
////            case MediaPlayer.MEDIA_ERROR_MALFORMED:
////                return FuPlayerError.MEDIA_ERROR_MALFORMED;
////            case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
////                return FuPlayerError.MEDIA_ERROR_UNSUPPORTED;
////            default:
////                return FuPlayerError.MEDIA_ERROR_UNKNOWN;
////        }
//    }
//
//    final SimpleExoPlayer.VideoListener mVideoListener = new SimpleExoPlayer.VideoListener() {
//        @Override
//        public void onVideoSizeChanged(int width, int height,
//                                       int unappliedRotationDegrees, float pixelWidthHeightRatio) {
//            submitVideoSizeChanged(width, height, unappliedRotationDegrees, pixelWidthHeightRatio);
//        }
//
//        @Override
//        public void onRenderedFirstFrame() {
//            FuLog.d(TAG, "onRenderedFirstFrame");
//            submitRenderedFirstFrame();
//        }
//    };
//
//    final Player.EventListener mEventListener = new Player.EventListener() {
//        @Override
//        public void onTimelineChanged(Timeline timeline, Object manifest) {
//            FuLog.d(TAG, "onTimelineChanged...");
//        }
//
//        @Override
//        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
//            FuLog.d(TAG, "onTracksChanged...");
//        }
//
//        @Override
//        public void onLoadingChanged(boolean isLoading) {
//            FuLog.d(TAG, "onLoadingChanged : isLoading=" + isLoading);
//        }
//
//        @Override
//        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
//            FuLog.d(TAG, "onPlayerStateChanged : playWhenReady = " + playWhenReady
//                    + ", playbackState = " + playbackState);
//
//            switch (playbackState) {
//                case Player.STATE_IDLE:
//                    updataState(STATE_IDLE);
//                    break;
//                case Player.STATE_BUFFERING:
//                    if (mCurrentState != STATE_PREPARING) {
//                        submitLoadingChanged(true);
//                    }
//                    break;
//                case Player.STATE_READY:
//                    if (mCurrentState == STATE_PREPARING) {
//                        updataState(STATE_PREPARED);
//                    }
//                    break;
//                case Player.STATE_ENDED:
//                    break;
//            }
//
//            if (!mPreparing) {
//                if (playWhenReady) {
//                    mCurrentState = STATE_PLAYING;
//                } else {
//                    mCurrentState = STATE_PAUSED;
//                }
//            }
//
//            if (mPreparing) {
//                switch (playbackState) {
//                    case Player.STATE_READY:
//                        mPreparing = false;
//                        mCurrentState = STATE_PREPARED;
//                        if (mSeekWhenPrepared > 0) {
//                            mMediaPlayer.seekTo(mSeekWhenPrepared);
//                            mSeekWhenPrepared = -1;
//                        }
//                        break;
//                }
//            }
//
//            if (isBuffering) {
//                switch (playbackState) {
//                    case Player.STATE_READY:
//                    case Player.STATE_ENDED:
//                        long bitrateEstimate = mBandwidthMeter.getBitrateEstimate();
//                        FuLog.d(TAG, "buffer_end, BandWidth : " + bitrateEstimate);
//                        isBuffering = false;
//                        break;
//                }
//            }
//
//            if (isPendingSeek) {
//                switch (playbackState) {
//                    case Player.STATE_READY:
//                        isPendingSeek = false;
//                        submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_SEEK_COMPLETE, null);
//                        break;
//                }
//            }
//
//            if (!isPreparing) {
//                switch (playbackState) {
//                    case Player.STATE_BUFFERING:
//                        long bitrateEstimate = mBandwidthMeter.getBitrateEstimate();
//                        FuLog.d(TAG, "buffer_start, BandWidth : " + bitrateEstimate);
//                        isBuffering = true;
//                        break;
//                    case Player.STATE_ENDED:
//                        updateStatus(IPlayer.STATE_PLAYBACK_COMPLETE);
//                        submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_PLAY_COMPLETE, null);
//                        break;
//                }
//            }
//
//        }
//
//        @Override
//        public void onRepeatModeChanged(int repeatMode) {
//
//        }
//
//        @Override
//        public void onPlayerError(ExoPlaybackException error) {
//            if (error == null) {
//                submitError(FuPlayerError.create(FuPlayerError.MEDIA_ERROR_UNKNOWN);
//                return;
//            }
//            FuLog.e(TAG, error.getMessage() == null ? "" : error.getMessage());
//            int type = error.type;
//            switch (type) {
//                case ExoPlaybackException.TYPE_SOURCE:
//                    submitError(FuPlayerError.create(FuPlayerError.MEDIA_ERROR_IO);
//                    break;
//                case ExoPlaybackException.TYPE_RENDERER:
//                    submitError(FuPlayerError.create(FuPlayerError.MEDIA_ERROR_UNKNOWN);
//                    break;
//                case ExoPlaybackException.TYPE_UNEXPECTED:
//                    submitError(FuPlayerError.create(FuPlayerError.MEDIA_ERROR_UNKNOWN);
//                    break;
//            }
//        }
//
//        @Override
//        public void onPositionDiscontinuity() {
//
//        }
//
//        @Override
//        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
//            FuLog.d(TAG, "onPlaybackParametersChanged : " + playbackParameters.toString());
//        }
//    };
//
//}
