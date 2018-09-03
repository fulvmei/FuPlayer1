package fuplayer.chengfu.com.demo_sys;

import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import com.chengfu.fuplayer.MediaSource;
import com.chengfu.fuplayer.player.sys.SysPlayer;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener, MediaPlayer.OnInfoListener {
    String path = "https://media.w3.org/2010/05/sintel/trailer.mp4";

    MediaPlayer player;
    TextureView textureView;
    Surface surface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textureView = findViewById(R.id.textureView);

        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                surface = new Surface(surfaceTexture);
                player.setSurface(surface);
                System.out.println("onSurfaceTextureAvailable");
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                System.out.println("onSurfaceTextureAvailable");
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });

        findViewById(R.id.play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                f.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH);
//                player.start();
            }
        });

        findViewById(R.id.pause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                f.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT);
//                player.pause();
            }
        });

        findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                f.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
//                player.stop();
            }
        });

        findViewById(R.id.resume).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                f.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
//                player.release();
            }
        });

        player = new MediaPlayer();
        player.setOnPreparedListener(this);
        player.setOnInfoListener(this);
//        player.setPlayWhenReady(true);
        try {
            player.setDataSource(path);
            player.prepareAsync();
            Log.e("FFFF", "prepareAsync");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.release();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.e("FFFF", "onPrepared");
        player.start();
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                Log.e("FFFF", "media_info_buffering_start");
//                player.pause();
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                Log.e("FFFF", "media_info_buffering_end");
                break;
        }
        return false;
    }
}
