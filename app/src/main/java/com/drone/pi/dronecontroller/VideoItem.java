package com.drone.pi.dronecontroller;

import android.support.annotation.Nullable;
import android.support.annotation.NonNull;

/**
 * Created by usrc on 17. 8. 31.
 */

public class VideoItem {
    private final long id;
    @Nullable
    private final String text;
    @NonNull
    private String video;
    @Nullable
    private String key;

    public VideoItem(long id,
                     @Nullable String text,
                     @NonNull String video,
                     @Nullable String key) {
        this.id = id;
        this.text = text;
        this.video = video;
        this.key = key;
    }

    public long id() {
        return id;
    }

    @Nullable
    public String text() {
        return text;
    }

    @NonNull
    public String video() {
        return video;
    }

    @Nullable
    public String key() {
        return key;
    }
}
