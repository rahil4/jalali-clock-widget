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

/** تنظیمات ویجت: رنگ، گردی گوشه‌ها، رنگ و شفافیت داخل قاب؛ با پیش‌نمایش زنده. */
public class SettingsActivity extends Activity {
    private static final int PREVIEW_W_DP = 150;
    private static final int PREVIEW_H_DP = 270;
    private static final int[] SWATCHES = {
            Color.WHITE, Color.BLACK, 0xFFF4B23E, 0xFF5EEAD4, 0xFFE5583A, 0xFF7DB7FF, 0xFFFF8FB1};

    private int widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private WidgetPrefs.Config cfg;
    private boolean lightBackdrop;

    private FrameLayout preview;
    private TextView radiusLabel;
    private TextView opacityLabel;
    private float density;

    private int dp(float v) {
        return Math.round(v * density);
    }

    private interface OnProgress {
        void on(int progress);
    }

    private interface OnColor {
        void on(int color);
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
        cfg = WidgetPrefs.load(this, widgetId);

        ScrollView scroll = new ScrollView(this);
        scroll.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(dp(20), dp(24), dp(20), dp(24));
        scroll.addView(col);

        col.addView(text("تنظیمات ویجت", 22, 0, 12));

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

        addColorSection(col, "رنگ نوشته و خط قاب", cfg.color, c -> cfg.color = c);

        col.addView(text("گردی گوشه‌ها", 17, 0, 8, 20));
        radiusLabel = text("", 13, 0, 0, 8);
        col.addView(radiusLabel);
        SeekBar radiusBar = seek(WidgetPrefs.MAX_RADIUS_DP, p -> {
            cfg.radiusDp = p;
            refreshPreview();
        });
        radiusBar.setProgress(cfg.radiusDp);
        col.addView(radiusBar);

        addColorSection(col, "رنگ داخل قاب", cfg.fillColor, c -> cfg.fillColor = c);

        col.addView(text("شفافیت داخل قاب", 17, 0, 8, 20));
        opacityLabel = text("", 13, 0, 0, 8);
        col.addView(opacityLabel);
        SeekBar opacityBar = seek(100, p -> {
            cfg.fillOpacity = p;
            refreshPreview();
        });
        opacityBar.setProgress(cfg.fillOpacity);
        col.addView(opacityBar);
        col.addView(text("۰٪ یعنی داخل قاب کاملاً شفاف و ۱۰۰٪ یعنی کاملاً رنگی.", 12, 0, 0, 4));

        Button save = new Button(this);
        save.setText("ذخیره");
        save.setOnClickListener(v -> save());
        LinearLayout.LayoutParams saveLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        saveLp.topMargin = dp(20);
        col.addView(save, saveLp);

        setContentView(scroll);
        refreshPreview();
    }

    /** بخش انتخاب رنگ: رنگ‌های آماده + طیف + روشنایی */
    private void addColorSection(LinearLayout col, String title, int initial, OnColor onColor) {
        col.addView(text(title, 17, 0, 8, 20));

        SeekBar[] bars = new SeekBar[2]; // [طیف، روشنایی]
        LinearLayout swatchRow = new LinearLayout(this);
        swatchRow.setGravity(Gravity.CENTER);
        col.addView(swatchRow);

        col.addView(text("طیف رنگ", 13, 0, 0, 8));
        bars[0] = seek(360, p -> {
            onColor.on(Color.HSVToColor(new float[]{bars[0].getProgress(), 0.65f,
                    Math.max(0.2f, bars[1].getProgress() / 100f)}));
            refreshPreview();
        });
        col.addView(bars[0]);
        col.addView(text("روشنایی", 13, 0, 0, 8));
        bars[1] = seek(100, p -> {
            onColor.on(Color.HSVToColor(new float[]{bars[0].getProgress(), 0.65f,
                    Math.max(0.2f, p / 100f)}));
            refreshPreview();
        });
        col.addView(bars[1]);

        float[] hsv = new float[3];
        Color.colorToHSV(initial, hsv);
        bars[0].setProgress(Math.round(hsv[0]));
        bars[1].setProgress(Math.round(hsv[2] * 100));

        for (int c : SWATCHES) {
            View s = new View(this);
            GradientDrawable d = new GradientDrawable();
            d.setShape(GradientDrawable.OVAL);
            d.setColor(c);
            d.setStroke(dp(1.5f), 0x66808080);
            s.setBackground(d);
            s.setOnClickListener(v -> {
                onColor.on(c);
                float[] h = new float[3];
                Color.colorToHSV(c, h);
                bars[0].setProgress(Math.round(h[0]));
                bars[1].setProgress(Math.round(h[2] * 100));
                refreshPreview();
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(36), dp(36));
            lp.setMargins(dp(5), 0, dp(5), 0);
            swatchRow.addView(s, lp);
        }
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

    private void refreshPreview() {
        radiusLabel.setText(JalaliDate.toPersianDigits(cfg.radiusDp + " dp"));
        opacityLabel.setText(JalaliDate.toPersianDigits(cfg.fillOpacity + "٪"));
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                lightBackdrop ? new int[]{0xFF9ED3D0, 0xFFF4E9D2} : new int[]{0xFF2A1F4D, 0xFFFF9A6B});
        bg.setCornerRadius(dp(20));
        preview.setBackground(bg);
        preview.removeAllViews();
        View w = WidgetRenderer.build(this, widgetId, cfg, PREVIEW_W_DP, PREVIEW_H_DP).apply(this, preview);
        preview.addView(w, new FrameLayout.LayoutParams(dp(PREVIEW_W_DP), dp(PREVIEW_H_DP), Gravity.CENTER));
    }

    private void save() {
        WidgetPrefs.save(this, widgetId, cfg);
        AppWidgetManager mgr = AppWidgetManager.getInstance(this);
        ClockWidgetProvider.update(this, mgr, widgetId);
        setResult(RESULT_OK, new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId));
        finish();
    }

    private TextView text(String t, int sp, int l, int b) {
        return text(t, sp, l, b, 0);
    }

    private TextView text(String t, int sp, int l, int b, int top) {
        TextView v = new TextView(this);
        v.setText(t);
        v.setTextSize(sp);
        v.setPadding(dp(l), dp(top), 0, dp(b));
        return v;
    }
}
