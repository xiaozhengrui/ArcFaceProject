package com.ftp;



import android.app.Activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;



import android.app.Dialog;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.arcsoft.sdk_demo_1.R;

import it.sauronsoftware.ftp4j.FTPClient;

/**
 * Created by xiao on 2018/1/26.
 */

public class ArcFtpActivity extends Activity implements android.view.View.OnClickListener {

    private static final String TAG = "FtpActivity";
    private static final String TAG1 = "FtpConnect";
    private Context mContext;
    private ArcFtpHandler handler;
    private static ArcFtp arcFtp;
    private Button bt;
    private ProgressBar pb;
    /** 下载过程中不能点击 */
    private boolean isClick = false;
    private boolean downloadOk = false;
    private TextView tv;

    private static final int DOWNLOAD_PREPARE = 0;
    private static final int DOWNLOAD_WORK = 1;
    private static final int DOWNLOAD_OK = 2;
    private static final int DOWNLOAD_ERROR = 3;

    String filePath;




    private ImageView imageView;

    /*public DownloadDialog(Context context, String url) {

        super(context, R.style.Theme_CustomDialog);
        mContext = context;
        this.url = url;
        filePath = FileUtil.getPath(mContext, url);
    }*/

    /*@Override
    public void cancel() {
        super.cancel();
    }*/

    /**
     * 下载文件
     */
    private void downloadFile() {
        /*try {
            URL u = new URL(url);
            URLConnection conn = u.openConnection();
            InputStream is = conn.getInputStream();
            fileSize = conn.getContentLength();
            if (fileSize < 1 || is == null) {
                sendMessage(DOWNLOAD_ERROR);
            } else {
                sendMessage(DOWNLOAD_PREPARE);
                FileOutputStream fos = new FileOutputStream(filePath);
                byte[] bytes = new byte[1024];
                int len = -1;
                while ((len = is.read(bytes)) != -1) {
                    fos.write(bytes, 0, len);
                    fos.flush();
                    downloadSize += len;
                    sendMessage(DOWNLOAD_WORK);
                }
                sendMessage(DOWNLOAD_OK);
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            sendMessage(DOWNLOAD_ERROR);
            e.printStackTrace();
        }*/
        try{
            arcFtp.download(new File("/sdcard/ftp/ftpdata/test/work.txt"),"test_1.jpg");
            downloadOk = true;
            sendMessage(DOWNLOAD_OK);
        }catch(Exception e){
            downloadOk = false;
            sendMessage(DOWNLOAD_ERROR);
            e.printStackTrace();
        }

    }
    /***
     * 得到文件的路径
     *
     * @return
     */
    public String getFilePath() {
        return filePath;
    }
    private void init() {
        arcFtp = ArcFtp.getInstance();
        /*FTPClient client;
        try{
            client =  arcFtp.getClient();
        }catch(Exception e){
            Log.i(TAG1, "client login exception!");
            e.printStackTrace();
        }*/
        handler = new ArcFtpHandler();
        bt = (Button) findViewById(R.id.button_down);
        bt.setOnClickListener(this);
        tv = (TextView) this.findViewById(R.id.down_tv);
        pb = (ProgressBar) this.findViewById(R.id.down_pb);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_down:
                Log.d(TAG1,"push down button!");
                if (true) {
                    // 启动一个线程下载文件
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            downloadFile();
                        }
                    });
                    thread.start();
                    isClick = false;
                }

                if (downloadOk) // 下载完成后 ，把图片显示在ImageView上面
                {
                    imageView.setImageBitmap(BitmapFactory.decodeFile(filePath));
                    //cancel();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ftptest);
        init();
    }

    /**
     * @param what
     */
    private void sendMessage(int what) {
        Message m = new Message();
        m.what = what;
        handler.sendMessage(m);
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    /*@Override
    public void show() {
        isClick = true;
        downloadOk = false;
        super.show();
    }*/

}
