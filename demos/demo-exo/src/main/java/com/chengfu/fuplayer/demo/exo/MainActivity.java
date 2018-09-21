package com.chengfu.fuplayer.demo.exo;

import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import com.chengfu.fuplayer.demo.exo.render.MyRenderersFactory;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.DefaultHlsDataSourceFactory;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    String path1 = "rtmp://3891.liveplay.myqcloud.com/live/3891_user_6f8cc72d_5956";

    String path2 = "http://3891.liveplay.myqcloud.com/live/3891_user_6f8cc72d_5956.flv";

    String path3 = "http://3891.liveplay.myqcloud.com/live/3891_user_6f8cc72d_5956.m3u8";

    String path4 = "rtmp://3891.liveplay.myqcloud.com/live/3891_user_6f8cc72d_5956?bizid=3891&txSecret=8a7461e11b8b6798383a6380b4da4968&txTime=5BA898E0";

    String path = "https://mov.bn.netease.com/open-movie/nos/mp4/2015/08/27/SB13F5AGJ_sd.mp4";

    private TextureView textureView;
    private Button play, pause, stop, release, p1, p2, p3, p4;

    private SimpleExoPlayer player;
    private DefaultBandwidthMeter mBandwidthMeter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        textureView = findViewById(R.id.textureView);
        play = findViewById(R.id.play);
        pause = findViewById(R.id.pause);
        stop = findViewById(R.id.stop);
        release = findViewById(R.id.release);
        p1 = findViewById(R.id.path1);
        p2 = findViewById(R.id.path2);
        p3 = findViewById(R.id.path3);
        p4 = findViewById(R.id.path4);

        play.setOnClickListener(this);
        pause.setOnClickListener(this);
        stop.setOnClickListener(this);
        release.setOnClickListener(this);
        p1.setOnClickListener(this);
        p2.setOnClickListener(this);
        p3.setOnClickListener(this);
        p4.setOnClickListener(this);

        initPlayer();
    }

    private void initPlayer() {
        RenderersFactory renderersFactory = new MyRenderersFactory(this);
        DefaultTrackSelector trackSelector =
                new DefaultTrackSelector();
        player = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector);

        // Measures bandwidth during playback. Can be null if not required.
        mBandwidthMeter = new DefaultBandwidthMeter();

//        player.addListener(this);

        player.setVideoTextureView(textureView);
        player.prepare(getMediaSource(Uri.parse(path)));
        player.setPlayWhenReady(true);
    }

    private MediaSource getMediaSource(Uri uri) {
        int contentType = Util.inferContentType(uri);
        DefaultDataSourceFactory dataSourceFactory =
                new DefaultDataSourceFactory(this,
                        Util.getUserAgent(this, this.getPackageName()), mBandwidthMeter);


//        String scheme = uri.getScheme();
//        if (scheme != null && scheme.contains("rtmp")) {
//            return new ExtractorMediaSource(uri, new RtmpDataSourceFactory(), new DefaultExtractorsFactory(), null, null);
//        }

        switch (contentType) {
            case C.TYPE_DASH:
                DefaultDashChunkSource.Factory factory = new DefaultDashChunkSource.Factory(dataSourceFactory);
                return new DashMediaSource(uri, dataSourceFactory, factory, null, null);
            case C.TYPE_SS:
                DefaultSsChunkSource.Factory ssFactory = new DefaultSsChunkSource.Factory(dataSourceFactory);
                return new SsMediaSource(uri, dataSourceFactory, ssFactory, null, null);
            case C.TYPE_HLS:
                return new HlsMediaSource(uri, dataSourceFactory, null, null);

            case C.TYPE_OTHER:
            default:
                // This is the MediaSource representing the media to be played.
                ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
                return new ExtractorMediaSource(uri,
                        dataSourceFactory, extractorsFactory, null, null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if (v == play) {
            player.setPlayWhenReady(true);
        } else if (v == pause) {
            player.setPlayWhenReady(false);
        } else if (v == stop) {
            player.stop();
        } else if (v == release) {
            player.release();
        } else if (v == p1) {
            player.prepare(getMediaSource(Uri.parse(path1)));
        } else if (v == p2) {
            player.prepare(getMediaSource(Uri.parse(path2)));
        } else if (v == p3) {
            player.prepare(getMediaSource(Uri.parse(path3)));
        } else if (v == p4) {
            player.prepare(getMediaSource(Uri.parse(path4)));
        }
    }
}
