package com.teambrella.android.util;

import java.util.Date;

/**
 * Time utility class
 */
public class TimeUtils {

    private static final long TICKS_AT_EPOCH = 621355968000000000L;
    private static final long TICKS_PER_MILLISECOND = 10000;

    public static Date getDateFromTicks(long ticks) {
        return new Date((ticks - TICKS_AT_EPOCH) / TICKS_PER_MILLISECOND);
    }
}
