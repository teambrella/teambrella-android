package com.teambrella.android.util;

import android.content.Context;
import android.text.format.DateUtils;

import com.teambrella.android.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Teambrella date utils
 */
public class TeambrellaDateUtils {

    public static final String TEAMBRELLA_UI_DATE = "d LLLL yyyy";
    public static final String TEAMBRELLA_UI_DATE_SHORT = "d MMM yyyy";
    private static final String TEAMBRELLA_SERVER_DATE = "yyyy-MM-dd HH:mm:ss";
    private static SimpleDateFormat SDF = new SimpleDateFormat(TEAMBRELLA_SERVER_DATE, Locale.US);

    private static int MINUTE = 1;
    private static int HOUR = 60 * MINUTE;
    private static int DAY = HOUR * 24;

    static {
        SDF.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static String getDatePresentation(Context context, String formatString, String serverDate) {
        Locale locale = new Locale(context.getString(R.string.locale));
        try {
            return new SimpleDateFormat(formatString, locale).format(SDF.parse(serverDate));
        } catch (ParseException e) {
            return "";
        }
    }

    public static String getDatePresentation(Context context, String formatString, long time) {
        Locale locale = new Locale(context.getString(R.string.locale));
        return new SimpleDateFormat(formatString, locale).format(time);
    }

    public static long getServerTime(String serverDate) throws ParseException {
        return SDF.parse(serverDate).getTime();
    }

    public static String getRelativeTime(long remainedMinutes) {
        long now = System.currentTimeMillis();
        long when = now + 60000 * remainedMinutes;
        return DateUtils.getRelativeTimeSpanString(when, now, DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE).toString();
    }

    public static String getRelativeTimeLocalized(Context context, long remainedMinutes) {
        if (remainedMinutes == 0) {
            return context.getString(R.string.now);
        } else if (remainedMinutes / DAY > 0) {
            int count = (int) remainedMinutes / DAY + 1;
            return context.getResources().getQuantityString(R.plurals.days, count, count);
        } else if (remainedMinutes / HOUR > 0) {
            int count = (int) remainedMinutes / HOUR + 1;
            return context.getResources().getQuantityString(R.plurals.hours, count, count);
        } else {
            return context.getResources().getQuantityString(R.plurals.minutes, (int) remainedMinutes, (int) remainedMinutes);
        }
    }
}
