package com.example.did;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "osslog";
    FirebaseDatabase database;
    DatabaseReference connectedRef, myStatus, serverStatus, adminSignal, startWorkTime, finishWorkTime;

    public static int USER_SIGNAL= 0;

    // 시간 비교를 위한 객체
    Calendar startWorkCalendar, finishWorkCalendar;

    long startTime, finishTime;

    //화면 킬 시간 변수
    public static final int WAKEUP_HOUR = 8;
    public static final int WAKEUP_MINIUTE = 30;
    public static final int WAKEUP_SECOND = 0;
    public static final int WAKEUP_MILISECOND = 0;

    //화면 끌 시간 변수
    public static final int GOTOSLEEP_HOUR = 17;
    public static final int GOTOSLEEP_MINIUTE = 24;
    public static final int GOTOSLEEP_SECOND = 0;
    public static final int GOTOSLEEP_MILISECOND = 0;


    // 뒤로가기 버튼 두번 누르면 종료되는 기능 중 사용될 변수
    private long backKeyPressedTime = 0;
    private Toast toast;

    // 시간 데이터 포맷팅 (ex. 2022/07/14 11:25:35)
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    // 데이터베이스에서 시간넘어와서 밀리세컨드로 변환하기위한 변수수
   Date date = null;


    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume!!");


        // WatchDog에서 HOME키나 멀티탭키로 나갔을때 다시 앱을 실행시켜주면 onResume을 타기때문에 여기서 초기화
        adminSignal.setValue(0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = FirebaseDatabase.getInstance();
        connectedRef = database.getReference(".info/connected");
        myStatus = database.getReference("yeonggwang1/STATUS_Client");
        serverStatus = database.getReference("yeonggwang1/STATUS_Server");
        adminSignal = database.getReference("yeonggwang1/ADMIN_SIGNAL");
        startWorkTime = database.getReference("yeonggwang1/START_TIME");
        finishWorkTime = database.getReference("yeonggwang1/END_TIME");




        // 관리자가 보낼 시그널 처음엔 0초기화
        adminSignal.setValue(USER_SIGNAL);

        // 앱이 데이터베이스와 연결이 끊겼을시 파이어베이스 STATUS_Client 노드에 값 저장
        myStatus.onDisconnect().setValue("disconnected");


        // 일과 시작 시간 변경시 동작되는 이벤트
        startWorkTime.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.e(TAG, snapshot.getValue().toString());
                try {
                    date = sdf.parse(snapshot.getValue().toString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                startTime = date.getTime();
                Log.e(TAG, String.valueOf(startTime));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // 일과 종료 시간 변경시 동작되는 이벤트
        finishWorkTime.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.e(TAG, snapshot.getValue().toString());
                try {
                    date = sdf.parse(snapshot.getValue().toString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                finishTime = date.getTime();
                Log.e(TAG, String.valueOf(finishTime));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        // 일과 시간 메소드
        makeWorkTime(WAKEUP_HOUR, WAKEUP_MINIUTE, WAKEUP_SECOND, WAKEUP_MILISECOND, GOTOSLEEP_HOUR, GOTOSLEEP_MINIUTE, GOTOSLEEP_SECOND, GOTOSLEEP_MILISECOND);

        // 서버의 STATUS가 변경될때 동작하는 이벤트 리스너
        serverStatus.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.e(TAG, snapshot.toString() );
                Log.e(TAG, snapshot.getValue().toString() );
                Log.e(TAG, snapshot.getKey() );

                // watchdog앱이 꺼지면 다시실행
                if (snapshot.getValue().toString().equals("disconnected")){
                    // 일과시간 보다 일찍 앱이 종료되면 예기치 않은 종료라 판단하고 다시 앱실행.
                    if (finishTime > System.currentTimeMillis()){
                        Log.e(TAG, "일과시간 : " + finishTime + " 현재시간 : " + System.currentTimeMillis() + ". 아직 일과시간입니다. 앱을 다시 실행시킵니다.");
                        getPackageList("watchdog");
                    }
                } else if(snapshot.getValue().toString().equals("connected")){
                    Log.e(TAG, "WatchDog앱이 다시 연결되었습니다.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // 이 앱이 다시 연결되었을때 반응하는 이벤트 리스너
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    Log.e(TAG, "connected!");
                    myStatus.setValue("connected");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled!! " + error);
            }
        });

    }


    //다른 앱을 실행시켜주는 메소드
    public void getPackageList(String packageName) {
        //SDK30이상은 Manifest권한 추가가 필요 출처:https://inpro.tistory.com/214
        PackageManager pkgMgr = getPackageManager();
        List<ResolveInfo> mApps;
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mApps = pkgMgr.queryIntentActivities(mainIntent, 0);

        try {
            for (int i = 0; i < mApps.size(); i++) {
                if(mApps.get(i).activityInfo.packageName.startsWith("com.example." + packageName)){
                    Log.d(TAG, "실행시킴");
                    break;
                }
            }
            Intent intent = getPackageManager().getLaunchIntentForPackage("com.example." + packageName);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    // 일과 시간 지정하는 메소드
    public void makeWorkTime(int startHour, int startMinute, int startSecond, int startMillisecond,
                              int finishHour, int finishMinute, int finishSecond, int finishMillisecond){
        startWorkCalendar = Calendar.getInstance();
        finishWorkCalendar = Calendar.getInstance();

        startWorkCalendar.set(Calendar.HOUR_OF_DAY, startHour);
        startWorkCalendar.set(Calendar.MINUTE, startMinute);
        startWorkCalendar.set(Calendar.SECOND, startSecond);
        startWorkCalendar.set(Calendar.MILLISECOND, startMillisecond);

        finishWorkCalendar.set(Calendar.HOUR_OF_DAY, finishHour);
        finishWorkCalendar.set(Calendar.MINUTE, finishMinute);
        finishWorkCalendar.set(Calendar.SECOND, finishSecond);
        finishWorkCalendar.set(Calendar.MILLISECOND, finishMillisecond);

        startTime = startWorkCalendar.getTimeInMillis();
        finishTime = finishWorkCalendar.getTimeInMillis();


        String formatStartTime = sdf.format(startWorkCalendar.getTimeInMillis());
        String formatFinishTime = sdf.format(finishWorkCalendar.getTimeInMillis());
        startWorkTime.setValue(formatStartTime);
        finishWorkTime.setValue(formatFinishTime);

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "onPause !! ");
    }



    // 홈버튼 눌렀을때
    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        adminSignal.setValue(2);
    }

    @Override
    public void onBackPressed() {
        backKeyPressed("뒤로가기 버튼을 한번 더 누르면 종료됩니다.", 5);
    }

    public void backKeyPressed(String msg, double time) {
        if (System.currentTimeMillis() > backKeyPressedTime + (time * 1000)) {
            backKeyPressedTime = System.currentTimeMillis();
            showGuide(msg);
            return;
        }

        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            adminSignal.setValue(1);
            ActivityCompat.finishAffinity(this);
            System.exit(0);
            toast.cancel();
        }
    }

    private void showGuide(String msg) {
        toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        toast.show();
    }
}