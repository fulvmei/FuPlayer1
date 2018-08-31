package com.chengfu.fuplayer;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.chengfu.fuplayer.controller.IPlayerController;
import com.chengfu.fuplayer.player.AbsPlayer;
import com.chengfu.fuplayer.player.IPlayer;
import com.chengfu.fuplayer.player.sys.SysPlayerFactory;
import com.chengfu.fuplayer.widget.IPlayerView;

import java.util.Map;


public class FuPlayer implements IPlayer {

    private final static String TAG = "FuPlayer";

    private MediaSource mMediaSource;

    private AbsPlayer mPlayer;
    private IPlayerView mPlayerView;
    private IPlayerController mPlayerController;

    private Surface mSurface;

    private long mCurrentPosition;

//    private TimerCounterProxy mTimerCounterProxy;
//    private OnPlayerEventListener mOnPlayerEventListener;
//    private OnErrorEventListener mOnErrorEventListener;
//    private OnBufferingListener mOnBufferingListener;

    public FuPlayer() {

    }

    public void setPlayer(AbsPlayer player) {
        if (mPlayer == player) {
            return;
        }
        if (mPlayer != null) {
            mCurrentPosition = mPlayer.getCurrentPosition();
            mPlayer.release();
        }
        mPlayer = player;

        initListener();
        if (mMediaSource != null) {
            mPlayer.setMediaSource(mMediaSource);
        }

        mPlayer.setVideoSurface(mSurface);
        mPlayer.seekTo(mCurrentPosition);
        mPlayer.setPlayWhenReady(true);
    }

    public IPlayer getPlayer() {
        return mPlayer;
    }

    public void setPlayerView(IPlayerView playerView) {
        if (mPlayerView == playerView) {
            return;
        }
        if (mPlayerView != null) {
            mPlayerView.setSurfaceCallBack(null);
        }

        if (mPlayer != null) {
            mPlayer.setVideoSurface(playerView.getSurface());
        }

        playerView.setSurfaceCallBack(mSurfaceCallBack);

        playerView.setPlayerController(mPlayerController);

        mPlayerView = playerView;
    }

    public IPlayerView getPlayerView() {
        return mPlayerView;
    }

    public void setPlayerController(IPlayerController playerController) {
        if (mPlayerController == playerController) {
            return;
        }
        if (mPlayerController != null) {
            mPlayerController.setPlayer(null);
        }

        mPlayerController = playerController;

        playerController.setPlayer(this);
        if (mPlayerView != null) {
            mPlayerView.setPlayerController(playerController);
        }
        attachMediaController();
    }

    public IPlayerController getPlayerController() {
        return mPlayerController;
    }

//    private void initPlayer() {
//        mPlayer.setSurface(mPlayerView.getSurface());
//        mPlayerView.setSurfaceCallBack(new IPlayerView.SurfaceCallBack() {
//            @Override
//            public void onSurfaceAvailable(Surface surface) {
//                FuLog.e("HHH", "onSurfaceAvailable");
//                mPlayer.setSurface(surface);
//            }
//
//            @Override
//            public void onSurfaceDestroyed(Surface surface) {
//                FuLog.e("HHH", "onSurfaceDestroyed");
//                mPlayer.setSurface(null);
//            }
//        });
//    }

    /**
     * setting timer proxy state. default open.
     *
     * @param useTimerProxy
     */
    public void setUseTimerProxy(boolean useTimerProxy) {
//        this.mTimerCounterProxy.setUseProxy(useTimerProxy);
    }

    private void attachMediaController() {
        if (mPlayerController != null) {
            mPlayerController.setPlayer(this);
            mPlayerController.setControllerEnabled(true);
        }
    }


    private void initListener() {
//        mTimerCounterProxy.setOnCounterUpdateListener(mOnCounterUpdateListener);
//        if (mPlayer != null) {
//            mPlayer.setOnPlayerEventListener(mInternalPlayerEventListener);
//            mPlayer.setOnErrorEventListener(mInternalErrorEventListener);
//            mPlayer.setOnBufferingListener(mInternalBufferingListener);
//        }
    }

