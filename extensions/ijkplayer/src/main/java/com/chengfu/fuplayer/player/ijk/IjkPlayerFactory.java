package com.chengfu.fuplayer.player.ijk;

import android.content.Context;

import com.chengfu.fuplayer.player.IPlayerFactory;

public final class IjkPlayerFactory implements IPlayerFactory<IjkPlayer, IjkPlayerOption> {

    @Override
    public IjkPlayer createPlayer(Context context, IjkPlayerOption option) {
        return new IjkPlayer(context, option);
    }
}
