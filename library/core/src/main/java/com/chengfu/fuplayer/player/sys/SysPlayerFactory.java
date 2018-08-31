package com.chengfu.fuplayer.player.sys;

import android.content.Context;

import com.chengfu.fuplayer.player.IPlayerFactory;

public final class SysPlayerFactory implements IPlayerFactory<SysPlayer,SysPlayerOption> {


    @Override
    public SysPlayer createPlayer(Context context, SysPlayerOption option) {
        return new SysPlayer(context,option);
    }
}
