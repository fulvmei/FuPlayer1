package com.chengfu.fuplayer.player.sys;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.widget.VideoView;


import com.chengfu.fuplayer.FuLog;
import com.chengfu.fuplayer.bean.DataSource;
import com.chengfu.fuplayer.player.BaseInternalPlayer;
import com.chengfu.fuplayer.player.BundlePool;
import com.chengfu.fuplayer.player.EventKey;
import com.chengfu.fuplayer.player.OnErrorEventListener;
import com.chengfu.fuplayer.player.OnPlayerEventListener;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SysPlayer extends BaseInternalPlayer {

    final String TAG = "SysPlayer";

    private final static int MEDIA_INFO_NETWORK_BANDWIDTH = 703;

    final Context mContext;

    private MediaPlayer mMediaPlayer;

    private int mTargetState;

    private long mBandWidth;
    private int mVideoWidth;
    private int mVideoHeight;

    private int startSeekPos;

    public SysPlayer(Context context) {
        this(context, null);
    }

    public SysPlayer(Context context, SysPlayerOption option) {
        mContext = context;
        mMediaPlayer = new MediaPlayer();
    }

    @Override
    public void setDataSource(DataSource dataSource) {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        } else {
            stop();
            reset();
            resetListener();
        }
        mMediaPlayer.setOnPreparedListener(mPreparedListener);
        mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
        mMediaPlayer.setOnCompletionListener(mCompletionListener);
        mMediaPlayer.setOnErrorListener(mErrorListener);
        mMediaPlayer.setOnInfoListener(mInfoListener);
        mMediaPlayer.setOnSeekCompleteListener(mOnSeekCompleteListener);
        mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);

        updateStatus(STATE_INITIALIZED);

        try {
            String data = dataSource.getData();
            Uri uri = dataSource.getUri();
            HashMap<String, String> headers = dataSource.getExtra();
            FileDescriptor fileDescriptor = dataSource.getFileDescriptor();
            AssetFileDescriptor assetFileDescriptor = dataSource.getAssetFileDescriptor();
            if (data != null) {
                mMediaPlayer.setDataSource(data);
            } else if (uri != null) {
                if (headers == null)
                    mMediaPlayer.setDataSource(mContext, uri);
                else
                    mMediaPlayer.setDataSource(mContext, uri, headers);
            } else if (fileDescriptor != null) {
                mMediaPlayer.setDataSource(fileDescriptor);
            } else if (assetFileDescriptor != null
                    && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mMediaPlayer.setDataSource(assetFileDescriptor);
            }

            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.prepareAsync();

            Bundle bundle = BundlePool.obtain();
            bundle.putSerializable(EventKey.SERIALIZABLE_DATA, dataSource);
            submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_DATA_SOURCE_SET, bundle);
        } catch (IOException e) {
            e.printStackTrace();
            updateStatus(STATE_ERROR);
            mTargetState = STATE_ERROR;
        }

    }

    @Override
    public void setDisplay(SurfaceHolder surfaceHolder) {
        mMediaPlayer.setDisplay(surfaceHolder);
        submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_SURFACE_HOLDER_UPDATE, null);
    }

    @Override
    public void setSurface(Surface surface) {
        mMediaPlayer.setSurface(surface);
        submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_SURFACE_UPDATE, null);
    }

    @Override
    public void setVolume(float volume) {
        if (volume < 0.0f) {
            mCurrentVolume = 0.0f;
        } else if (volume > 1.0f) {
            mCurrentVolume = 1.0f;
        } else {
            mCurrentVolume = volume;
        }

        mMediaPlayer.setVolume(mCurrentVolume, mCurrentVolume);
    }

    @Override
    public void setSpeed(float speed) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PlaybackParams playbackParams = mMediaPlayer.getPlaybackParams();
            playbackParams.setSpeed(speed);
            mMediaPlayer.setPlaybackParams(playbackParams);
        } else {
            FuLog.e(TAG, "not support play speed setting.");
        }
    }

    @Override
    public boolean isPlaying() {
        if (getState() != STATE_ERROR) {
            return mMediaPlayer.isPlaying();
        }
        return false;
    }

    @Override
    public long getCurrentPosition() {
        if (getState() == STATE_PREPARED
                || getState() == STATE_STARTED
                || getState() == STATE_PAUSED
                || getState() == STATE_PLAYBACK_COMPLETE) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public long getDuration() {
        if (getState() != STATE_ERROR
                && getState() != STATE_INITIALIZED
                && getState() != STATE_IDLE) {
            return mMediaPlayer.getDuration();
        }
        return 0;
    }

    @Override
    public int getAudioSessionId() {
        return mMediaPlayer.getAudioSessionId();
    }

    @Override
    public int getVideoWidth() {
        return mMediaPlayer.getVideoWidth();
    }

    @Override
    public int getVideoHeight() {
        return mMediaPlayer.getVideoHeight();
    }

    @Override
    public void start() {
        if (getState() == STATE_PREPARED
                || getState() == STATE_PAUSED
                || getState() == STATE_PLAYBACK_COMPLETE) {
            mMediaPlayer.start();
            updateStatus(STATE_STARTED);
            submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_START, null);
        }

        mTargetState = STATE_STARTED;
    }

    @Override
    public void start(int msc) {
        if (msc > 0) {
            startSeekPos = msc;
        }
        start();
    }

    @Override
    public void pause() {
        mMediaPlayer.pause();
        updateStatus(STATE_PAUSED);
        submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_PAUSE, null);

        mTargetState = STATE_PAUSED;
    }

    @Override
    public void resume() {
        if (getState() == STATE_PAUSED) {
            mMediaPlayer.start();
            updateStatus(STATE_STARTED);
            submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_RESUME, null);
        }

        mTargetState = STATE_STARTED;
    }

    @Override
    public void seekTo(int msc) {
        if (getState() == STATE_PREPARED
                || getState() == STATE_STARTED
                || getState() == STATE_PAUSED
                || getState() == STATE_PLAYBACK_COMPLETE) {
            mMediaPlayer.seekTo(msc);
            Bundle bundle = BundlePool.obtain();
            bundle.putInt(EventKey.INT_DATA, msc);
            submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_SEEK_TO, bundle);
        }
    }

    @Override
    public void stop() {
        if (getState() == STATE_PREPARED
                || getState() == STATE_STARTED
                || getState() == STATE_PAUSED
                || getState() == STATE_PLAYBACK_COMPLETE) {
            mMediaPlayer.stop();
            updateStatus(STATE_STOPPED);
            submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_STOP, null);
        }
        mTargetState = STATE_STOPPED;
    }

    @Override
    public void reset() {
        mMediaPlayer.reset();
        updateStatus(STATE_IDLE);
        submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_RESET, null);

        mTargetState = STATE_IDLE;
    }

    @Override
    public void destroy() {
        updateStatus(STATE_END);
        resetListener();
        mMediaPlayer.release();
        submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_DESTROY, null);
    }

    private void resetListener() {
        mMediaPlayer.setOnPreparedListener(null);
        mMediaPlayer.setOnVideoSizeChangedListener(null);
        mMediaPlayer.setOnCompletionListener(null);
        mMediaPlayer.setOnErrorListener(null);
        mMediaPlayer.setOnInfoListener(null);
        mMediaPlayer.setOnBufferingUpdateListener(null);
    }

    MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            FuLog.d(TAG, "onPrepared...");
            updateStatus(STATE_PREPARED);

            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();

            Bundle bundle = BundlePool.obtain();
            bundle.putInt(EventKey.INT_ARG1, mVideoWidth);
            bundle.putInt(EventKey.INT_ARG2, mVideoHeight);

            submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_PREPARED, bundle);

            int seekToPosition = startSeekPos;  // mSeekWhenPrepared may be changed after seekTo() call
            if (seekToPosition != 0) {
                //seek to start position
                mMediaPlayer.seekTo(seekToPosition);
                startSeekPos = 0;
            }

            // We don't know the video size yet, but should start anyway.
            // The video size might be reported to us later.
            FuLog.d(TAG, "mTargetState = " + mTargetState);
            if (mTargetState == STATE_STARTED) {
                start();
            } else if (mTargetState == STATE_PAUSED) {
                pause();
            } else if (mTargetState == STATE_STOPPED
                    || mTargetState == STATE_IDLE) {
                reset();
            }
        }
    };

    MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener =
            new MediaPlayer.OnVideoSizeChangedListener() {
                public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                    mVideoWidth = mp.getVideoWidth();
                    mVideoHeight = mp.getVideoHeight();
                    Bundle bundle = BundlePool.obtain();
                    bundle.putInt(EventKey.INT_ARG1, mVideoWidth);
                    bundle.putInt(EventKey.INT_ARG2, mVideoHeight);
                    submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_VIDEO_SIZE_CHANGE, bundle);
                }
            };

    private MediaPlayer.OnCompletionListener mCompletionListener =
            new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    updateStatus(STATE_PLAYBACK_COMPLETE);
                    mTargetState = STATE_PLAYBACK_COMPLETE;
                    submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_PLAY_COMPLETE, null);
                }
            };

    private MediaPlayer.OnInfoListener mInfoListener =
            new MediaPlayer.OnInfoListener() {
                public boolean onInfo(MediaPlayer mp, int arg1, int arg2) {
                    switch (arg1) {
                        case MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
                            FuLog.d(TAG, "MEDIA_INFO_VIDEO_TRACK_LAGGING:");
                            break;
                        case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                            FuLog.d(TAG, "media_info_video_rendering_start");
                            startSeekPos = 0;
                            submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_VIDEO_RENDER_START, null);
                            break;
                        case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                            FuLog.d(TAG, "MEDIA_INFO_BUFFERING_START:" + arg2);
                            Bundle bundle = BundlePool.obtain();
                            bundle.putLong(EventKey.LONG_DATA, mBandWidth);
                            submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_BUFFERING_START, bundle);
                            break;
                        case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                            FuLog.d(TAG, "MEDIA_INFO_BUFFERING_END:" + arg2);
                            Bundle bundle1 = BundlePool.obtain();
                            bundle1.putLong(EventKey.LONG_DATA, mBandWidth);
                            submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_BUFFERING_END, bundle1);
                            break;
                        case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                            FuLog.d(TAG, "MEDIA_INFO_BAD_INTERLEAVING:");
                            submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_BAD_INTERLEAVING, null);
                            break;
                        case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                            FuLog.d(TAG, "MEDIA_INFO_NOT_SEEKABLE:");
                            submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_NOT_SEEK_ABLE, null);
                            break;
                        case MediaPlayer.MEDIA_INFO_METADATA_UPDATE:
                            FuLog.d(TAG, "MEDIA_INFO_METADATA_UPDATE:");
                            submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_METADATA_UPDATE, null);
                            break;
                        case MediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE:
                            FuLog.d(TAG, "MEDIA_INFO_UNSUPPORTED_SUBTITLE:");
                            submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_UNSUPPORTED_SUBTITLE, null);
                            break;
                        case MediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT:
                            FuLog.d(TAG, "MEDIA_INFO_SUBTITLE_TIMED_OUT:");
                            submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_SUBTITLE_TIMED_OUT, null);
                            break;
                        case MEDIA_INFO_NETWORK_BANDWIDTH:
                            FuLog.d(TAG, "band_width : " + arg2);
                            mBandWidth = arg2 * 1000;
                            break;
                    }
                    return true;
                }
            };

    private MediaPlayer.OnSeekCompleteListener mOnSeekCompleteListener = new MediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(MediaPlayer mp) {
            FuLog.d(TAG, "EVENT_CODE_SEEK_COMPLETE");
            submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_SEEK_COMPLETE, null);
        }
    };

    private MediaPlayer.OnErrorListener mErrorListener =
            new MediaPlayer.OnErrorListener() {
                public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
                    FuLog.d(TAG, "Error: " + framework_err + "," + impl_err);
                    updateStatus(STATE_ERROR);
                    mTargetState = STATE_ERROR;

                    int eventCode = OnErrorEventListener.ERROR_EVENT_COMMON;

                    switch (framework_err) {
                        case MediaPlayer.MEDIA_ERROR_IO:
                            eventCode = OnErrorEventListener.ERROR_EVENT_IO;
                            break;
                        case MediaPlayer.MEDIA_ERROR_MALFORMED:
                            eventCode = OnErrorEventListener.ERROR_EVENT_MALFORMED;
                            break;
                        case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                            eventCode = OnErrorEventListener.ERROR_EVENT_TIMED_OUT;
                            break;
                        case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                            eventCode = OnErrorEventListener.ERROR_EVENT_UNKNOWN;
                            break;
                        case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                            eventCode = OnErrorEventListener.ERROR_EVENT_UNSUPPORTED;
                            break;
                        case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                            eventCode = OnErrorEventListener.ERROR_EVENT_SERVER_DIED;
                            break;
                        case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                            eventCode = OnErrorEventListener.ERROR_EVENT_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK;
                            break;
                    }

                    /* If an error handler has been supplied, use it and finish. */
                    Bundle bundle = BundlePool.obtain();
                    submitErrorEvent(eventCode, bundle);
                    return true;
                }
            };

    private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener =
            new MediaPlayer.OnBufferingUpdateListener() {
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    submitBufferingUpdate(percent, null);
                }
            };

}
