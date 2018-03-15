package com.ftp;

import android.content.Context;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import android.os.Handler;

/**
 * Created by xiao on 2018/1/26.
 */

public class ArcFtpHandler extends Handler {
    private static final String TAG = "FtpHandler";
    private static final int DOWNLOAD_PREPARE = 0;
    private static final int DOWNLOAD_WORK = 1;
    private static final int DOWNLOAD_OK = 2;
    private static final int DOWNLOAD_ERROR = 3;
    private Context mContext;
    private ProgressBar pb;
    int fileSize = 0;
    int downloadSize = 0;
    private Button bt;
    private TextView tv;
    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case DOWNLOAD_PREPARE:
                Toast.makeText(mContext, "准备下载", Toast.LENGTH_SHORT).show();
                pb.setVisibility(ProgressBar.VISIBLE);
                Log.e(TAG, "文件大小:" + fileSize);
                pb.setMax(fileSize);
                break;
            case DOWNLOAD_WORK:
                Log.e(TAG, "已经下载:" + downloadSize);
                pb.setProgress(downloadSize);
                int res = downloadSize * 100 / fileSize;
                tv.setText("已下载：" + res + "%");
                    /*bt.setText(FileUtil.FormetFileSize(downloadSize) + "/"
                            + FileUtil.FormetFileSize(fileSize));*/
                break;
            case DOWNLOAD_OK:
                //bt.setText("下载完成显示图片");
                downloadSize = 0;
                fileSize = 0;
                //Toast.makeText(mContext, "下载成功", Toast.LENGTH_SHORT).show();
                break;
            case DOWNLOAD_ERROR:
                downloadSize = 0;
                fileSize = 0;
                //Toast.makeText(mContext, "下载失败", Toast.LENGTH_SHORT).show();
                break;
        }
        super.handleMessage(msg);
    }
}
