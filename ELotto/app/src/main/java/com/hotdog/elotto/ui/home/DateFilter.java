package com.hotdog.elotto.ui.home;

import java.util.Calendar;
import java.util.Date;

/**
 * Enum used to define the constants (the calendar options)
 * */
public enum DateFilter {
    ALL_DATES,
    TODAY,
    TOMORROW,
    WITHIN_7_DAYS,
    WITHIN_14_DAYS,
    THIS_MONTH;

    public boolean matchesFilter(Date eventDate) {
        if (eventDate == null || this == ALL_DATES) {
            return true;
        }

        Calendar currentDateCal = Calendar.getInstance();
        Calendar eventDateCal = Calendar.getInstance();
        eventDateCal.setTime(eventDate);

        currentDateCal.set(Calendar.HOUR_OF_DAY, 0);
        currentDateCal.set(Calendar.MINUTE, 0);
        currentDateCal.set(Calendar.SECOND, 0);
        currentDateCal.set(Calendar.MILLISECOND, 0);
        eventDateCal.set(Calendar.HOUR_OF_DAY, 0);
        eventDateCal.set(Calendar.MINUTE, 0);
        eventDateCal.set(Calendar.SECOND, 0);
        eventDateCal.set(Calendar.MILLISECOND, 0);

        switch (this) {
            case TODAY:
                return isSameDay(currentDateCal, eventDateCal);

            case TOMORROW:
                Calendar tomorrow = (Calendar) currentDateCal.clone();
                tomorrow.add(Calendar.DAY_OF_MONTH, 1);
                return isSameDay(tomorrow, eventDateCal);

            case WITHIN_7_DAYS:
                Calendar sevenDaysLater = (Calendar) currentDateCal.clone();
                sevenDaysLater.add(Calendar.DAY_OF_MONTH, 7);
                return isWithinRange(eventDateCal, currentDateCal, sevenDaysLater);

            case WITHIN_14_DAYS:
                Calendar fourteenDaysLater = (Calendar) currentDateCal.clone();
                fourteenDaysLater.add(Calendar.DAY_OF_MONTH, 14);
                return isWithinRange(eventDateCal, currentDateCal, fourteenDaysLater);

            case THIS_MONTH:
                return currentDateCal.get(Calendar.MONTH) == eventDateCal.get(Calendar.MONTH) && currentDateCal.get(Calendar.YEAR) == eventDateCal.get(Calendar.YEAR);

            default:
                return true;
        }
    }
    /**
     * used to compare current date with event date
     */

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * used for checking ranges of dates
     */
    private boolean isWithinRange(Calendar eventDate, Calendar start, Calendar end) {
        boolean isAfterOrEqualToStart = eventDate.equals(start) || eventDate.after(start);
        boolean isBeforeOrEqualToEnd = eventDate.equals(end) || eventDate.before(end);
        return isAfterOrEqualToStart && isBeforeOrEqualToEnd;
    }
}