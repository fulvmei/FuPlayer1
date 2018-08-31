package com.chengfu.fuplayer.controller;

import com.chengfu.fuplayer.player.IPlayer;

public abstract class BaseController implements IPlayerController {

    private IPlayer mPlayer;

    @Override
    public IPlayer getPlayer() {
        return mPlayer;

    }

    @Override
    public void setPlayer(IPlayer player) {
        mPlayer = player;
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
}
