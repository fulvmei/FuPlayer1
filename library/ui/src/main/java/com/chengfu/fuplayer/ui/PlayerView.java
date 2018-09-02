package com.chengfu.fuplayer.ui;

import android.content.Context;

import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.chengfu.fuplayer.FuLog;
import com.chengfu.fuplayer.FuPlayerError;
import com.chengfu.fuplayer.player.IPlayer;
import com.chengfu.fuplayer.text.TextOutput;
import com.chengfu.fuplayer.video.VideoListener;
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

    private ImageView artworkView;

    private ComponentListener componentListener;

    private IPlayer player;

    private boolean useArtwork;
    private Bitmap defaultArtwork;

    private int textureViewRotation;

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
        componentListener = new ComponentListener();

        boolean useArtwork = true;
        int defaultArtworkId = 0;
        boolean showBuffering = false;

        int surfaceType = SURFACE_TYPE_SURFACE_VIEW;
        int resizeMode = RESIZE_MODE_FIT;

        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PlayerView, 0, 0);
            try {
                useArtwork = a.getBoolean(R.styleable.PlayerView_use_artwork, useArtwork);
                defaultArtworkId =
                        a.getResourceId(R.styleable.PlayerView_default_artwork, defaultArtworkId);
                surfaceType = a.getInt(R.styleable.PlayerView_surface_type, SURFACE_TYPE_SURFACE_VIEW);
                resizeMode = a.getInt(R.styleable.PlayerView_resize_mode, RESIZE_MODE_FIT);

                showBuffering = a.getBoolean(R.styleable.PlayerView_show_buffering, showBuffering);
            } finally {
                a.recycle();
            }
        }
        LayoutInflater.from(context).inflate(R.layout.default_player_view, this);

        mSurfaceContainer = findViewById(R.id.surface_container);
        mShutterView = findViewById(R.id.view_shutter);
        mShutterView.setBackground(getBackground());

        setSurfaceView(surfaceType);

        setResizeMode(resizeMode);

        // Artwork view.
        artworkView = findViewById(R.id.img_artwork);
        this.useArtwork = useArtwork && artworkView != null;
        if (defaultArtworkId != 0) {
            defaultArtwork = BitmapFactory.decodeResource(context.getResources(), defaultArtworkId);
        }
    }

    public void setSurfaceView(int surfaceType) {
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

        if (player != null && player.getVideoComponent() != null) {
            if (mSurfaceView instanceof TextureView) {
                player.getVideoComponent().setVideoTextureView((TextureView) mSurfaceView);
            } else if (mSurfaceView instanceof SurfaceView) {
                player.getVideoComponent().setVideoSurfaceView((SurfaceView) mSurfaceView);
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

    @Override
    public void setPlayer(IPlayer player) {
        if (this.player == player) {
            return;
        }
        if (this.player != null) {
            this.player.removeEventListener(componentListener);
            IPlayer.VideoComponent oldVideoComponent = this.player.getVideoComponent();
            if (oldVideoComponent != null) {
                oldVideoComponent.removeVideoListener(componentListener);
                if (mSurfaceView instanceof TextureView) {
                    oldVideoComponent.setVideoTextureView(null);
                } else if (mSurfaceView instanceof SurfaceView) {
                    oldVideoComponent.setVideoSurfaceView(null);
                }
            }
            IPlayer.TextComponent oldTextComponent = this.player.getTextComponent();
            if (oldTextComponent != null) {
                oldTextComponent.removeTextOutput(componentListener);
            }
        }
        this.player = player;
//        updateBuffering();
//        updateErrorMessage();
        if (player != null) {
            IPlayer.VideoComponent newVideoComponent = player.getVideoComponent();
            if (newVideoComponent != null) {
                if (mSurfaceView instanceof TextureView) {
                    newVideoComponent.setVideoTextureView((TextureView) mSurfaceView);
                } else if (mSurfaceView instanceof SurfaceView) {
                    newVideoComponent.setVideoSurfaceView((SurfaceView) mSurfaceView);
                }
                newVideoComponent.addVideoListener(componentListener);
            }
            IPlayer.TextComponent newTextComponent = player.getTextComponent();
            if (newTextComponent != null) {
                newTextComponent.addTextOutput(componentListener);
            }
            player.addEventListener(componentListener);
        }
    }

    @Override
    public IPlayer getPlayer() {
        return player;
    }

    public void addStateView(BaseStateView stateView) {
        if (stateView == null) {
            return;
        }
        addView(stateView);
        updateStateViews(stateView);
        mStateViews.add(stateView);
    }

    public void removeStateView(BaseStateView stateView) {
        if (stateView == null) {
            return;
        }
        removeView(stateView);
        mStateViews.remove(stateView);
    }

    private void updateStateViews(BaseStateView stateView) {
        if (player == null || stateView == null) {
            return;
        }
        stateView.onStateChanged(player.getPlayWhenReady(), player.getPlayerState());
    }

//    private void updateBuffering() {
//        if (bufferingView != null) {
//            boolean showBufferingSpinner =
//                    showBuffering
//                            && player != null
//                            && player.getPlayerState() == IPlayer.STATE_BUFFERING
//                            && player.getPlayWhenReady();
//            bufferingView.setVisibility(showBufferingSpinner ? View.VISIBLE : View.GONE);
//        }
//    }

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


    private final class ComponentListener implements IPlayer.EventListener, VideoListener, TextOutput, OnLayoutChangeListener {

        @Override
        public void onStateChanged(boolean playWhenReady, int playbackState) {
//            updateBuffering();
            for (BaseStateView stateView : mStateViews) {
                stateView.onStateChanged(playWhenReady, playbackState);
            }
        }

        @Override
        public void onBufferingUpdate(int percent) {
            for (BaseStateView stateView : mStateViews) {
                stateView.onBufferingUpdate(percent);
            }
        }


        @Override
        public void onSeekComplete() {
            for (BaseStateView stateView : mStateViews) {
                stateView.onSeekComplete();
            }
        }

        @Override
        public void onError(FuPlayerError error) {
            for (BaseStateView stateView : mStateViews) {
                stateView.onError(error);
            }
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
                if (textureViewRotation != 0) {
                    mSurfaceView.removeOnLayoutChangeListener(this);
                }
                textureViewRotation = unappliedRotationDegrees;
                if (textureViewRotation != 0) {
                    // The texture view's dimensions might be changed after layout step.
                    // So add an OnLayoutChangeListener to apply rotation after layout step.
                    mSurfaceView.addOnLayoutChangeListener(this);
                }
                applyTextureViewRotation((TextureView) mSurfaceView, textureViewRotation);
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
            applyTextureViewRotation((TextureView) v, textureViewRotation);
        }
    }
}
