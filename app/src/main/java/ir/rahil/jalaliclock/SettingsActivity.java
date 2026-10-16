package ir.rahil.jalaliclock;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

/** تنظیمات ویجت: رنگ و گردی گوشه‌های قاب، با پیش‌نمایش زنده. */
public class SettingsActivity extends Activity {
    private static final int PREVIEW_W_DP = 150;
    private static final int PREVIEW_H_DP = 270;
    private static final int[] SWATCHES = {
            Color.WHITE, Color.BLACK, 0xFFF4B23E, 0xFF5EEAD4, 0xFFE5583A, 0xFF7DB7FF, 0xFFFF8FB1};

    private int widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private int color;
    private int radiusDp;
    private boolean lightBackdrop;

    private FrameLayout preview;
    private SeekBar hueBar;
    private SeekBar valueBar;
    private TextView radiusLabel;
    private boolean syncing;

    private float density;

    private int dp(float v) {
        return Math.round(v * density);
    }

    @Override
    protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        density = getResources().getDisplayMetrics().density;
        setResult(RESULT_CANCELED);

        Intent in = getIntent();
        if (in.getExtras() != null) {
            widgetId = in.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }
        setResult(RESULT_CANCELED, new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId));

        color = WidgetPrefs.color(this, widgetId);
        radiusDp = WidgetPrefs.radiusDp(this, widgetId);

        ScrollView scroll = new ScrollView(this);
        scroll.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(dp(20), dp(24), dp(20), dp(24));
        scroll.addView(col);

        col.addView(title("تنظیمات ویجت"));

        preview = new FrameLayout(this);
        col.addView(preview, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(PREVIEW_H_DP + 24)));

        Button backdrop = new Button(this);
        backdrop.setText("پیش‌نمایش روی زمینهٔ روشن / تیره");
        backdrop.setOnClickListener(v -> {
            lightBackdrop = !lightBackdrop;
            refreshPreview();
        });
        col.addView(backdrop);

        col.addView(section("رنگ"));
        LinearLayout swatchRow = new LinearLayout(this);
        swatchRow.setGravity(Gravity.CENTER);
        for (int c : SWATCHES) {
            View s = new View(this);
            GradientDrawable d = new GradientDrawable();
            d.setShape(GradientDrawable.OVAL);
            d.setColor(c);
            d.setStroke(dp(1.5f), 0x66808080);
            s.setBackground(d);
            s.setOnClickListener(v -> {
                color = c;
                syncSliders();
                refreshPreview();
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(36), dp(36));
            lp.setMargins(dp(5), 0, dp(5), 0);
            swatchRow.addView(s, lp);
        }
        col.addView(swatchRow);

        col.addView(label("رنگ دلخواه (طیف)"));
        hueBar = seek(360, p -> setCustomColor());
        col.addView(hueBar);
        col.addView(label("روشنایی"));
        valueBar = seek(100, p -> setCustomColor());
        col.addView(valueBar);

        col.addView(section("گردی گوشه‌ها"));
        radiusLabel = label("");
        col.addView(radiusLabel);
        SeekBar radiusBar = seek(WidgetPrefs.MAX_RADIUS_DP, p -> {
            radiusDp = p;
            refreshPreview();
        });
        radiusBar.setProgress(radiusDp);
        col.addView(radiusBar);

        Button save = new Button(this);
        save.setText("ذخیره");
        save.setOnClickListener(v -> save());
        LinearLayout.LayoutParams saveLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        saveLp.topMargin = dp(20);
        col.addView(save, saveLp);

        setContentView(scroll);
        syncSliders();
        refreshPreview();
    }

    private interface OnProgress {
        void on(int progress);
    }

    private SeekBar seek(int max, OnProgress cb) {
        SeekBar s = new SeekBar(this);
        s.setMax(max);
        s.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar b, int p, boolean fromUser) {
                if (fromUser) cb.on(p);
            }

            @Override
            public void onStartTrackingTouch(SeekBar b) {}

            @Override
            public void onStopTrackingTouch(SeekBar b) {}
        });
        return s;
    }

    private void setCustomColor() {
        float hue = hueBar.getProgress();
        float value = Math.max(0.2f, valueBar.getProgress() / 100f);
        color = Color.HSVToColor(new float[]{hue, 0.65f, value});
        refreshPreview();
    }

    /** لغزنده‌ها را با رنگ فعلی هماهنگ می‌کند. */
    private void syncSliders() {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hueBar.setProgress(Math.round(hsv[0]));
        valueBar.setProgress(Math.round(hsv[2] * 100));
    }

    private void refreshPreview() {
        radiusLabel.setText(JalaliDate.toPersianDigits(radiusDp + " dp"));
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                lightBackdrop ? new int[]{0xFF9ED3D0, 0xFFF4E9D2} : new int[]{0xFF2A1F4D, 0xFFFF9A6B});
        bg.setCornerRadius(dp(20));
        preview.setBackground(bg);
        preview.removeAllViews();
        View w = WidgetRenderer.build(this, widgetId, color, radiusDp, PREVIEW_W_DP, PREVIEW_H_DP)
                .apply(this, preview);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(dp(PREVIEW_W_DP), dp(PREVIEW_H_DP),
                Gravity.CENTER);
        preview.addView(w, lp);
    }

    private void save() {
        WidgetPrefs.save(this, widgetId, color, radiusDp);
        AppWidgetManager mgr = AppWidgetManager.getInstance(this);
        ClockWidgetProvider.update(this, mgr, widgetId);
        setResult(RESULT_OK, new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId));
        finish();
    }

    private TextView title(String t) {
        TextView v = new TextView(this);
        v.setText(t);
        v.setTextSize(22);
        v.setPadding(0, 0, 0, dp(12));
        return v;
    }

    private TextView section(String t) {
        TextView v = new TextView(this);
        v.setText(t);
        v.setTextSize(17);
        v.setPadding(0, dp(20), 0, dp(8));
        return v;
    }

    private TextView label(String t) {
        TextView v = new TextView(this);
        v.setText(t);
        v.setTextSize(13);
        v.setPadding(0, dp(8), 0, 0);
        return v;
    }
}
