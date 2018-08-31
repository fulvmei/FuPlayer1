//package com.chengfu.fuplayer.player.qiniu;
//
//import android.content.Context;
//import android.net.Uri;
//import android.os.Bundle;
//import android.view.Surface;
//import android.view.SurfaceHolder;
//
//import com.chengfu.fuplayer.FuLog;
//import com.chengfu.fuplayer.bean.DataSource;
//import com.chengfu.fuplayer.player.BaseInternalPlayer;
//import com.chengfu.fuplayer.player.BundlePool;
//import com.chengfu.fuplayer.player.EventKey;
//import com.chengfu.fuplayer.player.OnErrorEventListener;
//import com.chengfu.fuplayer.player.OnPlayerEventListener;
//import com.pili.pldroid.player.PLMediaPlayer;
//import com.pili.pldroid.player.PLOnBufferingUpdateListener;
//import com.pili.pldroid.player.PLOnCompletionListener;
//import com.pili.pldroid.player.PLOnErrorListener;
//import com.pili.pldroid.player.PLOnInfoListener;
//import com.pili.pldroid.player.PLOnPreparedListener;
//import com.pili.pldroid.player.PLOnSeekCompleteListener;
//import com.pili.pldroid.player.PLOnVideoSizeChangedListener;
//
//import java.io.FileDescriptor;
//import java.io.IOException;
//import java.util.HashMap;
//
//public class QiNiuPlayer1 extends BaseInternalPlayer {
//
//    final String TAG = "QiNiuPlayer";
//
//    private Context mContext;
//
//    private PLMediaPlayer mMediaPlayer;
//
//    private int mTargetState;
//
//    private int mVideoWidth;
//
//    private int mVideoHeight;
//
//    private int startSeekPos;
//
//    public QiNiuPlayer1(Context context) {
//        this(context, null);
//    }
//
//    public QiNiuPlayer1(Context context, QiNiuPlayerOption option) {
//        mContext = context;
//        mMediaPlayer = createPlayer();
//    }
//
//    private PLMediaPlayer createPlayer() {
//        PLMediaPlayer plMediaPlayer = new PLMediaPlayer(mContext);
//        return plMediaPlayer;
//    }
//
//    @Override
//    public void setDataSource(DataSource dataSource) {
//        if (mMediaPlayer == null) {
//            mMediaPlayer = createPlayer();
//        } else {
//            stop();
//            reset();
//            resetListener();
//        }
//        mMediaPlayer.setOnPreparedListener(mPreparedListener);
//        mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
//        mMediaPlayer.setOnCompletionListener(mCompletionListener);
//        mMediaPlayer.setOnErrorListener(mErrorListener);
//        mMediaPlayer.setOnInfoListener(mInfoListener);
//        mMediaPlayer.setOnSeekCompleteListener(mOnSeekCompleteListener);
//        mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
//        updateStatus(STATE_INITIALIZED);
//
//        try {
//            String data = dataSource.getData();
//            Uri uri = dataSource.getUri();
//            HashMap<String, String> headers = dataSource.getExtra();
//            FileDescriptor fileDescriptor = dataSource.getFileDescriptor();
//            if (data != null) {
//                if (headers == null)
//                    mMediaPlayer.setDataSource(data);
//                else
//                    mMediaPlayer.setDataSource(data, headers);
//            } else if (uri != null) {
//                if (headers == null)
//                    mMediaPlayer.setDataSource(uri.getPath());
//                else
//                    mMediaPlayer.setDataSource(uri.getPath(), headers);
//            } else {
//                FuLog.e(TAG, "QiNiuPlayer just support String path");
//            }
//
//            mMediaPlayer.setScreenOnWhilePlaying(true);
//            mMediaPlayer.prepareAsync();
//
//            Bundle bundle = BundlePool.obtain();
//            bundle.putSerializable(EventKey.SERIALIZABLE_DATA, dataSource);
//            submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_DATA_SOURCE_SET, bundle);
//        } catch (IOException e) {
//            e.printStackTrace();
//            updateStatus(STATE_ERROR);
//            mTargetState = STATE_ERROR;
//        }
//    }
//
//    @Override
//    public void setDisplay(SurfaceHolder surfaceHolder) {
//        mMediaPlayer.setDisplay(surfaceHolder);
//        submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_SURFACE_HOLDER_UPDATE, null);
//    }
//
//    @Override
//    public void setSurface(Surface surface) {
//        mMediaPlayer.setSurface(surface);
//        submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_SURFACE_UPDATE, null);
//    }
//
//    @Override
//    public void setVolume(float volume) {
//        if (volume < 0.0f) {
//            mCurrentVolume = 0.0f;
//        } else if (volume > 1.0f) {
//            mCurrentVolume = 1.0f;
//        } else {
//            mCurrentVolume = volume;
//        }
//        mMediaPlayer.setVolume(mCurrentVolume, mCurrentVolume);
//    }
//
//    @Override
//    public void setSpeed(float speed) {
//        mMediaPlayer.setPlaySpeed(speed);
//    }
//
//    @Override
//    public boolean isPlaying() {
//        return mMediaPlayer.isPlaying();
//    }
//
//    @Override
//    public long getCurrentPosition() {
//        return mMediaPlayer.getCurrentPosition();
//    }
//
//    @Override
//    public long getDuration() {
//        return mMediaPlayer.getDuration();
//    }
//
//    @Override
//    public int getAudioSessionId() {
//        return mMediaPlayer.getAudioChannels();
//    }
//
//    @Override
//    public int getVideoWidth() {
//        return mMediaPlayer.getVideoWidth();
//    }
//
//    @Override
//    public int getVideoHeight() {
//        return mMediaPlayer.getVideoHeight();
//    }
//
//    @Override
//    public void start() {
//        if (getState() == STATE_PREPARED
//                || getState() == STATE_PAUSED
//                || getState() == STATE_PLAYBACK_COMPLETE) {
//            mMediaPlayer.start();
//            updateStatus(STATE_STARTED);
//            submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_START, null);
//        }
//
//        mTargetState = STATE_STARTED;
//    }
//
//    @Override
//    public void start(int msc) {
//        if (msc > 0) {
//            startSeekPos = msc;
//        }
//        start();
//    }
//
//    @Override
//    public void pause() {
//        mMediaPlayer.pause();
//        updateStatus(STATE_PAUSED);
//        submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_PAUSE, null);
//
//        mTargetState = STATE_PAUSED;
//    }
//
//    @Override
//    public void resume() {
//        if (getState() == STATE_PAUSED) {
//            mMediaPlayer.start();
//            updateStatus(STATE_STARTED);
//            submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_RESUME, null);
//        }
//
//        mTargetState = STATE_STARTED;
//    }
//
//    @Override
//    public void seekTo(int msc) {
//        if (getState() == STATE_PREPARED
//                || getState() == STATE_STARTED
//                || getState() == STATE_PAUSED
//                || getState() == STATE_PLAYBACK_COMPLETE) {
//            mMediaPlayer.seekTo(msc);
//            Bundle bundle = BundlePool.obtain();
//            bundle.putInt(EventKey.INT_DATA, msc);
//            submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_SEEK_TO, bundle);
//        }
//    }
//
//    @Override
//    public void stop() {
//        if (getState() == STATE_PREPARED
//                || getState() == STATE_STARTED
//                || getState() == STATE_PAUSED
//                || getState() == STATE_PLAYBACK_COMPLETE) {
//            mMediaPlayer.stop();
//            updateStatus(STATE_STOPPED);
//            submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_STOP, null);
//        }
//        mTargetState = STATE_STOPPED;
//    }
//
//    @Override
//    public void reset() {
//        mMediaPlayer.stop();
//        updateStatus(STATE_IDLE);
//        submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_RESET, null);
//
//        mTargetState = STATE_IDLE;
//    }
//
//    @Override
//    public void destroy() {
//        updateStatus(STATE_END);
//        resetListener();
//        mMediaPlayer.release();
//        submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_DESTROY, null);
//    }
//
//    private void resetListener() {
//        mMediaPlayer.setOnPreparedListener(null);
//        mMediaPlayer.setOnVideoSizeChangedListener(null);
//        mMediaPlayer.setOnCompletionListener(null);
//        mMediaPlayer.setOnErrorListener(null);
//        mMediaPlayer.setOnInfoListener(null);
//        mMediaPlayer.setOnBufferingUpdateListener(null);
//    }
//
//    PLOnPreparedListener mPreparedListener = new PLOnPreparedListener() {
//        @Override
//        public void onPrepared(int code) {
//            FuLog.d(TAG, "onPrepared...");
//            updateStatus(STATE_PREPARED);
//
//            mVideoWidth = mMediaPlayer.getVideoWidth();
//            mVideoHeight = mMediaPlayer.getVideoHeight();
//
//            Bundle bundle = BundlePool.obtain();
//            bundle.putInt(EventKey.INT_ARG1, mVideoWidth);
//            bundle.putInt(EventKey.INT_ARG2, mVideoHeight);
//
//            submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_PREPARED, bundle);
//
//            int seekToPosition = startSeekPos;  // mSeekWhenPrepared may be changed after seekTo() call
//            if (seekToPosition != 0) {
//                //seek to start position
//                mMediaPlayer.seekTo(seekToPosition);
//                startSeekPos = 0;
//            }
//
//            // We don't know the video size yet, but should start anyway.
//            // The video size might be reported to us later.
//            FuLog.d(TAG, "mTargetState = " + mTargetState);
//            if (mTargetState == STATE_STARTED) {
//                start();
//            } else if (mTargetState == STATE_PAUSED) {
//                pause();
//            } else if (mTargetState == STATE_STOPPED
//                    || mTargetState == STATE_IDLE) {
//                reset();
//            }
//        }
//    };
//
//    PLOnVideoSizeChangedListener mSizeChangedListener =
//            new PLOnVideoSizeChangedListener() {
//                @Override
//                public void onVideoSizeChanged(int width, int height) {
//                    mVideoWidth = mMediaPlayer.getVideoWidth();
//                    mVideoHeight = mMediaPlayer.getVideoHeight();
//                    Bundle bundle = BundlePool.obtain();
//                    bundle.putInt(EventKey.INT_ARG1, mVideoWidth);
//                    bundle.putInt(EventKey.INT_ARG2, mVideoHeight);
//                    submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_VIDEO_SIZE_CHANGE, bundle);
//                }
//            };
//
//    PLOnCompletionListener mCompletionListener =
//            new PLOnCompletionListener() {
//                @Override
//                public void onCompletion() {
//                    updateStatus(STATE_PLAYBACK_COMPLETE);
//                    mTargetState = STATE_PLAYBACK_COMPLETE;
//                    submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_PLAY_COMPLETE, null);
//                }
//            };
//
//    PLOnInfoListener mInfoListener =
//            new PLOnInfoListener() {
//                @Override
//                public void onInfo(int arg1, int arg2) {
//                    switch (arg1) {
//                        case MEDIA_INFO_UNKNOWN:
//                            break;
//                        case MEDIA_INFO_VIDEO_RENDERING_START:
//                            FuLog.d(TAG, "MEDIA_INFO_VIDEO_RENDERING_START");
//                            startSeekPos = 0;
//                            submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_VIDEO_RENDER_START, null);
//                            break;
//                        case MEDIA_INFO_CONNECTED:
//                            break;
//                        case MEDIA_INFO_METADATA:
//                            break;
//                        case MEDIA_INFO_BUFFERING_START:
//                            FuLog.d(TAG, "MEDIA_INFO_BUFFERING_START:" + arg2);
//                            submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_BUFFERING_START, null);
//                            break;
//                        case MEDIA_INFO_BUFFERING_END:
//                            FuLog.d(TAG, "MEDIA_INFO_BUFFERING_END:" + arg2);
//                            submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_BUFFERING_END, null);
//                            break;
//                        case MEDIA_INFO_SWITCHING_SW_DECODE:
//                            break;
//                        case MEDIA_INFO_VIDEO_ROTATION_CHANGED:
//                            break;
//                        case MEDIA_INFO_AUDIO_RENDERING_START:
//                            break;
//                        case MEDIA_INFO_VIDEO_GOP_TIME:
//                            break;
//                        case MEDIA_INFO_VIDEO_FRAME_RENDERING:
//                            break;
//                        case MEDIA_INFO_AUDIO_FRAME_RENDERING:
//                            break;
//                        case MEDIA_INFO_VIDEO_BITRATE:
//                            break;
//                        case MEDIA_INFO_VIDEO_FPS:
//                            break;
//                        case MEDIA_INFO_AUDIO_BITRATE:
//                            break;
//                        case MEDIA_INFO_AUDIO_FPS:
//                            break;
//                        case MEDIA_INFO_LOOP_DONE:
//                            break;
//                        case MEDIA_INFO_CACHED_COMPLETE:
//                            break;
//                        case MEDIA_INFO_IS_SEEKING:
//                            break;
//                    }
//                }
//            };
//
//    PLOnSeekCompleteListener mOnSeekCompleteListener = new PLOnSeekCompleteListener() {
//        @Override
//        public void onSeekComplete() {
//            FuLog.d(TAG, "EVENT_CODE_SEEK_COMPLETE");
//            submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_SEEK_COMPLETE, null);
//        }
//    };
//
//    PLOnErrorListener mErrorListener =
//            new PLOnErrorListener() {
//                @Override
//                public boolean onError(int code) {
//                    FuLog.d(TAG, "code: " + code);
//                    updateStatus(STATE_ERROR);
//                    mTargetState = STATE_ERROR;
//
//                    /* If an error handler has been supplied, use it and finish. */
//                    Bundle bundle = BundlePool.obtain();
//                    submitErrorEvent(OnErrorEventListener.ERROR_EVENT_COMMON, bundle);
//                    return true;
//                }
//            };
//
//    PLOnBufferingUpdateListener mBufferingUpdateListener =
//            new PLOnBufferingUpdateListener() {
//
//                @Override
//                public void onBufferingUpdate(int percent) {
//                    submitBufferingUpdate(percent, null);
//                }
//            };
//
//}
