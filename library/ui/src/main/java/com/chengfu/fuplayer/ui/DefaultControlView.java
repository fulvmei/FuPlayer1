package com.chengfu.fuplayer.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
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

public class DefaultControlView extends RelativeLayout implements IPlayerControllerView, View.OnClickListener {

    public interface OnShowHideChangedListener {
        void onShowHideChanged(boolean show);
    }

    public interface OnBackClickListener {
        void onBackClick();
    }


    private static final int sDefaultTimeout = 4500;
    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;

    private Context mContext;
    private IPlayer mPlayer;

    private final ComponentListener mComponentListener = new ComponentListener();

    private OnShowHideChangedListener mOnShowHideChangedListener;
    private OnBackClickListener mOnBackClickListener;

    private boolean mAttachedToWindow;
    private boolean mShouldShowing;
    private boolean mShowing;

    //Top
    private LinearLayout llTopContainer;
    private ImageButton imgBtnBack;
    private TextView tvTitle;

    //CENTER
    private LinearLayout lvProgressContainer;
    private LinearLayout lvVolumeContainer;
    private LinearLayout lvBrightnessContainer;


    //Bottom
    private LinearLayout llBottomContainer;

    private ImageButton mImgBtnPlayPause;
    private ImageButton imgBtnSwitchScreen;
    private TextView tvBrightness;

    private ImageView imgProgress;
    private TextView tvProgress;

    private ImageView imgVolume;
    private TextView tvVolume;
    private SeekBar seekBar;
    private TextView tvCurrentTime;
    private TextView tvEndTime;
    private boolean mDragging;
    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;
    private boolean isFullScreen;
    private boolean mEnabled = false;
    private boolean mTitleEnabled = false;
    //    private boolean mShowProgress = true;
    private boolean mEnabledGesture = true;

    private GestureDetectorCompat mGestureDetector;
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


    @Override
    public int getShowTimeoutMs() {
        return 0;
    }

    @Override
    public void setShowTimeoutMs(int showTimeoutMs) {

    }

    @Override
    public void show() {
        show(sDefaultTimeout);
    }

    @Override
    public void show(int showTimeoutMs) {
        if (!isInShowState()) {
            return;
        }
        if (!mShowing) {
            updtatProgress();
            mShowing = true;
        }
        updatePlayPauseButton();

        // cause the progress bar to be updated even if mShowing
        // was already true.  This happens, for example, if we're
        // paused with the progress bar showing the user hits play.
        post(mShowProgress);

        llTopContainer.setVisibility(View.VISIBLE);
        mImgBtnPlayPause.setVisibility(View.VISIBLE);
        llBottomContainer.setVisibility(View.VISIBLE);
        mShowing = true;
    }

    @Override
    public void hide() {
        llTopContainer.setVisibility(View.GONE);
        mImgBtnPlayPause.setVisibility(View.GONE);
        llBottomContainer.setVisibility(View.GONE);
        mShowing = false;
    }

    @Override
    public void hideNow() {

    }

    @Override
    public void setControllerEnabled(boolean enabled) {

    }

