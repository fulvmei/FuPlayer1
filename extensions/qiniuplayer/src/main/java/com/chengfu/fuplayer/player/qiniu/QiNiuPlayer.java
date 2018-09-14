package com.chengfu.fuplayer.player.qiniu;

import android.content.Context;
import android.text.TextUtils;
import android.view.Surface;

import com.chengfu.fuplayer.FuLog;
import com.chengfu.fuplayer.MediaSource;
import com.chengfu.fuplayer.PlayerError;
import com.chengfu.fuplayer.player.AbsPlayer;
import com.pili.pldroid.player.AVOptions;
import com.pili.pldroid.player.PLMediaPlayer;
import com.pili.pldroid.player.PLOnAudioFrameListener;
import com.pili.pldroid.player.PLOnBufferingUpdateListener;
import com.pili.pldroid.player.PLOnCompletionListener;
import com.pili.pldroid.player.PLOnErrorListener;
import com.pili.pldroid.player.PLOnImageCapturedListener;
import com.pili.pldroid.player.PLOnInfoListener;
import com.pili.pldroid.player.PLOnPreparedListener;
import com.pili.pldroid.player.PLOnSeekCompleteListener;
import com.pili.pldroid.player.PLOnVideoFrameListener;
import com.pili.pldroid.player.PLOnVideoSizeChangedListener;

import java.io.IOException;

public final class QiNiuPlayer extends AbsPlayer {

    public static final String TAG = "QiNiuPlayer";

    private final Context mContext;
    private PLMediaPlayer mMediaPlayer;

    private Surface mSurface;
    private boolean mLooping;
    private float mVolume = 1.0f;

    private MediaSource mMediaSource;

    private PlayerError mPlayerError;
    private int mCurrentBufferPercentage;
    private int mVideoWidth;
    private int mVideoHeight;
    private boolean mRenderedFirstFrame;

    private boolean mSeekable;
    private boolean mPlayWhenReady;

    private int mCurrentState = -1;

    private long mSeekWhenPrepared;  // recording the seek position while preparing
    private boolean mIsPreparing;

    public QiNiuPlayer(Context context) {
        this(context, null);
    }

    public QiNiuPlayer(Context context, QiNiuPlayerOption option) {
        mContext = context;
        mVideoWidth = 0;
        mVideoHeight = 0;
        mSeekable = true;
        mPlayerError = null;
        mRenderedFirstFrame = false;
        mCurrentBufferPercentage = 0;
        setPlayerState(mPlayWhenReady, STATE_IDLE);
    }

    private PLMediaPlayer createPlayer() {
        AVOptions options = new AVOptions();
        options.setInteger(AVOptions.KEY_MEDIACODEC, AVOptions.MEDIA_CODEC_SW_DECODE);

        PLMediaPlayer mediaPlayer = new PLMediaPlayer(mContext);

//        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//        if (mAudioSession != 0) {
//            mediaPlayer.setAudioSessionId(mAudioSession);
//        } else {
//            mAudioSession = mediaPlayer.getAudioSessionId();
//        }

        mediaPlayer.setLooping(mLooping);
        mediaPlayer.setVolume(mVolume, mVolume);
        mediaPlayer.setSurface(mSurface);

        mediaPlayer.setOnPreparedListener(mPreparedListener);
        mediaPlayer.setOnVideoSizeChangedListener(mVideoSizeChangedListener);
        mediaPlayer.setOnCompletionListener(mCompletionListener);
        mediaPlayer.setOnSeekCompleteListener(mSeekCompleteListener);
        mediaPlayer.setOnErrorListener(mErrorListener);
        mediaPlayer.setOnInfoListener(mInfoListener);
        mediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
        mediaPlayer.setOnVideoFrameListener(mVideoFrameListener);
        mediaPlayer.setOnImageCapturedListener(mImageCapturedListener);
        mediaPlayer.setOnAudioFrameListener(mAudioFrameListener);
        return mediaPlayer;
    }

    public boolean isInPlaybackState() {
        return (mMediaPlayer != null &&
                mCurrentState != STATE_IDLE
                && !mIsPreparing);
    }

    private void openMedia() {
        if (mMediaSource == null || TextUtils.isEmpty(mMediaSource.getPath())) {
            FuLog.w(TAG, "this mediaSource is null or path is empty", new NullPointerException("mediaSource is null"));
            mPlayerError = PlayerError.create(PlayerError.MEDIA_ERROR_IO);
            submitError(mPlayerError);
            return;
        }

        try {
            mMediaPlayer = createPlayer();
            mMediaPlayer.setDataSource(mMediaSource.getPath(), mMediaSource.getHeaders());

            mIsPreparing = true;
            mMediaPlayer.prepareAsync();

            setPlayerState(mPlayWhenReady, STATE_BUFFERING);
            FuLog.i(TAG, "Set media source for the player: source=" + mMediaSource.toString());
        } catch (IOException e) {
            e.printStackTrace();
            FuLog.w(TAG, "Unable to open content: " + mMediaSource.getPath(), e);
            mPlayerError = PlayerError.create(PlayerError.MEDIA_ERROR_IO);
            submitError(mPlayerError);
            setPlayerState(mPlayWhenReady, STATE_IDLE);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            FuLog.w(TAG, "Unable to open content: " + mMediaSource.getPath(), e);
            mPlayerError = PlayerError.create(PlayerError.MEDIA_ERROR_IO);
            submitError(mPlayerError);
            setPlayerState(mPlayWhenReady, STATE_IDLE);
            return;
        }
    }

