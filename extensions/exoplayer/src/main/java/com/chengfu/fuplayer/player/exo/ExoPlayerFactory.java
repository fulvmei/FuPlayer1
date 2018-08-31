package com.chengfu.fuplayer.player.exo;

import android.content.Context;

import com.chengfu.fuplayer.player.IPlayerFactory;

public final class ExoPlayerFactory implements IPlayerFactory<ExoPlayer, ExoPlayerOption> {

    @Override
    public ExoPlayer createPlayer(Context context, ExoPlayerOption option) {
        return new ExoPlayer(context, option);
    }
}
