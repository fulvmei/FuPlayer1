package com.chengfu.fuplayer.demo;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;

import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;


import com.chengfu.fuplayer.FuPlayer;
import com.chengfu.fuplayer.MediaSource;
import com.chengfu.fuplayer.demo.bean.Media;
import com.chengfu.fuplayer.demo.immersion.ImmersionBar;
import com.chengfu.fuplayer.demo.immersion.QMUIStatusBarHelper;
import com.chengfu.fuplayer.player.IPlayer;
import com.chengfu.fuplayer.player.exo.ExoPlayerFactory;
import com.chengfu.fuplayer.player.ijk.IjkPlayerFactory;
import com.chengfu.fuplayer.player.qiniu.QiNiuPlayerFactory;
import com.chengfu.fuplayer.player.sys.SysPlayerFactory;
import com.chengfu.fuplayer.player.vlc.VlcPlayerFactory;
import com.chengfu.fuplayer.ui.DefaultControlView;
import com.chengfu.fuplayer.widget.PlayerView;

public class PlayerActivity extends AppCompatActivity {
    private Media media;

    private String[] playerTag = {"系统", "Exo", "Ijk", "七牛", "Vlc"};

    private String[] playerViewTag = {"V1", "V2"};


    private int currentPlayer = -1;

    private int currentPlayerView = -1;

    private SharedPreferences sp;

    private PlayerView mPlayerView1;
    private DefaultControlView mControlView1;

    private PlayerView mPlayerView2;
    private DefaultControlView mControlView2;

    private Spinner spinnerPlayer;

    private Spinner spinnerPlayerView;


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

        media = (Media) getIntent().getSerializableExtra("media");

        initPlayer();

        initSpinnerData();
    }

    private void initPlayer() {
        sp = getSharedPreferences("player_set", Context.MODE_PRIVATE);
        int currentPlayer = sp.getInt("currentPlayer", 0);
        int currentPlayerView = sp.getInt("currentPlayerView", 0);


        mFuPlayer = new FuPlayer();
        setPlayer(currentPlayer);
        setPlayerView(currentPlayerView);
        mFuPlayer.setMediaSource(new MediaSource(media.getPath()));
    }

    private void initSpinnerData() {
        spinnerPlayer.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, playerTag));

        spinnerPlayerView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, playerViewTag));


        spinnerPlayer.setSelection(currentPlayer);

        spinnerPlayerView.setSelection(currentPlayerView);


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

    }


    public void setPlayer(int index) {
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
            case 4:
                mFuPlayer.setPlayer(new VlcPlayerFactory().createPlayer(getApplicationContext(), null));
                break;
        }
    }

    public void setPlayerView(int index) {
        if (currentPlayerView == index) {
            return;
        }
        currentPlayerView = index;
        sp.edit().putInt("currentPlayerView", currentPlayerView).commit();
        switch (index) {
            case 0:
                mFuPlayer.setPlayerView(mPlayerView1);
                break;
            case 1:
                mFuPlayer.setPlayerView(mPlayerView2);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFuPlayer.stop();
    }
}
