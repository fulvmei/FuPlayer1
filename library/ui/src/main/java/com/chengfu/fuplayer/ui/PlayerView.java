package com.chengfu.fuplayer.ui;

import android.content.Context;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.chengfu.fuplayer.controller.IPlayerController;
import com.chengfu.fuplayer.player.IPlayer;

public class PlayerView extends FrameLayout {

    private IPlayerController controller;

    private IPlayer player;
    private final ComponentListener componentListener=new ComponentListener();

    public PlayerView(@NonNull Context context) {
        this(context, null);
    }

    public PlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (isInEditMode()) {
            setBackgroundResource(R.color.player_view_edit_mode_bg);
            return;
        }

//        initTextureView(context, attrs, defStyleAttr);
    }


    public IPlayerController getController() {
        return controller;
    }

    public void setController(IPlayerController player) {

    }


    public IPlayer getPlayer() {
        return player;
    }

    public void setPlayer(IPlayer player) {
        if (this.player == player) {
            return;
        }
        if (this.player != null) {

        }
        this.player = player;

        if (player != null) {

        }
    }

    private final class ComponentListener {

    }
}
