package com.example.biolock;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

//creates anotifcation so that the app can run in the abckground without being eliminated
public class LockChannel extends Application {
    public static final String CHANNEL_ID = "LockingChannel";

    //creates the notification
    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
    }

    //creates the notification and inserts text
    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ServiceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Locking Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(ServiceChannel);
        }
    }
}
