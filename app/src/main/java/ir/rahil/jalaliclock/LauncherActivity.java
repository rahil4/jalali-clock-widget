package ir.rahil.jalaliclock;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

public class LauncherActivity extends Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        TextView t = new TextView(this);
        t.setText("ساعت شمسی\n\nبرای استفاده، صفحهٔ اصلی گوشی را نگه دارید، «ویجت‌ها» را باز کنید و «ساعت شمسی» را اضافه کنید.\nبا لمس ویجت، رنگ و گردی گوشه‌ها را تغییر دهید.");
        t.setTextSize(17);
        t.setGravity(Gravity.CENTER);
        t.setLineSpacing(0, 1.4f);
        t.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        int p = (int) (28 * getResources().getDisplayMetrics().density);
        t.setPadding(p, p, p, p);
        setContentView(t);
    }
}
