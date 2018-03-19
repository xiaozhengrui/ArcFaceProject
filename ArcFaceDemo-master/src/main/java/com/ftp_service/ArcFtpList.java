package com.ftp_service;

import it.sauronsoftware.ftp4j.FTPDataTransferListener;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
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

import com.arcsoft_face_ui.MainActivity;
import com.arcsoft_face_ui.R;

/**
 * Created by techwinxiao on 18-2-4.
 */

public class ArcFtpList extends Activity {
    private String USERNAME = "";
    private String PASSWORD = "";
    private static final String TAG1 = "ArcFtpList";
    private ListView listView;
    private ArrayAdapter<String> adapter;

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
                                try {
                                    String[] file = Environment.getExternalStorageDirectory().list();//client.listNames();
                                    for (int i = 0; i < file.length; i++) {
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
                String dir = Environment.getExternalStorageDirectory()+"/";
                File fileDir = new File(dir);
                if (!fileDir.exists()) {
                    fileDir.mkdirs();
                }
                String path = dir + adapter.getItem(position);
                localFile = new File(path);
                // 意图实现activity的跳转
                Intent intent = new Intent(ArcFtpList.this, MainActivity.class);
                intent.putExtra("localpath",localFile.toString());
                // 这种启动方式：startActivity(intent);并不能返回结果
                setResult(RESULT_OK,intent);
                finish();
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