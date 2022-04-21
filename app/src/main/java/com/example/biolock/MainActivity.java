package com.example.biolock;


import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    TextView text;
    ListView listView;
    List<String> apps;
    SharedPreferences block;
    ArrayList<String> blockedlist;
    Intent serviceIntent;
    private LockService lockService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainactivity);

        listView = findViewById(R.id.listview);
        text = findViewById(R.id.totalapp);
        block = getSharedPreferences("blocklist", MODE_PRIVATE);

        lockService = new LockService();
        serviceIntent = new Intent(this, lockService.getClass());
        if (!isMyServiceRunning(lockService.getClass())) {
            startService(serviceIntent);
        }
    }

    public void startService(View v) {
        Intent serviceIntent = new Intent(this, LockService.class);
        startService(serviceIntent);
    }

    public void stopService() {
        Intent serviceIntent = new Intent(this, LockService.class);
        stopService(serviceIntent);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("Service status", "Running");
                return true;
            }
        }
        Log.i ("Service status", "Not running");
        return false;
    }

    @Override
    protected void onDestroy() {
        //stopService(mServiceIntent);
        Log.i ("Service status", "Not running");
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, RestarterLockReciever.class);
        this.sendBroadcast(broadcastIntent);
        super.onDestroy();
    }

    public void getallapps(View view) throws PackageManager.NameNotFoundException {
        startService(view);
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        // get list of all the apps installed
        List<ResolveInfo> installed = getPackageManager().queryIntentActivities(mainIntent, 0);
        String name;

        // get size of installed list and create a list
        apps = new ArrayList<String>();
        for (ResolveInfo ri : installed) {
            if (ri.activityInfo != null) {
                // get package
                Resources res = getPackageManager().getResourcesForApplication(ri.activityInfo.applicationInfo);
                // if activity label res is found
                if (ri.activityInfo.labelRes != 0) {
                    name = res.getString(ri.activityInfo.labelRes);
                } else {
                    name = ri.activityInfo.applicationInfo.loadLabel(
                            getPackageManager()).toString();
                }
                apps.add(name);
            }
        }
        java.util.Collections.sort(apps);
        // set all the apps name in list view
        listView.setAdapter(new MyListAdapter(this, R.layout.applistitem, apps));
        // write total count of apps available.
        text.setText(installed.size() + " Apps are installed");
    }

    private class MyListAdapter extends ArrayAdapter<String> {
        private int layout;
        private MyListAdapter(Context context, int resource, List<String> objects) {
            super(context, resource, objects);
            layout = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder mainViewHolder = null;
            blockedlist = new ArrayList<String>();
            blockedlist = retrieveList(blockedlist);
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(layout, parent, false);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.applistname = (TextView) convertView.findViewById(R.id.appname);
                viewHolder.applistswitch = (Switch) convertView.findViewById(R.id.appswitch);
                if(!(blockedlist.isEmpty()) && blockedlist.contains(viewHolder.applistname.getText().toString())) {
                    viewHolder.applistswitch.setChecked(true);
                }
                viewHolder.applistswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                        if(isChecked) {
                            blockedlist = retrieveList(blockedlist);
                            String appname = viewHolder.applistname.getText().toString();
                            Log.d("apple4","new "+blockedlist);
                            if(!(blockedlist.contains(appname))) {
                                blockedlist.add(appname);
                                Log.d("apple","saved "+appname);
                            }
                            saveList(blockedlist);
                        }
                        else {
                            blockedlist = retrieveList(blockedlist);
                            String appname = viewHolder.applistname.getText().toString();
                            Log.d("apple2","removed "+appname);
                            blockedlist = removeInstance(blockedlist, appname);
                            Log.d("apple3","new "+blockedlist);
                            saveList(blockedlist);
                        }
                    }
                });
                convertView.setTag(viewHolder);
            }
            else {
                mainViewHolder = (ViewHolder) convertView.getTag();
                mainViewHolder.applistname.setText(getItem(position));
                if(!(blockedlist.isEmpty()) && blockedlist.contains(mainViewHolder.applistname.getText().toString())) {
                    mainViewHolder.applistswitch.setChecked(true);
                }
            }

            return convertView;
        }
    }

    public class ViewHolder {
        Switch applistswitch;
        TextView applistname;
    }

    private void saveList(ArrayList<String> listblock) {
        SharedPreferences.Editor editor = block.edit();
        if(!(listblock.isEmpty())) {
            Set<String> set = new HashSet<String>();
            set.addAll(listblock);
            editor.clear();
            editor.putStringSet("blocklist", set);
            editor.apply();
            Log.d("storesharedPref","saved "+listblock);
        }
        else {
            editor.clear();
            editor.apply();
        }
    }

    public ArrayList<String> retrieveList(ArrayList<String> listblock) {
        Set<String> set = block.getStringSet("blocklist", null);
        if(set != null) {
            listblock.addAll(set);
            Log.d("retrievesharedPref","retrieved "+listblock);
            return listblock;
        }
        else {
            return new ArrayList<String>();
        }
    }

    private ArrayList<String> removeInstance(ArrayList<String> listblock, String remov) {
        if(listblock.get(0).equals(remov)) {
            listblock.remove(0);
        }
        for(int i = 0; i < listblock.size(); i++ ) {
            if(listblock.get(i).equals(remov)) {
                listblock.remove(i);
            }
        }
        return listblock;
    }


}