    private void setPlayerState(boolean playWhenReady, int state) {
        if (mPlayWhenReady == playWhenReady && mCurrentState == state) {
            return;
        }
        if (mCurrentState == STATE_IDLE) {
            mVideoWidth = 0;
            mVideoHeight = 0;
            mRenderedFirstFrame = false;
        }
        mPlayWhenReady = playWhenReady;
        mCurrentState = state;
        submitStateChanged(playWhenReady, state);
    }

    @Override
    public PlayerError getPlayerError() {
        return mPlayerError;
    }

    @Override
    public int getPlayerState() {
        return mCurrentState;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    @Override
    public void setMediaSource(MediaSource mediaSource) {
        stop();
        mMediaSource = mediaSource;
        mVideoWidth = 0;
        mVideoHeight = 0;
        mSeekable = true;
        mPlayerError = null;
        mRenderedFirstFrame = false;
        mSeekWhenPrepared = 0;
        mCurrentBufferPercentage = 0;
        openMedia();
    }

    @Override
    public void setPlayWhenReady(boolean playWhenReady) {
        if (isInPlaybackState()) {
            if (playWhenReady) {
                if (mCurrentState == STATE_ENDED) {
                    setPlayerState(playWhenReady, STATE_READY);
                }
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
            setPlayWhenReady(mPlayWhenReady);
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

    private int getErrorCode(int code) {
        switch (code) {
            case PLOnErrorListener.MEDIA_ERROR_UNKNOWN:
                return PlayerError.MEDIA_ERROR_UNKNOWN;
            case PLOnErrorListener.ERROR_CODE_IO_ERROR:
                return PlayerError.MEDIA_ERROR_IO;
            default:
                return PlayerError.MEDIA_ERROR_UNKNOWN;
        }
    }

    final PLOnPreparedListener mPreparedListener = new PLOnPreparedListener() {
        @Override
        public void onPrepared(int i) {
            FuLog.d(TAG, "onPrepared..." + mMediaPlayer.getDuration());

            mIsPreparing = false;
            if (getDuration() <= 0) {
                mSeekable = false;
                submitSeekableChanged(mSeekable);
            }
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


    final PLOnVideoSizeChangedListener mVideoSizeChangedListener =
            new PLOnVideoSizeChangedListener() {
                public void onVideoSizeChanged(int width, int height) {
                    FuLog.i(TAG, "onVideoSizeChanged : width=" + width + ",height=" + height);
                    mVideoWidth = width;
                    mVideoHeight = height;
                    submitVideoSizeChanged(width, height, 0, 0);
                }
            };

    final PLOnCompletionListener mCompletionListener =
            new PLOnCompletionListener() {
                public void onCompletion() {
                    FuLog.i(TAG, "onCompletion");
                    setPlayerState(mPlayWhenReady, STATE_ENDED);
                }
            };

    final PLOnInfoListener mInfoListener =
            new PLOnInfoListener() {
                public void onInfo(int what, int extra) {
                    switch (what) {
                        case PLOnInfoListener.MEDIA_INFO_VIDEO_RENDERING_START:
                            FuLog.i(TAG, "onInfo : video_rendering_start");
                            mRenderedFirstFrame = true;
                            submitRenderedFirstFrame();
                            break;
                        case PLOnInfoListener.MEDIA_INFO_BUFFERING_START:
                            FuLog.i(TAG, "onInfo : buffering_start");
                            setPlayerState(mPlayWhenReady, STATE_BUFFERING);
                            break;
                        case PLOnInfoListener.MEDIA_INFO_BUFFERING_END:
                            FuLog.i(TAG, "onInfo : buffering_end");
                            setPlayerState(mPlayWhenReady, STATE_READY);
                            break;
                        case PLOnInfoListener.MEDIA_INFO_IS_SEEKING:
                            FuLog.i(TAG, "onInfo : not_seekable");
                            break;
                        case PLOnInfoListener.MEDIA_INFO_METADATA:
                            FuLog.i(TAG, "onInfo : media_info_metadata  " + mMediaPlayer.getMetadata());
                            break;
                    }
                }
            };

    final PLOnSeekCompleteListener mSeekCompleteListener = new PLOnSeekCompleteListener() {
        @Override
        public void onSeekComplete() {
            FuLog.d(TAG, "onSeekComplete");
            submitSeekComplete();
        }
    };

    final PLOnErrorListener mErrorListener =
            new PLOnErrorListener() {
                public boolean onError(int framework_err) {
                    FuLog.d(TAG, "Error : code=" + getErrorCode(framework_err));
                    mPlayerError = PlayerError.create(getErrorCode(framework_err));
                    submitError(mPlayerError);
                    setPlayerState(mPlayWhenReady, STATE_IDLE);
                    return true;
                }
            };

    final PLOnBufferingUpdateListener mBufferingUpdateListener =
            new PLOnBufferingUpdateListener() {
                public void onBufferingUpdate(int percent) {
                    FuLog.d(TAG, "onBufferingUpdate : percent=" + percent);
                    mCurrentBufferPercentage = percent;
                    submitBufferingUpdate(percent);
                }
            };

    final PLOnVideoFrameListener mVideoFrameListener = new PLOnVideoFrameListener() {

        @Override
        public void onVideoFrameAvailable(byte[] bytes, int i, int i1, int i2, int i3, long l) {
//            FuLog.d(TAG, "onVideoFrameAvailable");
        }
    };

    final PLOnAudioFrameListener mAudioFrameListener = new PLOnAudioFrameListener() {

        @Override
        public void onAudioFrameAvailable(byte[] bytes, int i, int i1, int i2, int i3, long l) {
//            FuLog.d(TAG, "onAudioFrameAvailable");
        }
    };

    final PLOnImageCapturedListener mImageCapturedListener = new PLOnImageCapturedListener() {

        @Override
        public void onImageCaptured(byte[] bytes) {
            FuLog.d(TAG, "onImageCaptured");
        }
    };
}
