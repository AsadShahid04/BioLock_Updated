package com.example.biolock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class RebootLockReceiver extends BroadcastReceiver  {
    
    @Override
    //used to reboot the serviceIntent
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)){
            Intent serviceIntent  = new Intent(context, LockService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                context.startForegroundService(serviceIntent);
            else
                context.startService(serviceIntent);
        }
    }
}
