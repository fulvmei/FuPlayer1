package com.chengfu.fuplayer.demo.exo.render;

import com.google.android.exoplayer2.BaseRenderer;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.util.MimeTypes;

public class MyVideoRenderer extends BaseRenderer {

    public MyVideoRenderer() {
        super(C.TRACK_TYPE_VIDEO);
    }

    @Override
    public void render(long l, long l1) throws ExoPlaybackException {
        System.out.println("render");
    }

    @Override
    public boolean isReady() {
        System.out.println("isReady");
        return false;
    }

    @Override
    public boolean isEnded() {
        System.out.println("isEnded");
        return false;
    }

    @Override
    public int supportsFormat(Format format) throws ExoPlaybackException {
        System.out.println("supportsFormat");
        return FORMAT_HANDLED | ADAPTIVE_SEAMLESS;
    }
}
