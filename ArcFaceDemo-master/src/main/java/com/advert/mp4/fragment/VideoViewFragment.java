package com.advert.mp4.fragment;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.arcsoft_face_ui.R;

import java.io.File;
import java.util.ArrayList;

import static com.arcsoft_face_ui.R.id.path_tv;

/**
 * 使用 {@link VideoView} 播放MP4
 * Created by Xiao on 2018/10/11.
 */
public class VideoViewFragment extends Fragment {
    private static final String TAG = "VideoApp";
    private static ArrayList<String> mMP4Path = new ArrayList<>();
    private static int playId = 0;
    private static int mp4TotalNum = 0;
    VideoView mVideoView;
    MediaController mMediaController;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        String mMP4ParentPath = dir.getAbsolutePath();
        Log.d(TAG, "onCreate: dir -> " + mMP4ParentPath);
        if (dir.exists()) {
            for (File f : dir.listFiles()) {
                Log.d(TAG, " ----- " + f);
                if (f.getAbsolutePath().endsWith(".mp4")) {
                    mMP4Path.add(f.getAbsolutePath());
                    Log.d(TAG, "onCreate: mp4 -> " + f.getAbsolutePath());
                    //break;
                }
            }
            mp4TotalNum = mMP4Path.size();
        } else {
            Log.e(TAG, "onCreate: DCIM not exist");
        }
        if(mMP4Path.isEmpty()){
            Toast.makeText(getContext(), "未找到视频文件播放!", Toast.LENGTH_SHORT).show();
        }
//        if (TextUtils.isEmpty(mMP4Path)) {
//            Toast.makeText(getContext(), "MP4 not found!", Toast.LENGTH_SHORT).show();
//        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_video_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView pathTv = (TextView) view.findViewById(path_tv);
        mVideoView = (VideoView)view.findViewById(R.id.video_view);
        Log.d(TAG, "onViewCreated: mMP4Path: " + mMP4Path);
        mMediaController = new MediaController(getContext());

        if (!TextUtils.isEmpty(mMP4Path.get(playId))) {
            mVideoView.setVideoPath(mMP4Path.get(playId));
            mVideoView.setMediaController(mMediaController);
            mVideoView.seekTo(0);
            mVideoView.requestFocus();
            if(mVideoView.isPlaying()){
                mVideoView.resume();
            }

            mVideoView.start();
            pathTv.setText(mMP4Path.get(playId));
        }
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playId ++;
                if(playId >= mp4TotalNum){
                    playId = 0;
                }
                Log.e(TAG, "play completion,next:"+mMP4Path.get(playId));
                if (!TextUtils.isEmpty(mMP4Path.get(playId))) {
                    mVideoView.setVideoPath(mMP4Path.get(playId));
                    mVideoView.seekTo(0);
                    mVideoView.start();
                    pathTv.setText(mMP4Path.get(playId));
                }
            }
        });
    }

}
