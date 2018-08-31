package com.chengfu.fuplayer.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.chengfu.fuplayer.FuLog;
import com.chengfu.fuplayer.R;
import com.chengfu.fuplayer.controller.IPlayerController;
import com.chengfu.fuplayer.player.IPlayer;

public class PlayerView extends FrameLayout implements IPlayerView {

    public static final String TAG = "PlayerView";

    private VideoTextureView videoTextureView;
    private Surface surface;

    private ScaleType scaleType = ScaleType.FIT_CENTER;

    private SurfaceCallBack surfaceCallBack;

    private IPlayerController playerController;

    private IPlayer player;
    private final ComponentListener componentListener=new ComponentListener();


    public PlayerView(@NonNull Context context) {
        this(context, null);
    }

    public PlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (isInEditMode()) {
            setBackgroundColor(Color.BLACK);
            TextView textView = new TextView(context);
            textView.setTextColor(Color.WHITE);
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(20);
            textView.setText(R.string.app_name);

            addView(textView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

            return;
        }

        initTextureView(context, attrs, defStyleAttr);
    }

    private void initTextureView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        videoTextureView = new VideoTextureView(context, attrs, defStyleAttr);

        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.CENTER);
        videoTextureView.setLayoutParams(lp);
        videoTextureView.setScaleType(scaleType);

        videoTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        addView(videoTextureView);

    }

    public IPlayer getPlayer() {
        return player;
    }

    public void setPlayer(IPlayer player) {
        if (this.player == player) {
            return;
        }
        if (this.player != null) {
            this.player.removeEventListener(componentListener);

        }
        this.player = player;

        if (player != null) {

        }
    }

    @Override
    public IPlayerController getPlayerController() {
        return playerController;
    }

    @Override
    public void setPlayerController(IPlayerController playerController) {
        if (playerController == null || this.playerController == playerController) {
            return;
        }
        this.playerController = playerController;
    }

    @Override
    public Surface getSurface() {
        return surface;
    }

    @Override
    public void setSurfaceCallBack(SurfaceCallBack surfaceCallBack) {
        this.surfaceCallBack = surfaceCallBack;
    }

    private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            FuLog.i(TAG, "onSurfaceTextureAvailable---surfaceTexture=" + surfaceTexture + ",width=" + width + ",height="
                    + height);
            surface = new Surface(surfaceTexture);
            if (surfaceCallBack != null) {
                surfaceCallBack.onSurfaceAvailable(surface);
            }
//            mExoMediaPlayer.setVideoSurface(mSurface);
//            if (mPlayRequested) {
//                mExoMediaPlayer.setPlayWhenReady(mPlayRequested);
//            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
            FuLog.i(TAG, "onSurfaceTextureSizeChanged---surfaceTexture=" + surfaceTexture + ",width=" + width + ",height="
                    + height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            FuLog.i(TAG, "onSurfaceTextureDestroyed---surfaceTexture=" + surfaceTexture);
            surface = null;
//            mExoMediaPlayer.clearVideoSurface();
            surfaceTexture.release();
//            stopPlayback(true);
//            pause();
            if (surfaceCallBack != null) {
                surfaceCallBack.onSurfaceDestroyed(surface);
            }
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
            // Log.i(TAG, "onSurfaceTextureUpdated---surfaceTexture=" +
            // surfaceTexture);
        }

    };

    private final class ComponentListener extends IPlayer.DefaultEventListener {

        @Override
        public void onStateChanged(int state) {

        }
    }
}
