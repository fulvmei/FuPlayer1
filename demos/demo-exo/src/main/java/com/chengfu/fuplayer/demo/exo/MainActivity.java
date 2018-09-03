package com.chengfu.fuplayer.demo.exo;

import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

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
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class MainActivity extends AppCompatActivity implements Player.EventListener, View.OnClickListener {

    String path1 = "https://mov.bn.netease.com/open-movie/nos/mp4/2018/01/12/SD70VQJ74_sd.mp4";

    String path2 = "https://mov.bn.netease.com/open-movie/nos/mp4/2015/08/27/SB13F5AGJ_sd.mp4";

    private TextureView textureView;
    private Button play, pause, stop, release, seek, add, remove;

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
        seek = findViewById(R.id.seek);
        add = findViewById(R.id.add);
        remove = findViewById(R.id.remove);

        play.setOnClickListener(this);
        pause.setOnClickListener(this);
        stop.setOnClickListener(this);
        release.setOnClickListener(this);
        seek.setOnClickListener(this);
        add.setOnClickListener(this);
        remove.setOnClickListener(this);

        initPlayer();
    }

    private void initPlayer() {
        RenderersFactory renderersFactory = new DefaultRenderersFactory(this);
        DefaultTrackSelector trackSelector =
                new DefaultTrackSelector();
        player = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector);

        // Measures bandwidth during playback. Can be null if not required.
        mBandwidthMeter = new DefaultBandwidthMeter();

//        player.addListener(this);

        player.setVideoTextureView(textureView);
        player.prepare(getMediaSource(Uri.parse(path2)));
        player.setPlayWhenReady(true);
    }

    private MediaSource getMediaSource(Uri uri) {
        int contentType = Util.inferContentType(uri);
        DefaultDataSourceFactory dataSourceFactory =
                new DefaultDataSourceFactory(this,
                        Util.getUserAgent(this, this.getPackageName()), mBandwidthMeter);
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
    public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {
        System.out.println("onTimelineChanged : timeline=" + timeline + " , manifest=" + manifest);
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        System.out.println("onTracksChanged : trackGroups=" + trackGroups + " , trackSelections=" + trackSelections);
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
        System.out.println("onLoadingChanged : isLoading=" + isLoading);
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        System.out.println("onPlayerStateChanged : playWhenReady=" + playWhenReady + " , playbackState=" + playbackState);
        if (playbackState == Player.STATE_READY) {
            System.out.println("ContentTime" + player.getContentPosition() / 1000);
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {
        System.out.println("onRepeatModeChanged : repeatMode=" + repeatMode);
    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
        System.out.println("onShuffleModeEnabledChanged : shuffleModeEnabled=" + shuffleModeEnabled);
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        System.out.println("onPlayerError : error=" + error);
    }

    @Override
    public void onPositionDiscontinuity(int reason) {
        System.out.println("onPositionDiscontinuity : reason=" + reason);
    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
        System.out.println("onPlaybackParametersChanged : playbackParameters=" + playbackParameters);
    }

    @Override
    public void onSeekProcessed() {
        System.out.println("onSeekProcessed");
    }

    long pos = 0;

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
        } else if (v == seek) {
            long contentPosition = player.getContentPosition();
            pos = pos + 1000 * 3;
            player.seekTo(pos);
        } else if (v == add) {
            player.addListener(this);
        } else if (v == remove) {
            player.removeListener(this);
        }
    }
}
