package com.chengfu.fuplayer.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.TextureView;
import android.widget.Button;

public class ExoActivity extends AppCompatActivity {
    String path1 = "https://mov.bn.netease.com/open-movie/nos/mp4/2018/01/12/SD70VQJ74_sd.mp4";

    String path2 = "https://mov.bn.netease.com/open-movie/nos/mp4/2015/08/27/SB13F5AGJ_sd.mp4";

    private TextureView textureView;
    private Button play, pause, stop, release;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_exo);

        textureView=findViewById(R.id.textureView);
        play=findViewById(R.id.play);
        pause=findViewById(R.id.pause);
        stop=findViewById(R.id.stop);
        release=findViewById(R.id.release);
    }

    private void initPlayer(){

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
