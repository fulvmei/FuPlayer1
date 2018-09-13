package com.chengfu.fuplayer.ui;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;

import java.util.concurrent.CopyOnWriteArraySet;

public class DefaultTimeBar extends View implements TimeBar {

    /**
     * Default height for the time bar, in dp.
     */
    public static final int DEFAULT_BAR_HEIGHT_DP = 4;
    /**
     * Default height for the touch target, in dp.
     */
    public static final int DEFAULT_TOUCH_TARGET_HEIGHT_DP = 26;
    /**
     * Default width for ad markers, in dp.
     */
    public static final int DEFAULT_AD_MARKER_WIDTH_DP = 4;
    /**
     * Default diameter for the scrubber when enabled, in dp.
     */
    public static final int DEFAULT_SCRUBBER_ENABLED_SIZE_DP = 12;
    /**
     * Default diameter for the scrubber when disabled, in dp.
     */
    public static final int DEFAULT_SCRUBBER_DISABLED_SIZE_DP = 0;
    /**
     * Default diameter for the scrubber when dragged, in dp.
     */
    public static final int DEFAULT_SCRUBBER_DRAGGED_SIZE_DP = 16;
    /**
     * Default color for the played portion of the time bar.
     */
    public static final int DEFAULT_PLAYED_COLOR = 0xFFFFFFFF;
    /**
     * Default color for ad markers.
     */
    public static final int DEFAULT_AD_MARKER_COLOR = 0xB2FFFF00;

    /**
     * The threshold in dps above the bar at which touch events trigger fine scrub mode.
     */
    private static final int FINE_SCRUB_Y_THRESHOLD_DP = -50;
    /**
     * The ratio by which times are reduced in fine scrub mode.
     */
    private static final int FINE_SCRUB_RATIO = 3;
    /**
     * The time after which the scrubbing listener is notified that scrubbing has stopped after
     * performing an incremental scrub using key input.
     */
    private static final long STOP_SCRUBBING_TIMEOUT_MS = 1000;
    private static final int DEFAULT_INCREMENT_COUNT = 20;


    private Rect seekBounds;
    private Rect progressBar;
    private Rect bufferedBar;
    private Rect scrubberBar;
    private Paint playedPaint;
    private Paint bufferedPaint;
    private Paint unplayedPaint;
    private Paint adMarkerPaint;
    private Paint playedAdMarkerPaint;
    private Paint scrubberPaint;
    private Drawable scrubberDrawable;
    private int barHeight;
    private int touchTargetHeight;
    private int adMarkerWidth;
    private int scrubberEnabledSize;
    private int scrubberDisabledSize;
    private int scrubberDraggedSize;
    private int scrubberPadding;
    private int fineScrubYThreshold;

    private boolean scrubbing;
    private long scrubPosition;
    private long duration;
    private long position;
    private long bufferedPosition;
    private int adGroupCount;
    private long[] adGroupTimesMs;
    private boolean[] playedAdGroups;

    private CopyOnWriteArraySet<OnScrubListener> listeners;

    public DefaultTimeBar(Context context) {
        this(context, null);

    }

    public DefaultTimeBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DefaultTimeBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs, defStyleAttr, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public DefaultTimeBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        seekBounds = new Rect();
        progressBar = new Rect();
        bufferedBar = new Rect();
        scrubberBar = new Rect();
        playedPaint = new Paint();
        bufferedPaint = new Paint();
        unplayedPaint = new Paint();
        adMarkerPaint = new Paint();
        playedAdMarkerPaint = new Paint();
        scrubberPaint = new Paint();


