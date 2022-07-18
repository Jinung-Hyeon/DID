package com.example.did;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "osslog";
    FirebaseDatabase database;
    DatabaseReference connectedRef, myStatus, serverStatus, adminSignal, startWorkTime, finishWorkTime;

    public static int USER_SIGNAL= 0;

    // 시간 비교를 위한 객체
    Calendar startWorkCalendar, finishWorkCalendar;

    // 일과 시작, 종료 시간(밀리세컨드)으로 사용할 변수
    public static long startTime, finishTime;

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
    SimpleDateFormat day = new SimpleDateFormat("yyyy/MM/dd");
    SimpleDateFormat hours = new SimpleDateFormat("HH:mm:ss");
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");


    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume!!");

        // WatchDog에서 HOME키나 멀티탭키로 나갔을때 다시 앱을 실행시켜주면 onResume을 타기때문에 여기서 초기화
        adminSignal.setValue(0);

        // 꺼진 화면을 켜주는 기능
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        try {
            overridePendingTransition(0,0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Alarm alarm = new Alarm();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(alarm, filter);



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

                // 파이어베이스에 일과시작 데이터가 ""이면 코드에있는 기본값으로 일과 시작 시간 설정
                if(snapshot.getValue().toString().equals("")){
                    Log.e(TAG, "일과 시작에 설정된 시간이 없습니다. 기본 시간으로 설정합니다.");
                    makeStartWorkTime(WAKEUP_HOUR, WAKEUP_MINIUTE, WAKEUP_SECOND, WAKEUP_MILISECOND);
                }

                try {
                    // 오늘 날짜 yyyy/MM/dd 형태 + 공백 + 파이어베이스 END_TIME 데이터 를 합친 문자열을 Date로 파싱하고 getTime으로 long값 구함.
                    Date startDate = dateFormat.parse(day.format(System.currentTimeMillis()) + " " + snapshot.getValue().toString());
                    Log.e(TAG, "testDate: " + startDate + " testDate.getTime : " + startDate.getTime());
                    startTime = startDate.getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

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



                // 파이어베이스에 일과종료 데이터가 ""이면 코드에있는 기본값으로 일과 종료 시간 설정
                if(snapshot.getValue().toString().equals("")){
                    Log.e(TAG, "일과 종료에 설정된 시간이 없습니다. 기본 시간으로 설정합니다.");
                    makeFinishWorkTime(GOTOSLEEP_HOUR, GOTOSLEEP_MINIUTE, GOTOSLEEP_SECOND, GOTOSLEEP_MILISECOND);
                }
                try {
                    // 오늘 날짜 yyyy/MM/dd 형태 + 공백 + 파이어베이스 END_TIME 데이터 를 합친 문자열을 Date로 파싱하고 getTime으로 long값 구함.
                    Date finishDate = dateFormat.parse(day.format(System.currentTimeMillis()) + " " + snapshot.getValue().toString());
                    Log.e(TAG, "testDate: " + finishDate + " testDate.getTime : " + finishDate.getTime());
                    finishTime = finishDate.getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                }


                // 화면 절전 모드 진입
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                // 현재 시간이 일과 종료시간 보다 작을시 == 일과 종료시간 전까지는 화면 절전 모드 해제
                if (System.currentTimeMillis() <= finishTime) {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    Log.e(TAG, "화면계속켜기on!!");
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


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
                    if (System.currentTimeMillis() < finishTime){
                        Log.e(TAG, "일과시간 : " + finishTime + " 현재시간 : " + System.currentTimeMillis() + ". 아직 일과시간입니다. 앱을 다시 실행시킵니다.");
                        getPackageList("watchdog");
                    }
                } else if(startTime != 0 && System.currentTimeMillis() > startTime && snapshot.getValue().toString().equals("connected")){
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
    public void makeStartWorkTime(int startHour, int startMinute, int startSecond, int startMillisecond){
        startWorkCalendar = Calendar.getInstance();
        finishWorkCalendar = Calendar.getInstance();

        startWorkCalendar.set(Calendar.HOUR_OF_DAY, startHour);
        startWorkCalendar.set(Calendar.MINUTE, startMinute);
        startWorkCalendar.set(Calendar.SECOND, startSecond);
        startWorkCalendar.set(Calendar.MILLISECOND, startMillisecond);


        // 일과 시작 시간.
        startTime = startWorkCalendar.getTimeInMillis();

        String formatStartTimeHours = hours.format(startWorkCalendar.getTimeInMillis());
        startWorkTime.setValue(formatStartTimeHours);

    }

    public void makeFinishWorkTime(int finishHour, int finishMinute, int finishSecond, int finishMillisecond){
        finishWorkCalendar = Calendar.getInstance();

        finishWorkCalendar.set(Calendar.HOUR_OF_DAY, finishHour);
        finishWorkCalendar.set(Calendar.MINUTE, finishMinute);
        finishWorkCalendar.set(Calendar.SECOND, finishSecond);
        finishWorkCalendar.set(Calendar.MILLISECOND, finishMillisecond);

        // 일과 종료 시간.
        finishTime = finishWorkCalendar.getTimeInMillis();


        String formatFinishTimeHours = hours.format(finishWorkCalendar.getTimeInMillis());
        finishWorkTime.setValue(formatFinishTimeHours);

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


    // 뒤로가기 버튼 두번 누르면 앱 종료되는 기능
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