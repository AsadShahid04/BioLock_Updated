package com.example.biolock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class RestarterLockReciever extends BroadcastReceiver {

    @Override
    //starting the serviceIntent
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent  = new Intent(context, com.example.biolock.LockService.class); //make new service

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(serviceIntent);
        else
            context.startService(serviceIntent);
    }
}
