package com.apk_update;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import com.arcsoft_face_ui.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by techwinxiao on 18-3-20.
 */

public class ApkDownLoad extends Activity{
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        String AppVersionName = getAPPVersionName(this);
        String NewApkVersionName = getNewApkVersionName(this);
        new AlertDialog.Builder(this)
                .setTitle("确认版本信息，是否升级?"+" 当前版本:"+AppVersionName)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showDownloadProgressDialog(ApkDownLoad.this);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                })
                .show();
        //showDownloadProgressDialog(this);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    private void showDownloadProgressDialog(Context context) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("提示");
        progressDialog.setMessage("正在下载...");
        progressDialog.setIndeterminate(false);
        progressDialog.setMax(100);
        progressDialog.setCancelable(false);                    //设置不可点击界面之外的区域让对话框小时
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);         //进度条类型
        progressDialog.show();
        String downloadUrl = "http://192.168.1.104:8008/ArcFaceDemo-master-release.apk"; //这里写你的apk url地址
        new DownloadAPK(progressDialog).execute(downloadUrl);
    }

    /**
     * 获取apk的版本号 currentVersionCode
     *
     * @param ctx
     * @return
     */
    public float getAPPVersionCode(Context ctx) {
        float currentVersionCode = 0;
        PackageManager manager = ctx.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(ctx.getPackageName(), 0);
            currentVersionCode = info.versionCode; // 版本号
            System.out.println(currentVersionCode + " " + currentVersionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return currentVersionCode;
    }


    public String getAPPVersionName(Context ctx) {
        String currentVersionName = null;
        PackageManager manager = ctx.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(ctx.getPackageName(), 0);
            currentVersionName = info.versionName; // 版本名
            System.out.println(currentVersionName + " " + currentVersionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return currentVersionName;
    }

    public float getNewApkVersionCode(Context ctx){
        float newVersionCode = 0;
        String archiveFilePath=Environment.getExternalStorageDirectory().getPath() + "/arcFaceUpdate/ArcFaceDemo-master-release.apk";//安装包路径
        PackageManager pm = ctx.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(archiveFilePath, PackageManager.GET_ACTIVITIES);
        if(info != null){
            ApplicationInfo appInfo = info.applicationInfo;
            String appName = pm.getApplicationLabel(appInfo).toString();
            String packageName = appInfo.packageName;  //得到安装包名称
            String version=info.versionName;       //得到版本信息
            newVersionCode = info.versionCode;
        }
        return newVersionCode;
    }


    public String getNewApkVersionName(Context ctx){
        String newVersionCodeName = null;
        String archiveFilePath=Environment.getExternalStorageDirectory().getPath() + "/arcFaceUpdate/ArcFaceDemo-master-release.apk";//安装包路径
        PackageManager pm = ctx.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(archiveFilePath, PackageManager.GET_ACTIVITIES);
        if(info != null){
            ApplicationInfo appInfo = info.applicationInfo;
            //String appName = pm.getApplicationLabel(appInfo).toString();
            //String packageName = appInfo.packageName;  //得到安装包名称
            newVersionCodeName=info.versionName;       //得到版本信息
        }
        return newVersionCodeName;
    }

    private class DownloadAPK extends AsyncTask<String, Integer, String> {
        ProgressDialog progressDialog;
        File file;

        public DownloadAPK(ProgressDialog progressDialog) {
            this.progressDialog = progressDialog;
        }

        @Override
        protected String doInBackground(String... params) {
            URL url;
            HttpURLConnection conn;
            BufferedInputStream bis = null;
            FileOutputStream fos = null;

            try {
                url = new URL(params[0]);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);

                int fileLength = conn.getContentLength();
                bis = new BufferedInputStream(conn.getInputStream());
                String fileName = Environment.getExternalStorageDirectory().getPath() + "/arcFaceUpdate/ArcFaceDemo-master-release.apk";
                file = new File(fileName);
                if(file != null && file.exists()){
                    //delete old apk
                    file.delete();
                }

                if (!file.exists()) {
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }
                    file.createNewFile();
                }

                fos = new FileOutputStream(file);
                byte data[] = new byte[70 * 1024];
                long total = 0;
                int count;
                while ((count = bis.read(data)) != -1) {
                    total += count;
                    publishProgress((int) (total * 100 / fileLength));
                    fos.write(data, 0, count);
                    fos.flush();
                }
                fos.flush();

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    if (bis != null) {
                        bis.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
// 这里 改变ProgressDialog的进度值
            progressDialog.setProgress(progress[0]);
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            openFile(file); //打开安装apk文件操作
            progressDialog.dismiss(); //关闭对话框
        }
        private void openFile(File file) {
            if (file!=null){
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                startActivity(intent);
            }
        }
    }

}
