package com.example.did;

import static com.example.did.MainActivity.GOTOSLEEP_HOUR;
import static com.example.did.MainActivity.GOTOSLEEP_MILISECOND;
import static com.example.did.MainActivity.GOTOSLEEP_MINIUTE;
import static com.example.did.MainActivity.GOTOSLEEP_SECOND;
import static com.example.did.MainActivity.WAKEUP_HOUR;
import static com.example.did.MainActivity.WAKEUP_MILISECOND;
import static com.example.did.MainActivity.WAKEUP_MINIUTE;
import static com.example.did.MainActivity.WAKEUP_SECOND;
import static com.example.did.MainActivity.date;
import static com.example.did.MainActivity.finishTime;
import static com.example.did.MainActivity.startTime;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

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

        Calendar c = Calendar.getInstance();

        if(action != null) {
            switch (action) {
                case Intent.ACTION_BOOT_COMPLETED:
                    Toast.makeText(context, "BOOT_COMPLETED", Toast.LENGTH_SHORT).show();
                    c.set(Calendar.HOUR_OF_DAY, GOTOSLEEP_HOUR);
                    c.set(Calendar.MINUTE, GOTOSLEEP_MINIUTE);
                    c.set(Calendar.SECOND, GOTOSLEEP_SECOND);
                    c.set(Calendar.MILLISECOND, GOTOSLEEP_MILISECOND);
                    Log.e(TAG, "GoToSleep 알람 예약!!! time : " + c.getTime() + " getTimeInMillis : " + c.getTimeInMillis() + " currentTime : " + System.currentTimeMillis());

                    if(c.before(Calendar.getInstance())){
                        c.add(Calendar.DATE, 1);
                    }

                    PendingIntent bootPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 2, sendGoToSleepAlarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                    AlarmManager bootAlarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                    AlarmManager.AlarmClockInfo bootAc = new AlarmManager.AlarmClockInfo(c.getTimeInMillis(), bootPendingIntent);
                    bootAlarmManager.setAlarmClock(bootAc, bootPendingIntent);
                    //context.startActivity(i);
                    break;
                case Intent.ACTION_SCREEN_ON:
                    Log.e(TAG, "SCREEN_ON");

                    if(Calendar.getInstance().getTimeInMillis() >= finishTime){
                        finishTime += 86400000;
                    }

                    PendingIntent goToSleepPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, sendGoToSleepAlarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                    AlarmManager goToSleepAlarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                    AlarmManager.AlarmClockInfo goToSleepAc = new AlarmManager.AlarmClockInfo(finishTime, goToSleepPendingIntent);
                    goToSleepAlarmManager.setAlarmClock(goToSleepAc, goToSleepPendingIntent);
                    break;
                case Intent.ACTION_SCREEN_OFF:
                    Log.e(TAG, "SCREEN_OFF");
                    Log.e(TAG, "현재시간 : " + System.currentTimeMillis());
                    Log.e(TAG, "종료예약시간 : " + finishTime);
                    if (System.currentTimeMillis() < finishTime) {
                        Log.e(TAG, "아직 일과 종료시간 전입니다. 다시 화면을 실행합니다.");
                        context.startActivity(i);
                    } else {
                        if(Calendar.getInstance().getTimeInMillis() >= startTime){
                            startTime += 86400000;
                        }

                        PendingIntent wakeUpPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 1, sendWakeUpAlarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager wakeUpAlarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                        AlarmManager.AlarmClockInfo wakeUpAc = new AlarmManager.AlarmClockInfo(startTime, wakeUpPendingIntent);
                        wakeUpAlarmManager.setAlarmClock(wakeUpAc, wakeUpPendingIntent);
                        //context.startActivity(i);
                    }

                    break;
            }
        }
    }


}
