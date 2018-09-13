package com.chengfu.fuplayer.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.SystemClock;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.chengfu.fuplayer.player.IPlayer;
import com.chengfu.fuplayer.widget.IPlayerControllerView;

import java.util.Formatter;
import java.util.Locale;

public class DefaultControlView extends FrameLayout implements IPlayerControllerView, View.OnClickListener {

    /**
     * The default fast forward increment, in milliseconds.
     */
    public static final int DEFAULT_FAST_FORWARD_MS = 15000;
    /**
     * The default rewind increment, in milliseconds.
     */
    public static final int DEFAULT_REWIND_MS = 5000;
    /**
     * The default show timeout, in milliseconds.
     */
    public static final int DEFAULT_SHOW_TIMEOUT_MS = 5000;
    /**
     * The default repeat toggle modes.
     */

    private static final int sDefaultTimeout = 4500;
    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;

    private final Context mContext;
    private IPlayer mPlayer;
    private final ComponentListener mComponentListener;

    private GestureDetectorCompat mGestureDetector;
    private final ControlGestureListener mGestureListener;

    private View mContainerView;
    private View mPlayView;
    private View mPauseView;
    private View mRewindView;
    private View mFastForwardView;
    private View mDurationView;
    private View mPositionView;
    private SeekBar mSeekView;


    private int mRewindMs;
    private int mFastForwardMs;
    private int mShowTimeoutMs;


    private boolean mAttachedToWindow;
    private boolean mShowing;
    private boolean mAniming;
    private boolean mAnimEnabled;

    private long mHideAtMs;


    private boolean mTracking;
    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;
    private boolean isFullScreen;
    private boolean mEnabled = false;
    private boolean mTitleEnabled = false;
    //    private boolean mShowProgress = true;
    private boolean mEnabledGesture = true;

    private AdjustType mAdjustType = AdjustType.None;
    private boolean mForward;
    private float mDistanceX;
    private long mCurrentPosition;
    private float mCurrentVolume;
    private boolean mCloseVolume;

    private long mSeekbarPosition;

    private boolean mShowInBuffering;
    private boolean mShowInEnded;

    private CustomProgress mCustomProgress;

    private final Runnable mFadeOut = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    private final Runnable mShowProgress = new Runnable() {
        @Override
        public void run() {
            updtatProgress();
        }
    };

    private static enum AdjustType {
        None, Volume, Brightness, FastBackwardOrForward,
    }

    public DefaultControlView(Context context) {
        this(context, null);
    }

    public DefaultControlView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DefaultControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mContext = context;
        mComponentListener = new ComponentListener();

