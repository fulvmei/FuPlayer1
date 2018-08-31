package com.chengfu.fuplayer;

import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.chengfu.fuplayer.bean.DataSource;
import com.chengfu.fuplayer.controller.IPlayerController;
import com.chengfu.fuplayer.player.BaseInternalPlayer;
import com.chengfu.fuplayer.player.BundlePool;
import com.chengfu.fuplayer.player.EventKey;
import com.chengfu.fuplayer.player.IPlayer;
import com.chengfu.fuplayer.player.OnBufferingListener;
import com.chengfu.fuplayer.player.OnErrorEventListener;
import com.chengfu.fuplayer.player.OnPlayerEventListener;
import com.chengfu.fuplayer.player.TimerCounterProxy;
import com.chengfu.fuplayer.player.sys.SysPlayerFactory;
import com.chengfu.fuplayer.widget.IPlayerView;

public class FuPlayer implements IPlayer {

    private final static String TAG = "FuPlayer";

    static final BaseInternalPlayer DEFAULT_PLAYER = new SysPlayerFactory().createPlayer(null, null);
    static final IPlayerView DEFAULT_PLAYER_VIEW = null;

    private IPlayer mPlayer;

    private Surface surface;
    private IPlayerView mPlayerView;
    private IPlayerController mPlayerController;

    private DataSource mDataSource;
    private long mCurrentPosition;

    private TimerCounterProxy mTimerCounterProxy;
    private OnPlayerEventListener mOnPlayerEventListener;
    private OnErrorEventListener mOnErrorEventListener;
    private OnBufferingListener mOnBufferingListener;

    public FuPlayer() {
        this(new Builder());
    }

    FuPlayer(Builder builder) {
        mTimerCounterProxy = new TimerCounterProxy(1000);

//        mPlayer = builder.mPlayer;
//        mPlayerView = builder.mPlayerView;

//        initPlayer();
    }

    public void setPlayer(IPlayer player) {
        if (mPlayer == player) {
            return;
        }
        if (mPlayer != null) {
            mCurrentPosition = mPlayer.getCurrentPosition();
            mPlayer.destroy();
        }
        mPlayer = player;

        initListener();
        if(mDataSource!=null){
            mPlayer.setDataSource(mDataSource);
        }

        mPlayer.setSurface(surface);
        mPlayer.seekTo((int) mCurrentPosition);
        mPlayer.start();
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

        setSurface(playerView.getSurface());

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
        this.mTimerCounterProxy.setUseProxy(useTimerProxy);
    }

    private void attachMediaController() {
        if (mPlayerController != null) {
            mPlayerController.setPlayer(this);
            mPlayerController.setControllerEnabled(true);
        }
    }


    private void initListener() {
        mTimerCounterProxy.setOnCounterUpdateListener(mOnCounterUpdateListener);
        if (mPlayer != null) {
            mPlayer.setOnPlayerEventListener(mInternalPlayerEventListener);
            mPlayer.setOnErrorEventListener(mInternalErrorEventListener);
            mPlayer.setOnBufferingListener(mInternalBufferingListener);
        }
    }

    //destroy some listener
    private void resetListener() {
        mTimerCounterProxy.setOnCounterUpdateListener(null);
        if (mPlayer != null) {
            mPlayer.setOnPlayerEventListener(null);
            mPlayer.setOnErrorEventListener(null);
            mPlayer.setOnBufferingListener(null);
        }
    }

    private IPlayerView.SurfaceCallBack mSurfaceCallBack = new IPlayerView.SurfaceCallBack() {

        @Override
        public void onSurfaceAvailable(Surface surface) {
            setSurface(surface);
        }

        @Override
        public void onSurfaceDestroyed(Surface surface) {
            setSurface(null);
        }
    };

    private TimerCounterProxy.OnCounterUpdateListener mOnCounterUpdateListener =
            new TimerCounterProxy.OnCounterUpdateListener() {
                @Override
                public void onCounter() {
                    long curr = getCurrentPosition();
                    long duration = getDuration();
                    int bufferPercentage = getBufferPercentage();
                    //check valid data.
                    if (duration <= 0 || curr < 0)
                        return;
                    Bundle bundle = BundlePool.obtain();
                    bundle.putLong(EventKey.INT_ARG1, curr);
                    bundle.putLong(EventKey.INT_ARG2, duration);
                    bundle.putLong(EventKey.INT_ARG3, bufferPercentage);
                    callBackPlayEventListener(
                            OnPlayerEventListener.PLAYER_EVENT_ON_TIMER_UPDATE, bundle);
                }
            };

    private OnPlayerEventListener mInternalPlayerEventListener =
            new OnPlayerEventListener() {
                @Override
                public void onPlayerEvent(int eventCode, Bundle bundle) {
                    mTimerCounterProxy.proxyPlayEvent(eventCode, bundle);
                    callBackPlayEventListener(eventCode, bundle);
                }
            };

