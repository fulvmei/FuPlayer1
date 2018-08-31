package com.chengfu.fuplayer.ui;

public class CustomProgress {

    private String startTime;
    private String endTime;
    private long currentProgress;
    private long duration;
    private OnUpdatadeLinster onUpdatadeLinster;

    public CustomProgress() {
    }

    public void updata(String startTime, String endTime, long currentProgress, long duration) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.currentProgress = currentProgress;
        this.duration = duration;

        if (onUpdatadeLinster != null) {
            onUpdatadeLinster.updatade(startTime, endTime, currentProgress, duration);
        }
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public long getCurrentProgress() {
        return currentProgress;
    }

    public long getDuration() {
        return duration;
    }

    public void setOnUpdatadeLinster(OnUpdatadeLinster onUpdatadeLinster) {
        this.onUpdatadeLinster = onUpdatadeLinster;
    }

    public interface OnUpdatadeLinster {
        void updatade(String startTime, String endTime, long currentProgress, long duration);
    }
}