package icu.xiyoumc.mmboss.util;

import java.util.Calendar;

public class TimeUtils {

    public static int str2seconds(String time) {
        // time: 5m1d2h3M4s
        int seconds = 0;
        int index = 0;
        int length = time.length();
        while (index < length) {
            int num = 0;
            while (index < length && Character.isDigit(time.charAt(index))) {
                num = num * 10 + time.charAt(index) - '0';
                index++;
            }
            if (index < length) {
                char unit = time.charAt(index);
                switch (unit) {
                    case 's':
                        seconds += num;
                        break;
                    case 'M':
                        seconds += num * 60;
                        break;
                    case 'h':
                        seconds += num * 60 * 60;
                        break;
                    case 'd':
                        seconds += num * 60 * 60 * 24;
                        break;
                    case 'm':
                        seconds += num * 60 * 60 * 24 * 30;
                        break;
                    case 'y':
                        seconds += num * 60 * 60 * 24 * 30 * 12;
                        break;
                }
                index++;
            }
        }
        return seconds;
    }

    public static Calendar str2local(String time) {
        // time: 2021-01-01 00:00:00
        Calendar calendar = Calendar.getInstance();
        int index = 0;
        int length = time.length();
        int year = 0;
        int month = 0;
        int day = 0;
        int hour = 0;
        int minute = 0;
        int second = 0;
        while (index < length) {
            int num = 0;
            while (index < length && Character.isDigit(time.charAt(index))) {
                num = num * 10 + time.charAt(index) - '0';
                index++;
            }
            if (index < length) {
                char unit = time.charAt(index);
                switch (unit) {
                    case '-':
                        if (year == 0)
                            year = num;
                        else
                            month = num;
                        break;
                    case ':':
                        if (hour == 0)
                            hour = num;
                        else
                            minute = num;
                        break;
                    case ' ':
                        day = num;
                        break;
                }
                index++;
            } else {
                second = num;
            }
        }
        calendar.set(year, month - 1, day, hour, minute, second);
        return calendar;
    }

}
