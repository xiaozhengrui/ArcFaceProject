package com.sadhana.sdk;

import android.content.res.AssetManager;
import android.util.Log;

public class FaceEngine {
	private static final String TAG = "FaceEngine";
	private long mEngine = 0;

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
    public final static int E_OPEN_MODEL_FAILED = -200;	        // 无法打开模型

	public native int initialize(AssetManager assetManager, String modelName);
	public native void release();
	public native byte[] extractFeature(byte[] bgr, int width, int height);
	public native Candidate identify(byte[] feature, Person[] persons, float threshold);

	static {
		Log.i(TAG, "Will load libSaFace.so");
		System.loadLibrary("SaFace");
		Log.i(TAG, "Loaded libSaFace.so");
	}
}
