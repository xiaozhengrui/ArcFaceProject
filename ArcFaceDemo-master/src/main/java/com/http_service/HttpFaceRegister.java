package com.http_service;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;


import com.arcsoft.facedetection.AFD_FSDKEngine;
import com.arcsoft.facedetection.AFD_FSDKError;
import com.arcsoft.facedetection.AFD_FSDKFace;
import com.arcsoft.facedetection.AFD_FSDKVersion;
import com.arcsoft.facerecognition.AFR_FSDKEngine;
import com.arcsoft.facerecognition.AFR_FSDKError;
import com.arcsoft.facerecognition.AFR_FSDKFace;
import com.arcsoft.facerecognition.AFR_FSDKVersion;
import com.arcsoft_face_ui.FaceDB;
import com.guo.android_extend.image.ImageConverter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

//import sun.misc.BASE64Decoder;

/**
 * Created by techwinxiao on 18-3-21.
 */

public class HttpFaceRegister {
    final static String TAG="HttpFaceRegister";
    final static int REGISTER_ERROR_NO_FACE = 1;//未识别到人脸
    //final static int REGISTER_ERROR_RECOVER = 2;//注册信息重复，重名+匹配度>0.6
    File cache;
    String httpServerPath = "http://192.168.1.105:8008";
    public FaceDB mFaceDB;
    public HashMap<String,byte[]> faceList = new HashMap<String,byte[]>();
    public HttpFaceRegister(Context mContext){
        cache = new File(Environment.getExternalStorageDirectory(),"cache");
        if(!cache.exists()){
            cache.mkdirs();
        }
        mFaceDB = new FaceDB(mContext.getExternalCacheDir().getPath());
    }


    public int detecterHttpFace(String name){
        int detectResult = 0;
        Bitmap mBitmap = decodeBitmapSafe(faceList.get(name), 1920, 1080);
        if(mBitmap == null){
            return REGISTER_ERROR_NO_FACE;
        }
        Log.d(TAG,"detecterHttpFace ok");
        byte[] data = new byte[mBitmap.getWidth() * mBitmap.getHeight() * 3 / 2];
        ImageConverter convert = new ImageConverter();
        convert.initial(mBitmap.getWidth(), mBitmap.getHeight(), ImageConverter.CP_PAF_NV21);
        if (convert.convert(mBitmap, data)) {
            Log.d(TAG, "convert ok!");
        }
        convert.destroy();

        AFD_FSDKEngine engine = new AFD_FSDKEngine();
        AFD_FSDKVersion version = new AFD_FSDKVersion();
        List<AFD_FSDKFace> result = new ArrayList<AFD_FSDKFace>();
        AFD_FSDKError err = engine.AFD_FSDK_InitialFaceEngine(FaceDB.appid, FaceDB.fd_key, AFD_FSDKEngine.AFD_OPF_0_HIGHER_EXT, 16, 5);
        Log.d(TAG, "AFD_FSDK_InitialFaceEngine = " + err.getCode());
        if (err.getCode() != AFD_FSDKError.MOK) {
            Log.d(TAG,"AFD_FSDK_InitialFaceEngine error!");
        }
        err = engine.AFD_FSDK_GetVersion(version);
        Log.d(TAG, "AFD_FSDK_GetVersion =" + version.toString() + ", " + err.getCode());
        err  = engine.AFD_FSDK_StillImageFaceDetection(data, mBitmap.getWidth(), mBitmap.getHeight(), AFD_FSDKEngine.CP_PAF_NV21, result);
        Log.d(TAG, "AFD_FSDK_StillImageFaceDetection =" + err.getCode() + "<" + result.size());

        if (!result.isEmpty()) {
            AFR_FSDKVersion version1 = new AFR_FSDKVersion();
            AFR_FSDKEngine engine1 = new AFR_FSDKEngine();
            AFR_FSDKFace result1 = new AFR_FSDKFace();
            AFR_FSDKError error1 = engine1.AFR_FSDK_InitialEngine(FaceDB.appid, FaceDB.fr_key);
            Log.d("com.arcsoft", "AFR_FSDK_InitialEngine = " + error1.getCode());
            if (error1.getCode() != AFD_FSDKError.MOK) {
                Log.d(TAG,"AFR_FSDK_InitialEngine error!");
            }
            error1 = engine1.AFR_FSDK_GetVersion(version1);
            Log.d("com.arcsoft", "FR=" + version.toString() + "," + error1.getCode()); //(210, 178 - 478, 446), degree = 1　780, 2208 - 1942, 3370
            error1 = engine1.AFR_FSDK_ExtractFRFeature(data, mBitmap.getWidth(), mBitmap.getHeight(), AFR_FSDKEngine.CP_PAF_NV21, new Rect(result.get(0).getRect()), result.get(0).getDegree(), result1);
            Log.d("com.arcsoft", "Face=" + result1.getFeatureData()[0] + "," + result1.getFeatureData()[1] + "," + result1.getFeatureData()[2] + "," + error1.getCode());
            if(error1.getCode() == error1.MOK) {
                mFaceDB.addFace(name, result1.clone());
                Log.d(TAG, "mFaceDB.addFace");
            } else {
                detectResult = REGISTER_ERROR_NO_FACE;
            }
            error1 = engine1.AFR_FSDK_UninitialEngine();
            Log.d("com.arcsoft", "AFR_FSDK_UninitialEngine : " + error1.getCode());
        } else {
            detectResult = REGISTER_ERROR_NO_FACE;
        }
        err = engine.AFD_FSDK_UninitialFaceEngine();
        Log.d(TAG, "AFD_FSDK_UninitialFaceEngine =" + err.getCode());
        return detectResult;
    }



