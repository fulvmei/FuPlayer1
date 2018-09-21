package com.chengfu.fuplayer.ui;

import android.content.Context;

import android.content.res.TypedArray;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.VideoView;

import com.chengfu.fuplayer.FuLog;
import com.chengfu.fuplayer.player.IPlayer;
import com.chengfu.fuplayer.text.TextOutput;
import com.chengfu.fuplayer.video.VideoListener;
import com.chengfu.fuplayer.widget.IPlayerControllerView;
import com.chengfu.fuplayer.widget.IPlayerView;

import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

public class PlayerView extends FrameLayout implements IPlayerView {

    public static final String TAG = "PlayerView";

    public static final int SURFACE_TYPE_SURFACE_VIEW = 0;
    public static final int SURFACE_TYPE_TEXTURE_VIEW = 1;

    public static final int RESIZE_MODE_FIT = AspectRatioFrameLayout.RESIZE_MODE_FIT;
    public static final int RESIZE_MODE_FIXED_WIDTH = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH;
    public static final int RESIZE_MODE_FIXED_HEIGHT = AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT;
    public static final int RESIZE_MODE_FILL = AspectRatioFrameLayout.RESIZE_MODE_FILL;
    public static final int RESIZE_MODE_ZOOM = AspectRatioFrameLayout.RESIZE_MODE_ZOOM;

    private Context mContext;

    private AspectRatioFrameLayout mSurfaceContainer;
    private View mSurfaceView;
    private View mShutterView;
    private final CopyOnWriteArraySet<BaseStateView> mStateViews = new CopyOnWriteArraySet<>();

    private IPlayer mPlayer;
    private ComponentListener mComponentListener;

    private int mTextureViewRotation;
    private int mSurfaceType = -1;
    private int mResizeMode = -1;

    public PlayerView(@NonNull Context context) {
        this(context, null);
    }

    public PlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (isInEditMode()) {
            setBackgroundResource(R.color.player_view_edit_mode_bg);
            return;
        }
        mContext = context;
        mComponentListener = new ComponentListener();


        int surfaceType = SURFACE_TYPE_SURFACE_VIEW;
        int resizeMode = RESIZE_MODE_FIT;

        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PlayerView, 0, 0);
            try {
                surfaceType = a.getInt(R.styleable.PlayerView_surface_type, SURFACE_TYPE_SURFACE_VIEW);
                resizeMode = a.getInt(R.styleable.PlayerView_resize_mode, RESIZE_MODE_FIT);

            } finally {
                a.recycle();
            }
        }
        LayoutInflater.from(context).inflate(R.layout.default_player_view, this);

        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);

