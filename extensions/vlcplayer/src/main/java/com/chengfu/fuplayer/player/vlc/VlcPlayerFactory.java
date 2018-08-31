package com.chengfu.fuplayer.player.vlc;

import android.content.Context;

import com.chengfu.fuplayer.player.IPlayerFactory;

public final class VlcPlayerFactory implements IPlayerFactory<VlcPlayer, VlcPlayerOption> {


    @Override
    public VlcPlayer createPlayer(Context context, VlcPlayerOption option) {
        return new VlcPlayer(context, option);
    }
}
