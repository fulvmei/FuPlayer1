package com.chengfu.fuplayer.ui;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public abstract class C extends View {
    public C(Context context) {
        super(context);
    }

    public C(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public C(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
