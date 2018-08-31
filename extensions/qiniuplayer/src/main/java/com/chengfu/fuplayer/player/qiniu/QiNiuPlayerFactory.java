package com.chengfu.fuplayer.player.qiniu;

import android.content.Context;

import com.chengfu.fuplayer.player.IPlayerFactory;

public final class QiNiuPlayerFactory implements IPlayerFactory<QiNiuPlayer, QiNiuPlayerOption> {

    @Override
    public QiNiuPlayer createPlayer(Context context, QiNiuPlayerOption option) {
        return new QiNiuPlayer(context, option);
    }
}
