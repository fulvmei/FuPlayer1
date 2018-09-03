package com.chengfu.fuplayer;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Thrown when a non-recoverable playback failure occurs.
 */
public final class PlayerError {
    public static final int MEDIA_ERROR_UNKNOWN = 1;
    public static final int MEDIA_ERROR_IO = 2;
    public static final int MEDIA_ERROR_SERVER_DIED = 3;
    public static final int MEDIA_ERROR_TIMED_OUT = 4;
    public static final int MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK = 5;
    public static final int MEDIA_ERROR_MALFORMED = 6;
    public static final int MEDIA_ERROR_UNSUPPORTED = 7;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({MEDIA_ERROR_UNKNOWN, MEDIA_ERROR_IO, MEDIA_ERROR_SERVER_DIED, MEDIA_ERROR_TIMED_OUT, MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK, MEDIA_ERROR_MALFORMED, MEDIA_ERROR_UNSUPPORTED})
    public @interface Code {
    }

    @Code
    public final int code;

    public final String message;

    public static PlayerError create() {
        return new PlayerError(MEDIA_ERROR_UNKNOWN, getCodeMessage(MEDIA_ERROR_UNKNOWN));
    }

    public static PlayerError create(@Code int code) {
        return new PlayerError(code, getCodeMessage(code));
    }

    public static PlayerError create(@Code int code, String message) {
        return new PlayerError(code, message);
    }

    private PlayerError(@Code int code, String message) {
        this.code = code;
        this.message = message;
    }

    public @Code
    int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static String getCodeMessage(@Code int code) {
        switch (code) {
            case MEDIA_ERROR_UNKNOWN:
                return "MEDIA_ERROR_UNKNOWN";
            case MEDIA_ERROR_IO:
                return "MEDIA_ERROR_IO";
            case MEDIA_ERROR_SERVER_DIED:
                return "MEDIA_ERROR_SERVER_DIED";
            case MEDIA_ERROR_TIMED_OUT:
                return "MEDIA_ERROR_TIMED_OUT";
            case MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                return "MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK";
            case MEDIA_ERROR_MALFORMED:
                return "MEDIA_ERROR_MALFORMED";
            case MEDIA_ERROR_UNSUPPORTED:
                return "MEDIA_ERROR_UNSUPPORTED";
            default:
                return "";
        }
    }
}
