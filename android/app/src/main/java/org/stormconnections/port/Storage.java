package org.stormconnections.port;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;

public final class Storage {
    public static final String KEY_TOUCH = "touch";
    public static final String KEY_TOUCH_OPACITY = "touch_opacity";
    public static final String KEY_TOUCH_SCALE = "touch_scale";
    public static final String KEY_GPU_DRIVER = "gpu_driver";
    public static final String KEY_TURNIP_MODE = "turnip_mode";
    public static final String KEY_PERF_LOG = "perf_log";
    public static final String KEY_ISO_URI = "iso_uri";

    private Storage() {}

    public static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences("storm_connections", Context.MODE_PRIVATE);
    }

    public static File dataDir(Context context) {
        return context.getFilesDir();
    }
}
