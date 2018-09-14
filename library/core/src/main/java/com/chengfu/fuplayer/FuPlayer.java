package com.chengfu.fuplayer;

import android.os.Bundle;
import android.view.Surface;

import com.chengfu.fuplayer.player.AbsPlayer;
import com.chengfu.fuplayer.player.IPlayer;
import com.chengfu.fuplayer.widget.IPlayerControllerView;
import com.chengfu.fuplayer.widget.IPlayerView;


public class FuPlayer implements IPlayer {

    private final static String TAG = "FuPlayer";

    private MediaSource mMediaSource;

    private AbsPlayer mPlayer;
    private IPlayerView mPlayerView;
    private IPlayerControllerView mPlayerController;

    private long mCurrentPosition;

    public FuPlayer() {

    }

    public void setPlayer(AbsPlayer player) {
        if (mPlayer == player) {
            return;
        }
        if (mPlayer != null) {
            mCurrentPosition = mPlayer.getCurrentPosition();
            mPlayer.release();
        }
        mPlayer = player;

        if (mMediaSource != null) {
            mPlayer.setMediaSource(mMediaSource);
        }
        if (mPlayerView != null) {
            mPlayerView.setPlayer(mPlayer);
        }
        if (mPlayerController != null) {
            mPlayerController.setPlayer(mPlayer);
        }
        mPlayer.seekTo(mCurrentPosition);
        mPlayer.setPlayWhenReady(true);
    }

    public IPlayer getPlayer() {
        return mPlayer;
    }

    public void setPlayerView(IPlayerView playerView) {
        if (mPlayerView == playerView) {
            return;
        }
        if (mPlayerView != null) {
            mPlayerView.setPlayer(null);
        }
        mPlayerView = playerView;
        mPlayerView.setPlayer(mPlayer);
    }

    public IPlayerView getPlayerView() {
        return mPlayerView;
    }

    public void setPlayerController(IPlayerControllerView playerController) {
        if (mPlayerController == playerController) {
            return;
        }
        if (mPlayerController != null) {
            mPlayerController.setPlayer(null);
        }

        mPlayerController = playerController;

        playerController.setPlayer(mPlayer);
    }

    public IPlayerControllerView getPlayerController() {
        return mPlayerController;
    }

    @Override
    public VideoComponent getVideoComponent() {
        return mPlayer != null ? mPlayer.getVideoComponent() : null;
    }

    @Override
    public TextComponent getTextComponent() {
        return mPlayer != null ? mPlayer.getTextComponent() : null;
    }

    @Override
    public void addEventListener(EventListener listener) {
        if (mPlayer != null) {
            mPlayer.addEventListener(listener);
        }
    }

    @Override
    public void removeEventListener(EventListener listener) {
        if (mPlayer != null) {
            mPlayer.removeEventListener(listener);
        }
    }

    @Override
    public PlayerError getPlayerError() {
        return mPlayer != null ? mPlayer.getPlayerError() : null;
    }

    @Override
    public int getPlayerState() {
        return mPlayer != null ? mPlayer.getPlayerState() : IPlayer.STATE_IDLE;
    }


    @Override
    public void setPlayWhenReady(boolean playWhenReady) {
        mPlayer.setPlayWhenReady(playWhenReady);
    }

    @Override
    public boolean getPlayWhenReady() {
        return mPlayer.getPlayWhenReady();
    }


    @Override
    public void setVolume(float volume) {

        mPlayer.setVolume(volume);
    }

    @Override
    public float getVolume() {
        return 0;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean isPlaying() {

        return mPlayer.isPlaying();

    }

    @Override
    public boolean isSeekable() {
        return false;
    }

    @Override
    public long getCurrentPosition() {

        return mPlayer.getCurrentPosition();

    }

    @Override
    public long getDuration() {

        return mPlayer.getDuration();

    }

    @Override
    public int getAudioSessionId() {

        return mPlayer.getAudioSessionId();

    }

    @Override
    public void setMediaSource(MediaSource mediaSource) {
        mMediaSource = mediaSource;
        mPlayer.setMediaSource(mediaSource);
    }

    @Override
    public void setLooping(boolean looping) {

        mPlayer.setLooping(looping);

    }

    @Override
    public boolean isLooping() {
        return false;
    }

    @Override
    public void resume() {
        mPlayer.resume();
    }

    @Override
    public void stop() {
        mPlayer.stop();
    }

    @Override
    public void release() {
        mPlayer.release();
    }


    @Override
    public void seekTo(long msc) {

        mPlayer.seekTo(msc);
    }
}
