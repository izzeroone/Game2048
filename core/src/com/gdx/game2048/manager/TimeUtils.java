package com.gdx.game2048.manager;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Create By Phan Huu Chi on 4/2018
 */
public class TimeUtils {
    public static final SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static final SimpleDateFormat userDateFormat = new SimpleDateFormat("EEEE dd MMMM yyyy");
    public static final SimpleDateFormat userTimeFormat = new SimpleDateFormat("HH:mm");
    public static final SimpleDateFormat userDateTimeFormat = new SimpleDateFormat("EEEE dd MMMM, HH:mm");


    public static String getCurrentTimeAsString() {
        Calendar c = Calendar.getInstance();
        return  fullDateFormat.format(c.getTime());
    }

    public static Date getCurrentTimeAsDate() {
        Calendar c = Calendar.getInstance();
        return  c.getTime();
    }

    // Convert

    public static Date strToDate(String string) {
        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(fullDateFormat.parse(string));
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
        return cal.getTime();
    }

    public static String dateToStr(Date date) {
        return fullDateFormat.format(date);
    }

    /**
     * User string mean a String of date that User can easily read
     */
    public static String dateToUserDateStr(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return userDateFormat.format(cal.getTime());
    }
    public static String dateToUserDateStr(int day, int month, int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(year,month,day);
        return dateToUserDateStr(cal.getTime());
    }
    public static String curDateToUserDateStr() {
        Calendar cal = Calendar.getInstance();
        return dateToUserDateStr(cal.getTime());
    }

    public static String timeToUserTimeStr(int hourOfDay, int minute) {
        return String.format("%02d:%02d",hourOfDay,minute);
    }
    public static String timeToUserTimeStr(Date date) {
        return userTimeFormat.format(date);
    }
    public static String curTimeToUserTimeStr() {
        Calendar cal = Calendar.getInstance();
        return userTimeFormat.format(cal.getTime());
    }
    public static String curTimeToUserTimeStr(int plusMinute) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, plusMinute);
        return userTimeFormat.format(cal.getTime());
    }
    public static String dateToUserDateTimeStr(Date date) {
        return userDateTimeFormat.format(date);
    }
    public static String dateToUserDateTimeStr(String date) {
        return userDateTimeFormat.format(strToDate(date));
    }
    // Func

    /**
     * @return -1 == checkTime before Now( checkTime in pass )
     * @return 0 ==
     * @return 1 == checkTime after Now   ( checkTime in feature )
     * note : >=0 valid
     */
    public static int compareWithNow(Date checkTime) {
        return checkTime.compareTo(getCurrentTimeAsDate());
    }
    public static int compareWithNow(String checkTime) {
        return compareWithNow(strToDate(checkTime));
    }

    /**
     * Calculate time have pass from checkT
     * @return second = now - lastTime
     */
    public static long getPassTime(String lastTime) {
        long difference = getCurrentTimeAsDate().getTime() - strToDate(lastTime).getTime();
        return TimeUnit.MILLISECONDS.toSeconds(difference);
    }

    public static long getPassTime(Date startTime, Date endTime) {
        long difference = startTime.getTime() - endTime.getTime();
        return TimeUnit.MILLISECONDS.toSeconds(difference);
    }

    public static boolean checkTimeOut(long checkAmountSec, String lastTimeCheck) {
        long secondsPass = TimeUtils.getPassTime(lastTimeCheck);

        if( secondsPass > checkAmountSec)
            return true;
        return false;
    }

}
