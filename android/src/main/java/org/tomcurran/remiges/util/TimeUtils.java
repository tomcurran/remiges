package org.tomcurran.remiges.util;


import android.content.Context;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Months;
import org.joda.time.Weeks;
import org.joda.time.Years;
import org.tomcurran.remiges.R;

public class TimeUtils {

    public static String getTimeAgo(Context context, long time) {
        DateTime then = new DateTime(time);
        DateTime now = new DateTime();

        int days = Days.daysBetween(then, now).getDays();
        int weeks = Weeks.weeksBetween(then, now).getWeeks();
        int months = Months.monthsBetween(then, now).getMonths();
        int years = Years.yearsBetween(then, now).getYears();

        if (days < 1) {
            return context.getString(R.string.time_ago_today);
        } else if (days == 1) {
            return context.getString(R.string.time_ago_day);
        } else if (weeks < 1) {
            return context.getString(R.string.time_ago_days, days);
        } else if (weeks == 1) {
            return context.getString(R.string.time_ago_week);
        } else if (months < 2) {
            return context.getString(R.string.time_ago_weeks, weeks);
        } else if (years < 2) {
            return context.getString(R.string.time_ago_months, months);
        } else {
            return context.getString(R.string.time_ago_years, weeks);
        }
    }

}
