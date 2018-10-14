package com.sadhana.sdk;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class LicenseUtil {
    private final static String TAG = "LicenseUtil";

    public final static int E_SUCCESS = 0;		                // 成功
    public final static int E_FAILED = -1;		                // 失败
    public final static int E_INVALID_PARAM = -3;		        // 无效的参数
    public final static int E_NOT_INITIALIZED = -4;		        // 未初始化
    public final static int E_LICENSE_NOT_FOUND = -100;	        // 未找到授权
    public final static int E_CREATE_LICENSE_FAILED = -101;	    // 无法创建授权
    public final static int E_OPEN_LICENSE_FAILED = -102;	    // 无法打开授权
    public final static int E_READ_LICENSE_FAILED = -103;	    // 无法读取授权
    public final static int E_WRITE_LICENSE_FAILED = -104;	    // 无法写入授权
    public final static int E_BAD_LICENSE = -105;	            // 授权损坏
    public final static int E_LICENSE_EXPIRED = -106;	        // 授权已过期
    public final static int E_LICENSE_TIME_ABNORMAL = -107;	    // 授权访问时间异常（有可能用户恶意修改系统时间）
    public final static int E_INVALID_MACHINE = -108;	        // 非法的机器
    public final static int E_INVALID_LICENSE_SERVER_URL = -109;// 非法的授权服务器地址
    public final static int E_NETWORK_ERROR = -110;             // 网络错误
    public final static int E_INVALID_REQUEST = -111;           // 非法的请求
    public final static int E_INTERNAL_SERVER_ERROR = -112;     // 授权服务器内部错误
    public final static int E_INVALID_USERNAME = -113;          // 非法授权用户名
    public final static int E_INVALID_PASSWORD = -114;          // 非法授权密码
    public final static int E_INVALID_DATA_FORMAT = -115;       // 非法的数据格式


    public static class ActivateResult {
        private int error;
        private String message;

        public ActivateResult(int error, String message) {
            this.error = error;
            this.message = message;
        }

        public int getError() {
            return error;
        }

        public void setError(int error) {
            this.error = error;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }


    public LicenseUtil() {

    }

    public ActivateResult activate(String username, String password) {
        int error = E_SUCCESS;
        String message = "";
        String certificatesCypher = "";
        String licenseCypher = "";

        do {
            try {
                byte[] accountCypher = generateAccountCypher(username, password);
                byte[] buf = Util.downloadToBuffer("http://221.122.93.152:8888/license/list",
                        accountCypher);
                String data = new String(buf, "UTF-8");
                JSONObject entity = new JSONObject(data);
                error = entity.getInt("error");
                message = entity.getString("message");
                if (error == 0)
                    certificatesCypher = entity.getString("data");
            } catch (Util.DownloadException e) {
                if (1 == e.getError()) {
                    error = E_INVALID_LICENSE_SERVER_URL;
                    message = "非法的授权服务器地址";
                } else if (2 == e.getError()) {
                    error = E_NETWORK_ERROR;
                    message = "网络错误";
                } else if (400 == e.getError()) {
                    error = E_INVALID_REQUEST;
                    message = "非法的请求";
                }
            } catch (UnsupportedEncodingException e) {
                error = E_INVALID_DATA_FORMAT;
                message = "非法的数据格式";
            } catch (JSONException e) {
                error = E_INVALID_DATA_FORMAT;
                message = "非法的数据格式";
            }

            if (error != 0)
                break;

            byte[] applyForlicenseCypher = generateApplyForLicenseCypher(username,
                    password, certificatesCypher);

            try {
                byte[] buf = Util.downloadToBuffer("http://221.122.93.152:8888/license/generate",
                        applyForlicenseCypher);
                String data = new String(buf, "UTF-8");
                JSONObject entity = new JSONObject(data);
                error = entity.getInt("error");
                message = entity.getString("message");
                if (error == 0)
                    licenseCypher = entity.getString("data");
            } catch (Util.DownloadException e) {
                if (1 == e.getError()) {
                    error = E_INVALID_LICENSE_SERVER_URL;
                    message = "非法的授权服务器地址";
                } else if (2 == e.getError()) {
                    error = E_NETWORK_ERROR;
                    message = "网络错误";
                } else if (400 == e.getError()) {
                    error = E_INVALID_REQUEST;
                    message = "非法的请求";
                }
            } catch (UnsupportedEncodingException e) {
                error = E_INVALID_DATA_FORMAT;
                message = "非法的数据格式";
            } catch (JSONException e) {
                error = E_INVALID_DATA_FORMAT;
                message = "非法的数据格式";
            }

            if (error == E_SUCCESS)
                error = generateLicense(licenseCypher);

            if (error == E_SUCCESS)
                message = "成功";
            else if (error == E_CREATE_LICENSE_FAILED)
                message = "无法创建授权";

        } while (false);

        return new ActivateResult(error, message);
    }

    public native byte[] generateAccountCypher(String username, String password);
    public native byte[] generateApplyForLicenseCypher(String username, String password, String certificates);
    public native int generateLicense(String licenseCypher);

    static {
        Log.i(TAG, "Will load libSaLicense.so");
        System.loadLibrary("SaLicense");
        Log.i(TAG, "Loaded libSaLicense.so");
    }
}