        int controllerLayoutId = R.layout.default_controller_view;
        mRewindMs = DEFAULT_REWIND_MS;
        mFastForwardMs = DEFAULT_FAST_FORWARD_MS;
        mShowTimeoutMs = DEFAULT_SHOW_TIMEOUT_MS;

        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PlayerControlView, 0, 0);
            try {
                mRewindMs = a.getInt(R.styleable.PlayerControlView_rewind_increment, DEFAULT_REWIND_MS);
                mFastForwardMs =
                        a.getInt(R.styleable.PlayerControlView_fast_forward_increment, DEFAULT_FAST_FORWARD_MS);
                mShowTimeoutMs = a.getInt(R.styleable.PlayerControlView_show_timeout, DEFAULT_SHOW_TIMEOUT_MS);
                controllerLayoutId =
                        a.getResourceId(R.styleable.PlayerControlView_controller_layout_id, controllerLayoutId);

            } finally {
                a.recycle();
            }
        }

        LayoutInflater.from(context).inflate(getLayoutResourcesId(controllerLayoutId), this);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);

        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

        mGestureListener = new ControlGestureListener();
        mGestureDetector = new GestureDetectorCompat(mContext, mGestureListener);

        initView(context, attrs, defStyleAttr);

    }

    protected int getLayoutResourcesId(int id) {
        return id;
    }

    protected void initView(Context context, AttributeSet attrs, int defStyleAttr) {

        mContainerView = findViewById(R.id.controller_container);
        if (mContainerView != null) {
            mContainerView.setVisibility(View.GONE);
        }
        mDurationView = findViewById(R.id.controller_duration);
        mPositionView = findViewById(R.id.controller_position);
        mSeekView = findViewById(R.id.controller_seek);
        if (mSeekView != null) {
            mSeekView.setOnSeekBarChangeListener(mSeekListener);
        }
        mPlayView = findViewById(R.id.controller_play);
        if (mPlayView != null) {
            mPlayView.setOnClickListener(this);
        }
        mPauseView = findViewById(R.id.controller_pause);
        if (mPauseView != null) {
            mPauseView.setOnClickListener(this);
        }
        mRewindView = findViewById(R.id.controller_rewind);
        if (mRewindView != null) {
            mRewindView.setOnClickListener(this);
        }
        mFastForwardView = findViewById(R.id.controller_fast_forward);
        if (mFastForwardView != null) {
            mFastForwardView.setOnClickListener(this);
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        mAttachedToWindow = true;
//        if (hideAtMs != C.TIME_UNSET) {
//            long delayMs = hideAtMs - SystemClock.uptimeMillis();
//            if (delayMs <= 0) {
//                hide();
//            } else {
//                postDelayed(hideAction, delayMs);
//            }
//        } else if (isVisible()) {
//            hideAfterTimeout();
//        }
        updateAll();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mAttachedToWindow = false;
//        removeCallbacks(updateProgressAction);
//        removeCallbacks(hideAction);
    }

    @Override
    public IPlayer getPlayer() {
        return mPlayer;
    }

    @Override
    public void setPlayer(IPlayer player) {
        if (mPlayer == player) {
            return;
        }
        if (mPlayer != null) {
            mPlayer.removeEventListener(mComponentListener);
        }
        mPlayer = player;
        if (player != null) {
            player.addEventListener(mComponentListener);
        }
        updateAll();
    }

    @Override
    public int getShowTimeoutMs() {
        return 0;
    }

    @Override
    public void setShowTimeoutMs(int showTimeoutMs) {

    }

    @Override
    public boolean isShowing() {
        return mShowing;
    }

    @Override
    public void show() {
        if (!isInShowState()) {
            return;
        }
        if (!mShowing) {
            mContainerView.setVisibility(View.VISIBLE);
            mShowing = true;
            updateAll();
        }

        hideAfterTimeout();
    }

    @Override
    public void hide() {
        if (mShowing) {
            mShowing = false;
            mContainerView.setVisibility(View.GONE);
            removeCallbacks(mShowProgress);
            removeCallbacks(mFadeOut);
            mHideAtMs = -1;
        }
    }

    public boolean getAnimEnabled() {
        return mAnimEnabled;
    }


    public void setAnimEnabled(boolean animEnabled) {
        mAnimEnabled = animEnabled;
    }

    public boolean getTitleEnabled() {
        return mTitleEnabled;
    }


    public void setTitleEnabled(boolean titleEnabled) {
        if (mShowing) {
            if (titleEnabled) {
                showTitle(false);
            } else {
                hideTitle(false);
            }
        }
        mTitleEnabled = titleEnabled;
    }


    private void showTitle(boolean anim) {
//        mLlTopContainer.setVisibility(View.VISIBLE);
//        if (anim) {
//            mLlTopContainer.clearAnimation();
//            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.option_entry_from_top);
//            animation.setAnimationListener(new ControlAnimationListener(false, null));
//            mLlTopContainer.startAnimation(animation);
//        }
    }

    private void showBottom(boolean anim) {
//        mLlBottomContainer.setVisibility(View.VISIBLE);
//        if (anim) {
//            mLlBottomContainer.clearAnimation();
//            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.option_entry_from_bottom);
//            animation.setAnimationListener(new ControlAnimationListener(false, null));
//            mLlBottomContainer.startAnimation(animation);
//        }
    }

    protected void showOther(boolean anim) {

    }


    private void hide(boolean anim) {
        if (mShowing) {
            mShowing = false;
            if (mTitleEnabled) {
                hideTitle(anim);
            }
            hideBottom(anim);
            removeCallbacks(mShowProgress);
            removeCallbacks(mFadeOut);
            mHideAtMs = -1;
        }
    }

    private void hideTitle(boolean anim) {
//        mLlTopContainer.clearAnimation();
//        if (!anim) {
//            mLlTopContainer.setVisibility(View.GONE);
//            return;
//        } else {
//            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.option_leave_from_top);
//            animation.setAnimationListener(new ControlAnimationListener(true, mLlTopContainer));
//            mLlTopContainer.startAnimation(animation);
//        }
    }

    private void hideBottom(boolean anim) {
//        mLlBottomContainer.clearAnimation();
//        if (!anim) {
//            mLlBottomContainer.setVisibility(View.GONE);
//        } else {
//            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.option_leave_from_bottom);
//            animation.setAnimationListener(new ControlAnimationListener(true, mLlBottomContainer));
//            mLlBottomContainer.startAnimation(animation);
//        }
    }

    protected void hideOther(boolean anim) {

    }

    private boolean isInShowState() {
        if (mPlayer == null || !mAttachedToWindow) {
            return false;
        }
        switch (mPlayer.getPlayerState()) {
            case IPlayer.STATE_BUFFERING:
                if (!mShowInBuffering) {
                    return false;
                }
            case IPlayer.STATE_ENDED:
                if (!mShowInEnded) {
                    return false;
                }
            case IPlayer.STATE_READY:
                return true;
            default:
                return false;
        }
    }

    private void hideAfterTimeout() {
        removeCallbacks(mFadeOut);
        if (mShowTimeoutMs > 0) {
            mHideAtMs = SystemClock.uptimeMillis() + mShowTimeoutMs;
            if (mAttachedToWindow) {
                postDelayed(mFadeOut, mShowTimeoutMs);
            }
        } else {
            mHideAtMs = -1;
        }
    }

    private void updateAll() {
        if (!isInShowState()) {
            hide();
            return;
        }

        updatePlayPauseButton();
        updtatProgress();
    }

    private void updatePlayPauseButton() {
        if (!isShowing()) {
            return;
        }
        boolean requestPlayPauseFocus = false;
        boolean playing = isPlaying();
//        if (mImgBtnPlayPause != null) {
//            requestPlayPauseFocus |= mImgBtnPlayPause.isFocused();
//            if (playing) {
//                mImgBtnPlayPause.setImageResource(R.drawable.ic_default_controller_pause);
//            } else {
//                mImgBtnPlayPause.setImageResource(R.drawable.ic_default_controller_play);
//            }
//            if (requestPlayPauseFocus) {
//                mImgBtnPlayPause.requestFocus();
//            }
//        }
    }

    private void updtatProgress() {
        if (!isShowing()) {
            return;
        }

        long position = mPlayer.getCurrentPosition();
        long duration = mPlayer.getDuration();
        int bufferedPercent = mPlayer.getBufferPercentage();

//        if (mSeekBar != null) {
//            if (duration > 0) {
//                // use long to avoid overflow
//                long pos = 1000L * position / duration;
//                mSeekBar.setProgress((int) pos);
//            }
//            mSeekBar.setSecondaryProgress(bufferedPercent * 10);
//        }
//
//        if (mTvEndTime != null)
//            mTvEndTime.setText(stringForTime(duration));
//        if (mTvCurrentTime != null)
//            mTvCurrentTime.setText(stringForTime(position));
//        removeCallbacks(mShowProgress);
//        postDelayed(mShowProgress, 1000);
    }

    private String stringForTime(long timeMs) {
        long totalSeconds = timeMs / 1000;
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private boolean isPlaying() {
        return mPlayer != null
                && mPlayer.getPlayerState() != IPlayer.STATE_ENDED
                && mPlayer.getPlayerState() != IPlayer.STATE_IDLE
                && mPlayer.getPlayWhenReady();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            if (mPlayer != null) {
                mCurrentPosition = mPlayer.getCurrentPosition();
            }
        }
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            mAdjustType = AdjustType.None;
//            lvVolumeRoot.setVisibility(View.GONE);
//            lvProgressRoot.setVisibility(View.GONE);
//            lvBrightnessRoot.setVisibility(View.GONE);
            if (mForward) {
//                int percent = (int) (mDistanceX / getMeasuredWidth() * 1000) * -1 / 4;
//                long duration = mPlayer.getDuration();
//                long newposition = (duration * percent) / 1000 + mCurrentPosition;
//                mPlayer.seekTo(newposition);
//                mDistanceX = 0;
//                mForward = false;
            }
        }
        return true;
    }

