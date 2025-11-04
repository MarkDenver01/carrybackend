package com.carry_guide.carry_guide_admin.utils;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

public class DateTimeHelper {

    private static final String ISO_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT;

    static {
        SIMPLE_DATE_FORMAT = new SimpleDateFormat(ISO_PATTERN);
        SIMPLE_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    // For java.util.Date
    public static String formatDate(Date date) {
        if (date == null) return null;
        return SIMPLE_DATE_FORMAT.format(date);
    }

    // For java.time.Instant
    public static String formatInstant(Instant instant) {
        if (instant == null) return null;
        return DateTimeFormatter.ISO_INSTANT
                .withZone(ZoneOffset.UTC)
                .format(instant);
    }

    // Auto-detect type (overloaded convenience method)
    public static String format(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Date) return formatDate((Date) obj);
        if (obj instanceof Instant) return formatInstant((Instant) obj);
        throw new IllegalArgumentException("Unsupported date type: " + obj.getClass());
    }
}
