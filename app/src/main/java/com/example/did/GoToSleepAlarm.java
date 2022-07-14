package com.example.did;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class GoToSleepAlarm extends BroadcastReceiver {

    private static final String TAG = "AlarmTest";

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "퇴근 ㅊㅋㅊㅋ", Toast.LENGTH_SHORT).show();    // AVD 확인용
        Log.e(TAG,"퇴근 ㅊㅋㅊㅋ");    // 로그 확인용
        Intent intent1 = new Intent(context, MainActivity.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        intent1.putExtra("ScreenOff", "ScreenOff");
        context.startActivity(intent1);

    }
}