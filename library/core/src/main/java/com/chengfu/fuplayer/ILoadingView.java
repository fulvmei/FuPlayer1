package com.chengfu.fuplayer;

public interface ILoadingView {

    int TYPE_PREPARING = 1;

    int TYPE_BUFFERING = 2;

    boolean isShow();

    void show(int type);

    void hide();
}
