package com.sadhana.sdk;

import android.graphics.Bitmap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Util {

    public static class DownloadException extends Exception {
        private int error = 0;

        public DownloadException() {
            super();
        }

        public int getError() {
            return error;
        }

        public void setError(int error) {
            this.error = error;
        }
    }

    public static String md5Digest(String val) {
        String md5_str = "";
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(val.getBytes());
            byte[] m = md5.digest();//加密
            md5_str = toHex(m);
        } catch (NoSuchAlgorithmException e) {

        }
        return md5_str;
    }

    public static byte[] downloadToBuffer(String sUrl) throws DownloadException {
        URL url = null;//请求的URL地址
        HttpURLConnection conn = null;
        byte[] responseBody = null;
        int error = 0;

        try {
            url = new URL(sUrl);
            conn = (HttpURLConnection) url.openConnection();
            //HttpURLConnection默认就是用GET发送请求，所以下面的setRequestMethod可以省略
            conn.setRequestMethod("GET");
            //HttpURLConnection默认也支持从服务端读取结果流，所以下面的setDoInput也可以省略
            conn.setDoInput(true);
            //禁用网络缓存
            conn.setUseCaches(false);
            //在对各种参数配置完成后，通过调用connect方法建立TCP连接，但是并未真正获取数据
            //conn.connect()方法不必显式调用，当调用conn.getInputStream()方法时内部也会自动调用connect方法
            conn.connect();
            //调用getInputStream方法后，服务端才会收到请求，并阻塞式地接收服务端返回的数据
            InputStream is = conn.getInputStream();
            //将InputStream转换成byte数组,getBytesByInputStream会关闭输入流
            responseBody = getBytesFromInputStream(is);
            error = conn.getResponseCode();
        } catch (MalformedURLException e) {
            error = 1;
        } catch (IOException e) {
            error = 2;
        } finally {
            //最后将conn断开连接
            if (conn != null) {
                conn.disconnect();
            }
        }

        if (error != 200) {
            DownloadException e = new DownloadException();
            e.setError(error);
            throw e;
        }

        return responseBody;
    }

    public static byte[] downloadToBuffer(String sUrl, byte[] postData) throws DownloadException {
        URL url = null;//请求的URL地址
        HttpURLConnection conn = null;
        byte[] responseBody = null;
        int error = 0;

        try {
            url = new URL(sUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            //HttpURLConnection需要post数据，所以setDoOutput设置为true
            conn.setDoOutput(true);
            //HttpURLConnection默认也支持从服务端读取结果流，所以下面的setDoInput也可以省略
            conn.setDoInput(true);
            //禁用网络缓存
            conn.setUseCaches(false);
            //设置请求属性
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
            conn.setRequestProperty("Charset", "UTF-8");
            //在对各种参数配置完成后，通过调用connect方法建立TCP连接，但是并未真正获取数据
            //conn.connect()方法不必显式调用，当调用conn.getInputStream()方法时内部也会自动调用connect方法
            conn.connect();
            //建立输出流，post数据
            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
            dos.write(postData);
            dos.flush();
            dos.close();
            //调用getInputStream方法后，服务端才会收到请求，并阻塞式地接收服务端返回的数据
            InputStream is = conn.getInputStream();
            //将InputStream转换成byte数组,getBytesByInputStream会关闭输入流
            responseBody = getBytesFromInputStream(is);
            error = conn.getResponseCode();
        } catch (MalformedURLException e) {
            error = 1;
        } catch (IOException e) {
            error = 2;
        } finally {
            //最后将conn断开连接
            if (conn != null) {
                conn.disconnect();
            }
        }

        if (error != 200) {
            DownloadException e = new DownloadException();
            e.setError(error);
            throw e;
        }

        return responseBody;
    }

    //从InputStream中读取数据，转换成byte数组，最后关闭InputStream
    private static byte[] getBytesFromInputStream(InputStream is) {
        byte[] bytes = null;
        BufferedInputStream bis = new BufferedInputStream(is);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(baos);
        byte[] buffer = new byte[1024 * 8];
        int length = 0;
        try {
            while ((length = bis.read(buffer)) > 0) {
                bos.write(buffer, 0, length);
            }
            bos.flush();
            bytes = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                bis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return bytes;
    }

    public static String toHex(byte[] buf) {
        if (buf == null)
            return "";
        StringBuffer result = new StringBuffer(2*buf.length);
        for (int i = 0; i < buf.length; i++) {
            appendHex(result, buf[i]);
        }
        return result.toString();
    }
    private final static String HEX = "0123456789ABCDEF";
    private static void appendHex(StringBuffer sb, byte b) {
        sb.append(HEX.charAt((b>>4)&0x0f)).append(HEX.charAt(b&0x0f));
    }

    public static byte[] getPixelsBGR(Bitmap image) {
        // calculate how many bytes our image consists of
        int bytes = image.getByteCount();

        // Create a new buffer
        ByteBuffer buffer = ByteBuffer.allocate(bytes);

        // Move the byte data to the buffer
        image.copyPixelsToBuffer(buffer);

        // Get the underlying array containing the data.
        byte[] buf = buffer.array();

        // Allocate for BGR
        int pixelCount = buf.length / 4;
        byte[] bgr = new byte[pixelCount * 3];

        // Copy pixels into place
        for (int i = 0; i < pixelCount; i++) {
            bgr[i * 3] = buf[i * 4 + 2];        //B
            bgr[i * 3 + 1] = buf[i * 4 + 1];    //G
            bgr[i * 3 + 2] = buf[i * 4];        //R
        }

        return bgr;
    }
}