//        setFocusable(true);
//        setFocusableInTouchMode(true);
//        requestFocus();

        mSurfaceContainer = findViewById(R.id.surface_container);
        mShutterView = findViewById(R.id.view_shutter);
        mShutterView.setBackground(getBackground());

        updtatAllViews();

        setSurfaceViewType(surfaceType);

        setResizeMode(resizeMode);

    }

    public void setSurfaceViewType(int surfaceType) {
        if (mSurfaceType == surfaceType
                || (surfaceType != SURFACE_TYPE_SURFACE_VIEW
                && surfaceType != SURFACE_TYPE_TEXTURE_VIEW)) {
            return;
        }
        FuLog.i(TAG, "setSurfaceView : surfaceType=" + surfaceType);
        mSurfaceContainer.removeAllViews();

        mSurfaceType = surfaceType;
        mSurfaceView =
                surfaceType == SURFACE_TYPE_TEXTURE_VIEW
                        ? new TextureView(mContext)
                        : new SurfaceView(mContext);

        mSurfaceContainer.addView(mSurfaceView);

        if (mPlayer != null && mPlayer.getVideoComponent() != null) {
            if (mSurfaceView instanceof TextureView) {
                mPlayer.getVideoComponent().setVideoTextureView((TextureView) mSurfaceView);
            } else if (mSurfaceView instanceof SurfaceView) {
                mPlayer.getVideoComponent().setVideoSurfaceView((SurfaceView) mSurfaceView);
            }
        }
    }

    public int getSurfaceType() {
        return mSurfaceType;
    }

    public void setResizeMode(int resizeMode) {
        if (mResizeMode == resizeMode
                || (resizeMode != RESIZE_MODE_FIT
                && resizeMode != RESIZE_MODE_FIXED_WIDTH
                && resizeMode != RESIZE_MODE_FIXED_HEIGHT
                && resizeMode != RESIZE_MODE_FILL
                && resizeMode != RESIZE_MODE_ZOOM)) {
            return;
        }
        mResizeMode = resizeMode;
        mSurfaceContainer.setResizeMode(resizeMode);
    }

    public int getResizeMode() {
        return mResizeMode;
    }

    private void updtatAllViews() {
        updtatScreenOn();
        updtatShutterView();
        updtatStateViews();
    }

    private void updtatScreenOn() {
        boolean keepScreenOn = false;
        if (mPlayer != null && mPlayer.getPlayerState() != IPlayer.STATE_IDLE
                && mPlayer.getPlayerState() != IPlayer.STATE_ENDED && mPlayer.getPlayWhenReady()) {
            keepScreenOn = true;
        }
        setKeepScreenOn(keepScreenOn);
    }

    private void updtatShutterView() {
        if (mPlayer != null && mPlayer.getVideoComponent() != null) {
            if (mPlayer.getVideoComponent().hasRenderedFirstFrame()) {
                mShutterView.setVisibility(View.GONE);
            } else {
                mShutterView.setVisibility(View.VISIBLE);
            }
        } else {
            mShutterView.setVisibility(View.VISIBLE);
        }
    }

    private void updtatStateViews() {
        for (BaseStateView stateView : mStateViews) {
            stateView.setPlayer(mPlayer);
        }
    }

    @Override
    public void setPlayer(IPlayer player) {
        if (mPlayer == player) {
            return;
        }
        if (mPlayer != null) {
            mPlayer.removeEventListener(mComponentListener);
            IPlayer.VideoComponent oldVideoComponent = mPlayer.getVideoComponent();
            if (oldVideoComponent != null) {
                oldVideoComponent.removeVideoListener(mComponentListener);
                if (mSurfaceView instanceof TextureView) {
                    oldVideoComponent.setVideoTextureView(null);
                } else if (mSurfaceView instanceof SurfaceView) {
                    oldVideoComponent.setVideoSurfaceView(null);
                }
            }
            IPlayer.TextComponent oldTextComponent = mPlayer.getTextComponent();
            if (oldTextComponent != null) {
                oldTextComponent.removeTextOutput(mComponentListener);
            }
        }
        mPlayer = player;

        if (player != null) {
            IPlayer.VideoComponent newVideoComponent = player.getVideoComponent();
            if (newVideoComponent != null) {
                if (mSurfaceView instanceof TextureView) {
                    newVideoComponent.setVideoTextureView((TextureView) mSurfaceView);
                } else if (mSurfaceView instanceof SurfaceView) {
                    newVideoComponent.setVideoSurfaceView((SurfaceView) mSurfaceView);
                }
                newVideoComponent.addVideoListener(mComponentListener);
            }
            IPlayer.TextComponent newTextComponent = player.getTextComponent();
            if (newTextComponent != null) {
                newTextComponent.addTextOutput(mComponentListener);
            }
            player.addEventListener(mComponentListener);
        }
        updtatAllViews();
    }

    @Override
    public IPlayer getPlayer() {
        return mPlayer;
    }

    public void addStateView(BaseStateView stateView) {
        if (stateView == null || mStateViews.contains(stateView)) {
            return;
        }
        addView(stateView);
        mStateViews.add(stateView);
        stateView.setPlayer(mPlayer);
    }

    public void removeStateView(BaseStateView stateView) {
        if (stateView == null || !mStateViews.contains(stateView)) {
            return;
        }
        removeView(stateView);
        mStateViews.remove(stateView);
        stateView.setPlayer(null);
    }

    /**
     * Applies a texture rotation to a {@link TextureView}.
     */
    private static void applyTextureViewRotation(TextureView textureView, int textureViewRotation) {
        float textureViewWidth = textureView.getWidth();
        float textureViewHeight = textureView.getHeight();
        if (textureViewWidth == 0 || textureViewHeight == 0 || textureViewRotation == 0) {
            textureView.setTransform(null);
        } else {
            Matrix transformMatrix = new Matrix();
            float pivotX = textureViewWidth / 2;
            float pivotY = textureViewHeight / 2;
            transformMatrix.postRotate(textureViewRotation, pivotX, pivotY);

            // After rotation, scale the rotated texture to fit the TextureView size.
            RectF originalTextureRect = new RectF(0, 0, textureViewWidth, textureViewHeight);
            RectF rotatedTextureRect = new RectF();
            transformMatrix.mapRect(rotatedTextureRect, originalTextureRect);
            transformMatrix.postScale(
                    textureViewWidth / rotatedTextureRect.width(),
                    textureViewHeight / rotatedTextureRect.height(),
                    pivotX,
                    pivotY);
            textureView.setTransform(transformMatrix);
        }
    }

    private final class ComponentListener extends IPlayer.DefaultEventListener implements VideoListener, TextOutput, OnLayoutChangeListener {

        @Override
        public void onStateChanged(boolean playWhenReady, int playbackState) {
            updtatScreenOn();
        }

        @Override
        public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
            FuLog.d(TAG, "onVideoSizeChanged : width=" + width + ",height=" + height + ",unappliedRotationDegrees=" + unappliedRotationDegrees + ",pixelWidthHeightRatio=" + pixelWidthHeightRatio);
            if (pixelWidthHeightRatio == 0) {
                pixelWidthHeightRatio = 1.0f;
            }
            float videoAspectRatio =
                    (height == 0 || width == 0) ? 1 : (width * pixelWidthHeightRatio) / height;

            if (mSurfaceView instanceof TextureView) {
                // Try to apply rotation transformation when our surface is a TextureView.
                if (unappliedRotationDegrees == 90 || unappliedRotationDegrees == 270) {
                    // We will apply a rotation 90/270 degree to the output texture of the TextureView.
                    // In this case, the output video's width and height will be swapped.
                    videoAspectRatio = 1 / videoAspectRatio;
                }
                if (mTextureViewRotation != 0) {
                    mSurfaceView.removeOnLayoutChangeListener(this);
                }
                mTextureViewRotation = unappliedRotationDegrees;
                if (mTextureViewRotation != 0) {
                    // The texture view's dimensions might be changed after layout step.
                    // So add an OnLayoutChangeListener to apply rotation after layout step.
                    mSurfaceView.addOnLayoutChangeListener(this);
                }
                applyTextureViewRotation((TextureView) mSurfaceView, mTextureViewRotation);
            }
            FuLog.d(TAG, "videoAspectRatio : videoAspectRatio=" + videoAspectRatio);
            mSurfaceContainer.setAspectRatio(videoAspectRatio);
        }

        @Override
        public void onRenderedFirstFrame() {
            if (mShutterView != null) {
                mShutterView.setVisibility(INVISIBLE);
            }
        }

        @Override
        public void onCues(List<String> cues) {

        }

        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            applyTextureViewRotation((TextureView) v, mTextureViewRotation);
        }
    }
}
