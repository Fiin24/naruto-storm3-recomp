package org.libsdl.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import java.io.File;

/**
 * Minimal SDL3 Activity stub for Storm Connections.
 * Provides the minimal API surface that GameActivity expects.
 */
public class SDLActivity extends Activity {
    private static final String TAG = "SDLActivity";

    // Layout where SDL puts its surface and overlay views
    protected ViewGroup mLayout;

    // Whether the native engine has started successfully
    protected boolean mNativeStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("SDLActivity", "onCreate");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("SDLActivity", "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("SDLActivity", "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("SDLActivity", "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("SDLActivity", "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("SDLActivity", "onDestroy");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    // Force fullscreen and keep screen on
    protected void forceFullscreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN |
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_FULLSCREEN |
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        );
    }

    // Native interface methods - stubbed for compilation
    // These will be implemented by the native library
    static {
        try {
            System.loadLibrary("connections");
        } catch (UnsatisfiedLinkError e) {
            Log.e("SDLActivity", "Failed to load libconnections.so", e);
        }
    }

    // Native methods that GameActivity might call
    public static native void nativeInit(String[] arguments);
    public static native void nativeResume();
    public static native void nativePause();
    public static native void nativeDestroy();
    public static native void nativeQuit();

    // Surface callbacks for SDL
    public void onNativeSurfaceCreated() {}
    public void onNativeSurfaceChanged(int format, int width, int height) {}
    public void onNativeSurfaceDestroyed() {}

    // Text input
    public void onNativeKeyboardFocusLost() {}
    public void onNativeTextInput(int unicode) {}

    // Audio
    public void onNativeAudioDeviceChanged() {}

    // Controller
    public void onNativeJoystickAdded(int deviceId) {}
    public void onNativeJoystickRemoved(int deviceId) {}

    // Clipboard
    public String getClipboardText() { return ""; }
    public void setClipboardText(String text) {}

    // File access
    public ParcelFileDescriptor openFileDescriptor(Uri uri, String mode) {
        return null;
    }

    // Game controller
    public void onNativeControllerAxis(int deviceId, int axis, float value) {}
    public void onNativeControllerButton(int deviceId, int button, boolean down) {}
    public void onNativeControllerHat(int deviceId, int hat, int x, int y) {}

    // Text input for SDL
    public void showTextInput(int x, int y, int width, int height) {}
    public void hideTextInput() {}

    // Clipboard for SDL
    public void onNativeClipboardChanged() {}

    // For GameActivity.openDisc()
    public static ParcelFileDescriptor getIsoDescriptor() {
        return null;
    }

    public static void setIsoDescriptor(ParcelFileDescriptor descriptor) {}

    // Game data root
    public static String getGameDataRoot() {
        return null;
    }

    public static void setGameDataRoot(String path) {}
}
