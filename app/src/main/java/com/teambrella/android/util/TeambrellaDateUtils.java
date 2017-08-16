package com.teambrella.android.util;

import android.content.Context;

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
    public static final String TEAMBRELLA_SERVER_DATE = "yyyy-MM-dd HH:mm:ss";
    public static SimpleDateFormat SDF = new SimpleDateFormat(TEAMBRELLA_SERVER_DATE, Locale.US);

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

    public static long getServerTime(String serverDate) throws ParseException {
        return SDF.parse(serverDate).getTime();
    }
}
