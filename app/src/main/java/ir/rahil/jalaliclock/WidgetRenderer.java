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

    public static RemoteViews build(Context ctx, int widgetId, int color, int radiusDp, int widthDp, int heightDp) {
        float density = ctx.getResources().getDisplayMetrics().density;

        // فضای قاب = کل ویجت منهای حاشیه و ردیف تاریخ
        float availW = widthDp - 12;
        float availH = heightDp - 12 - 34;
        float frameW = Math.max(60f, Math.min(availW, availH * FRAME_RATIO));
        float frameH = frameW / FRAME_RATIO;

        RemoteViews rv = new RemoteViews(ctx.getPackageName(), R.layout.widget_clock);

        rv.setImageViewBitmap(R.id.frame, frameBitmap(frameW, frameH, radiusDp, color, density));

        float digits = frameH * 0.33f;
        rv.setTextViewTextSize(R.id.hour, TypedValue.COMPLEX_UNIT_DIP, digits);
        rv.setTextViewTextSize(R.id.minute, TypedValue.COMPLEX_UNIT_DIP, digits);
        rv.setTextColor(R.id.hour, color);
        rv.setTextColor(R.id.minute, alpha(color, 0.62f));
        rv.setInt(R.id.divider, "setBackgroundColor", alpha(color, 0.5f));

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

    /** سقف تعداد پیکسل بیت‌مپ؛ بیت‌مپ‌های بزرگ از حد ۱ مگابایتی Binder می‌گذرند و ویجت اصلاً نمایش داده نمی‌شود. */
    private static final int MAX_PIXELS = 120_000;

    private static Bitmap frameBitmap(float wDp, float hDp, int radiusDp, int color, float density) {
        float fullW = wDp * density;
        float fullH = hDp * density;
        // اگر قاب بزرگ بود، با وضوح کمتر رسم می‌شود و با density پایین‌تر در همان اندازهٔ dp نمایش داده می‌شود
        float scale = Math.min(1f, (float) Math.sqrt(MAX_PIXELS / (fullW * fullH)));
        float d = density * scale;
        int w = Math.max(1, Math.round(wDp * d));
        int h = Math.max(1, Math.round(hDp * d));
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bmp.setDensity(Math.max(1, Math.round(160 * d)));
        Canvas canvas = new Canvas(bmp);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(STROKE_DP * d);
        p.setColor(color);
        float half = p.getStrokeWidth() / 2f;
        RectF rect = new RectF(half, half, w - half, h - half);
        float r = Math.min(radiusDp * d, Math.min(rect.width(), rect.height()) / 2f);
        canvas.drawRoundRect(rect, r, r, p);
        return bmp;
    }
}