//    @Override
//    public boolean onTrackballEvent(MotionEvent ev) {
//        show(sDefaultTimeout);
//        return false;
//    }

//    @Override
//    public boolean dispatchKeyEvent(KeyEvent event) {
//        int keyCode = event.getKeyCode();
//        final boolean uniqueDown = event.getRepeatCount() == 0 && event.getAction() == KeyEvent.ACTION_DOWN;
//        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
//                || keyCode == KeyEvent.KEYCODE_SPACE) {
//            if (uniqueDown) {
////                doPauseResume();
//                show(sDefaultTimeout);
//                if (mImgBtnPlayPause != null) {
//                    mImgBtnPlayPause.requestFocus();
//                }
//            }
//            return true;
//        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
//            if (uniqueDown && !mPlayer.isPlaying()) {
////                mPlayer.start();
////                updatePausePlay();
//                show(sDefaultTimeout);
//            }
//            return true;
//        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
//            if (uniqueDown && mPlayer.isPlaying()) {
////                mPlayer.pause();
////                updatePausePlay();
//                show(sDefaultTimeout);
//            }
//            return true;
//        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP
//                || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE || keyCode == KeyEvent.KEYCODE_CAMERA) {
//            // don't show the controls for volume adjustment
//            return super.dispatchKeyEvent(event);
//        } else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
//            if (uniqueDown) {
//                hide();
//            }
//            return true;
//        }
//
//        show(sDefaultTimeout);
//        return super.dispatchKeyEvent(event);
//    }


    @Override
    public void onClick(View v) {
        if (mPlayer == null) {
            return;
        }
//        if (mImgBtnBack == v) {
//
//        }
//        if (mImgBtnPlayPause == v) {
//            if (mPlayer.getPlayWhenReady()) {
//                mPlayer.setPlayWhenReady(false);
//            } else {
//                mPlayer.setPlayWhenReady(true);
//            }
//            updatePlayPauseButton();
//        }
    }

    private final SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {

        private int progress;

        @Override
        public void onStartTrackingTouch(SeekBar bar) {

            removeCallbacks(mFadeOut);

            mTracking = true;
        }

        @Override
        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            this.progress = progress;
            long duration = mPlayer.getDuration();
            long newposition = (duration * progress) / 1000L;
//            mTvCurrentTime.setText(stringForTime(newposition));
        }

        @Override
        public void onStopTrackingTouch(SeekBar bar) {
            mTracking = false;

            long duration = mPlayer.getDuration();
            long newposition = (duration * progress) / 1000L;

            updtatProgress();
            updatePlayPauseButton();
            hideAfterTimeout();
        }
    };

    private final class ComponentListener extends IPlayer.DefaultEventListener {


        @Override
        public void onStateChanged(boolean playWhenReady, int playbackState) {
            updateAll();
        }

        @Override
        public void onBufferingUpdate(int percent) {
            updtatProgress();
        }
    }

    private final class ControlGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (mAniming) {
                return super.onSingleTapUp(e);
            }
            if (isShowing()) {
                hide();
            } else {
                show();
            }
            return super.onSingleTapUp(e);
        }
    }

}
