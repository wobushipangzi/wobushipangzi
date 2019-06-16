package com.liujie.Utils;

import java.util.Calendar;

public class TimeUtils {


    /**
     * 设置当前时间到某天某时某分的秒数
     * @param day   有没有隔夜  隔夜加一
     * @param hour  几点钟
     * @param minute  几分钟
     * @return
     */
    public static Long getsomeSeconds(Integer day, Integer hour, Integer minute){
        long secondsOne = System.currentTimeMillis();// 当前时间毫秒数

        //往后设时间
        Calendar calendarTwo = Calendar.getInstance();
        calendarTwo.add(Calendar.DAY_OF_MONTH, day);
        calendarTwo.set(Calendar.HOUR_OF_DAY, hour);
        calendarTwo.set(Calendar.MINUTE, minute);
        calendarTwo.set(Calendar.SECOND, 0);
        calendarTwo.set(Calendar.MILLISECOND, 0);
        long secondsTwo = calendarTwo.getTimeInMillis();

        long seconds = (secondsTwo - secondsOne) / 1000;
        return seconds;
    }
}
