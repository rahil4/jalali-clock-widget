package ir.rahil.jalaliclock;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import org.junit.Test;

public class JalaliDateTest {
    private static void check(int gy, int gm, int gd, int jy, int jm, int jd) {
        JalaliDate j = JalaliDate.fromGregorian(gy, gm, gd);
        assertEquals(jy, j.year);
        assertEquals(jm, j.month);
        assertEquals(jd, j.day);
    }

    @Test
    public void convertsKnownDates() {
        check(2026, 10, 16, 1405, 7, 24);
        check(2025, 3, 21, 1404, 1, 1);
        check(2024, 3, 20, 1403, 1, 1);
        check(2025, 3, 20, 1403, 12, 30); // سال کبیسه
        check(2026, 3, 21, 1405, 1, 1);
    }

    @Test
    public void formatsPersianText() {
        LocalDate d = LocalDate.of(2026, 10, 16);
        assertEquals("۲۴ مهر ۱۴۰۵", JalaliDate.format(d, false));
        assertEquals("۲۴ مهر ۱۴۰۵ · جمعه", JalaliDate.format(d, true));
    }
}