        // Calculate the dimensions and paints for drawn elements.
        Resources res = context.getResources();
        DisplayMetrics displayMetrics = res.getDisplayMetrics();
        fineScrubYThreshold = dpToPx(displayMetrics, FINE_SCRUB_Y_THRESHOLD_DP);
        int defaultBarHeight = dpToPx(displayMetrics, DEFAULT_BAR_HEIGHT_DP);
        int defaultTouchTargetHeight = dpToPx(displayMetrics, DEFAULT_TOUCH_TARGET_HEIGHT_DP);
        int defaultAdMarkerWidth = dpToPx(displayMetrics, DEFAULT_AD_MARKER_WIDTH_DP);
        int defaultScrubberEnabledSize = dpToPx(displayMetrics, DEFAULT_SCRUBBER_ENABLED_SIZE_DP);
        int defaultScrubberDisabledSize = dpToPx(displayMetrics, DEFAULT_SCRUBBER_DISABLED_SIZE_DP);
        int defaultScrubberDraggedSize = dpToPx(displayMetrics, DEFAULT_SCRUBBER_DRAGGED_SIZE_DP);
        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DefaultTimeBar, 0,
                    0);
            try {
                scrubberDrawable = a.getDrawable(R.styleable.DefaultTimeBar_scrubber_drawable);
                if (scrubberDrawable != null) {
//                    setDrawableLayoutDirection(scrubberDrawable);
                    defaultTouchTargetHeight =
                            Math.max(scrubberDrawable.getMinimumHeight(), defaultTouchTargetHeight);
                }
                barHeight = a.getDimensionPixelSize(R.styleable.DefaultTimeBar_bar_height,
                        defaultBarHeight);
                touchTargetHeight = a.getDimensionPixelSize(R.styleable.DefaultTimeBar_touch_target_height,
                        defaultTouchTargetHeight);
                adMarkerWidth = a.getDimensionPixelSize(R.styleable.DefaultTimeBar_ad_marker_width,
                        defaultAdMarkerWidth);
                scrubberEnabledSize = a.getDimensionPixelSize(
                        R.styleable.DefaultTimeBar_scrubber_enabled_size, defaultScrubberEnabledSize);
                scrubberDisabledSize = a.getDimensionPixelSize(
                        R.styleable.DefaultTimeBar_scrubber_disabled_size, defaultScrubberDisabledSize);
                scrubberDraggedSize = a.getDimensionPixelSize(
                        R.styleable.DefaultTimeBar_scrubber_dragged_size, defaultScrubberDraggedSize);
                int playedColor = a.getInt(R.styleable.DefaultTimeBar_played_color, DEFAULT_PLAYED_COLOR);
                int scrubberColor = a.getInt(R.styleable.DefaultTimeBar_scrubber_color,
                        getDefaultScrubberColor(playedColor));
                int bufferedColor = a.getInt(R.styleable.DefaultTimeBar_buffered_color,
                        getDefaultBufferedColor(playedColor));
                int unplayedColor = a.getInt(R.styleable.DefaultTimeBar_unplayed_color,
                        getDefaultUnplayedColor(playedColor));
                int adMarkerColor = a.getInt(R.styleable.DefaultTimeBar_ad_marker_color,
                        DEFAULT_AD_MARKER_COLOR);
//                int playedAdMarkerColor = a.getInt(R.styleable.DefaultTimeBar_played_ad_marker_color,
//                        getDefaultPlayedAdMarkerColor(adMarkerColor));
                playedPaint.setColor(playedColor);
                scrubberPaint.setColor(scrubberColor);
                bufferedPaint.setColor(bufferedColor);
                unplayedPaint.setColor(unplayedColor);
                adMarkerPaint.setColor(adMarkerColor);
//                playedAdMarkerPaint.setColor(playedAdMarkerColor);
            } finally {
                a.recycle();
            }
        } else {
            barHeight = defaultBarHeight;
            touchTargetHeight = defaultTouchTargetHeight;
            adMarkerWidth = defaultAdMarkerWidth;
            scrubberEnabledSize = defaultScrubberEnabledSize;
            scrubberDisabledSize = defaultScrubberDisabledSize;
            scrubberDraggedSize = defaultScrubberDraggedSize;
            playedPaint.setColor(DEFAULT_PLAYED_COLOR);
            scrubberPaint.setColor(getDefaultScrubberColor(DEFAULT_PLAYED_COLOR));
            bufferedPaint.setColor(getDefaultBufferedColor(DEFAULT_PLAYED_COLOR));
            unplayedPaint.setColor(getDefaultUnplayedColor(DEFAULT_PLAYED_COLOR));
            adMarkerPaint.setColor(DEFAULT_AD_MARKER_COLOR);
            scrubberDrawable = null;
        }

        listeners = new CopyOnWriteArraySet<>();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int height = heightMode == MeasureSpec.UNSPECIFIED ? touchTargetHeight
                : heightMode == MeasureSpec.EXACTLY ? heightSize : Math.min(touchTargetHeight, heightSize);
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), height);
//        updateDrawableState();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int width = right - left;
        int height = bottom - top;
        int barY = (height - touchTargetHeight) / 2;
        int seekLeft = getPaddingLeft();
        int seekRight = width - getPaddingRight();
        int progressY = barY + (touchTargetHeight - barHeight) / 2;
        seekBounds.set(seekLeft, barY, seekRight, barY + touchTargetHeight);
        progressBar.set(seekBounds.left + scrubberPadding, progressY,
                seekBounds.right - scrubberPadding, progressY + barHeight);
        update();
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.save();
        drawTimeBar(canvas);
        drawPlayhead(canvas);
        canvas.restore();
    }

    private void drawTimeBar(Canvas canvas) {
        int progressBarHeight = progressBar.height();
        int barTop = progressBar.centerY() - progressBarHeight / 2;
        int barBottom = barTop + progressBarHeight;
        if (duration <= 0) {
            canvas.drawRect(progressBar.left, barTop, progressBar.right, barBottom, unplayedPaint);
            return;
        }
        int bufferedLeft = bufferedBar.left;
        int bufferedRight = bufferedBar.right;
        int progressLeft = Math.max(Math.max(progressBar.left, bufferedRight), scrubberBar.right);
        if (progressLeft < progressBar.right) {
            canvas.drawRect(progressLeft, barTop, progressBar.right, barBottom, unplayedPaint);
        }
        bufferedLeft = Math.max(bufferedLeft, scrubberBar.right);
        if (bufferedRight > bufferedLeft) {
            canvas.drawRect(bufferedLeft, barTop, bufferedRight, barBottom, bufferedPaint);
        }
        if (scrubberBar.width() > 0) {
            canvas.drawRect(scrubberBar.left, barTop, scrubberBar.right, barBottom, playedPaint);
        }
    }

    private void drawPlayhead(Canvas canvas) {
        if (duration <= 0) {
            return;
        }
        int playheadX = Math.max(scrubberBar.left, Math.min(scrubberBar.right, progressBar.right));
        int playheadY = scrubberBar.centerY();
        if (scrubberDrawable == null) {
            int scrubberSize = (scrubbing || isFocused()) ? scrubberDraggedSize
                    : (isEnabled() ? scrubberEnabledSize : scrubberDisabledSize);
            int playheadRadius = scrubberSize / 2;
            canvas.drawCircle(playheadX, playheadY, playheadRadius, scrubberPaint);
        } else {
            int scrubberDrawableWidth = scrubberDrawable.getIntrinsicWidth();
            int scrubberDrawableHeight = scrubberDrawable.getIntrinsicHeight();
            scrubberDrawable.setBounds(
                    playheadX - scrubberDrawableWidth / 2,
                    playheadY - scrubberDrawableHeight / 2,
                    playheadX + scrubberDrawableWidth / 2,
                    playheadY + scrubberDrawableHeight / 2);
            scrubberDrawable.draw(canvas);
        }
    }

    private void update() {
        bufferedBar.set(progressBar);
        scrubberBar.set(progressBar);
        long newScrubberTime = scrubbing ? scrubPosition : position;
        if (duration > 0) {
            int bufferedPixelWidth = (int) ((progressBar.width() * bufferedPosition) / duration);
            bufferedBar.right = Math.min(progressBar.left + bufferedPixelWidth, progressBar.right);
            int scrubberPixelPosition = (int) ((progressBar.width() * newScrubberTime) / duration);
            scrubberBar.right = Math.min(progressBar.left + scrubberPixelPosition, progressBar.right);
        } else {
            bufferedBar.right = progressBar.left;
            scrubberBar.right = progressBar.left;
        }
        invalidate(seekBounds);
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        if (!isEnabled() || duration <= 0) {
//            return false;
//        }
//        Point touchPosition = resolveRelativeTouchPosition(event);
//        int x = touchPosition.x;
//        int y = touchPosition.y;
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                if (isInSeekBar(x, y)) {
//                    positionScrubber(x);
//                    startScrubbing();
//                    scrubPosition = getScrubberPosition();
//                    update();
//                    invalidate();
//                    return true;
//                }
//                break;
//            case MotionEvent.ACTION_MOVE:
//                if (scrubbing) {
//                    if (y < fineScrubYThreshold) {
//                        int relativeX = x - lastCoarseScrubXPosition;
//                        positionScrubber(lastCoarseScrubXPosition + relativeX / FINE_SCRUB_RATIO);
//                    } else {
//                        lastCoarseScrubXPosition = x;
//                        positionScrubber(x);
//                    }
//                    scrubPosition = getScrubberPosition();
//                    for (OnScrubListener listener : listeners) {
//                        listener.onScrubMove(this, scrubPosition);
//                    }
//                    update();
//                    invalidate();
//                    return true;
//                }
//                break;
//            case MotionEvent.ACTION_UP:
//            case MotionEvent.ACTION_CANCEL:
//                if (scrubbing) {
//                    stopScrubbing(event.getAction() == MotionEvent.ACTION_CANCEL);
//                    return true;
//                }
//                break;
//            default:
//                // Do nothing.
//        }
//        return false;
//    }
//
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (isEnabled()) {
//            long positionIncrement = getPositionIncrement();
//            switch (keyCode) {
//                case KeyEvent.KEYCODE_DPAD_LEFT:
//                    positionIncrement = -positionIncrement;
//                    // Fall through.
//                case KeyEvent.KEYCODE_DPAD_RIGHT:
//                    if (scrubIncrementally(positionIncrement)) {
//                        removeCallbacks(stopScrubbingRunnable);
//                        postDelayed(stopScrubbingRunnable, STOP_SCRUBBING_TIMEOUT_MS);
//                        return true;
//                    }
//                    break;
//                case KeyEvent.KEYCODE_DPAD_CENTER:
//                case KeyEvent.KEYCODE_ENTER:
//                    if (scrubbing) {
//                        removeCallbacks(stopScrubbingRunnable);
//                        stopScrubbingRunnable.run();
//                        return true;
//                    }
//                    break;
//                default:
//                    // Do nothing.
//            }
//        }
//        return super.onKeyDown(keyCode, event);
//    }


    @Override
    public void addListener(OnScrubListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(OnScrubListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void setKeyTimeIncrement(long time) {

    }

    @Override
    public void setKeyCountIncrement(int count) {

    }

    @Override
    public void setPosition(long position) {
        this.position = position;
//        setContentDescription(getProgressText());
        update();
    }

    @Override
    public void setBufferedPosition(long bufferedPosition) {
        this.bufferedPosition = bufferedPosition;
        update();
    }

    @Override
    public void setDuration(long duration) {
        this.duration = duration;
//        if (scrubbing && duration == C.TIME_UNSET) {
//            stopScrubbing(true);
//        }
        update();
    }

//    @Override
//    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
//        super.onInitializeAccessibilityEvent(event);
////        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_SELECTED) {
////            event.getText().add(getProgressText());
////        }
////        event.setClassName(DefaultTimeBar.class.getName());
//    }

//    @TargetApi(21)
//    @Override
//    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
//        super.onInitializeAccessibilityNodeInfo(info);
////        info.setClassName(DefaultTimeBar.class.getCanonicalName());
////        info.setContentDescription(getProgressText());
////        if (duration <= 0) {
////            return;
////        }
////        if (Util.SDK_INT >= 21) {
////            info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD);
////            info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD);
////        } else if (Util.SDK_INT >= 16) {
////            info.addAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
////            info.addAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
////        }
//    }

//    private static boolean setDrawableLayoutDirection(Drawable drawable, int layoutDirection) {
//        return Util.SDK_INT >= 23 && drawable.setLayoutDirection(layoutDirection);
//    }

    private static int getDefaultScrubberColor(int playedColor) {
        return 0xFF000000 | playedColor;
    }

    private static int getDefaultUnplayedColor(int playedColor) {
        return 0x33000000 | (playedColor & 0x00FFFFFF);
    }

    private static int getDefaultBufferedColor(int playedColor) {
        return 0xCC000000 | (playedColor & 0x00FFFFFF);
    }

    private static int dpToPx(DisplayMetrics displayMetrics, int dps) {
        return (int) (dps * displayMetrics.density + 0.5f);
    }

}