    public static Bitmap decodeBitmapSafe(byte[] bytes,int maxWidth, int maxHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; //只读取图片尺寸

        //计算实际缩放比例
        int scale = 1;
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            if ((options.outWidth / scale > maxWidth &&
                    options.outWidth / scale > maxWidth * 1.4) ||
                    (options.outHeight / scale > maxHeight &&
                            options.outHeight / scale > maxHeight * 1.4)) {
                scale++;
            } else {
                break;
            }
        }

        options.inSampleSize = scale;
        options.inJustDecodeBounds = false;//读取图片内容
        //options.inPreferredConfig = Bitmap.Config.RGB_565; //根据情况进行修改
        Bitmap bitmap = null;
        try {
            //bitmap = BitmapFactory.decodeStream(is,null,options);
            bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length,options);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return bitmap;
    }



    public String HttpRegisterGet() throws JSONException {
        JSONObject jsonRec = new JSONObject();
        try {
            JSONObject  obj = new JSONObject();
            //要向服务器发的json
            //obj.put("content", "XXX");
            // 创建url资源
            URL url = new URL("http://192.168.1.103:8080/arcFace_web_2/testServlet1");
            // 建立http连接
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // 设置允许输出
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 设置不用缓存
            conn.setUseCaches(false);
            // 设置传递方式
            conn.setRequestMethod("POST");
            // 设置维持长连接
            conn.setRequestProperty("Connection", "Keep-Alive");
            // 设置文件字符集:
            conn.setRequestProperty("Charset", "UTF-8");
            // 设置文件类型:
            conn.setRequestProperty("contentType", "application/json");

            //debug property
            Map map = conn.getRequestProperties();
            Set set = map.entrySet();

            Iterator iterator = set.iterator();
            while (iterator.hasNext()) {
                System.out.println(iterator.next());
            }

            // 开始连接请求
            conn.connect();
            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
            // 写入请求的字符串
            writer.write(obj.toString());
            writer.flush();
            writer.close();


            //接收服务器返回的json
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8")) ;
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            conn.disconnect();
            if(sb.toString()==null|| "".equals(sb.toString())){
                return null;
            }
            jsonRec = new JSONObject(sb.toString());//JSONObject.fromObject(sb.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
        //处理json
        String content = jsonRec.getString("content");
        String name = jsonRec.getString("name");
        Log.d(TAG,"jsonRec content:"+content);
        Log.d(TAG,"jsonRec name:"+name);
        if(content == null || name == null){
            return null;
        }
        byte[] bytes=null;

        //BASE64Decoder decoder = new BASE64Decoder();
        try {
            // Base64解码
            //bytes = decoder.decodeBuffer(content);
            bytes = Base64.decode(content,Base64.DEFAULT);

            for (int i = 0; i < bytes.length; ++i) {
                if (bytes[i] < 0) {// 调整异常数据
                    bytes[i] += 256;
                }
            }

            // 生成图片
            /*OutputStream outs = new FileOutputStream(Environment.getExternalStorageDirectory()+name);
            outs.write(bytes);
            outs.flush();
            outs.close();*/

        } catch (Exception e) {
            e.printStackTrace();
        }
        if(bytes != null){
            faceList.put(name,bytes);
            return name;
        }
        return null;
    }



    public void getImageFromHttp(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG,"getImageFromHttp");
                while(true) {
                    try {
                        String name = HttpRegisterGet();
                        if (name != null) {
                            Log.d(TAG,"get http register name:"+name);
                            detecterHttpFace(name);
                            faceList.clear();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try{
                        Thread.sleep(1000);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }
}
