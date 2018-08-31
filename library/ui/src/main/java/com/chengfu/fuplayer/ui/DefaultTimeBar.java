package com.chengfu.fuplayer.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class DefaultTimeBar extends View {
    public DefaultTimeBar(Context context) {
        this(context, null);

    }

    public DefaultTimeBar(Context context, @Nullable AttributeSet attrs) {
        this(context, null, 0);
    }

    public DefaultTimeBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
//        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
//        int height = heightMode == MeasureSpec.UNSPECIFIED ? touchTargetHeight
//                : heightMode == MeasureSpec.EXACTLY ? heightSize : Math.min(touchTargetHeight, heightSize);
//        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), height);
//        updateDrawableState();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
//        int width = right - left;
//        int height = bottom - top;
//        int barY = (height - touchTargetHeight) / 2;
//        int seekLeft = getPaddingLeft();
//        int seekRight = width - getPaddingRight();
//        int progressY = barY + (touchTargetHeight - barHeight) / 2;
//        seekBounds.set(seekLeft, barY, seekRight, barY + touchTargetHeight);
//        progressBar.set(seekBounds.left + scrubberPadding, progressY,
//                seekBounds.right - scrubberPadding, progressY + barHeight);
//        update();
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.save();
        drawTimeBar(canvas);
        drawPlayhead(canvas);
        canvas.restore();
    }

    private void drawTimeBar(Canvas canvas) {

    }

    private void drawPlayhead(Canvas canvas) {

    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
//        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_SELECTED) {
//            event.getText().add(getProgressText());
//        }
//        event.setClassName(DefaultTimeBar.class.getName());
    }

    @TargetApi(21)
    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
//        info.setClassName(DefaultTimeBar.class.getCanonicalName());
//        info.setContentDescription(getProgressText());
//        if (duration <= 0) {
//            return;
//        }
//        if (Util.SDK_INT >= 21) {
//            info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD);
//            info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD);
//        } else if (Util.SDK_INT >= 16) {
//            info.addAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
//            info.addAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
//        }
    }

}
