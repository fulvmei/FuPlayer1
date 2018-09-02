package com.chengfu.fuplayer.ui;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.chengfu.fuplayer.FuPlayerError;
import com.chengfu.fuplayer.player.IPlayer;
import com.chengfu.fuplayer.widget.IPlayerControllerView;

import java.util.Formatter;
import java.util.Locale;

public class DefaultControlView extends RelativeLayout implements IPlayerControllerView, View.OnClickListener, SeekBar.OnSeekBarChangeListener {

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

    //Top
    private LinearLayout llTopContainer;
    private ImageButton imgBtnBack;
    private TextView tvTitle;

    //CENTER
    private LinearLayout lvProgressContainer;
    private LinearLayout lvVolumeContainer;
    private LinearLayout lvBrightnessContainer;


    //Bottom
    private LinearLayout lvBottomContainer;

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
    private boolean mShowProgress = true;
    private boolean mEnabledGesture = true;

    private GestureDetectorCompat mGestureDetector;
    private AdjustType mAdjustType = AdjustType.None;
    private boolean mForward;
    private float mDistanceX;
    private long mCurrentPosition;
    private float mCurrentVolume;
    private boolean mCloseVolume;

    private long mSeekbarPosition;

    private CustomProgress mCustomProgress;

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

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

    }

    @Override
    public void hide() {

    }

    @Override
    public void hideNow() {

    }

    @Override
    public void setControllerEnabled(boolean enabled) {

    }

    @Override
    public boolean isShowing() {
        return false;
    }

    public DefaultControlView(Context context) {
        this(context, null);
    }

    public DefaultControlView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DefaultControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        mContext = context;
        inflateControllerView();

        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
//        mGestureDetector = new GestureDetectorCompat(mContext, mGestureListener);
    }

    protected void inflateControllerView() {
        View.inflate(mContext, R.layout.view_default_controller, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        initView();
    }

    protected void initView() {
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
        lvBottomContainer = findViewById(R.id.default_controller_lvBottomContainer);
        tvCurrentTime = findViewById(R.id.default_controller_tvCurrentTime);
        seekBar = findViewById(R.id.default_controller_seekBar);
        tvEndTime = findViewById(R.id.default_controller_tvEndTime);

        imgBtnBack.setOnClickListener(this);
        mImgBtnPlayPause.setOnClickListener(this);

        seekBar.setMax(1000);
        seekBar.setOnSeekBarChangeListener(this);

    }

    private void updateAll() {
        updatePlayPauseButton();
//        updateNavigation();
//        updateRepeatModeButton();
//        updateShuffleButton();
//        updateProgress();
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

    private void requestPlayPauseFocus() {
        if (mImgBtnPlayPause != null) {
            mImgBtnPlayPause.requestFocus();
        }
    }

    private void updateProgress() {
        if (!isShowing() || !mAttachedToWindow) {
            return;
        }

        long position = 0;
        long bufferedPosition = 0;
        long duration = 0;

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

    private boolean isPlaying() {
        return mPlayer != null
                && mPlayer.isPlaying();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        mGestureDetector.onTouchEvent(event);
//        int action = event.getActionMasked();
//        if (action == MotionEvent.ACTION_DOWN) {
//            if (mPlayer != null) {
//                mCurrentPosition = mPlayer.getCurrentPosition();
//            }
//        }
//        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
//            mAdjustType = AdjustType.None;
////            lvVolumeRoot.setVisibility(View.GONE);
////            lvProgressRoot.setVisibility(View.GONE);
////            lvBrightnessRoot.setVisibility(View.GONE);
//            if (mForward) {
////                int percent = (int) (mDistanceX / getMeasuredWidth() * 1000) * -1 / 4;
////                long duration = mPlayer.getDuration();
////                long newposition = (duration * percent) / 1000 + mCurrentPosition;
////                mPlayer.seekTo(newposition);
////                mDistanceX = 0;
////                mForward = false;
//            }
//        }
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

    private final class ComponentListener implements IPlayer.EventListener {


        @Override
        public void onStateChanged(boolean playWhenReady, int playbackState) {

        }

        @Override
        public void onBufferingUpdate(int percent) {

        }


        @Override
        public void onSeekComplete() {

        }

        @Override
        public void onError(FuPlayerError error) {

        }
    }

    @Override
    public void onClick(View v) {
        if (imgBtnBack == v) {
            if (mOnBackClickListener != null) {
                mOnBackClickListener.onBackClick();
            }
        }
        if (mImgBtnPlayPause == v) {
            if (isPlaying()){
//                mPlayer.pause();
            }else {
//                mPlayer.start();
            }

            updatePlayPauseButton();
        }
    }

}
