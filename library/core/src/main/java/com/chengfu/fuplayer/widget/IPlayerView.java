package com.chengfu.fuplayer.widget;

import android.view.Surface;

import com.chengfu.fuplayer.controller.IPlayerController;

public interface IPlayerView {

    enum ScaleType {
        FIT_CENTER(0), FIT_XY(1), CENTER(2), CENTER_CROP(3), CENTER_INSIDE(4);

        ScaleType(int ni) {
            nativeInt = ni;
        }

        final int nativeInt;
    }

    interface SurfaceCallBack {

        void onSurfaceAvailable(Surface surface);

        void onSurfaceDestroyed(Surface surface);
    }

    IPlayerController getPlayerController();

    void setPlayerController(IPlayerController playerController);

    Surface getSurface();

    void setSurfaceCallBack(SurfaceCallBack surfaceCallBack);

}
