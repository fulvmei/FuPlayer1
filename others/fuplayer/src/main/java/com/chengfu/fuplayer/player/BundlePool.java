package com.chengfu.fuplayer.player;

import android.os.Bundle;

import com.chengfu.fuplayer.FuLog;

import java.util.ArrayList;
import java.util.List;


public class BundlePool {

    private static final int POOL_SIZE = 3;

    private static List<Bundle> mPool;

    static {
        mPool = new ArrayList<>();
        for (int i = 0; i < POOL_SIZE; i++)
            mPool.add(new Bundle());
    }

    public synchronized static Bundle obtain() {
        for (int i = 0; i < POOL_SIZE; i++) {
            if (mPool.get(i).isEmpty()) {
                return mPool.get(i);
            }
        }
        FuLog.w("BundlePool", "<create new bundle object>");
        return new Bundle();
    }

}
