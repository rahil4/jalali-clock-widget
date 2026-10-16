package ir.rahil.jalaliclock;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

/** تنظیمات هر ویجت: رنگ و گردی گوشه‌های قاب. */
public final class WidgetPrefs {
    public static final int DEFAULT_COLOR = Color.WHITE;
    public static final int DEFAULT_RADIUS_DP = 28;
    public static final int MAX_RADIUS_DP = 60;

    private WidgetPrefs() {}

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE);
    }

    public static int color(Context c, int id) {
        return prefs(c).getInt("color_" + id, DEFAULT_COLOR);
    }

    public static int radiusDp(Context c, int id) {
        return prefs(c).getInt("radius_" + id, DEFAULT_RADIUS_DP);
    }

    public static void save(Context c, int id, int color, int radiusDp) {
        prefs(c).edit().putInt("color_" + id, color).putInt("radius_" + id, radiusDp).apply();
    }

    public static void delete(Context c, int id) {
        prefs(c).edit().remove("color_" + id).remove("radius_" + id).apply();
    }
}
