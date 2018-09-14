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
import android.widget.VideoView;

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
     * The default progress updata interval, in milliseconds.
     */
    public static final int DEFAULT_PROGRESS_UPDATE_INTERVAL_MS = 1000;
    /**
     * The default seek number.
     */
    public static final int DEFAULT_SEEK_NUMBER = 1000;

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
    private TextView mDurationView;
    private TextView mPositionView;
    private SeekBar mSeekView;

    private int mRewindMs;
    private int mFastForwardMs;
    private int mShowTimeoutMs;
    private int mProgressUpdateIntervalMs;
    private int mSeekNumber;
    private boolean mShowInBuffering;
    private boolean mShowInEnded;

    private boolean mAttachedToWindow;
    private boolean mShowing;
    private long mHideAtMs;
    private boolean mTracking;

    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;


    private final Runnable mHideAction = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    private final Runnable mUpdateProgressAction = new Runnable() {
        @Override
        public void run() {
            updateProgress();
        }
    };

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
        mProgressUpdateIntervalMs = DEFAULT_PROGRESS_UPDATE_INTERVAL_MS;
        mSeekNumber = DEFAULT_SEEK_NUMBER;
        mShowInBuffering = false;
        mShowInEnded = false;


        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PlayerControlView, 0, 0);
            try {
                controllerLayoutId =
                        a.getResourceId(R.styleable.PlayerControlView_controller_layout_id, controllerLayoutId);
                mRewindMs = a.getInt(R.styleable.PlayerControlView_rewind_increment, DEFAULT_REWIND_MS);
                mFastForwardMs =
                        a.getInt(R.styleable.PlayerControlView_fast_forward_increment, DEFAULT_FAST_FORWARD_MS);
                mShowTimeoutMs = a.getInt(R.styleable.PlayerControlView_show_timeout, DEFAULT_SHOW_TIMEOUT_MS);
                mProgressUpdateIntervalMs = a.getInt(R.styleable.PlayerControlView_progress_update_interval, DEFAULT_PROGRESS_UPDATE_INTERVAL_MS);
                mSeekNumber = a.getInt(R.styleable.PlayerControlView_seek_number, DEFAULT_SEEK_NUMBER);
                mShowInBuffering = a.getBoolean(R.styleable.PlayerControlView_show_in_buffering, false);
                mShowInEnded = a.getBoolean(R.styleable.PlayerControlView_show_in_ended, false);
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

    protected int getLayoutResourcesId(int layoutId) {
        return layoutId;
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
            mSeekView.setMax(mSeekNumber);
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
        if (mHideAtMs != -1) {
            long delayMs = mHideAtMs - SystemClock.uptimeMillis();
            if (delayMs <= 0) {
                hide();
            } else {
                postDelayed(mHideAction, delayMs);
            }
        } else if (isShowing()) {
            hideAfterTimeout();
        }
        updateAll();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mAttachedToWindow = false;
        removeCallbacks(mUpdateProgressAction);
        removeCallbacks(mHideAction);
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

    private void updateAll() {
        if (!isInShowState()) {
            hide();
            return;
        }

        updatePlayPauseButton();
        updateNavigation();
        updateProgress();
    }

    private void updatePlayPauseButton() {
        if (!isShowing() || !mAttachedToWindow) {
            return;
        }
        boolean requestPlayPauseFocus = false;
        boolean playing = isPlaying();
        if (mPlayView != null) {
            requestPlayPauseFocus |= playing && mPlayView.isFocused();
            mPlayView.setVisibility(playing ? View.GONE : View.VISIBLE);
        }
        if (mPauseView != null) {
            requestPlayPauseFocus |= !playing && mPauseView.isFocused();
            mPauseView.setVisibility(!playing ? View.GONE : View.VISIBLE);
        }
        if (requestPlayPauseFocus) {
            requestPlayPauseFocus();
        }
    }

    private void updateNavigation() {
        if (!isShowing() || !mAttachedToWindow) {
            return;
        }
        boolean isSeekable = mPlayer != null && mPlayer.isSeekable();
        setViewEnabled(mFastForwardMs > 0 && isSeekable, mFastForwardView);
        setViewEnabled(mRewindMs > 0 && isSeekable, mRewindView);
        if (mSeekView != null) {
            mSeekView.setEnabled(isSeekable);
        }
    }

    private void updateProgress() {
        if (!isShowing() || !mAttachedToWindow) {
            return;
        }
        long position = mPlayer.getCurrentPosition();
        long duration = mPlayer.getDuration();
        int bufferedPercent = mPlayer.getBufferPercentage();

        if (mSeekView != null) {
            if (duration > 0 && !mTracking) {
                // use long to avoid overflow
                long pos = mSeekNumber * position / duration;
                mSeekView.setProgress((int) pos);
            }
            mSeekView.setSecondaryProgress(bufferedPercent * 10);
        }

        if (mDurationView != null)
            mDurationView.setText(stringForTime(duration));
        if (mPositionView != null && !mTracking)
            mPositionView.setText(stringForTime(position));
        removeCallbacks(mUpdateProgressAction);
        postDelayed(mUpdateProgressAction, mProgressUpdateIntervalMs);
    }

    private void requestPlayPauseFocus() {
        boolean playing = isPlaying();
        if (!playing && mPlayView != null) {
            mPlayView.requestFocus();
        } else if (playing && mPauseView != null) {
            mPauseView.requestFocus();
        }
    }

    private void setViewEnabled(boolean enabled, View view) {
        if (view == null) {
            return;
        }
        view.setEnabled(enabled);
        view.setAlpha(enabled ? 1f : 0.3f);
        view.setVisibility(VISIBLE);
    }

    @Override
    public int getShowTimeoutMs() {
        return mShowTimeoutMs;
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
            removeCallbacks(mUpdateProgressAction);
            removeCallbacks(mHideAction);
            mHideAtMs = -1;
        }
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
        removeCallbacks(mHideAction);
        if (mShowTimeoutMs > 0) {
            mHideAtMs = SystemClock.uptimeMillis() + mShowTimeoutMs;
            if (mAttachedToWindow) {
                postDelayed(mHideAction, mShowTimeoutMs);
            }
        } else {
            mHideAtMs = -1;
        }
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
        if (mPlayView == v) {
            mPlayer.setPlayWhenReady(true);
        } else if (mPauseView == v) {
            mPlayer.setPlayWhenReady(false);
        }
        hideAfterTimeout();
    }

    private final SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {

        private int progress;

        @Override
        public void onStartTrackingTouch(SeekBar bar) {
            removeCallbacks(mHideAction);
            mTracking = true;
        }

        @Override
        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (!fromuser) {
                return;
            }
            this.progress = progress;
            if (!isInShowState()) {
                return;
            }
            long duration = mPlayer.getDuration();
            long newPosition = (duration * progress) / mSeekNumber;
            if (mPositionView != null) {
                mPositionView.setText(stringForTime(newPosition));
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar bar) {
            mTracking = false;
            if (!isInShowState()) {
                return;
            }
            long duration = mPlayer.getDuration();
            long newPosition = duration * progress / mSeekNumber;
            mPlayer.seekTo(newPosition);

            hideAfterTimeout();

        }
    };

    private final class ComponentListener extends IPlayer.DefaultEventListener {


        @Override
        public void onStateChanged(boolean playWhenReady, int playbackState) {
            updateAll();
        }

        @Override
        public void onSeekableChanged(boolean seekable) {
            updateNavigation();
        }

        @Override
        public void onBufferingUpdate(int percent) {
            updateProgress();
        }
    }

    private final class ControlGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (isShowing()) {
                hide();
            } else {
                show();
            }
            return super.onSingleTapUp(e);
        }
    }

}
