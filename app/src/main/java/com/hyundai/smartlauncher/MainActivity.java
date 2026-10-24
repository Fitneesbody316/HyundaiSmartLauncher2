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
    private final int BG = Color.rgb(12, 2, 4), WHITE = Color.WHITE, MUTED = Color.rgb(204,188,190), PINK = Color.rgb(255,38,94);
    private LinearLayout root, appsRow;
    private final ArrayList<String> pinned = new ArrayList<>();
    private final String PREF = "home_prefs", KEY = "pinned";
    private final Map<String, ApplicationInfo> appInfo = new HashMap<>();

    @Override public void onCreate(Bundle b) { super.onCreate(b); getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY); loadPinned(); buildHome(); }
    private void loadPinned() {
        Set<String> saved = getSharedPreferences(PREF, MODE_PRIVATE).getStringSet(KEY, null);
        if (saved != null) pinned.addAll(saved);
        if (pinned.isEmpty()) {
            addIfInstalled("com.google.android.youtube.tv"); addIfInstalled("com.netflix.ninja"); addIfInstalled("com.amazon.amazonvideo.livingroom"); addIfInstalled("com.android.chrome");
            if (pinned.isEmpty()) { for (ApplicationInfo a : getPackageManager().getInstalledApplications(0)) if (getPackageManager().getLaunchIntentForPackage(a.packageName)!=null && !a.packageName.equals(getPackageName())) { pinned.add(a.packageName); if (pinned.size()>=5) break; } }
        }
    }
    private void addIfInstalled(String p) { if (getPackageManager().getLaunchIntentForPackage(p)!=null) pinned.add(p); }
    private void savePinned() { getSharedPreferences(PREF, MODE_PRIVATE).edit().putStringSet(KEY, new HashSet<>(pinned)).apply(); }

    private void buildHome() {
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(30), dp(20), dp(30), dp(12));
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{Color.rgb(64,0,8), Color.rgb(28,0,4), BG, Color.BLACK}); root.setBackground(bg);
        setContentView(root);
        LinearLayout top = new LinearLayout(this); top.setGravity(Gravity.CENTER_VERTICAL); top.setOrientation(LinearLayout.HORIZONTAL); root.addView(top, new LinearLayout.LayoutParams(-1, dp(86)));
        LinearLayout clockCol = new LinearLayout(this); clockCol.setOrientation(LinearLayout.VERTICAL); top.addView(clockCol, new LinearLayout.LayoutParams(0,-2,1));
        TextView greeting = text("HYUNDAI  •  BUENAS TARDES", 14, MUTED, true); greeting.setLetterSpacing(.12f); clockCol.addView(greeting);
        TextView clock = text(new SimpleDateFormat("h:mm", Locale.getDefault()).format(new Date()) + "  " + new SimpleDateFormat("a", Locale.getDefault()).format(new Date()), 38, WHITE, false); clockCol.addView(clock);
        TextView date = text(new SimpleDateFormat("EEEE d 'de' MMMM", new Locale("es","CO")).format(new Date()).toUpperCase(new Locale("es","CO")), 12, MUTED, true); clockCol.addView(date);
        top.addView(topButton("▰  Entradas", () -> showInputs()), new LinearLayout.LayoutParams(dp(145), dp(46)));
        top.addView(gapW(dp(10)));
        top.addView(topButton("◉  Red", () -> openSettings("android.settings.WIFI_SETTINGS")), new LinearLayout.LayoutParams(dp(120), dp(46)));
        top.addView(gapW(dp(10)));
        top.addView(topButton("⚙  Ajustes", () -> openSettings("android.settings.SETTINGS")), new LinearLayout.LayoutParams(dp(132), dp(46)));
        root.addView(space(dp(12)));
        addSectionTitle("Aplicaciones");
        HorizontalScrollView hsv = new HorizontalScrollView(this); hsv.setHorizontalScrollBarEnabled(false); hsv.setClipToPadding(false); root.addView(hsv, new LinearLayout.LayoutParams(-1, dp(140)));
        appsRow = new LinearLayout(this); appsRow.setOrientation(LinearLayout.HORIZONTAL); hsv.addView(appsRow);
        refreshApps();
        root.addView(space(dp(10)));
        addSectionTitle("Entradas");
        LinearLayout inputs = new LinearLayout(this); inputs.setOrientation(LinearLayout.HORIZONTAL); root.addView(inputs, new LinearLayout.LayoutParams(-1, dp(94)));
        String[][] choices = {{"▣","TV"},{"▱","HDMI 1"},{"▱","HDMI 2"},{"▱","HDMI 3"},{"◉","AV"},{"▷","Reproductor"},{"♧","USB"}};
        for (String[] c : choices) { TextView v = text(c[0]+"\n"+c[1], 14, WHITE, false); v.setGravity(Gravity.CENTER); v.setFocusable(true); v.setBackground(cardBg(false)); LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,-1,1); lp.setMargins(dp(3),dp(4),dp(3),dp(4)); inputs.addView(v,lp); v.setOnClickListener(w -> runInput(c[1])); focusStyle(v); }
        root.addView(space(dp(10)));
        addSectionTitle("Accesos rápidos");
        TextView hint = text("Mantén presionado OK sobre una aplicación para moverla, quitarla o agregar otra.", 13, MUTED, false); root.addView(hint);
        root.setFocusableInTouchMode(true); root.requestFocus();
    }
    private void refreshApps() {
        appsRow.removeAllViews(); PackageManager pm = getPackageManager(); appInfo.clear();
        for (String p : new ArrayList<>(pinned)) {
            try { ApplicationInfo ai=pm.getApplicationInfo(p,0); if(pm.getLaunchIntentForPackage(p)==null) continue; appInfo.put(p,ai); appsRow.addView(appCard(ai),new LinearLayout.LayoutParams(dp(190),dp(112))); appsRow.addView(gapW(dp(12))); } catch(Exception ignored) {}
        }
        TextView add = text("＋\nAgregar app",18,WHITE,true); add.setGravity(Gravity.CENTER); add.setFocusable(true); add.setBackground(cardBg(false)); appsRow.addView(add,new LinearLayout.LayoutParams(dp(150),dp(112))); add.setOnClickListener(v->showAppPicker()); add.setOnLongClickListener(v->{showAppPicker();return true;}); focusStyle(add);
    }
    private View appCard(ApplicationInfo ai) {
        LinearLayout card = new LinearLayout(this); card.setGravity(Gravity.CENTER); card.setOrientation(LinearLayout.VERTICAL); card.setFocusable(true); card.setClickable(true); card.setBackground(cardBg(false));
        ImageView icon = new ImageView(this); try { icon.setImageDrawable(getPackageManager().getApplicationIcon(ai)); } catch(Exception ignored) {} card.addView(icon,new LinearLayout.LayoutParams(dp(52),dp(52)));
        TextView name=text(getPackageManager().getApplicationLabel(ai).toString(),14,WHITE,true); name.setGravity(Gravity.CENTER); name.setMaxLines(1); name.setPadding(dp(4),dp(5),dp(4),0); card.addView(name,new LinearLayout.LayoutParams(-1,-2));
        card.setOnClickListener(v->launch(ai.packageName));
        card.setOnLongClickListener(v->{new AlertDialog.Builder(this).setTitle(getPackageManager().getApplicationLabel(ai)).setItems(new String[]{"Mover al inicio","Quitar de favoritos","Agregar aplicación"},(d,which)->{ if(which==0){pinned.remove(ai.packageName);pinned.add(0,ai.packageName);savePinned();refreshApps();} else if(which==1){pinned.remove(ai.packageName);savePinned();refreshApps();} else showAppPicker();}).show(); return true;}); focusStyle(card); return card;
    }
    private void showAppPicker() {
        PackageManager pm=getPackageManager(); ArrayList<ApplicationInfo> all=new ArrayList<>(); for(ApplicationInfo a:pm.getInstalledApplications(0)) if(pm.getLaunchIntentForPackage(a.packageName)!=null && !a.packageName.equals(getPackageName())) all.add(a);
        Collections.sort(all,(a,b)->pm.getApplicationLabel(a).toString().compareToIgnoreCase(pm.getApplicationLabel(b).toString()));
        String[] labels=new String[all.size()]; for(int i=0;i<all.size();i++) labels[i]=pm.getApplicationLabel(all.get(i)).toString();
        new AlertDialog.Builder(this).setTitle("Agregar aplicación").setItems(labels,(d,i)->{String p=all.get(i).packageName; pinned.remove(p);pinned.add(p);savePinned();refreshApps();}).setNegativeButton("Cancelar",null).show();
    }
    private void showInputs() { new AlertDialog.Builder(this).setTitle("Entradas del televisor").setItems(new String[]{"TV","HDMI 1","HDMI 2","HDMI 3","AV","Reproductor","USB"},(d,i)->runInput(new String[]{"TV","HDMI 1","HDMI 2","HDMI 3","AV","Reproductor","USB"}[i])).setNegativeButton("Cerrar",null).show(); }
    private void runInput(String input) {
        if(input.equals("Reproductor")){launchFirst(new String[]{"com.realtek.media.player","com.rtk.mediabrowser"});return;}
        if(input.equals("USB")){launchFirst(new String[]{"com.rtk.mediabrowser","com.realtek.media.player"});return;}
        // Input switching on Realtek firmware is vendor-specific; open the system TV app instead of invoking undocumented APIs.
        if(input.equals("TV")){launchFirst(new String[]{"com.android.tv","com.realtek.dtv","com.toptech.launcher"});return;}
        launchFirst(new String[]{"com.realtek.tv.hdmitvinput","com.android.tv"});
    }
    private void launchFirst(String[] packages){for(String p:packages)if(getPackageManager().getLaunchIntentForPackage(p)!=null){launch(p);return;} showToast("Esta entrada depende del firmware del televisor.");}
    private void launch(String p){try{Intent i=getPackageManager().getLaunchIntentForPackage(p);if(i!=null)startActivity(i);else showToast("La aplicación no tiene una pantalla de inicio disponible.");}catch(Exception e){showToast("No se pudo abrir la aplicación.");}}
    private void openSettings(String action){try{startActivity(new Intent(action));}catch(Exception e){try{startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));}catch(Exception ignored){showToast("Ajustes no disponible.");}}}
    private TextView topButton(String s,Runnable r){TextView v=text(s,14,WHITE,true);v.setGravity(Gravity.CENTER);v.setFocusable(true);v.setBackground(round(Color.argb(110,255,255,255),Color.argb(80,255,255,255),dp(25)));v.setOnClickListener(w->r.run());focusStyle(v);return v;}
    private void addSectionTitle(String s){TextView t=text(s,20,WHITE,true);t.setPadding(0,dp(3),0,dp(8));root.addView(t);}
    private TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setGravity(Gravity.CENTER_VERTICAL);return t;}
    private View space(int h){View v=new View(this);v.setLayoutParams(new LinearLayout.LayoutParams(-1,h));return v;}
    private View gapW(int w){View v=new View(this);v.setLayoutParams(new LinearLayout.LayoutParams(w,1));return v;}
    private int dp(int x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
    private GradientDrawable round(int color,int stroke,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(radius);d.setStroke(dp(1),stroke);return d;}
    private GradientDrawable cardBg(boolean focused){return round(focused?Color.WHITE:Color.argb(95,45,40,43),focused?PINK:Color.argb(65,255,255,255),dp(18));}
    private void focusStyle(View v){v.setOnFocusChangeListener((w,focused)->{w.setBackground(round(focused?Color.argb(220,255,255,255):Color.argb(95,45,40,43),focused?PINK:Color.argb(65,255,255,255),dp(18))); if(focused){w.setScaleX(1.035f);w.setScaleY(1.035f);}else{w.setScaleX(1f);w.setScaleY(1f);}});}
    private void showToast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
    @Override public boolean onKeyDown(int keyCode, android.view.KeyEvent event){if(keyCode==android.view.KeyEvent.KEYCODE_MENU){showAppPicker();return true;}return super.onKeyDown(keyCode,event);}
}
