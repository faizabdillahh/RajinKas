package com.rajinkas.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtils {
    private static final String ISO_8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    public static String nowIso8601() {
        SimpleDateFormat sdf = new SimpleDateFormat(ISO_8601_FORMAT, Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(new Date());
    }

    public static String getCurrentPeriodLabel() {
        return getWeekLabel(Calendar.getInstance());
    }

    public static String getWeekLabel(Calendar calendar) {
        int weekOfMonth = calendar.get(Calendar.WEEK_OF_MONTH);
        String month = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.forLanguageTag("id-ID"));
        int year = calendar.get(Calendar.YEAR);
        return "Minggu " + weekOfMonth + ", " + month + " " + year;
    }

    public static List<String> generateWeekLabels(String startDateStr, int weeksAhead) {
        List<String> labels = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Calendar current = Calendar.getInstance();
            Calendar start = Calendar.getInstance();
            start.setTime(sdf.parse(startDateStr));
            
            // Normalize start to Monday of that week for consistency
            start.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            
            // Limit the start to not be too far in the past if needed
            // Generate from start until (today + weeksAhead)
            Calendar end = Calendar.getInstance();
            end.add(Calendar.WEEK_OF_YEAR, weeksAhead);

            while (start.before(end) || isSameWeek(start, end)) {
                labels.add(getWeekLabel(start));
                start.add(Calendar.WEEK_OF_YEAR, 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return labels;
    }

    private static boolean isSameWeek(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR);
    }
}
