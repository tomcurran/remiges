package org.tomcurran.remiges.util;


import android.content.Context;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Months;
import org.joda.time.Weeks;
import org.joda.time.Years;
import org.tomcurran.remiges.R;

/**
 * Utility class for time functions
 */
public class TimeUtils {

    /**
     * Formats a previous time as string representing a relative time ago
     *
     * @param context  android {@link android.content.Context}
     * @param pastTime epoch milliseconds
     * @return pastTime ago formatted string
     */
    public static String getTimeAgo(Context context, long pastTime) {
        DateTime then = new DateTime(pastTime).withTimeAtStartOfDay();
        DateTime now = new DateTime().withTimeAtStartOfDay();

        int days = Days.daysBetween(then, now).getDays();
        int weeks = Weeks.weeksBetween(then, now).getWeeks();
        int months = Months.monthsBetween(then, now).getMonths();
        int years = Years.yearsBetween(then, now).getYears();

        if (days < 1) {
            return context.getString(R.string.time_ago_today);
        } else if (weeks < 1) {
            return context.getResources().getQuantityString(R.plurals.time_ago_days, days, days);
        } else if (months < 2) {
            return context.getResources().getQuantityString(R.plurals.time_ago_weeks, weeks, weeks);
        } else if (years < 2) {
            return context.getResources().getQuantityString(R.plurals.time_ago_months, months, months);
        } else {
            return context.getResources().getQuantityString(R.plurals.time_ago_years, years, years);
        }
    }

}
