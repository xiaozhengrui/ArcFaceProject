package com.arcsoft.sdk_demo_1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ftp.FsService;

/**
 * Created by techwinxiao on 18-3-6.
 */

public  class BootBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "BootBroadcastReceiver";
    static final String ACTION="android.intent.action.BOOT_COMPLETED";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "BootBroadcastReceiver");
        if(intent.getAction().equals(ACTION)) {
            Intent serverService = new Intent(context, FsService.class);
            //serverService.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (!FsService.isRunning()) {
                //warnIfNoExternalStorage();
                //ContextWrapper cWrapper = new ContextWrapper(context);
                Log.d(TAG, "startService");
                context.startService(serverService);
            }
        }
    }
}
