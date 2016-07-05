package com.flyup.common.utils;

import java.util.HashMap;
import java.util.Map;

/**
  *Time stamp helper class.
  * Created by Focux on 2016-4-16
  */
public class TimeStamper {

    private static Map<String, Long> hub = new HashMap<>();

    /**
     * 计时开始
     */
    public static void star(String tag) {
        hub.put(tag, System.currentTimeMillis());
    }

    /**
     * Get time elapse base on the base time.
     *
     * @return Time elapse base on the time you reset the {@link TimeStamper}.
     */
    public static long elapse(String tag) {
        Long star = hub.get(tag);
        if (star == null) {
            return System.currentTimeMillis();
        }
        return System.currentTimeMillis() - star;
    }

    /**
     * 计时
     */
    public static long elapseKickoff(String tag) {
        long star = hub.remove(tag);
        return System.currentTimeMillis() - star;
    }
}