    //destroy some listener
    private void resetListener() {
//        mTimerCounterProxy.setOnCounterUpdateListener(null);
//        if (mPlayer != null) {
//            mPlayer.setOnPlayerEventListener(null);
//            mPlayer.setOnErrorEventListener(null);
//            mPlayer.setOnBufferingListener(null);
//        }
    }

    private IPlayerView.SurfaceCallBack mSurfaceCallBack = new IPlayerView.SurfaceCallBack() {

        @Override
        public void onSurfaceAvailable(Surface surface) {
            if (isPlayerAvailable()) {
                mPlayer.setVideoSurface(surface);
            }
            mSurface = surface;
        }

        @Override
        public void onSurfaceDestroyed(Surface surface) {
            if (isPlayerAvailable()) {
                mPlayer.setVideoSurface(null);
            }
            mSurface = null;
        }
    };

//    private TimerCounterProxy.OnCounterUpdateListener mOnCounterUpdateListener =
//            new TimerCounterProxy.OnCounterUpdateListener() {
//                @Override
//                public void onCounter() {
//                    long curr = getCurrentPosition();
//                    long duration = getDuration();
//                    int bufferPercentage = getBufferPercentage();
//                    //check valid data.
//                    if (duration <= 0 || curr < 0)
//                        return;
//                    Bundle bundle = BundlePool.obtain();
//                    bundle.putLong(EventKey.INT_ARG1, curr);
//                    bundle.putLong(EventKey.INT_ARG2, duration);
//                    bundle.putLong(EventKey.INT_ARG3, bufferPercentage);
//                    callBackPlayEventListener(
//                            OnPlayerEventListener.PLAYER_EVENT_ON_TIMER_UPDATE, bundle);
//                }
//            };

//    private OnPlayerEventListener mInternalPlayerEventListener =
//            new OnPlayerEventListener() {
//                @Override
//                public void onPlayerEvent(int eventCode, Bundle bundle) {
//                    mTimerCounterProxy.proxyPlayEvent(eventCode, bundle);
//                    callBackPlayEventListener(eventCode, bundle);
//                }
//            };

//    private OnErrorEventListener mInternalErrorEventListener =
//            new OnErrorEventListener() {
//                @Override
//                public void onErrorEvent(int eventCode, Bundle bundle) {
//                    mTimerCounterProxy.proxyErrorEvent(eventCode, bundle);
//                    callBackErrorEventListener(eventCode, bundle);
//                }
//            };

//    private OnBufferingListener mInternalBufferingListener =
//            new OnBufferingListener() {
//                @Override
//                public void onBufferingUpdate(int bufferPercentage, Bundle extra) {
//                    if (mOnBufferingListener != null)
//                        mOnBufferingListener.onBufferingUpdate(bufferPercentage, extra);
//                }
//            };

    private boolean isPlayerAvailable() {
        return mPlayer != null;
    }

    //must last callback event listener , because bundle will be recycle after callback.
    private void callBackPlayEventListener(int eventCode, Bundle bundle) {
//        if (mOnPlayerEventListener != null)
//            mOnPlayerEventListener.onPlayerEvent(eventCode, bundle);
    }

    //must last callback event listener , because bundle will be recycle after callback.
    private void callBackErrorEventListener(int eventCode, Bundle bundle) {
//        if (mOnErrorEventListener != null)
//            mOnErrorEventListener.onErrorEvent(eventCode, bundle);
    }

//    @Override
//    public void setOnPlayerEventListener(OnPlayerEventListener onPlayerEventListener) {
//        this.mOnPlayerEventListener = onPlayerEventListener;
//    }
//
//    @Override
//    public void setOnErrorEventListener(OnErrorEventListener onErrorEventListener) {
//        this.mOnErrorEventListener = onErrorEventListener;
//    }
//
//    @Override
//    public void setOnBufferingListener(OnBufferingListener onBufferingListener) {
//        this.mOnBufferingListener = onBufferingListener;
//    }


//    @Override
//    public void option(int code, Bundle bundle) {
//
//    }

//    @Override
//    public void setDataSource(DataSource dataSource) {
//        this.mDataSource = dataSource;
//        //when data source update, attach listener.
//        initListener();
//        mPlayer.setDataSource(dataSource);
//    }

//    @Override
//    public void setDisplay(SurfaceHolder surfaceHolder) {
//        if (isPlayerAvailable())
//            mPlayer.setDisplay(surfaceHolder);
//    }