    @Override
    public boolean isShowing() {
        return true;
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

        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PlayerControlView, 0, 0);
            try {
//                surfaceType = a.getInt(R.styleable.PlayerView_surface_type, SURFACE_TYPE_SURFACE_VIEW);
//                resizeMode = a.getInt(R.styleable.PlayerView_resize_mode, RESIZE_MODE_FIT);

            } finally {
                a.recycle();
            }
        }

        LayoutInflater.from(context).inflate(R.layout.default_controller_view, this);

        //Top
        llTopContainer = findViewById(R.id.default_controller_llTopContainer);
        imgBtnBack = findViewById(R.id.default_controller_imgBtnBack);
        tvTitle = findViewById(R.id.default_controller_tvTitle);

        //CENTER
        mImgBtnPlayPause = findViewById(R.id.default_controller_imgBtnPlayPause);

        lvProgressContainer = findViewById(R.id.default_controller_llTopContainer);
        lvVolumeContainer = findViewById(R.id.default_controller_llTopContainer);
        lvBrightnessContainer = findViewById(R.id.default_controller_llTopContainer);

        //Bottom
        llBottomContainer = findViewById(R.id.default_controller_lvBottomContainer);
        tvCurrentTime = findViewById(R.id.default_controller_tvCurrentTime);
        seekBar = findViewById(R.id.default_controller_seekBar);
        tvEndTime = findViewById(R.id.default_controller_tvEndTime);


        imgBtnBack.setOnClickListener(this);
        mImgBtnPlayPause.setOnClickListener(this);

        seekBar.setMax(1000);
        seekBar.setOnSeekBarChangeListener(mSeekListener);

        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

        hide();
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


    public void setOnShowHideChangedListener(OnShowHideChangedListener listener) {
        mOnShowHideChangedListener = listener;
    }


    public void setOnBackClickListener(OnBackClickListener listener) {
        mOnBackClickListener = listener;
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

    private void updateAll() {
        updatePlayPauseButton();
        updtatProgress();
    }

    private void updatePlayPauseButton() {
        if (!isShowing() || !mAttachedToWindow) {
            return;
        }
        boolean requestPlayPauseFocus = false;
        boolean playing = isPlaying();
        if (mImgBtnPlayPause != null) {
            requestPlayPauseFocus |= mImgBtnPlayPause.isFocused();
            if (playing) {
                mImgBtnPlayPause.setImageResource(R.drawable.ic_default_controller_pause);
            } else {
                mImgBtnPlayPause.setImageResource(R.drawable.ic_default_controller_play);
            }
        }
        if (requestPlayPauseFocus) {
            requestPlayPauseFocus();
        }
    }

    private void updtatProgress() {
        if (mPlayer == null || !isShowing() || !mAttachedToWindow) {
            return ;
        }

        long position = mPlayer.getCurrentPosition();
        long duration = mPlayer.getDuration();
        int bufferedPercent = mPlayer.getBufferPercentage();

        if (seekBar != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                seekBar.setProgress((int) pos);
            }
            seekBar.setSecondaryProgress(bufferedPercent * 10);
        }

//        postDelayed(mUpdateProgressAction, 1000);

        if (tvEndTime != null)
            tvEndTime.setText(stringForTime(duration));
        if (tvCurrentTime != null)
            tvCurrentTime.setText(stringForTime(position));
        removeCallbacks(mShowProgress);
        postDelayed(mShowProgress, 1000);
    }

    private void requestPlayPauseFocus() {
        if (mImgBtnPlayPause != null) {
            mImgBtnPlayPause.requestFocus();
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

    private void toggleController() {
        if (mShowing) {
            hide();
        } else {
            show();
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
//        mGestureDetector.onTouchEvent(event);
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            if (mPlayer != null) {
                mCurrentPosition = mPlayer.getCurrentPosition();
            }
        }
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            toggleController();
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

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        show(sDefaultTimeout);
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        final boolean uniqueDown = event.getRepeatCount() == 0 && event.getAction() == KeyEvent.ACTION_DOWN;
        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                || keyCode == KeyEvent.KEYCODE_SPACE) {
            if (uniqueDown) {
//                doPauseResume();
                show(sDefaultTimeout);
                if (mImgBtnPlayPause != null) {
                    mImgBtnPlayPause.requestFocus();
                }
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
            if (uniqueDown && !mPlayer.isPlaying()) {
//                mPlayer.start();
//                updatePausePlay();
                show(sDefaultTimeout);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
            if (uniqueDown && mPlayer.isPlaying()) {
//                mPlayer.pause();
//                updatePausePlay();
                show(sDefaultTimeout);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP
                || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE || keyCode == KeyEvent.KEYCODE_CAMERA) {
            // don't show the controls for volume adjustment
            return super.dispatchKeyEvent(event);
        } else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
            if (uniqueDown) {
                hide();
            }
            return true;
        }

        show(sDefaultTimeout);
        return super.dispatchKeyEvent(event);
    }


    @Override
    public void onClick(View v) {
        if (mPlayer == null) {
            return;
        }
        if (imgBtnBack == v) {
            if (mOnBackClickListener != null) {
                mOnBackClickListener.onBackClick();
            }
        }
        if (mImgBtnPlayPause == v) {
            if (mPlayer.getPlayWhenReady()) {
                mPlayer.setPlayWhenReady(false);
            } else {
                mPlayer.setPlayWhenReady(true);
            }
            updatePlayPauseButton();
        }
    }

    private final SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onStartTrackingTouch(SeekBar bar) {
            show(3600000);

            mDragging = true;

            // By removing these pending progress messages we make sure
            // that a) we won't update the progress while the user adjusts
            // the seekbar and b) once the user is done dragging the thumb
            // we will post one of these messages to the queue again and
            // this ensures that there will be exactly one message queued up.
            removeCallbacks(mShowProgress);
        }

        @Override
        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (!fromuser) {
                // We're not interested in programmatically generated changes to
                // the progress bar's position.
                return;
            }

            long duration = mPlayer.getDuration();
            long newposition = (duration * progress) / 1000L;
            mPlayer.seekTo(newposition);
            if (tvCurrentTime != null)
                tvCurrentTime.setText(stringForTime(newposition));
        }

        @Override
        public void onStopTrackingTouch(SeekBar bar) {
            mDragging = false;
            updtatProgress();
            updatePlayPauseButton();
            show(sDefaultTimeout);

            // Ensure that progress is properly updated in the future,
            // the call to show() does not guarantee this because it is a
            // no-op if we are already showing.
            post(mShowProgress);
        }
    };

    private final class ComponentListener extends IPlayer.DefaultEventListener {


        @Override
        public void onStateChanged(boolean playWhenReady, int playbackState) {
            updatePlayPauseButton();
            updtatProgress();
        }

        @Override
        public void onBufferingUpdate(int percent) {
            updtatProgress();
        }
    }

    private final class ControlGestureListener extends GestureDetector.SimpleOnGestureListener {

    }

}
