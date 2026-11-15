package org.stormconnections.port;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.system.ErrnoException;
import android.system.Os;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import org.libsdl.app.SDLActivity;

import java.io.File;

/**
 * The game: SDL3's activity running the recompiled game (libconnections.so).
 */
public class GameActivity extends SDLActivity {
    private static final String TAG = "storm_conn";
    private static final int KEPT_PERFORMANCE_RUNS = 5;
    private static String gameDataRoot;

    private TouchControlsView touch;
    private boolean touchWanted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        File data = Storage.dataDir(this);
        forceFullscreen();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        try {
            Os.setenv("REX_ANDROID_DATA_DIR", data.getAbsolutePath(), true);
            Os.setenv("TMPDIR", getCacheDir().getAbsolutePath(), true);
        } catch (ErrnoException e) {
            Log.e(TAG, "setenv failed", e);
        }
        
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = Storage.prefs(this);
        touchWanted = prefs.getBoolean(Storage.KEY_TOUCH, true);
        touch = new TouchControlsView(this, prefs.getFloat(Storage.KEY_TOUCH_OPACITY, 0.85f),
                prefs.getFloat(Storage.KEY_TOUCH_SCALE, 1.0f));
        if (mLayout != null) {
            mLayout.addView(touch, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
        }
        updateTouchVisibility();
    }

    private void updateTouchVisibility() {
        if (touch != null) {
            touch.setVisibility(touchWanted ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    protected String[] getLibraries() {
        return new String[] {"c++_shared", "connections"};
    }
}
