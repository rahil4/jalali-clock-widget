package ir.rahil.jalaliclock;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.appwidget.AppWidgetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.TypedValue;
import android.os.Build;
import android.widget.RemoteViews;

import java.time.LocalDate;

/** ساخت ظاهر ویجت؛ هم برای ویجت واقعی و هم برای پیش‌نمایش تنظیمات. */
public final class WidgetRenderer {
    private static final float FRAME_RATIO = 0.65f; // عرض / ارتفاع قاب
    private static final float STROKE_DP = 1.5f;

    private WidgetRenderer() {}

    private static int alpha(int color, float a) {
        return Color.argb(Math.round(255 * a), Color.red(color), Color.green(color), Color.blue(color));
    }

    public static RemoteViews build(Context ctx, int widgetId, WidgetPrefs.Config cfg, int widthDp, int heightDp) {
        int color = cfg.color;

        // فضای قاب = کل ویجت منهای حاشیه و ردیف تاریخ
        float availW = widthDp - 12;
        float availH = heightDp - 12 - 34;
        float frameW = Math.max(60f, Math.min(availW, availH * FRAME_RATIO));
        float frameH = frameW / FRAME_RATIO;

        RemoteViews rv = new RemoteViews(ctx.getPackageName(), R.layout.widget_clock);

        // اندازهٔ قاب صریح تعیین می‌شود تا تصویر یکنواخت مقیاس شود و گوشه‌ها گرد بمانند
        if (Build.VERSION.SDK_INT >= 31) {
            rv.setViewLayoutWidth(R.id.frame, frameW, TypedValue.COMPLEX_UNIT_DIP);
            rv.setViewLayoutHeight(R.id.frame, frameH, TypedValue.COMPLEX_UNIT_DIP);
        }
        rv.setImageViewBitmap(R.id.frame, frameBitmap(frameW, frameH, cfg, ctx.getResources().getDisplayMetrics().density));

        float digits = frameH * 0.28f;
        rv.setTextViewTextSize(R.id.hour, TypedValue.COMPLEX_UNIT_DIP, digits);
        rv.setTextViewTextSize(R.id.minute, TypedValue.COMPLEX_UNIT_DIP, digits);
        rv.setTextColor(R.id.hour, color);
        rv.setTextColor(R.id.minute, alpha(color, 0.62f));

        boolean withWeekday = frameW >= 120f;
        rv.setTextViewText(R.id.date, JalaliDate.format(LocalDate.now(), withWeekday));
        rv.setTextViewTextSize(R.id.date, TypedValue.COMPLEX_UNIT_DIP,
                Math.max(11f, Math.min(17f, frameW * 0.105f)));
        rv.setTextColor(R.id.date, color);

        // لمس ویجت، صفحهٔ تنظیمات را باز می‌کند
        Intent intent = new Intent(ctx, SettingsActivity.class)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        PendingIntent pi = PendingIntent.getActivity(ctx, widgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        rv.setOnClickPendingIntent(R.id.root, pi);
        return rv;
    }

    /** سقف تعداد پیکسل بیت‌مپ؛ بیت‌مپ‌های بزرگ از حد ۱ مگابایتی Binder می‌گذرند. */
    private static final int MAX_PIXELS = 60_000;

    private static Bitmap frameBitmap(float wDp, float hDp, WidgetPrefs.Config cfg, float density) {
        // نسبت عرض به ارتفاع دقیقاً همان قاب است؛ وضوح بیت‌مپ فقط برای حجم کمتر کم می‌شود
        float px = Math.min(density, (float) Math.sqrt(MAX_PIXELS / (wDp * hDp)));
        int w = Math.max(1, Math.round(wDp * px));
        int h = Math.max(1, Math.round(hDp * px));
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bmp.setDensity(Math.max(1, Math.round(160 * px))); // برای نمایش در اندازهٔ dp درست روی اندروید قدیمی‌تر
        Canvas canvas = new Canvas(bmp);

        float stroke = STROKE_DP * px;
        float half = stroke / 2f;
        RectF rect = new RectF(half, half, w - half, h - half);
        float r = Math.min(cfg.radiusDp * px, Math.min(rect.width(), rect.height()) / 2f);

        if (cfg.fillOpacity > 0) {
            Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
            fill.setStyle(Paint.Style.FILL);
            fill.setColor(alpha(cfg.fillColor, cfg.fillOpacity / 100f));
            canvas.drawRoundRect(rect, r, r, fill);
        }

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(stroke);
        p.setColor(cfg.color);
        canvas.drawRoundRect(rect, r, r, p);

        // خط کوتاه بین ساعت و دقیقه (وسط قاب)
        Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
        line.setStyle(Paint.Style.STROKE);
        line.setStrokeWidth(stroke);
        line.setColor(alpha(cfg.color, 0.5f));
        float cy = h / 2f;
        float arm = 13f * px;
        canvas.drawLine(w / 2f - arm, cy, w / 2f + arm, cy, line);
        return bmp;
    }
}
