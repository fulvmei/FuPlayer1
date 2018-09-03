package com.chengfu.fuplayer.player.ijk;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.Surface;

import com.chengfu.fuplayer.FuLog;
import com.chengfu.fuplayer.FuPlayerError;
import com.chengfu.fuplayer.MediaSource;
import com.chengfu.fuplayer.player.AbsPlayer;

import java.io.IOException;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.IjkTimedText;

public class IjkPlayer extends AbsPlayer {

    private final String TAG = "IjkPlayer";

    private final Context mContext;
    private IjkMediaPlayer mMediaPlayer;

    private Surface mSurface;
    private int mAudioSession;
    private boolean mLooping;
    private float mVolume = 1.0f;

    private MediaSource mMediaSource;

    private boolean mSeekable;
    private boolean mPlayWhenReady;

    private int mCurrentState = -1;

    private int mCurrentBufferPercentage;
    private long mSeekWhenPrepared;  // recording the seek position while preparing

    static {
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");
    }

    public IjkPlayer(Context context) {
        this(context, null);
    }

    public IjkPlayer(Context context, IjkPlayerOption option) {
        this.mContext = context;
    }

    private IjkMediaPlayer createPlayer() {
        IjkMediaPlayer ijkMediaPlayer = new IjkMediaPlayer();

        //open mediacodec
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);

        //accurate seek
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 1);

        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 1);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0);

        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "timeout", 10000000);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "reconnect", 1);

        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);

        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);

        ijkMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mAudioSession = ijkMediaPlayer.getAudioSessionId();

        ijkMediaPlayer.setLooping(mLooping);
        ijkMediaPlayer.setVolume(mVolume, mVolume);
        ijkMediaPlayer.setSurface(mSurface);

        ijkMediaPlayer.setOnPreparedListener(mPreparedListener);
        ijkMediaPlayer.setOnVideoSizeChangedListener(mVideoSizeChangedListener);
        ijkMediaPlayer.setOnCompletionListener(mCompletionListener);
        ijkMediaPlayer.setOnSeekCompleteListener(mSeekCompleteListener);
        ijkMediaPlayer.setOnErrorListener(mErrorListener);
        ijkMediaPlayer.setOnInfoListener(mInfoListener);
        ijkMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
        ijkMediaPlayer.setOnTimedTextListener(mTimedTextListener);
