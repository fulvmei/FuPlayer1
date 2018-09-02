package com.chengfu.fuplayer.demo;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;


import com.chengfu.fuplayer.FuPlayer;
import com.chengfu.fuplayer.MediaSource;
import com.chengfu.fuplayer.demo.bean.Media;
import com.chengfu.fuplayer.demo.immersion.ImmersionBar;
import com.chengfu.fuplayer.demo.immersion.QMUIStatusBarHelper;
import com.chengfu.fuplayer.player.exo.ExoPlayerFactory;
import com.chengfu.fuplayer.player.ijk.IjkPlayerFactory;
import com.chengfu.fuplayer.player.qiniu.QiNiuPlayerFactory;
import com.chengfu.fuplayer.player.sys.SysPlayerFactory;
import com.chengfu.fuplayer.player.vlc.VlcPlayerFactory;
import com.chengfu.fuplayer.ui.AspectRatioFrameLayout;
import com.chengfu.fuplayer.ui.DefaultControlView;
import com.chengfu.fuplayer.ui.DefaultEndedView;
import com.chengfu.fuplayer.ui.DefaultErrorView;
import com.chengfu.fuplayer.ui.DefaultLoadingView;
import com.chengfu.fuplayer.ui.PlayerView;

public class PlayerActivity extends AppCompatActivity {
    private Media media;

    private String[] playerTag = {"系统", "Exo", "Ijk", "七牛", "Vlc"};
    private String[] playerViewTag = {"V1", "V2"};
    private String[] modeTag = {"FIT", "FIXED_WIDTH", "FIXED_HEIGHT", "FILL", "ZOOM"};
    private String[] surfcaeTag = {"SurfaceView", "TextureView"};

    private int currentPlayer = -1;

    private int currentPlayerView = -1;

    private int currentMode = -1;

    private int currentSurface = -1;

    private SharedPreferences sp;

    private PlayerView mPlayerView1;
    private DefaultControlView mControlView1;

    private PlayerView mPlayerView2;
    private DefaultControlView mControlView2;

    private Spinner spinnerPlayer;
    private Spinner spinnerPlayerView;
    private Spinner spinnerMode;
    private Spinner spinnerSurfcae;


    private FuPlayer mFuPlayer;

    private ImmersionBar mImmersionBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImmersionBar = ImmersionBar.with(this);
        mImmersionBar.statusBarColorInt(Color.BLACK).init();
        QMUIStatusBarHelper.setStatusBarDarkMode(this);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        setContentView(R.layout.activity_player);

        mPlayerView1 = findViewById(R.id.playerView1);
        mControlView1 = findViewById(R.id.controlView1);

        mPlayerView2 = findViewById(R.id.playerView2);
        mControlView2 = findViewById(R.id.controlView2);

        spinnerPlayer = findViewById(R.id.spinner_player);
        spinnerPlayerView = findViewById(R.id.spinner_player_view);
        spinnerMode = findViewById(R.id.spinner_mode);
        spinnerSurfcae = findViewById(R.id.spinner_surfcae);

        mPlayerView1.addStateView(new DefaultLoadingView(this));
        mPlayerView1.addStateView(new DefaultEndedView(this));
        mPlayerView1.addStateView(new DefaultErrorView(this));

        mPlayerView2.addStateView(new DefaultLoadingView(this));
        mPlayerView2.addStateView(new DefaultEndedView(this));
        mPlayerView2.addStateView(new DefaultErrorView(this));

        media = (Media) getIntent().getSerializableExtra("media");

        initPlayer();

