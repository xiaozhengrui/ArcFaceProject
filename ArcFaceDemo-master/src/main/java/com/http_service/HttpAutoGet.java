package com.http_service;

import android.os.Environment;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by techwinxiao on 18-3-22.
 */

public class HttpAutoGet {
    public void HttpGet() throws JSONException {
        JSONObject jsonRec = new JSONObject();
        try {
            JSONObject  obj = new JSONObject();
            //要向服务器发的json
            //obj.put("content", "XXX");
            // 创建url资源
            URL url = new URL("http://localhost:8080/arcFace_web_2/testServlet1");
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
            jsonRec = new JSONObject(sb.toString());//JSONObject.fromObject(sb.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
        //处理json
        String content = jsonRec.getString("content");
        String name = jsonRec.getString("name");


        //BASE64Decoder decoder = new BASE64Decoder();
        try {
            // Base64解码
            //byte[] bytes = decoder.decodeBuffer(content);
            byte[] bytes = Base64.decode(content,Base64.URL_SAFE);
            for (int i = 0; i < bytes.length; ++i) {
                if (bytes[i] < 0) {// 调整异常数据
                    bytes[i] += 256;
                }
            }
            // 生成图片
            OutputStream outs = new FileOutputStream(Environment.getExternalStorageDirectory()+name);
            outs.write(bytes);
            outs.flush();
            outs.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