    private OnErrorEventListener mInternalErrorEventListener =
            new OnErrorEventListener() {
                @Override
                public void onErrorEvent(int eventCode, Bundle bundle) {
                    mTimerCounterProxy.proxyErrorEvent(eventCode, bundle);
                    callBackErrorEventListener(eventCode, bundle);
                }
            };

    private OnBufferingListener mInternalBufferingListener =
            new OnBufferingListener() {
                @Override
                public void onBufferingUpdate(int bufferPercentage, Bundle extra) {
                    if (mOnBufferingListener != null)
                        mOnBufferingListener.onBufferingUpdate(bufferPercentage, extra);
                }
            };

    private boolean isPlayerAvailable() {
        return mPlayer != null;
    }

    //must last callback event listener , because bundle will be recycle after callback.
    private void callBackPlayEventListener(int eventCode, Bundle bundle) {
        if (mOnPlayerEventListener != null)
            mOnPlayerEventListener.onPlayerEvent(eventCode, bundle);
    }

    //must last callback event listener , because bundle will be recycle after callback.
    private void callBackErrorEventListener(int eventCode, Bundle bundle) {
        if (mOnErrorEventListener != null)
            mOnErrorEventListener.onErrorEvent(eventCode, bundle);
    }

    @Override
    public void setOnPlayerEventListener(OnPlayerEventListener onPlayerEventListener) {
        this.mOnPlayerEventListener = onPlayerEventListener;
    }

    @Override
    public void setOnErrorEventListener(OnErrorEventListener onErrorEventListener) {
        this.mOnErrorEventListener = onErrorEventListener;
    }

    @Override
    public void setOnBufferingListener(OnBufferingListener onBufferingListener) {
        this.mOnBufferingListener = onBufferingListener;
    }


    @Override
    public void option(int code, Bundle bundle) {

    }

    @Override
    public void setDataSource(DataSource dataSource) {
        this.mDataSource = dataSource;
        //when data source update, attach listener.
        initListener();
        mPlayer.setDataSource(dataSource);
    }

    @Override
    public void setDisplay(SurfaceHolder surfaceHolder) {
        if (isPlayerAvailable())
            mPlayer.setDisplay(surfaceHolder);
    }

    @Override
    public void setSurface(Surface surface) {
        if (isPlayerAvailable())
            mPlayer.setSurface(surface);
    }

    @Override
    public float getVolume() {
        return mPlayer.getVolume();
    }

    @Override
    public void setVolume(float volume) {
        if (isPlayerAvailable())
            mPlayer.setVolume(volume);
    }

    @Override
    public void setSpeed(float speed) {
        if (isPlayerAvailable())
            mPlayer.setSpeed(speed);
    }

    @Override
    public int getBufferPercentage() {
        if (isPlayerAvailable())
            return mPlayer.getBufferPercentage();
        return 0;
    }

    @Override
    public boolean isPlaying() {
        if (isPlayerAvailable())
            return mPlayer.isPlaying();
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
    public int getVideoWidth() {
        if (isPlayerAvailable())
            return mPlayer.getVideoWidth();
        return 0;
    }

    @Override
    public int getVideoHeight() {
        if (isPlayerAvailable())
            return mPlayer.getVideoHeight();
        return 0;
    }

    @Override
    public int getState() {
        if (isPlayerAvailable())
            return mPlayer.getState();
        return 0;
    }

    @Override
    public void start() {
        internalPlayerStart(0);
    }

    @Override
    public void start(int msc) {
        internalPlayerStart(msc);
    }

    private void internalPlayerStart(int msc) {
        if (isPlayerAvailable())
            mPlayer.start(msc);
    }


    @Override
    public void pause() {
        if (isPlayerAvailable())
            mPlayer.pause();
    }

    @Override
    public void resume() {
        if (isPlayerAvailable())
            mPlayer.resume();
    }

    @Override
    public void seekTo(int msc) {
        if (isPlayerAvailable())
            mPlayer.seekTo(msc);
    }

    @Override
    public void stop() {
        if (isPlayerAvailable())
            mPlayer.stop();
    }

    @Override
    public void reset() {
        if (isPlayerAvailable())
            mPlayer.reset();
    }

    @Override
    public void destroy() {
        if (isPlayerAvailable())
            mPlayer.destroy();
        resetListener();
    }

    public Builder newBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        IPlayer mPlayer;
        IPlayerView mPlayerView;

        public Builder() {
            mPlayer = DEFAULT_PLAYER;
            mPlayerView = DEFAULT_PLAYER_VIEW;
        }

        Builder(FuPlayer fuPlayer) {
            this.mPlayer = fuPlayer.mPlayer;
            this.mPlayerView = fuPlayer.mPlayerView;
        }

        public Builder player(BaseInternalPlayer internalPlayer) {
            this.mPlayer = internalPlayer;
            return this;
        }

        public Builder playerView(IPlayerView playerView) {
            this.mPlayerView = playerView;
            return this;
        }

        public FuPlayer build() {
            return new FuPlayer(this);
        }
    }
}