//        ijkMediaPlayer.setOnControlMessageListener(mControlMessageListener);
//        ijkMediaPlayer.setOnMediaCodecSelectListener(mMediaCodecSelectListener);
//        ijkMediaPlayer.setOnNativeInvokeListener(mNativeInvokeListener);
        return ijkMediaPlayer;
    }

    public boolean isInPlaybackState() {
        return (mMediaPlayer != null &&
                mCurrentState == STATE_READY
                && mCurrentState == STATE_BUFFERING
                && mCurrentState == STATE_ENDED);
    }

    private void openMedia() {
        if (mMediaSource == null || (mMediaSource.getPath() == null && mMediaSource.getUri() == null)) {
            FuLog.w(TAG, "this mediaSource is null or path and uri both are empty", new NullPointerException("mediaSource is null"));
            submitError(FuPlayerError.create(FuPlayerError.MEDIA_ERROR_IO));
            setPlayerState(mPlayWhenReady, STATE_ERROR);
            return;
        }

        try {
            mMediaPlayer = createPlayer();
            if (mMediaSource.getPath() != null) {
                mMediaPlayer.setDataSource(mMediaSource.getPath());
            } else {
                if (mMediaSource.getHeaders() != null) {
                    mMediaPlayer.setDataSource(mContext, mMediaSource.getUri(), mMediaSource.getHeaders());
                } else {
                    mMediaPlayer.setDataSource(mContext, mMediaSource.getUri());
                }
            }
            mMediaPlayer.prepareAsync();

            setPlayerState(mPlayWhenReady, STATE_PREPARING);
            FuLog.i(TAG, "Set media source for the player: source=" + mMediaSource.toString());
        } catch (IOException e) {
            e.printStackTrace();
            FuLog.e(TAG, "Unable to open content: " + mMediaSource.toString(), e);
            submitError(FuPlayerError.create(FuPlayerError.MEDIA_ERROR_IO));
            setPlayerState(mPlayWhenReady, STATE_ERROR);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            FuLog.e(TAG, "Unable to open content: " + mMediaSource.toString(), e);
            submitError(FuPlayerError.create(FuPlayerError.MEDIA_ERROR_IO));
            setPlayerState(mPlayWhenReady, STATE_ERROR);
            return;
        }
    }

    private void setPlayerState(boolean playWhenReady, int state) {
        if (mPlayWhenReady == playWhenReady && mCurrentState == state) {
            return;
        }
        mPlayWhenReady = playWhenReady;
        mCurrentState = state;
        submitStateChanged(playWhenReady, state);
    }

    @Override
    public int getPlayerState() {
        return mCurrentState;
    }

    @Override
    public int getAudioSessionId() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getAudioSessionId();
        }
        return 0;
    }

    @Override
    public void setMediaSource(MediaSource mediaSource) {
        stop();
        mMediaSource = mediaSource;
        mSeekable = false;
        mSeekWhenPrepared = 0;
        mCurrentBufferPercentage = 0;
        openMedia();
    }

    @Override
    public void setPlayWhenReady(boolean playWhenReady) {
        if (mPlayWhenReady == playWhenReady) {
            return;
        }
        if (isInPlaybackState()) {
            if (playWhenReady) {
                mMediaPlayer.start();
            } else if (isPlaying()) {
                mMediaPlayer.pause();
            }
        }
        setPlayerState(playWhenReady, mCurrentState);
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
            mMediaPlayer.setLooping(looping);
        }
        mLooping = looping;
    }

    @Override
    public boolean isLooping() {
        return mLooping;
    }

    @Override
    public void setVolume(float volume) {
        if (mVolume == volume) {
            return;
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.setVolume(volume, volume);
        }
        mVolume = volume;
    }

    @Override
    public float getVolume() {
        return mVolume;
    }

    @Override
    public int getBufferPercentage() {
        return mCurrentBufferPercentage;
    }

    @Override
    public void setVideoSurface(Surface surface) {
        if (mSurface == surface) {
            return;
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.setSurface(surface);
        }
        mSurface = surface;
    }

    @Override
    public long getCurrentPosition() {
        if (isInPlaybackState()) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;

    }

    @Override
    public long getDuration() {
        if (isInPlaybackState()) {
            return mMediaPlayer.getDuration();
        }
        return -1;
    }

    @Override
    public boolean isPlaying() {
        return isInPlaybackState() && mMediaPlayer.isPlaying();
    }

    @Override
    public boolean isSeekable() {
        return mSeekable;
    }

    @Override
    public void seekTo(long msec) {
        if (isInPlaybackState()) {
            mMediaPlayer.seekTo((int) msec);
            mSeekWhenPrepared = 0;
        } else {
            mSeekWhenPrepared = msec;
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
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mMediaSource = null;
            mPlayWhenReady = false;
            mSurface = null;
            setPlayerState(mPlayWhenReady, STATE_IDLE);
        }
    }

    private int getErrorCode(int code) {
        switch (code) {
            case IjkMediaPlayer.MEDIA_ERROR_UNKNOWN:
                return FuPlayerError.MEDIA_ERROR_UNKNOWN;
            case IjkMediaPlayer.MEDIA_ERROR_IO:
                return FuPlayerError.MEDIA_ERROR_IO;
            case IjkMediaPlayer.MEDIA_ERROR_SERVER_DIED:
                return FuPlayerError.MEDIA_ERROR_SERVER_DIED;
            case IjkMediaPlayer.MEDIA_ERROR_TIMED_OUT:
                return FuPlayerError.MEDIA_ERROR_TIMED_OUT;
            case IjkMediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                return FuPlayerError.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK;
            case IjkMediaPlayer.MEDIA_ERROR_MALFORMED:
                return FuPlayerError.MEDIA_ERROR_MALFORMED;
            case IjkMediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                return FuPlayerError.MEDIA_ERROR_UNSUPPORTED;
            default:
                return FuPlayerError.MEDIA_ERROR_UNKNOWN;
        }
    }

    final IjkMediaPlayer.OnPreparedListener mPreparedListener = new IjkMediaPlayer.OnPreparedListener() {
        public void onPrepared(IMediaPlayer mp) {
            FuLog.d(TAG, "onPrepared...");

            long seekToPosition = mSeekWhenPrepared;  // mSeekWhenPrepared may be changed after seekTo() call
            if (seekToPosition != 0) {
                seekTo(seekToPosition);
            }
            setPlayerState(mPlayWhenReady, STATE_READY);
            if (mPlayWhenReady) {
                mMediaPlayer.start();
            }
        }
    };


    final IjkMediaPlayer.OnVideoSizeChangedListener mVideoSizeChangedListener =
            new IjkMediaPlayer.OnVideoSizeChangedListener() {
                @Override
                public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int width, int height, int unappliedRotationDegrees, int pixelWidthHeightRatio) {
                    FuLog.d(TAG, "onVideoSizeChanged : width=" + width + ",height=" + height + ",unappliedRotationDegrees=" + unappliedRotationDegrees + ",pixelWidthHeightRatio=" + pixelWidthHeightRatio);
                    submitVideoSizeChanged(width, height, 0, 0);
                }
            };

    final IjkMediaPlayer.OnCompletionListener mCompletionListener =
            new IjkMediaPlayer.OnCompletionListener() {
                public void onCompletion(IMediaPlayer mp) {
                    FuLog.d(TAG, "onCompletion");
                    setPlayerState(mPlayWhenReady, STATE_ENDED);
                }
            };

    final IjkMediaPlayer.OnInfoListener mInfoListener =
            new IjkMediaPlayer.OnInfoListener() {
                public boolean onInfo(IMediaPlayer mp, int what, int extra) {
                    FuLog.d(TAG, "onInfo : what=" + what + ",extra=" + extra);
                    switch (what) {
                        case IjkMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                            FuLog.i(TAG, "onInfo : video_rendering_start");
                            submitRenderedFirstFrame();
                            break;
                        case IjkMediaPlayer.MEDIA_INFO_BUFFERING_START:
                            FuLog.i(TAG, "onInfo : buffering_start");
                            setPlayerState(mPlayWhenReady, STATE_BUFFERING);
                            break;
                        case IjkMediaPlayer.MEDIA_INFO_BUFFERING_END:
                            FuLog.i(TAG, "onInfo : buffering_end");
                            setPlayerState(mPlayWhenReady, STATE_READY);
                            break;
                        case IjkMediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                            mSeekable = false;
                            FuLog.i(TAG, "onInfo : not_seekable");
                            break;
                    }
                    return true;
                }
            };

    final IjkMediaPlayer.OnSeekCompleteListener mSeekCompleteListener = new IjkMediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(IMediaPlayer mp) {
            FuLog.d(TAG, "onSeekComplete");
            submitSeekComplete();
        }
    };

    final IjkMediaPlayer.OnErrorListener mErrorListener =
            new IjkMediaPlayer.OnErrorListener() {
                public boolean onError(IMediaPlayer mp, int framework_err, int impl_err) {
                    FuLog.d(TAG, "Error : framework_err=" + framework_err + ",impl_err=" + impl_err);
                    submitError(FuPlayerError.create(getErrorCode(framework_err)));
                    setPlayerState(mPlayWhenReady, STATE_ERROR);
                    return true;
                }
            };

    final IjkMediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener =
            new IjkMediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(IMediaPlayer mp, int percent) {
                    FuLog.d(TAG, "OnBufferingUpdate : percent" + percent);
                    mCurrentBufferPercentage = percent;
                    submitBufferingUpdate(percent);
                }
            };

    final IjkMediaPlayer.OnTimedTextListener mTimedTextListener = new IjkMediaPlayer.OnTimedTextListener() {

        @Override
        public void onTimedText(IMediaPlayer iMediaPlayer, IjkTimedText ijkTimedText) {
            FuLog.d(TAG, "onTimedText : ijkTimedText" + ijkTimedText != null ? ijkTimedText.getText() : "");
        }
    };

}
