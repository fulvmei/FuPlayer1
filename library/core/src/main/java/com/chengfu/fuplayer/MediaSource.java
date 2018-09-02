package com.chengfu.fuplayer;

import android.net.Uri;

import com.chengfu.fuplayer.player.IPlayer;

import java.util.Map;

/**
 * Defines and provides media to be played by an {@link IPlayer}.
 */
public class MediaSource {

    public final static int MEDIA_TYPE_DEFAULT = 0;
    public final static int MEDIA_TYPE_HLS = 1;
    public final static int MEDIA_TYPE_RTMP = 2;
    public final static int MEDIA_TYPE_DASH = 3;
    public final static int MEDIA_TYPE_SS = 4;

    private String path;
    private Uri uri;
    private Map<String, String> headers;
    private int type = MEDIA_TYPE_DEFAULT;

    public MediaSource() {

    }

    public MediaSource(Builder builder) {
        this.path = builder.path;
        this.uri = builder.uri;
        this.headers = builder.headers;
    }

    public MediaSource(String path) {
        this.path = path;
    }

    public MediaSource(Uri uri) {
        this.uri = uri;
    }

    public MediaSource(Uri uri, Map<String, String> headers) {
        this.uri = uri;
        this.headers = headers;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public Uri getUri() {
        return uri;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        return this.path;
    }

    public static class Builder {
        private String path;
        private Uri uri;
        private Map<String, String> headers;

        public Builder() {

        }

        public Builder(String path) {
            this.path = path;
        }

        public Builder(Uri uri) {
            this.uri = uri;
        }

        public Builder(Uri uri, Map<String, String> headers) {
            this.uri = uri;
            this.headers = headers;
        }

        public Builder setPath(String path) {
            return new Builder(path);
        }

        public Builder setUri(Uri uri) {
            return new Builder(uri);
        }

        public Builder setHeaders(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public MediaSource build() {
            return new MediaSource(this);
        }

    }

}
