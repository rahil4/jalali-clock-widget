package ir.rahil.jalaliclock;

import java.time.LocalDate;

/** تبدیل تاریخ میلادی به شمسی و قالب‌بندی فارسی. */
public final class JalaliDate {
    public final int year;
    public final int month; // 1..12
    public final int day;

    private static final int[] DAYS_BEFORE_MONTH = {0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334};

    public static final String[] MONTHS = {
            "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
            "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"};

    /** به ترتیب java.time.DayOfWeek: دوشنبه تا یکشنبه */
    private static final String[] WEEKDAYS = {
            "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه", "شنبه", "یکشنبه"};

    public JalaliDate(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public static JalaliDate fromGregorian(int gy, int gm, int gd) {
        int gy2 = gm > 2 ? gy + 1 : gy;
        int days = 355666 + 365 * gy + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400
                + gd + DAYS_BEFORE_MONTH[gm - 1];
        int jy = -1595 + 33 * (days / 12053);
        days %= 12053;
        jy += 4 * (days / 1461);
        days %= 1461;
        if (days > 365) {
            jy += (days - 1) / 365;
            days = (days - 1) % 365;
        }
        int jm;
        int jd;
        if (days < 186) {
            jm = 1 + days / 31;
            jd = 1 + days % 31;
        } else {
            jm = 7 + (days - 186) / 30;
            jd = 1 + (days - 186) % 30;
        }
        return new JalaliDate(jy, jm, jd);
    }

    public static JalaliDate from(LocalDate d) {
        return fromGregorian(d.getYear(), d.getMonthValue(), d.getDayOfMonth());
    }

    public static String toPersianDigits(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            sb.append(c >= '0' && c <= '9' ? (char) ('۰' + (c - '0')) : c);
        }
        return sb.toString();
    }

    public static String weekdayName(LocalDate d) {
        return WEEKDAYS[d.getDayOfWeek().getValue() - 1];
    }

    /** مثل «۲۴ مهر ۱۴۰۵» یا «۲۴ مهر ۱۴۰۵ · جمعه» */
    public static String format(LocalDate d, boolean withWeekday) {
        JalaliDate j = from(d);
        String s = toPersianDigits(j.day + " " + MONTHS[j.month - 1] + " " + j.year);
        return withWeekday ? s + " · " + weekdayName(d) : s;
    }
}
