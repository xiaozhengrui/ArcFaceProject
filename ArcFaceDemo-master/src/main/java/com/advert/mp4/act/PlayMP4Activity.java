package com.advert.mp4.act;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;



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
        setContentView(R.layout.act_frame);
        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new VideoViewFragment()).commit();
        }
    }

}