        initSpinnerData();
    }

    private void initPlayer() {
        sp = getSharedPreferences("player_set", Context.MODE_PRIVATE);
        int currentPlayer = sp.getInt("currentPlayer", 0);
        int currentPlayerView = sp.getInt("currentPlayerView", 0);
        int currentMode = sp.getInt("currentMode", 0);
        int currentSurface = sp.getInt("currentSurface", 0);


        mFuPlayer = new FuPlayer();
        setPlayer(currentPlayer);
        setPlayerView(currentPlayerView);
        setMode(currentMode);
        setSurface(currentSurface);
        mFuPlayer.setMediaSource(new MediaSource(media.getPath()));
    }

    private void initSpinnerData() {
        spinnerPlayer.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, playerTag));

        spinnerPlayerView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, playerViewTag));

        spinnerMode.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, modeTag));

        spinnerSurfcae.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, surfcaeTag));

        spinnerPlayer.setSelection(currentPlayer);

        spinnerPlayerView.setSelection(currentPlayerView);

        spinnerMode.setSelection(currentMode);

        spinnerSurfcae.setSelection(currentSurface);


        spinnerPlayer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setPlayer(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerPlayerView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setPlayerView(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        spinnerMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setMode(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerSurfcae.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setSurface(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }


    private void setPlayer(int index) {
        if (currentPlayer == index) {
            return;
        }
        currentPlayer = index;
        sp.edit().putInt("currentPlayer", currentPlayer).commit();
        switch (index) {
            case 0:
                mFuPlayer.setPlayer(new SysPlayerFactory().createPlayer(getApplicationContext(), null));
                break;
            case 1:
                mFuPlayer.setPlayer(new ExoPlayerFactory().createPlayer(getApplicationContext(), null));
                break;
            case 2:
                mFuPlayer.setPlayer(new IjkPlayerFactory().createPlayer(getApplicationContext(), null));
                break;
            case 3:
                mFuPlayer.setPlayer(new QiNiuPlayerFactory().createPlayer(getApplicationContext(), null));
                break;
            case 4:
                mFuPlayer.setPlayer(new VlcPlayerFactory().createPlayer(getApplicationContext(), null));
                break;
        }
    }

    private void setPlayerView(int index) {
        if (currentPlayerView == index) {
            return;
        }
        currentPlayerView = index;
        sp.edit().putInt("currentPlayerView", currentPlayerView).commit();
        switch (index) {
            case 0:
//                mPlayerView1.setPlayer(mFuPlayer);
//                mPlayerView2.setPlayer(null);
                mFuPlayer.setPlayerView(mPlayerView1);
                break;
            case 1:
//                mPlayerView2.setPlayer(mFuPlayer);
//                mPlayerView1.setPlayer(null);
                mFuPlayer.setPlayerView(mPlayerView2);
                break;
        }
    }

    private void setMode(int index) {
        if (currentMode == index) {
            return;
        }
        currentMode = index;
        sp.edit().putInt("currentMode", currentMode).commit();
        switch (index) {
            case 0:
                mPlayerView1.setResizeMode(0);
                mPlayerView2.setResizeMode(0);
                break;
            case 1:
                mPlayerView1.setResizeMode(1);
                mPlayerView2.setResizeMode(1);
                break;
            case 2:
                mPlayerView1.setResizeMode(2);
                mPlayerView2.setResizeMode(2);
                break;
            case 3:
                mPlayerView1.setResizeMode(3);
                mPlayerView2.setResizeMode(3);
                break;
            case 4:
                mPlayerView1.setResizeMode(4);
                mPlayerView2.setResizeMode(4);
                break;
        }
    }

    private void setSurface(int index) {
        if (currentSurface == index) {
            return;
        }
        currentSurface = index;
        sp.edit().putInt("currentSurface", currentSurface).commit();
        switch (index) {
            case 0:
                mPlayerView1.setSurfaceView(PlayerView.SURFACE_TYPE_SURFACE_VIEW);
                mPlayerView2.setSurfaceView(PlayerView.SURFACE_TYPE_SURFACE_VIEW);
                break;
            case 1:
                mPlayerView1.setSurfaceView(PlayerView.SURFACE_TYPE_TEXTURE_VIEW);
                mPlayerView2.setSurfaceView(PlayerView.SURFACE_TYPE_TEXTURE_VIEW);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFuPlayer.stop();
    }
}
