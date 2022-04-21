package com.example.biolock;

import static com.example.biolock.LockChannel.CHANNEL_ID;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LockService extends Service {

    private UsageStatsManager usageStatsManager;
    private ActivityManager activityManager;
    private SharedPreferences block;
    private com.example.biolock.MainActivity acquire;
    private ArrayList<String> blockedlist;
    private String past;
    public com.example.biolock.LockWindow window;
    private Handler handler;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Intent notificationIntent = new Intent(this, com.example.biolock.MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Your Locking Service is running in the background")
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);
        block = getSharedPreferences("blocklist", MODE_PRIVATE);
        acquire = new com.example.biolock.MainActivity();
        blockedlist = new ArrayList<String>();

        window = new com.example.biolock.LockWindow(this);
        // Start the initial runnable task by posting through the handler
        handler = new Handler();
        handler.post(runTask);

        return START_STICKY;
    }

    private Runnable runTask = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void run() {
            // Execute tasks on main thread
            blockedlist = retrieveList(blockedlist);
            String app = getTopApp(getApplicationContext(), activityManager);
            app = getAppNameFromPkgName(getApplicationContext(), app);
            if (blockedlist.contains(app.trim()) && past != app) {
                // instantiate block screen here
                window.open();
            }
            if(app.trim().equals("com.sec.android.app.launcher") && past != app) {
                window.close();
            }
            past = app;
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            usageStatsManager = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, RestarterLockReciever.class);
        this.sendBroadcast(broadcastIntent);
    }

    public String getTopApp(@NonNull Context context, @NonNull ActivityManager activityManager) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            List<ActivityManager.RunningTaskInfo> appTasks = activityManager.getRunningTasks(1);
            if (null != appTasks && !appTasks.isEmpty()) {
                return appTasks.get(0).topActivity.getPackageName();
            }
        }
        else {
            long endTime = System.currentTimeMillis();
            long beginTime = endTime - 10000;
            String result = "";
            UsageEvents.Event event = new UsageEvents.Event();
            UsageEvents usageEvents = usageStatsManager.queryEvents(beginTime, endTime);
            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event);
                if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    result = event.getPackageName();
                }
            }
            if (!TextUtils.isEmpty(result)) {
                return result;
            }
        }
        return "";
    }

    public static String getAppNameFromPkgName(Context context, String packageName) {
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> installed = context.getPackageManager().queryIntentActivities(mainIntent, 0);
        for (ResolveInfo ri : installed) {
            if (ri.activityInfo != null && packageName.equals(ri.activityInfo.packageName.toString())) {
                    return ri.activityInfo.applicationInfo.loadLabel(
                            context.getPackageManager()).toString();
            }
        }
        return packageName;
    }

    public ArrayList<String> retrieveList(ArrayList<String> listblock) {
        Set<String> set = block.getStringSet("blocklist", null);
        if(set != null) {
            listblock.clear();
            listblock.addAll(set);
            return listblock;
        }
        else {
            return new ArrayList<String>();
        }
    }
}