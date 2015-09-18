package com.sicoms.smartplug.util;

import java.util.HashMap;
import java.util.Iterator;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import com.sicoms.smartplug.R;

public class NotificationUtil {

    public static final int NOTIFICATION_ID = 1;

    public static void sendNotification(Context context, Class<?> viewClass, String title, String content, HashMap<String, String> extraMap) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(context.getApplicationContext(), viewClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Iterator<String> it = extraMap.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next().toString();
            intent.putExtra(key, extraMap.get(key));
        }

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.logo_alarm_icon)
                .setLargeIcon(bitmap)
                .setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setContentText(content)
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 500});

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    private int getNotificationIcon() {
        boolean whiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return whiteIcon ? R.drawable.logo : R.drawable.ic_launcher;
    }
}
