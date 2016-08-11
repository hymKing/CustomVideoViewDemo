package com.flyup.common.utils;

import java.util.Map;

/**
 *
 * Created by focux on 2016-3-22 .
 */
public class MapUtils {


    public static boolean isEmpty(Map collection) {
        return (collection == null || collection.isEmpty());
    }

    public static boolean hasData(Map collection) {
        return (collection != null && collection.size() > 0);
    }
}
