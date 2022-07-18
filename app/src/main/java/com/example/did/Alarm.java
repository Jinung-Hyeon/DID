package com.example.did;

import static com.example.did.MainActivity.finishTime;
import static com.example.did.MainActivity.startTime;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Alarm extends BroadcastReceiver {
    private static final String TAG = "osslog";
    private Intent i, sendWakeUpAlarmIntent, sendGoToSleepAlarmIntent;



    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        i = new Intent(context, MainActivity.class);
        sendWakeUpAlarmIntent = new Intent(context, WakeUpAlarm.class);
        sendGoToSleepAlarmIntent = new Intent(context, GoToSleepAlarm.class);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");


        if(action != null) {
            switch (action) {
                case Intent.ACTION_BOOT_COMPLETED:
                    Toast.makeText(context, "BOOT_COMPLETED", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "GoToSleep 알람 예약 !!! time : " + finishTime + " timeFormat : " + dateFormat.format(finishTime));


                    PendingIntent bootPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 2, sendGoToSleepAlarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                    AlarmManager bootAlarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                    AlarmManager.AlarmClockInfo bootAc = new AlarmManager.AlarmClockInfo(finishTime, bootPendingIntent);
                    bootAlarmManager.setAlarmClock(bootAc, bootPendingIntent);
                    //context.startActivity(i);
                    break;
                case Intent.ACTION_SCREEN_ON:
                    Log.e(TAG, "SCREEN_ON");
                    Log.e(TAG, "GoToSleep 알람 예약 !!! time : " + finishTime + " timeFormat : " + dateFormat.format(finishTime));

//                    if(Calendar.getInstance().getTimeInMillis() >= finishTime){
//                        finishTime += 86400000;
//                    }

                    PendingIntent goToSleepPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, sendGoToSleepAlarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                    AlarmManager goToSleepAlarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                    AlarmManager.AlarmClockInfo goToSleepAc = new AlarmManager.AlarmClockInfo(finishTime, goToSleepPendingIntent);
                    goToSleepAlarmManager.setAlarmClock(goToSleepAc, goToSleepPendingIntent);
                    break;
                case Intent.ACTION_SCREEN_OFF:
                    Log.e(TAG, "SCREEN_OFF");
//                    Log.e(TAG, "현재시간 : " + System.currentTimeMillis());
//                    Log.e(TAG, "종료예약시간 : " + finishTime);
                    if (System.currentTimeMillis() < finishTime) {
                        Log.e(TAG, "아직 일과 종료시간 전입니다. 다시 화면을 실행합니다.");
                        context.startActivity(i);
                    } else {
                        // 일과 시작 시간이 현재 시간보다 작을경우 하루 더해서 예약.
                        if(Calendar.getInstance().getTimeInMillis() >= startTime){
                            startTime += 86400000;
                        }
                        Log.e(TAG, "WakeUp 알람 예약 !!! time : " + startTime + " timeFormat : " + dateFormat.format(startTime));

                        PendingIntent wakeUpPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 1, sendWakeUpAlarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager wakeUpAlarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                        AlarmManager.AlarmClockInfo wakeUpAc = new AlarmManager.AlarmClockInfo(startTime, wakeUpPendingIntent);
                        wakeUpAlarmManager.setAlarmClock(wakeUpAc, wakeUpPendingIntent);
                    }

                    break;
            }
        }
    }


}
