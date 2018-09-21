package com.chengfu.fuplayer.demo.exo.render;

import android.content.Context;
import android.os.Handler;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import java.util.ArrayList;

public class MyRenderersFactory extends DefaultRenderersFactory {
    public MyRenderersFactory(Context context) {
        super(context);
    }

    public MyRenderersFactory(Context context, DrmSessionManager<FrameworkMediaCrypto> drmSessionManager) {
        super(context, drmSessionManager);
    }

    public MyRenderersFactory(Context context, DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, int extensionRendererMode) {
        super(context, drmSessionManager, extensionRendererMode);
    }

    public MyRenderersFactory(Context context, DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, int extensionRendererMode, long allowedVideoJoiningTimeMs) {
        super(context, drmSessionManager, extensionRendererMode, allowedVideoJoiningTimeMs);
    }

    @Override
    protected void buildVideoRenderers(Context context, DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, long allowedVideoJoiningTimeMs, Handler eventHandler, VideoRendererEventListener eventListener, int extensionRendererMode, ArrayList<Renderer> out) {
//        super.buildVideoRenderers(context, drmSessionManager, allowedVideoJoiningTimeMs, eventHandler, eventListener, extensionRendererMode, out);
        out.add(new MyVideoRenderer());
    }
}
