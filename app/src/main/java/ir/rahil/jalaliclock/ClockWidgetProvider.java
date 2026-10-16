package ir.rahil.jalaliclock;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;

public class ClockWidgetProvider extends AppWidgetProvider {

    /** ساعت و دقیقه را خود سیستم (TextClock) به‌روز می‌کند؛ اینجا فقط تاریخ و اندازه مهم است. */
    public static void update(Context ctx, AppWidgetManager mgr, int id) {
        Bundle o = mgr.getAppWidgetOptions(id);
        int w = o.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 110);
        int h = o.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 200);
        if (w <= 0) w = 110;
        if (h <= 0) h = 200;
        RemoteViews rv;
        try {
            rv = WidgetRenderer.build(ctx, id,
                    WidgetPrefs.color(ctx, id), WidgetPrefs.radiusDp(ctx, id), w, h);
        } catch (Throwable t) {
            // به‌جای ناپدید شدن بی‌صدا، علت خطا روی ویجت نوشته می‌شود
            rv = new RemoteViews(ctx.getPackageName(), R.layout.widget_error);
            rv.setTextViewText(R.id.error, String.valueOf(t));
        }
        mgr.updateAppWidget(id, rv);
    }

    @Override
    public void onUpdate(Context ctx, AppWidgetManager mgr, int[] ids) {
        for (int id : ids) update(ctx, mgr, id);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context ctx, AppWidgetManager mgr, int id, Bundle newOptions) {
        update(ctx, mgr, id);
    }

    @Override
    public void onDeleted(Context ctx, int[] ids) {
        for (int id : ids) WidgetPrefs.delete(ctx, id);
    }

    @Override
    public void onReceive(Context ctx, Intent intent) {
        super.onReceive(ctx, intent);
        String a = intent.getAction();
        // تغییر روز، ساعت یا منطقهٔ زمانی: تاریخ باید دوباره محاسبه شود
        if (Intent.ACTION_DATE_CHANGED.equals(a) || Intent.ACTION_TIME_CHANGED.equals(a)
                || Intent.ACTION_TIMEZONE_CHANGED.equals(a)) {
            AppWidgetManager mgr = AppWidgetManager.getInstance(ctx);
            int[] ids = mgr.getAppWidgetIds(new ComponentName(ctx, ClockWidgetProvider.class));
            for (int id : ids) update(ctx, mgr, id);
        }
    }
}
