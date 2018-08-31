package com.chengfu.fuplayer.demo.qiniu;

import android.graphics.SurfaceTexture;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Surface;
import android.view.TextureView;

import com.chengfu.fuplayer.MediaSource;
import com.chengfu.fuplayer.player.qiniu.QiNiuPlayer;
import com.pili.pldroid.player.PLMediaPlayer;
import com.pili.pldroid.player.PLOnPreparedListener;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private String path = "https://mov.bn.netease.com/open-movie/nos/mp4/2015/08/27/SB13F5AGJ_sd.mp4";
    private TextureView textureView;
    private PLMediaPlayer mediaPlayer;
    private QiNiuPlayer qiNiuPlayer;
    private PlayerListener playerListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textureView = findViewById(R.id.textureView);

//        textureView.setSurfaceTextureListener(playerListener);

        playerListener = new PlayerListener();

//        mediaPlayer = new PLMediaPlayer(getApplicationContext());
//        mediaPlayer.setOnPreparedListener(playerListener);

        qiNiuPlayer = new QiNiuPlayer(getApplicationContext());

        qiNiuPlayer.setMediaSource(new MediaSource(path));
        qiNiuPlayer.setVideoTextureView(textureView);
        qiNiuPlayer.setPlayWhenReady(true);

//        try {
//            mediaPlayer.setDataSource(path);
//            mediaPlayer.prepareAsync();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private final class PlayerListener implements PLOnPreparedListener, TextureView.SurfaceTextureListener {

        @Override
        public void onPrepared(int i) {
            mediaPlayer.start();
        }

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            mediaPlayer.setSurface(new Surface(surfaceTexture));
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    }


}
