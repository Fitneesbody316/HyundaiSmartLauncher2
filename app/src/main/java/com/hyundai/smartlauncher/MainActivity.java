
package com.hyundai.smartlauncher;

import android.app.*;
import android.os.Bundle;
import android.content.*;
import android.content.pm.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {

    private final int BG = Color.rgb(8, 10, 16);
    private final int WHITE = Color.WHITE;
    private final int MUTED = Color.rgb(185, 194, 208);
    private final int PINK = Color.rgb(0, 235, 100);
    private static final int REQUEST_PICK_APK = 4102;

    private LinearLayout root, appsRow;
    private final ArrayList<String> pinned = new ArrayList<>();
    private final String PREF = "home_prefs";
    private final String KEY = "pinned";
    private final Map<String, ApplicationInfo> appInfo = new HashMap<>();

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        loadPinned();
        buildHome();
    }

    private void loadPinned() {
        Set<String> saved = getSharedPreferences(PREF, MODE_PRIVATE)
            .getStringSet(KEY, null);

        if (saved != null) {
            pinned.addAll(saved);
        }

        if (pinned.isEmpty()) {
            addIfInstalled("com.google.android.youtube.tv");
            addIfInstalled("com.netflix.ninja");
            addIfInstalled("com.amazon.amazonvideo.livingroom");
            addIfInstalled("com.android.chrome");

            if (pinned.isEmpty()) {
                for (ApplicationInfo a :
                        getPackageManager().getInstalledApplications(0)) {

                    if (getPackageManager().getLaunchIntentForPackage(
                            a.packageName) != null
                            && !a.packageName.equals(getPackageName())) {

                        pinned.add(a.packageName);

                        if (pinned.size() >= 5) {
                            break;
                        }
                    }
                }
            }
        }
    }

    private void addIfInstalled(String p) {
        if (getPackageManager().getLaunchIntentForPackage(p) != null) {
            pinned.add(p);
        }
    }

    private void savePinned() {
        getSharedPreferences(PREF, MODE_PRIVATE)
            .edit()
            .putStringSet(KEY, new HashSet<>(pinned))
            .apply();
    }

    private void buildHome() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(28), dp(14), dp(28), dp(10));

        GradientDrawable bg = new GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            new int[]{
                Color.rgb(3, 14, 24),
                Color.rgb(5, 31, 43),
                Color.rgb(4, 10, 18),
                Color.BLACK
            }
        );
        root.setBackground(bg);
        root.setFocusable(false);
        root.setFocusableInTouchMode(false);
        root.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        setContentView(root);

        // Header inspired by the approved reference: brand left, clock/status right.
        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setOrientation(LinearLayout.HORIZONTAL);
        root.addView(top, new LinearLayout.LayoutParams(-1, dp(78)));

        LinearLayout brand = new LinearLayout(this);
        brand.setOrientation(LinearLayout.VERTICAL);
        brand.setGravity(Gravity.CENTER_VERTICAL);
        top.addView(brand, new LinearLayout.LayoutParams(0, -1, 1.4f));

        TextView logo = text("HYUNDAI", 31, WHITE, true);
        logo.setLetterSpacing(.025f);
        brand.addView(logo);
        TextView subLogo = text("S M A R T   L A U N C H E R", 10, MUTED, true);
        subLogo.setPadding(dp(3), 0, 0, 0);
        brand.addView(subLogo);

        LinearLayout status = new LinearLayout(this);
        status.setOrientation(LinearLayout.VERTICAL);
        status.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        top.addView(status, new LinearLayout.LayoutParams(0, -1, 1f));

        Date now = new Date();
        TextView greeting = text("●  " + greetingForHour(), 11, PINK, true);
        greeting.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        status.addView(greeting);
        TextView clock = text(
            new SimpleDateFormat("h:mm a", Locale.getDefault()).format(now),
            25, WHITE, true
        );
        clock.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        status.addView(clock);
        TextView date = text(
            new SimpleDateFormat("EEE, d MMM yyyy", new Locale("es", "CO"))
                .format(now),
            11, MUTED, false
        );
        date.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        status.addView(date);

        top.addView(gapW(dp(18)));
        top.addView(topButton("⚙  Ajustes", this::showLauncherSettings),
            new LinearLayout.LayoutParams(dp(112), dp(46)));

        root.addView(space(dp(9)));

        // Large source tiles occupy the main visual row.
        LinearLayout sources = new LinearLayout(this);
        sources.setOrientation(LinearLayout.HORIZONTAL);
        root.addView(sources, new LinearLayout.LayoutParams(-1, dp(228)));

        String[][] choices = {
            {"▣", "TV"},
            {"▱", "HDMI"},
            {"♧", "USB"},
            {"▦", "Aplicaciones"},
            {"◉", "AV"},
            {"▷", "Reproductor"},
            {"▱", "HDMI 1"},
            {"▱", "HDMI 2"},
            {"▱", "HDMI 3"}
        };

        // Four prominent cards match the reference; extra inputs stay in the row's scroll.
        HorizontalScrollView sourceScroll = new HorizontalScrollView(this);
        sourceScroll.setHorizontalScrollBarEnabled(false);
        sourceScroll.setClipToPadding(false);
        sources.addView(sourceScroll, new LinearLayout.LayoutParams(-1, -1));
        LinearLayout sourceStrip = new LinearLayout(this);
        sourceStrip.setOrientation(LinearLayout.HORIZONTAL);
        sourceScroll.addView(sourceStrip);

        String[][] mainChoices = {
            {"▣", "TV"}, {"▱", "HDMI"}, {"♧", "USB"}, {"▦", "Aplicaciones"}
        };
        for (int i = 0; i < mainChoices.length; i++) {
            final String label = mainChoices[i][1];
            LinearLayout tile = sourceTile(mainChoices[i][0], label, i == 0);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -1, 1f);
            lp.setMargins(dp(5), dp(3), dp(5), dp(5));
            sourceStrip.addView(tile, lp);
            tile.setOnClickListener(v -> {
                if (label.equals("Aplicaciones")) {
                    focusFirstApp();
                } else if (label.equals("HDMI")) {
                    showInputs();
                } else {
                    runInput(label);
                }
            });
        }

        root.addView(space(dp(10)));

        // Utility buttons with the color hierarchy from the reference.
        LinearLayout toolsRow = new LinearLayout(this);
        toolsRow.setOrientation(LinearLayout.HORIZONTAL);
        root.addView(toolsRow, new LinearLayout.LayoutParams(-1, dp(62)));
        toolsRow.addView(utilityButton("🧹  Limpiar caché", Color.rgb(86, 20, 200),
            this::showCacheInfo), new LinearLayout.LayoutParams(0, -1, 1.25f));
        toolsRow.addView(gapW(dp(9)));
        toolsRow.addView(utilityButton("▦  Gestionar apps", Color.rgb(0, 83, 164),
            () -> openSettings("android.settings.APPLICATION_SETTINGS")),
            new LinearLayout.LayoutParams(0, -1, 1f));
        toolsRow.addView(gapW(dp(9)));
        toolsRow.addView(utilityButton("⚙  Configuración", Color.rgb(44, 54, 67),
            () -> openSettings("android.settings.SETTINGS")),
            new LinearLayout.LayoutParams(0, -1, 1f));
        toolsRow.addView(gapW(dp(9)));
        toolsRow.addView(utilityButton("◉  Red", Color.rgb(22, 32, 46),
            () -> openSettings("android.settings.WIFI_SETTINGS")),
            new LinearLayout.LayoutParams(0, -1, .8f));

        root.addView(space(dp(10)));
        addSectionTitle("Apps favoritas");

        HorizontalScrollView hsv = new HorizontalScrollView(this);
        hsv.setHorizontalScrollBarEnabled(false);
        hsv.setClipToPadding(false);
        root.addView(hsv, new LinearLayout.LayoutParams(-1, dp(142)));
        appsRow = new LinearLayout(this);
        appsRow.setOrientation(LinearLayout.HORIZONTAL);
        appsRow.setFocusable(false);
        appsRow.setFocusableInTouchMode(false);
        hsv.addView(appsRow);
        refreshApps();

        root.post(() -> focusFirstApp());
    }

    private LinearLayout sourceTile(String symbol, String label, boolean selected) {
        LinearLayout tile = new LinearLayout(this);
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setGravity(Gravity.CENTER);
        tile.setFocusable(true);
        tile.setClickable(true);
        tile.setPadding(dp(6), dp(5), dp(6), dp(5));
        tile.setBackground(sourceCardBg(selected));

        TextView glyph = text(symbol, 58, WHITE, true);
        glyph.setGravity(Gravity.CENTER);
        glyph.setFocusable(false);
        tile.addView(glyph, new LinearLayout.LayoutParams(-1, 0, 1f));

        TextView name = text(label, 21, WHITE, true);
        name.setGravity(Gravity.CENTER);
        name.setFocusable(false);
        tile.addView(name, new LinearLayout.LayoutParams(-1, dp(43)));

        tile.setOnFocusChangeListener((v, focused) -> {
            v.setBackground(sourceCardBg(focused || selected));
            v.setScaleX(focused ? 1.025f : 1f);
            v.setScaleY(focused ? 1.025f : 1f);
        });
        return tile;
    }

    private GradientDrawable sourceCardBg(boolean selected) {
        GradientDrawable d = new GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            selected
                ? new int[]{Color.rgb(9, 74, 82), Color.rgb(12, 29, 48)}
                : new int[]{Color.argb(220, 11, 29, 49), Color.argb(235, 12, 21, 35)}
        );
        d.setCornerRadius(dp(20));
        d.setStroke(dp(selected ? 3 : 1),
            selected ? PINK : Color.argb(180, 67, 123, 164));
        return d;
    }

    private TextView utilityButton(String label, int color, Runnable action) {
        TextView v = text(label, 15, WHITE, true);
        v.setGravity(Gravity.CENTER);
        v.setFocusable(true);
        v.setClickable(true);
        v.setPadding(dp(8), 0, dp(8), 0);
        v.setBackground(round(color, Color.argb(180, 100, 150, 190), dp(16)));
        v.setOnClickListener(w -> action.run());
        v.setOnFocusChangeListener((w, focused) -> {
            w.setBackground(round(focused ? Color.rgb(0, 100, 120) : color,
                focused ? PINK : Color.argb(180, 100, 150, 190), dp(16)));
            w.setScaleX(focused ? 1.025f : 1f);
            w.setScaleY(focused ? 1.025f : 1f);
        });
        return v;
    }

    private void focusFirstApp() {
        if (appsRow == null) {
            return;
        }

        for (int i = 0; i < appsRow.getChildCount(); i++) {
            View child = appsRow.getChildAt(i);

            if (child.getVisibility() == View.VISIBLE
                    && child.isFocusable()) {

                child.requestFocus();
                return;
            }
        }

        // Si no hay aplicaciones, intentar seleccionar Agregar app.
        if (appsRow.getChildCount() > 0) {
            appsRow.getChildAt(
                appsRow.getChildCount() - 1
            ).requestFocus();
        }
    }

    private void refreshApps() {
        appsRow.removeAllViews();

        PackageManager pm = getPackageManager();
        appInfo.clear();

        for (String p : new ArrayList<>(pinned)) {
            try {
                ApplicationInfo ai = pm.getApplicationInfo(p, 0);

                if (pm.getLaunchIntentForPackage(p) == null) {
                    continue;
                }

                appInfo.put(p, ai);

                View card = appCard(ai);

                appsRow.addView(
                    card,
                    new LinearLayout.LayoutParams(dp(128), dp(112))
                );

                appsRow.addView(gapW(dp(12)));

            } catch (Exception ignored) {
            }
        }

        TextView add = text("＋\nAgregar app", 18, WHITE, true);
        add.setGravity(Gravity.CENTER);
        add.setFocusable(true);
        add.setClickable(true);
        add.setBackground(cardBg(false));

        appsRow.addView(
            add,
            new LinearLayout.LayoutParams(dp(128), dp(112))
        );

        add.setOnClickListener(v -> showAppPicker());
        add.setOnLongClickListener(v -> {
            showAppPicker();
            return true;
        });

        focusStyle(add);

        // Recuperar el foco después de actualizar las aplicaciones.
        appsRow.post(() -> focusFirstApp());
    }

    private View appCard(ApplicationInfo ai) {
        LinearLayout card = new LinearLayout(this);
        card.setGravity(Gravity.CENTER);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setFocusable(true);
        card.setFocusableInTouchMode(false);
        card.setClickable(true);
        card.setBackground(cardBg(false));

        ImageView icon = new ImageView(this);

        try {
            icon.setImageDrawable(
                getPackageManager().getApplicationIcon(ai)
            );
        } catch (Exception ignored) {
        }

        // El foco pertenece a la tarjeta, no a su icono.
        icon.setFocusable(false);

        card.addView(
            icon,
            new LinearLayout.LayoutParams(dp(70), dp(70))
        );

        TextView name = text(
            getPackageManager().getApplicationLabel(ai).toString(),
            14, WHITE, true
        );

        name.setGravity(Gravity.CENTER);
        name.setMaxLines(1);
        name.setFocusable(false);
        name.setPadding(dp(4), dp(5), dp(4), 0);

        card.addView(
            name,
            new LinearLayout.LayoutParams(-1, -2)
        );

        card.setOnClickListener(v -> launch(ai.packageName));

        card.setOnLongClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle(getPackageManager().getApplicationLabel(ai))
                .setItems(
                    new String[]{
                        "Mover al inicio",
                        "Quitar de favoritos",
                        "Agregar aplicación"
                    },
                    (d, which) -> {
                        if (which == 0) {
                            pinned.remove(ai.packageName);
                            pinned.add(0, ai.packageName);
                            savePinned();
                            refreshApps();

                        } else if (which == 1) {
                            pinned.remove(ai.packageName);
                            savePinned();
                            refreshApps();

                        } else {
                            showAppPicker();
                        }
                    }
                )
                .show();

            return true;
        });

        focusStyle(card);
        return card;
    }

    private void showAppPicker() {
        PackageManager pm = getPackageManager();
        ArrayList<ApplicationInfo> all = new ArrayList<>();

        for (ApplicationInfo a : pm.getInstalledApplications(0)) {
            if (pm.getLaunchIntentForPackage(a.packageName) != null
                    && !a.packageName.equals(getPackageName())) {
                all.add(a);
            }
        }

        Collections.sort(all, (a, b) ->
            pm.getApplicationLabel(a).toString()
                .compareToIgnoreCase(
                    pm.getApplicationLabel(b).toString()
                )
        );

        String[] labels = new String[all.size()];

        for (int i = 0; i < all.size(); i++) {
            labels[i] = pm.getApplicationLabel(all.get(i)).toString();
        }

        new AlertDialog.Builder(this)
            .setTitle("Agregar aplicación")
            .setItems(labels, (d, i) -> {
                String p = all.get(i).packageName;
                pinned.remove(p);
                pinned.add(p);
                savePinned();
                refreshApps();
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private String greetingForHour() {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        if (hour >= 5 && hour < 12) return "BUENOS DÍAS";
        if (hour >= 12 && hour < 19) return "BUENAS TARDES";
        return "BUENAS NOCHES";
    }

    private void showCacheInfo() {
        new AlertDialog.Builder(this)
            .setTitle("Limpiar caché de aplicaciones")
            .setMessage("Android 7 no permite que un launcher normal borre automáticamente la caché privada de todas las aplicaciones. Puedes abrir el almacenamiento de Android y limpiar los datos temporales desde allí. No se borrarán los datos de las aplicaciones desde este botón.")
            .setPositiveButton("Abrir almacenamiento", (d, w) -> {
                try {
                    startActivity(new Intent("android.settings.INTERNAL_STORAGE_SETTINGS"));
                } catch (Exception e) {
                    openSettings("android.settings.SETTINGS");
                }
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private void showLauncherSettings() {
        final String[] options = {
            "Instalar APK desde USB o almacenamiento",
            "Ajustes de Android",
            "Configuración de Wi-Fi",
            "Administrar aplicaciones instaladas",
            "Información del launcher"
        };
        new AlertDialog.Builder(this)
            .setTitle("Configuración Hyundai")
            .setItems(options, (dialog, which) -> {
                if (which == 0) pickApk();
                else if (which == 1) openSettings("android.settings.SETTINGS");
                else if (which == 2) openSettings("android.settings.WIFI_SETTINGS");
                else if (which == 3) openSettings("android.settings.APPLICATION_SETTINGS");
                else new AlertDialog.Builder(this)
                    .setTitle("Hyundai Smart Launcher 2.0")
                    .setMessage("Launcher independiente para Android 7.1.1. El launcher original del televisor no se modifica. La instalación de APK requiere confirmar en el instalador de Android.")
                    .setPositiveButton("Aceptar", null).show();
            })
            .setNegativeButton("Cerrar", null)
            .show();
    }

    private void pickApk() {
        try {
            Intent pick = new Intent(Intent.ACTION_GET_CONTENT);
            pick.setType("application/vnd.android.package-archive");
            pick.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(pick, "Seleccionar archivo APK"), REQUEST_PICK_APK);
        } catch (Exception e) {
            showToast("No se pudo abrir el selector. Prueba con un administrador de archivos.");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_APK && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                Intent install = new Intent(Intent.ACTION_VIEW);
                install.setDataAndType(data.getData(), "application/vnd.android.package-archive");
                install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(install);
            } catch (Exception e) {
                showToast("Android no pudo abrir el instalador para este archivo.");
            }
        }
    }

    private void showInputs() {
        String[] options = {
            "TV", "HDMI 1", "HDMI 2", "HDMI 3",
            "AV", "Reproductor", "USB"
        };

        new AlertDialog.Builder(this)
            .setTitle("Entradas del televisor")
            .setItems(options, (d, i) -> runInput(options[i]))
            .setNegativeButton("Cerrar", null)
            .show();
    }

    private void runInput(String input) {
        if (input.equals("Reproductor")) {
            launchFirst(new String[]{
                "com.realtek.media.player",
                "com.rtk.mediabrowser"
            });
            return;
        }

        if (input.equals("USB")) {
            launchFirst(new String[]{
                "com.rtk.mediabrowser",
                "com.realtek.media.player"
            });
            return;
        }

        if (input.equals("TV")) {
            launchFirst(new String[]{
                "com.android.tv",
                "com.realtek.dtv",
                "com.toptech.launcher"
            });
            return;
        }

        // El cambio de HDMI depende del firmware del televisor.
        launchFirst(new String[]{
            "com.realtek.tv.hdmitvinput",
            "com.android.tv"
        });
    }

    private void launchFirst(String[] packages) {
        for (String p : packages) {
            if (getPackageManager().getLaunchIntentForPackage(p) != null) {
                launch(p);
                return;
            }
        }

        showToast("Esta entrada depende del firmware del televisor.");
    }

    private void launch(String p) {
        try {
            Intent i = getPackageManager().getLaunchIntentForPackage(p);

            if (i != null) {
                startActivity(i);
            } else {
                showToast(
                    "La aplicación no tiene una pantalla de inicio disponible."
                );
            }

        } catch (Exception e) {
            showToast("No se pudo abrir la aplicación.");
        }
    }

    private void openSettings(String action) {
        try {
            startActivity(new Intent(action));

        } catch (Exception e) {
            try {
                startActivity(
                    new Intent(android.provider.Settings.ACTION_SETTINGS)
                );
            } catch (Exception ignored) {
                showToast("Ajustes no disponible.");
            }
        }
    }

    private TextView topButton(String s, Runnable r) {
        TextView v = text(s, 14, WHITE, true);
        v.setGravity(Gravity.CENTER);
        v.setFocusable(true);
        v.setClickable(true);

        v.setBackground(
            round(
                Color.argb(110, 255, 255, 255),
                Color.argb(80, 255, 255, 255),
                dp(25)
            )
        );

        v.setOnClickListener(w -> r.run());
        focusStyle(v);

        return v;
    }

    private void addSectionTitle(String s) {
        TextView t = text(s, 20, WHITE, true);
        t.setPadding(0, dp(3), 0, dp(8));
        root.addView(t);
    }

    private TextView text(
            String s, int size, int color, boolean bold) {

        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(color);

        if (bold) {
            t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }

        t.setGravity(Gravity.CENTER_VERTICAL);
        t.setFocusable(false);

        return t;
    }

    private View space(int h) {
        View v = new View(this);
        v.setLayoutParams(
            new LinearLayout.LayoutParams(-1, h)
        );
        return v;
    }

    private View gapW(int w) {
        View v = new View(this);
        v.setFocusable(false);
        v.setLayoutParams(
            new LinearLayout.LayoutParams(w, 1)
        );
        return v;
    }

    private int dp(int x) {
        return (int) (
            x * getResources().getDisplayMetrics().density + .5f
        );
    }

    private GradientDrawable round(
            int color, int stroke, int radius) {

        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(radius);
        d.setStroke(dp(1), stroke);

        return d;
    }

    private GradientDrawable cardBg(boolean focused) {
        return round(
            focused ? Color.WHITE : Color.argb(95, 45, 40, 43),
            focused ? PINK : Color.argb(65, 255, 255, 255),
            dp(18)
        );
    }

    private void focusStyle(View v) {
        v.setOnFocusChangeListener((w, focused) -> {
            w.setBackground(
                round(
                    focused
                        ? Color.argb(220, 255, 255, 255)
                        : Color.argb(95, 45, 40, 43),
                    focused
                        ? PINK
                        : Color.argb(65, 255, 255, 255),
                    dp(18)
                )
            );

            if (focused) {
                w.setScaleX(1.035f);
                w.setScaleY(1.035f);
            } else {
                w.setScaleX(1f);
                w.setScaleY(1f);
            }
        });
    }

    private void showToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onKeyDown(
            int keyCode, android.view.KeyEvent event) {

        if (keyCode == android.view.KeyEvent.KEYCODE_MENU) {
            showAppPicker();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
