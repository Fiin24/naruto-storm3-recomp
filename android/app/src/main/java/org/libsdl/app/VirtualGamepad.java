package org.libsdl.app;

/**
 * Launcher-build placeholder for the on-screen gamepad bridge.
 *
 * The launcher-only APK does not bundle SDL's Java sources or start the game,
 * so there is no SDLControllerManager to call here. Keep this small API so the
 * touch-control preview compiles; wire these methods to SDLControllerManager
 * when the SDL runtime is included in a game-enabled build.
 */
public final class VirtualGamepad {
    public static final int AXIS_LEFT_X = 0;
    public static final int AXIS_LEFT_Y = 1;
    public static final int AXIS_RIGHT_X = 2;
    public static final int AXIS_RIGHT_Y = 3;
    public static final int AXIS_LEFT_TRIGGER = 4;
    public static final int AXIS_RIGHT_TRIGGER = 5;

    private VirtualGamepad() {}

    /** No-op in the launcher-only build; SDL is not bundled. */
    public static void attach() {}

    /** No-op in the launcher-only build; SDL is not bundled. */
    public static void detach() {}

    /** No-op in the launcher-only build; SDL is not bundled. */
    public static void button(int keyCode, boolean down) {}

    /** No-op in the launcher-only build; SDL is not bundled. */
    public static void axis(int axis, float value) {}
}
