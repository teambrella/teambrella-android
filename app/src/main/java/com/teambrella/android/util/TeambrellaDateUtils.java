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
    private static final String TEAMBRELLA_SERVER_DATE = "yyyy-MM-dd HH:mm:ss";
    private static SimpleDateFormat SDF = new SimpleDateFormat(TEAMBRELLA_SERVER_DATE, Locale.US);

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

    public static String getRelativeTime(int remainedMinutes) {
        long now = System.currentTimeMillis();
        long when = now + 60000 * remainedMinutes;
        return DateUtils.getRelativeTimeSpanString(when, now, DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE).toString();
    }
}
