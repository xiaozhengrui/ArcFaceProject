package com.arcsoft_face_ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.advert.mp4.act.PlayMP4Activity;
import com.advert.mp4.fragment.VideoViewFragment;
import com.ftp_service.FsService;

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
                Log.d(TAG, "startService when boot completed!!");
                context.startService(serverService);
                Intent itForMp4 = new Intent();
                itForMp4.setClass(context,PlayMP4Activity.class);
                itForMp4.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(itForMp4);
            }
        }
    }
}
