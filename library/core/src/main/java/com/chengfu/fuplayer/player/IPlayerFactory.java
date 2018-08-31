package com.chengfu.fuplayer.player;

import android.content.Context;

public interface IPlayerFactory<T extends IPlayer, V> {
    T createPlayer(Context context, V option);
}
