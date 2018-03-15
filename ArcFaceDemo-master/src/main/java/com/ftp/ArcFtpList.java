package com.ftp;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.arcsoft.sdk_demo_1.MainActivity;
import com.arcsoft.sdk_demo_1.R;

/**
 * Created by techwinxiao on 18-2-4.
 */

public class ArcFtpList extends Activity {
    /** 只需要ip地址，不需要前面的ftp:// */
    private static final String HOST = "192.168.1.101";
    private static final int PORT = 21;
    private String USERNAME = "";
    private String PASSWORD = "";
    private static final String TAG1 = "FtpConnect";
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private static final int REQUEST_CODE_FTP_IMAGE_OP = 4;
    private FTPClient client;
    private static File localFile;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0x001) {
                adapter.add((String) msg.obj);
            } else if (msg.what == 0x002) {
                Toast.makeText(ArcFtpList.this,
                        "connect fail", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ftp_list);

        USERNAME = ((EditText) findViewById(R.id.etUserName)).getText()
                .toString();
        //USERNAME += "@***.com";//这里是你的域名。一般公司给员工分配的用户名都有@的后缀
        PASSWORD = ((EditText) findViewById(R.id.etPwd)).getText().toString();
        Log.d(TAG1, USERNAME);
        Log.d(TAG1, PASSWORD);
        USERNAME = "ftpuser";
        PASSWORD = "123456";
        listView = (ListView) findViewById(R.id.listView1);
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1);
        listView.setAdapter(adapter);
        ((Button) findViewById(R.id.btnGet))
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        adapter.clear();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                /*client = new FTPClient();
                                client.setCharset("utf-8");
                                client.setType(FTPClient.TYPE_BINARY);
                                Log.i(TAG1, "start client login");*/
                                try {
                                    /*client.connect(HOST, PORT);
                                    client.login(USERNAME, PASSWORD);
                                    Log.i(TAG1, "client login ok!");
                                    client.changeDirectory("picture");*/
                                    String[] file = Environment.getExternalStorageDirectory().list();//client.listNames();
                                    for (int i = 0; i < file.length; i++) {
                                        Log.d(TAG1, file[i]);
                                        Message message = handler
                                                .obtainMessage(0x001, file[i]);
                                        handler.sendMessage(message);
                                    }
                                } catch (Exception e) {
                                    handler.sendEmptyMessage(0x002);
                                    return;
                                }
                            }
                        }).start();
                    }
                });
        /**
         * commons-net-3.0.1.jar
         * listNames返回NULL,list返回Int,listFiles返回NULL
         * 因为传进去的参数是(String)null
         * 自己可以去了解，我这里就不演示了
         */

        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    final int position, long id) {
                /*String dir = Environment.getExternalStorageDirectory()
                        + "/Download/";
                File fileDir = new File(dir);
                if (!fileDir.exists()) {
                    fileDir.mkdirs();
                }*/
                String dir = Environment.getExternalStorageDirectory()+"/";
                File fileDir = new File(dir);
                if (!fileDir.exists()) {
                    fileDir.mkdirs();
                }
                String path = dir + adapter.getItem(position);
                localFile = new File(path);
                Log.d(TAG1,"p2:"+adapter.getItem(position));
                Log.d(TAG1,"p3:"+localFile.toString());
                /*if (localFile.exists()) {
                    localFile.delete();
                    Log.i("delete", "original ftpFile deleted");
                }*/
                // 意图实现activity的跳转
                Intent intent = new Intent(ArcFtpList.this, MainActivity.class);
                intent.putExtra("localpath",localFile.toString());
                // 这种启动方式：startActivity(intent);并不能返回结果
                setResult(RESULT_OK,intent);
                finish();
                /*try {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // 参考/doc/manual.en.html，最后面的参数是监听器
                                client.download(adapter.getItem(position),
                                        localFile, new MyTransferListener());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }*/
            }
        });
    }

    public class MyTransferListener implements FTPDataTransferListener {
        public void started() {
            Log.i(TAG1, "download start");
        }

        public void transferred(int length) {
            Log.i(TAG1, "have download " + length + " bytes");
        }

        public void completed() {
            Log.i(TAG1, "download completed");
             // 意图实现activity的跳转
             Intent intent = new Intent(ArcFtpList.this, MainActivity.class);
            intent.putExtra("localpath",localFile.toString());
             // 这种启动方式：startActivity(intent);并不能返回结果
             setResult(RESULT_OK,intent);
             finish();
        }

        public void aborted() {
            Log.i(TAG1, "download aborted");
        }

        public void failed() {
            Log.i(TAG1, "download failed");
        }
    }
}