    @Override
    public VideoComponent getVideoComponent() {
        return null;
    }

    @Override
    public TextComponent getTextComponent() {
        return null;
    }

    @Override
    public void addEventListener(EventListener listener) {

    }

    @Override
    public void removeEventListener(EventListener listener) {

    }

    @Override
    public int getPlayerState() {
        return 0;
    }


    @Override
    public void setPlayWhenReady(boolean playWhenReady) {
        mPlayer.setPlayWhenReady(playWhenReady);
    }

    @Override
    public boolean getPlayWhenReady() {
        return false;
    }

//    @Override
//    public void setSurface(Surface surface) {
//        if (mSurface == surface) {
//            return;
//        }
//        if (isPlayerAvailable()) {
//            mPlayer.setSurface(surface);
//        }
//        mSurface = surface;
//    }

//    @Override
//    public float getVolume() {
//        return mPlayer.getVolume();
//    }

    @Override
    public void setVolume(float volume) {
        if (isPlayerAvailable())
            mPlayer.setVolume(volume);
    }

    @Override
    public float getVolume() {
        return 0;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

//    @Override
//    public void setSpeed(float speed) {
//        if (isPlayerAvailable())
//            mPlayer.setSpeed(speed);
//    }

//    @Override
//    public int getBufferPercentage() {
//        if (isPlayerAvailable())
//            return mPlayer.getBufferPercentage();
//        return 0;
//    }

    @Override
    public boolean isPlaying() {
        if (isPlayerAvailable())
            return mPlayer.isPlaying();
        return false;
    }

    @Override
    public boolean isSeekable() {
        return false;
    }

    @Override
    public long getCurrentPosition() {
        if (isPlayerAvailable())
            return mPlayer.getCurrentPosition();
        return 0;
    }

    @Override
    public long getDuration() {
        if (isPlayerAvailable())
            return mPlayer.getDuration();
        return 0;
    }

    @Override
    public int getAudioSessionId() {
        if (isPlayerAvailable())
            return mPlayer.getAudioSessionId();
        return 0;
    }

    @Override
    public void setMediaSource(MediaSource mediaSource) {
        mMediaSource=mediaSource;
        mPlayer.setMediaSource(mediaSource);
    }

//    @Override
//    public int getVideoWidth() {
//        if (isPlayerAvailable())
//            return mPlayer.getVideoWidth();
//        return 0;
//    }
//
//    @Override
//    public int getVideoHeight() {
//        if (isPlayerAvailable())
//            return mPlayer.getVideoHeight();
//        return 0;
//    }

    @Override
    public void setLooping(boolean looping) {
        if (isPlayerAvailable())
            mPlayer.setLooping(looping);

    }

    @Override
    public boolean isLooping() {
        return false;
    }

//    @Override
//    public int getState() {
//        if (isPlayerAvailable())
//            return mPlayer.getState();
//        return 0;
//    }


//    @Override
//    public void start(int msc) {
//        internalPlayerStart(msc);
//    }


    @Override
    public void resume() {
        mPlayer.resume();
    }

    @Override
    public void stop() {
        mPlayer.stop();
    }

    @Override
    public void release() {
        mPlayer.release();
    }

//    @Override
//    public void resume() {
//        if (isPlayerAvailable())
//            mPlayer.resume();
//    }

    @Override
    public void seekTo(long msc) {
        if (isPlayerAvailable())
            mPlayer.seekTo(msc);
    }


}
