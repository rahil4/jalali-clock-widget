package ir.rahil.jalaliclock;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

/** تنظیمات هر ویجت: رنگ نوشته و خط قاب، گردی گوشه‌ها، رنگ و شفافیت داخل قاب. */
public final class WidgetPrefs {
    public static final int DEFAULT_COLOR = Color.WHITE;
    public static final int DEFAULT_RADIUS_DP = 28;
    public static final int MAX_RADIUS_DP = 60;
    public static final int DEFAULT_FILL_COLOR = Color.BLACK;
    public static final int DEFAULT_FILL_OPACITY = 0; // درصد؛ ۰ = کاملاً شفاف

    /** مقادیر تنظیمات یک ویجت */
    public static final class Config {
        public int color = DEFAULT_COLOR;
        public int radiusDp = DEFAULT_RADIUS_DP;
        public int fillColor = DEFAULT_FILL_COLOR;
        public int fillOpacity = DEFAULT_FILL_OPACITY;
    }

    private WidgetPrefs() {}

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE);
    }

    public static Config load(Context c, int id) {
        SharedPreferences p = prefs(c);
        Config cfg = new Config();
        cfg.color = p.getInt("color_" + id, DEFAULT_COLOR);
        cfg.radiusDp = p.getInt("radius_" + id, DEFAULT_RADIUS_DP);
        cfg.fillColor = p.getInt("fillColor_" + id, DEFAULT_FILL_COLOR);
        cfg.fillOpacity = p.getInt("fillOpacity_" + id, DEFAULT_FILL_OPACITY);
        return cfg;
    }

    public static void save(Context c, int id, Config cfg) {
        prefs(c).edit()
                .putInt("color_" + id, cfg.color)
                .putInt("radius_" + id, cfg.radiusDp)
                .putInt("fillColor_" + id, cfg.fillColor)
                .putInt("fillOpacity_" + id, cfg.fillOpacity)
                .apply();
    }

    public static void delete(Context c, int id) {
        prefs(c).edit()
                .remove("color_" + id).remove("radius_" + id)
                .remove("fillColor_" + id).remove("fillOpacity_" + id)
                .apply();
    }
}
