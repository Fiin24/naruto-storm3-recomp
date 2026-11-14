package dev.narutorecomp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;

import static dev.narutorecomp.LauncherViews.CARD;
import static dev.narutorecomp.LauncherViews.MUTED;
import static dev.narutorecomp.LauncherViews.ORANGE;

/**
 * The app's front screen: finds the game's files, sets the resolution,
 * anti-aliasing and the on-screen controller, and starts the game. The
 * background is the game's own key art once its folder is found.
 *
 * Works with a controller: the D-pad moves between settings, A picks one and
 * Start plays.
 */
public class LauncherActivity extends Activity {
    private static final int PICK_FOLDER = 1;
    private static final int ASK_STORAGE = 2;

    private SharedPreferences prefs;
    private float density;

    private LauncherViews.ArtBackground background;
    private LauncherViews.PlayButton play;
    private TextView status;
    private TextView summary;
    private TextView gamePath;
    private Button accessButton;
    private TouchControlsView preview;
    private boolean searching;
    private File artLoadedFor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = Settings.get(this);
        density = getResources().getDisplayMetrics().density;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(buildLayout());
        hideSystemBars();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemBars();
        refresh();
        // First run, or the game moved: look for it.
        if (GameFiles.hasStorageAccess(this) && !GameFiles.isGameFolder(gameFolder())) {
            searchForGame();
        }
    }

    // --- Layout ------------------------------------------------------------

    private int dp(float value) {
        return Math.round(value * density);
    }

    private View buildLayout() {
        FrameLayout root = new FrameLayout(this);
        background = new LauncherViews.ArtBackground(this);
        root.addView(background, match());

        LinearLayout columns = new LinearLayout(this);
        columns.setOrientation(LinearLayout.HORIZONTAL);
        columns.setPadding(dp(36), dp(20), dp(24), dp(20));
        root.addView(columns, match());

        // Left: the title and Play.
        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        left.setGravity(Gravity.BOTTOM | Gravity.START);
        left.setPadding(0, 0, dp(16), dp(12));
        columns.addView(left, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));

        TextView series = text("NARUTO X BORUTO", 15, ORANGE, true);
        series.setLetterSpacing(0.3f);
        left.addView(series);
        TextView title = text("ULTIMATE NINJA STORM\nCONNECTIONS", 40, 0xfffff4e6, true);
        title.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
        title.setLineSpacing(0, 0.9f);
        title.setShadowLayer(dp(6), 0, dp(2), 0xcc000000);
        left.addView(title);
        TextView subtitle = text("Rekompilasi  ·  renderer Vulkan native", 13, 0xffd8cfc8, false);
        subtitle.setPadding(0, dp(4), 0, dp(16));
        left.addView(subtitle);

        play = new LauncherViews.PlayButton(this);
        play.setOnClickListener(v -> startGame());
        left.addView(play);
        summary = text("", 13, 0xfffff4e6, true);
        summary.setPadding(0, dp(12), 0, 0);
        left.addView(summary);
        status = text("", 13, 0xffd8cfc8, false);
        status.setPadding(0, dp(2), 0, 0);
        left.addView(status);

        // Right: the settings card.
        ScrollView scroll = new ScrollView(this);
        scroll.setBackground(LauncherViews.rounded(CARD, dp(16)));
        scroll.setPadding(dp(20), dp(12), dp(20), dp(12));
        scroll.setVerticalScrollBarEnabled(true);
        columns.addView(scroll, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.05f));
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(card);
        buildSettings(card);
        return root;
    }

    private void buildSettings(LinearLayout card) {
        section(card, "FILE GAME");
        gamePath = text("", 13, 0xfff2ebe6, false);
        gamePath.setTypeface(Typeface.MONOSPACE);
        card.addView(gamePath);
        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.setPadding(0, dp(8), 0, 0);
        accessButton = button("Izinkan akses file", v -> askForStorage());
        buttons.addView(accessButton, wrapWithGap());
        buttons.addView(button("Cari di HP", v -> searchForGame()), wrapWithGap());
        buttons.addView(button("Pilih folder", v -> pickFolder()), wrapWithGap());
        card.addView(buttons);
        card.addView(hint("Folder berisi default.xex dan folder data, dari salinan game "
                + "Anda sendiri. Bisa di mana saja di HP atau kartu SD."));

        section(card, "RESOLUSI");
        card.addView(new LauncherViews.Segmented(this,
                new String[][] {{Settings.RES_720P, "720p"}, {Settings.RES_1080P, "1080p"}},
                Settings.resolution(prefs), key -> save(Settings.RESOLUTION, key)));
        card.addView(hint("Resolusi render game. 720p adalah resolusi asli Xbox 360 dan paling "
                + "hemat baterai; 1080p lebih tajam."));

        section(card, "ANTI-ALIASING");
        card.addView(new LauncherViews.Segmented(this,
                new String[][] {{"off", "Mati"}, {"fxaa", "FXAA"}},
                Settings.fxaa(prefs) ? "fxaa" : "off",
                key -> save(Settings.FXAA, key.equals("fxaa"))));
        card.addView(hint("FXAA menghaluskan tepi kasar dengan sedikit waktu GPU."));

        section(card, "KARAKTER");
        card.addView(new LauncherViews.Segmented(this,
                new String[][] {{"story", "Buka di cerita"}, {"all", "Buka semua"}},
                Settings.unlockAll(prefs) ? "all" : "story",
                key -> save(Settings.UNLOCK_ALL, key.equals("all"))));
        card.addView(hint("Buka semua membuat semua karakter bisa langsung dimainkan. Game "
                + "menyimpannya sebagai terbuka, jadi tetap terbuka."));

        section(card, "LAYAR");
        card.addView(text("Isi penuh layar", 15, 0xfff2ebe6, true));
        card.addView(hint("Gambar selalu mengisi seluruh layar HP, dari tepi ke tepi."));

        section(card, "PERFORMA");
        card.addView(text("30 FPS, hemat baterai", 15, 0xfff2ebe6, true));
        card.addView(hint("Game berjalan di 30 frame per detik. Layar turun ke refresh rate "
                + "terendah yang muat, dan HP menjaga clock stabil, sehingga frame tetap "
                + "rata tanpa buang daya."));

        section(card, "KONTROL XBOX 360");
        card.addView(new LauncherViews.Segmented(this,
                new String[][] {{"on", "Di layar"}, {"off", "Mati"}},
                Settings.touch(prefs) ? "on" : "off",
                key -> {
                    save(Settings.TOUCH, key.equals("on"));
                    preview.setVisibility(key.equals("on") ? View.VISIBLE : View.INVISIBLE);
                }));
        card.addView(hint("Kontroler Bluetooth atau USB juga bisa dipakai. Di dalam game, tombol "
                + "◐ di bagian bawah menyembunyikan yang di layar."));
        card.addView(slider("Ukuran", Settings.touchSize(prefs), 0.6f, 1.4f, Settings.TOUCH_SIZE));
        card.addView(slider("Opasitas", Settings.touchOpacity(prefs), 0.2f, 1.0f, Settings.TOUCH_OPACITY));

        // The layout as it will be over the game.
        FrameLayout frame = new FrameLayout(this);
        frame.setBackground(LauncherViews.rounded(0xff0a0809, dp(10)));
        preview = new TouchControlsView(this, true);
        preview.setLook(Settings.touchSize(prefs), Settings.touchOpacity(prefs));
        preview.setVisibility(Settings.touch(prefs) ? View.VISIBLE : View.INVISIBLE);
        frame.addView(preview, match());
        LinearLayout.LayoutParams frameParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(170));
        frameParams.topMargin = dp(8);
        frameParams.bottomMargin = dp(8);
        card.addView(frame, frameParams);
    }

    private View slider(String label, float value, float min, float max, String key) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        TextView name = text(label, 14, 0xfff2ebe6, true);
        row.addView(name, new LinearLayout.LayoutParams(dp(72), ViewGroup.LayoutParams.WRAP_CONTENT));
        SeekBar bar = new SeekBar(this);
        bar.setMax(100);
        bar.setProgress(Math.round((value - min) / (max - min) * 100));
        bar.setFocusable(true);
        TextView shown = text(Math.round(value * 100) + "%", 14, MUTED, false);
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float v = min + (max - min) * progress / 100f;
                shown.setText(Math.round(v * 100) + "%");
                prefs.edit().putFloat(key, v).apply();
                preview.setLook(Settings.touchSize(prefs), Settings.touchOpacity(prefs));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        row.addView(bar, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        row.addView(shown, new LinearLayout.LayoutParams(dp(52), ViewGroup.LayoutParams.WRAP_CONTENT));
        return row;
    }

    private void section(LinearLayout card, String title) {
        TextView header = text(title, 13, ORANGE, true);
        header.setLetterSpacing(0.18f);
        header.setPadding(0, dp(14), 0, dp(6));
        card.addView(header);
    }

    private TextView hint(String value) {
        TextView hint = text(value, 12, MUTED, false);
        hint.setPadding(0, dp(5), 0, 0);
        return hint;
    }

    private TextView text(String value, float sp, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sp);
        view.setTextColor(color);
        if (bold) {
            view.setTypeface(Typeface.DEFAULT_BOLD);
        }
        return view;
    }

    private Button button(String label, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextSize(14);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setTextColor(0xfff2ebe6);
        button.setBackground(LauncherViews.background(this, 0xff2a2426, 0xff3a3234, 10));
        button.setPadding(dp(14), dp(6), dp(14), dp(6));
        button.setMinHeight(dp(40));
        button.setMinimumHeight(dp(40));
        button.setOnClickListener(listener);
        return button;
    }

    private static FrameLayout.LayoutParams match() {
        return new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
    }

    private LinearLayout.LayoutParams wrapWithGap() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.rightMargin = dp(8);
        return params;
    }

    @SuppressWarnings("deprecation")
    private void hideSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.systemBars());
                controller.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
    }

    // --- State -------------------------------------------------------------

    private File gameFolder() {
        String path = prefs.getString(Settings.GAME_DIR, null);
        return path == null ? null : new File(path);
    }

    private void save(String key, String value) {
        prefs.edit().putString(key, value).apply();
        refresh();
    }

    private void save(String key, boolean value) {
        prefs.edit().putBoolean(key, value).apply();
        refresh();
    }

    private void refresh() {
        boolean access = GameFiles.hasStorageAccess(this);
        File game = gameFolder();
        boolean ready = access && GameFiles.isGameFolder(game);
        accessButton.setVisibility(access ? View.GONE : View.VISIBLE);
        gamePath.setText(game != null ? game.getAbsolutePath() : "Belum ditemukan");
        if (!access) {
            status.setText("Izinkan akses file agar game bisa ditemukan.");
        } else if (searching) {
            status.setText("Mencari game di HP Anda…");
        } else if (!ready) {
            status.setText("Game tidak ditemukan: pilih folder berisi default.xex.");
        } else {
            status.setText("Siap.");
        }
        play.setEnabled(ready);
        play.invalidate();
        summary.setText(Settings.resolution(prefs) + "   ·   Penuh layar   ·   "
                + (Settings.fxaa(prefs) ? "FXAA" : "Tanpa AA") + "   ·   30 FPS"
                + (Settings.unlockAll(prefs) ? "   ·   Semua karakter" : ""));
        if (ready && !game.equals(artLoadedFor)) {
            artLoadedFor = game;
            new Thread(() -> {
                GameArt art = GameArt.load(game);
                runOnUiThread(() -> background.setArt(art.background));
            }).start();
        }
    }

    // --- Finding the game -------------------------------------------------

    private void askForStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent intent = new Intent(
                    android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            try {
                startActivity(intent);
            } catch (RuntimeException e) {
                startActivity(new Intent(
                        android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION));
            }
        } else {
            requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, ASK_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        refresh();
        if (GameFiles.hasStorageAccess(this) && !GameFiles.isGameFolder(gameFolder())) {
            searchForGame();
        }
    }

    private void searchForGame() {
        if (!GameFiles.hasStorageAccess(this)) {
            askForStorage();
            return;
        }
        if (searching) {
            return;
        }
        searching = true;
        refresh();
        new Thread(() -> {
            File found = GameFiles.search(this);
            runOnUiThread(() -> {
                searching = false;
                if (found != null) {
                    prefs.edit().putString(Settings.GAME_DIR, found.getAbsolutePath()).apply();
                }
                refresh();
            });
        }).start();
    }

    private void pickFolder() {
        if (!GameFiles.hasStorageAccess(this)) {
            askForStorage();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, PICK_FOLDER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PICK_FOLDER || resultCode != RESULT_OK || data == null || data.getData() == null) {
            return;
        }
        File picked = GameFiles.fromTreeUri(data.getData());
        if (picked == null) {
                status.setText("Folder itu tidak ada di HP atau kartu SD.");
                return;
            }
            // Folder game itu sendiri, atau beberapa tingkat di atasnya.
            File game = GameFiles.isGameFolder(picked) ? picked : GameFiles.findIn(picked, 3);
            if (game == null) {
                prefs.edit().putString(Settings.GAME_DIR, picked.getAbsolutePath()).apply();
                refresh();
                status.setText("Tidak ada default.xex dengan folder data di " + picked.getName() + ".");
                return;
            }
        prefs.edit().putString(Settings.GAME_DIR, game.getAbsolutePath()).apply();
        refresh();
    }

    // --- Playing -----------------------------------------------------------

    private void startGame() {
        if (!GameFiles.isGameFolder(gameFolder())) {
            refresh();
            return;
        }
        // This public APK is launcher-only until the matching native game
        // libraries are supplied. Do not open the game activity without them.
        prefs.edit().commit();
        status.setText("Folder game valid. Build ini hanya launcher; native game belum tersedia.");
    }

    // A controller's A picks the focused setting, Start plays.
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int code = event.getKeyCode();
        if (code == KeyEvent.KEYCODE_BUTTON_START || code == KeyEvent.KEYCODE_MENU) {
            if (event.getAction() == KeyEvent.ACTION_UP) {
                startGame();
            }
            return true;
        }
        if (code == KeyEvent.KEYCODE_BUTTON_A) {
            return super.dispatchKeyEvent(new KeyEvent(event.getDownTime(), event.getEventTime(),
                    event.getAction(), KeyEvent.KEYCODE_DPAD_CENTER, event.getRepeatCount()));
        }
        return super.dispatchKeyEvent(event);
    }
}
