package me.drakeet.inmessage.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import me.drakeet.inmessage.App;
import me.drakeet.inmessage.BuildConfig;
import me.drakeet.inmessage.R;

public class TimeUtils {

    private static SimpleDateFormat sDayFormat = new SimpleDateFormat("HH:mm");
    private static SimpleDateFormat sDateFormat = new SimpleDateFormat("M-d HH:mm");
    private static SimpleDateFormat sYearFormat = new SimpleDateFormat("yyyy-M-d HH:mm");
    private static SimpleDateFormat sOrigFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.US);
    private static TimeUtils mInstance;
    private static String JUST_NOW, MIN, HOUR, DAY, MONTH, YEAR, YESTERDAY, THE_DAY_BEFORE_YESTERDAY, TODAY, WEEK;

    private static Calendar sCal1 = Calendar.getInstance(), sCal2 = Calendar.getInstance();

    public TimeUtils(Context context) {
        Resources res = context.getResources();
        JUST_NOW = res.getString(R.string.just_now);
        MIN = res.getString(R.string.min);
        HOUR = res.getString(R.string.hour);
        DAY = res.getString(R.string.day);
        MONTH = res.getString(R.string.month);
        YEAR = res.getString(R.string.year);
        YESTERDAY = res.getString(R.string.yesterday);
        WEEK = res.getString(R.string. week);
        THE_DAY_BEFORE_YESTERDAY = res.getString(R.string.the_day_before_yesterday);
        TODAY = res.getString(R.string.today);
    }

    public static TimeUtils getInstance() {
        if (mInstance == null) {
            mInstance = new TimeUtils(App.getContext());
        }

        return mInstance;
    }


    /**
     * 获取当天零点的时间戳
     *
     * @return
     */
    public static long getTimesMorning() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return (long) (cal.getTimeInMillis());
    }

    public static Date getDateFromTimestamp(long longTime) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String d = format.format(longTime);
        Date date = null;
        try {
            date = format.parse(d);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * 获取七天前零点的时间戳
     *
     * @return
     */
    public static long beforeSevenDaysMorning() {
        long todayMorning = getTimesMorning() / 1000;
        long beforeSevenDaysMorning = (todayMorning - 60 * 60 * 24 * 7) * 1000;
        return beforeSevenDaysMorning;
    }

    /**
     * 获取现在时间
     *
     * @return返回短时间格式 yyyy-MM-dd
     */
    public Date getNowDateShort() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = formatter.format(currentTime);
        ParsePosition pos = new ParsePosition(8);
        Date currentTime_2 = formatter.parse(dateString, pos);
        return currentTime_2;
    }

    /**
     * 得到现在时间
     *
     * @return 字符串 yyyyMMdd HHmmss
     */
    public String getStringToday(String format) {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        String dateString = formatter.format(currentTime);
        return dateString;
    }

    public static String getString(Date date, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        String dateString = formatter.format(date);
        return dateString;
    }

    public long getCurrentFromLinux() {
        Date currentTime = new Date();
        return currentTime.getTime();
    }

    public Calendar DateToCalendar(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    //todo
    public String getStatusTimeString(Date date) {
        String dateString = sOrigFormat.format(date);
        long time = parseTimeString(dateString);
        return buildTimeString(time);
    }

    public synchronized long parseTimeString(String createdAt) {
        try {
            return sOrigFormat.parse(createdAt).getTime();
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e("TimeUtils-->", "Failed parsing time: " + createdAt);
            }

            return -1;
        }
    }

    public synchronized String buildTimeString(long millis) {
        Calendar cal = sCal1;

        cal.setTimeInMillis(millis);

        long msg = cal.getTimeInMillis();
        long now = System.currentTimeMillis();

        Calendar nowCalendar = sCal2;
        sCal2.setTimeInMillis(now);

        long differ = now - msg;
        long difsec = differ / 1000;

        if (difsec < 60) {
            return JUST_NOW;
        }

        long difmin = difsec / 60;

        if (difmin < 60) {
            return String.valueOf(difmin) + MIN;
        }

        long difhour = difmin / 60;

        if (difhour < 24 && isSameDay(nowCalendar, cal)) {
            return TODAY + " " + sDayFormat.format(cal.getTime());
        }

        long difday = difhour / 24;

        if (difday < 31) {
            if (isYesterDay(nowCalendar, cal)) {
                return YESTERDAY + " " + sDayFormat.format(cal.getTime());
            } else if (isTheDayBeforeYesterday(nowCalendar, cal)) {
                return THE_DAY_BEFORE_YESTERDAY + " " + sDayFormat.format(cal.getTime());
            } else {
                return sDateFormat.format(cal.getTime());
            }
        }

        long difMonth = difday / 31;

        if (difMonth < 12 && isSameYear(nowCalendar, cal)) {
            return sDateFormat.format(cal.getTime());
        }

        return sYearFormat.format(cal.getTime());
    }

    public String getDateGroup(Date date) {
        String dateString = sOrigFormat.format(date);
        long mills = parseTimeString(dateString);
        Calendar cal = sCal1;

        cal.setTimeInMillis(mills);

        long msg = cal.getTimeInMillis();
        long now = System.currentTimeMillis();

        Calendar nowCalendar = sCal2;
        sCal2.setTimeInMillis(now);

        long differ = now - msg;
        long difsec = differ / 1000;
        long difmin = difsec / 60;
        long difhour = difmin / 60;
        long difday = difhour / 24;

        if (difhour < 24 && isSameDay(nowCalendar, cal)) {
            return TODAY;
        }

        if(isSameYear(nowCalendar, cal)) {
            if(isSameMonth(nowCalendar, cal)) {
                if(isSameWeek(nowCalendar, cal)) {
                    if (isYesterDay(nowCalendar, cal)) {
                        return YESTERDAY;
                    } else if (isTheDayBeforeYesterday(nowCalendar, cal)) {
                        return THE_DAY_BEFORE_YESTERDAY;
                    } else {
                        return WEEK;
                    }
                }
                else {
                    return MONTH;
                }
            }
            else {
                int month = cal.get(Calendar.MONTH) + 1;
                return month + "月";
            }
        }
        else {
            int month = cal.get(Calendar.MONTH) + 1;
            return cal.get(Calendar.YEAR) + "年" + month + "月";
        }
    }


    private boolean isSameDay(Calendar now, Calendar msg) {
        int nowDay = now.get(Calendar.DAY_OF_YEAR);
        int msgDay = msg.get(Calendar.DAY_OF_YEAR);
        return nowDay == msgDay;
    }

    private boolean isYesterDay(Calendar now, Calendar msg) {
        int nowDay = now.get(Calendar.DAY_OF_YEAR);
        int msgDay = msg.get(Calendar.DAY_OF_YEAR);
        return nowDay == (msgDay + 1);
    }

    private boolean isTheDayBeforeYesterday(Calendar now, Calendar msg) {
        int nowDay = now.get(Calendar.DAY_OF_YEAR);
        int msgDay = msg.get(Calendar.DAY_OF_YEAR);
        return nowDay == (msgDay + 2);
    }

    private boolean isSameYear(Calendar now, Calendar msg) {
        int nowYear = now.get(Calendar.YEAR);
        int msgYear = msg.get(Calendar.YEAR);
        return nowYear == msgYear;
    }

    private Boolean isSameMonth(Calendar now, Calendar msg) {
        int nowYear = now.get(Calendar.YEAR);
        int msgYear = msg.get(Calendar.YEAR);
        int nowMonth = now.get(Calendar.MONTH);
        int msgMonth = msg.get(Calendar.MONTH);
        return (nowYear == msgYear) && (nowMonth == msgMonth);
    }

    public static boolean isSameWeek(Calendar now, Calendar msg) {
        int subYear = now.get(Calendar.YEAR) - msg.get(Calendar.YEAR);
        if (subYear == 0) {
            if (now.get(Calendar.WEEK_OF_YEAR) == msg.get(Calendar.WEEK_OF_YEAR))
                return true;
        }
        else if (subYear == 1 && msg.get(Calendar.MONTH) == 11) {
            if (now.get(Calendar.WEEK_OF_YEAR) == msg.get(Calendar.WEEK_OF_YEAR))
                return true;
        }
        else if (subYear == -1 && now.get(Calendar.MONTH) == 11) {
            if (now.get(Calendar.WEEK_OF_YEAR) == msg.get(Calendar.WEEK_OF_YEAR))
                return true;

        }
        return false;
    }
}
