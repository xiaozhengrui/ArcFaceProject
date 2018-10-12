package com.advert.mp4.act;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;


import com.advert.mp4.fragment.VideoViewFragment;
import com.arcsoft_face_ui.R;

/**
 * Play mp4
 * Created by Xiao on 2018/10/11.
 */
public class PlayMP4Activity extends AppCompatActivity {
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        setContentView(R.layout.act_frame);
        hideBottomUIMenu();
        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new VideoViewFragment()).commit();
        }
    }

    /**
     * 隐藏虚拟按键，并且全屏
     */ protected void hideBottomUIMenu() {
         int flags;
         int curApiVersion = android.os.Build.VERSION.SDK_INT; // This work only for android 4.4+
         if(curApiVersion >= Build.VERSION_CODES.KITKAT){ // This work only for android 4.4+ // hide navigation bar permanently in android activity // touch the screen, the navigation bar will not show
             flags = View.SYSTEM_UI_FLAG_FULLSCREEN |
                     View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                     View.SYSTEM_UI_FLAG_IMMERSIVE |
                     View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                     View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
         }else{ // touch the screen, the navigation bar will show
             flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|
                     View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
         } // must be executed in main thread :)
         getWindow().getDecorView().setSystemUiVisibility(flags);
     }
}
