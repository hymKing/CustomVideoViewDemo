package com.flyup.common.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;

/**
 * Created by bruce on 2015/8/27.
 */
public class AlarmUtils {



    /**
     * 设置 闹钟
     * @param context
     *
     * @param triggerAtMillis   触发时间，如果比当前时间小，则立即生效
     *  param receiver
     *  Intent intent=new Intent(context,receiver.getClass());
     *  action
     *                                                                 requestCode   区分 sender
     *  PendingIntent operation = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
     */
    public static void setAlarm(Context context,long triggerAtMillis,PendingIntent operation) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        int type = AlarmManager.RTC_WAKEUP;//唤醒 CPU

        if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.KITKAT) {//API 19 以后优化了唤醒和省电，set可能不精确，用setEx..
            manager.setExact(type, triggerAtMillis, operation);
        }else {
            manager.set(type, triggerAtMillis, operation);
        }
    }


    public static void setAlarmBroadCast(Context context,int requestCode,long triggerAtMillis,Intent intent) {
        PendingIntent operation = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        setAlarm(context,triggerAtMillis,operation);
    }
    /**
     * 倒计时任务
     * @param context
     * @param requestCode   区分 sender
     * @param elapsedMillis  倒计时间 单位毫秒
     *  param receiver
     *  Intent intent=new Intent(context,receiver.getClass());
     *  action
     *  e.g:
     *    // Intent intent = new Intent(getActivity(), AlarmBroadCastReceiver.class);
             intent.setAction(Action.AlarmWork.name());
             AlarmUtils.setELapsedAlarm(getActivity(), Action.AlarmWork.ordinal(), 5000, intent);
     */
    public static void setELapsedAlarm(Context context,int requestCode,long elapsedMillis,Intent intent) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        int type = AlarmManager.ELAPSED_REALTIME_WAKEUP;//唤醒 CPU

        PendingIntent operation = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.KITKAT) {//API 19 以后优化了唤醒和省电，set可能不精确，用setEx..
            manager.setExact(type, SystemClock.elapsedRealtime() + elapsedMillis, operation);
        }else {
            manager.set(type, SystemClock.elapsedRealtime() + elapsedMillis, operation);
        }
    }
    /**
     * 设置重复闹钟
     * @param context
     * @param requestCode   区分 sender
     * @param triggerAtMillis   触发时间，如果比当前时间小，则立即生效
     * @param intervalMillis    间隔
     *  Intent intent=new Intent(context,receiver.getClass());
     *  action
     *  intervalMillis 重复闹钟间隔时间 millis
     */
    public static void setRepeatAlarm(Context context,int requestCode,long triggerAtMillis, long intervalMillis,Intent intent) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        int type = AlarmManager.RTC_WAKEUP;//唤醒 CPU
        //触发时间，如果比当前时间小，则立即生效
        PendingIntent operation = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            //as of API 19, all repeating alarms are inexact ，待机时优化了省电机制，使用一次性的setExact代替 精确些
        manager.setRepeating(type,triggerAtMillis,intervalMillis,operation);
    }
    /**
     * 设置重复闹钟
     * @param context
     * @param requestCode   区分 sender
     * @param triggerAtMillis   触发时间，如果比当前时间小，则立即生效
     * @param  intervalMillis 重复闹钟间隔时间 millis
     *  Intent intent=new Intent(context,receiver.getClass());
     *  action
     *  intervalMillis 重复闹钟间隔时间 millis
     */
    public static void setRepeatAlarmService(Context context,int requestCode,long triggerAtMillis, long intervalMillis,Intent intent) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        int type = AlarmManager.ELAPSED_REALTIME;//唤醒 CPU
        //触发时间，如果比当前时间小，则立即生效

        PendingIntent operation = PendingIntent.getService(context, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            //as of API 19, all repeating alarms are inexact ，待机时优化了省电机制，使用一次性的setExact代替 精确些
        manager.setRepeating(type,triggerAtMillis,intervalMillis,operation);
    }
    /**
     * 设置工作日重复闹钟
     * @param context
     */



    /**
     * 取消闹钟
     * @param context
     * {@link Intent#filterEquals}), will be canceled.
     * mComponent
     * action
     *
     * Intent intent2 = new Intent(getActivity(), AlarmBroadCastReceiver.class);
    intent2.setAction(Action.AlarmWork.name());
    AlarmUtils.cancle(getActivity(),Action.AlarmWork.ordinal(), intent2);


    PendingIntent operation = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
     */
    public static void cancle(Context context,PendingIntent operation) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        manager.cancel(operation);
    }



}
