package com.example.biolock;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        //here is the intent to move to new page
        Intent entertime = new Intent(Login.this, ScanFinger.class);
        if (!isAccessGrantedUsage()) {
            Intent setting = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(setting);
        }
        if (!isAccessGrantedDraw()) {
            Intent draw = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            startActivity(draw);
        }

        final EditText enter = findViewById(R.id.password);
        Button loginb = findViewById(R.id.loginbutton);
        TextView error = findViewById(R.id.errorMessage);

        SharedPreferences passlock = PreferenceManager.getDefaultSharedPreferences(this);
        String passDetails = passlock.getString("data", "pass");

        loginb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String check = enter.getText().toString();

                if(check.equals(passDetails)) {
                    startActivity(entertime);
                    //startActivity is a function in java that runs the intent
                }
                else {
                    error.setText("Error: Wrong Password");
                    error.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private boolean isAccessGrantedUsage() {
        try {
            PackageManager packageManager = getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            int mode = 0;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                        applicationInfo.uid, applicationInfo.packageName);
            }
            return (mode == AppOpsManager.MODE_ALLOWED);

        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private boolean isAccessGrantedDraw() {
        try {
            PackageManager packageManager = getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            int mode = 0;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW,
                        applicationInfo.uid, applicationInfo.packageName);
            }
            return (mode == AppOpsManager.MODE_ALLOWED);

        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
