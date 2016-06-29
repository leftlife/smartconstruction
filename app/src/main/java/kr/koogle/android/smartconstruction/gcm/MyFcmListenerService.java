package kr.koogle.android.smartconstruction.gcm;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.List;
import java.util.Map;

import kr.koogle.android.smartconstruction.MainActivity;

public class MyFcmListenerService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage message){
        String from = message.getFrom();
        Map data = message.getData();
        String result = "dddddddddddd"; //data.getString("data");
        //Log.e("result : ", result);

        // Non-blocking methods. No need to use AsyncTask or background thread.
        //FirebaseMessaging.getInstance().subscribeToTopic("mytopic");
        //FirebaseMessaging.getInstance().unsubscribeToTopic("mytopic");

        if ( getRunningProcess(getBaseContext()).equals("kr.koogle.android.smartconstruction") ) // 앱이 실행중이면..
        {

        }
        else
        {
            // 큰 아이콘
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_menu_gallery);

            // 알림 사운드
            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            // 알림 클릭시 이동할 인텐트
            //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://developers.google.com/cloud-messaging/"));
            Intent intent = new Intent(this, MainActivity.class);

            // 노티피케이션을 생성할때 매개변수는 PendingIntent 이므로 Intent를 PendingIntent 로 만들어 주어야함.
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

            // 노티피케이션 빌더 : 위에서 생성한 이미지나 텍스트, 사운드 등을 설정해 줍니다.
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(android.R.drawable.ic_menu_gallery)
                    .setTicker("새로운 공지사항 등록")
                    .setLargeIcon(bitmap)
                    .setContentTitle(getRunningProcess(getBaseContext()))
                    .setContentText(result)
                    .setAutoCancel(true)
                    .setNumber(5)
                    .setSound(soundUri)
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // 노티피케이션을 생성합니다.
            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
        }
    }

    public static String getRunningProcess(Context context) {

        String strPackage = "";

        ActivityManager am = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> process = am.getRunningAppProcesses();
        for(ActivityManager.RunningAppProcessInfo proc : process)
        {
            if(proc.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND)
            {
                strPackage = proc.processName;
            }
        }
        return strPackage;
        /*
        String packageName = "";
        if (Build.VERSION.SDK_INT > 20)
        {
            packageName = am.getRunningAppProcesses().get(0).processName;
        }
        else
        {
            packageName = am.getRunningTasks(1).get(0).topActivity.getPackageName();
        }
        return packageName;
        */
    }
}
