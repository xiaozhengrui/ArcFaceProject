package com.arcsoft.sdk_demo_1;

import com.ftp.ArcFtpList;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.arcsoft.ageestimation.ASAE_FSDKAge;
import com.arcsoft.ageestimation.ASAE_FSDKEngine;
import com.arcsoft.ageestimation.ASAE_FSDKError;
import com.arcsoft.ageestimation.ASAE_FSDKFace;
import com.arcsoft.ageestimation.ASAE_FSDKVersion;
import com.arcsoft.facerecognition.AFR_FSDKEngine;
import com.arcsoft.facerecognition.AFR_FSDKError;
import com.arcsoft.facerecognition.AFR_FSDKFace;
import com.arcsoft.facerecognition.AFR_FSDKMatching;
import com.arcsoft.facerecognition.AFR_FSDKVersion;
import com.arcsoft.facetracking.AFT_FSDKEngine;
import com.arcsoft.facetracking.AFT_FSDKError;
import com.arcsoft.facetracking.AFT_FSDKFace;
import com.arcsoft.facetracking.AFT_FSDKVersion;
import com.arcsoft.genderestimation.ASGE_FSDKEngine;
import com.arcsoft.genderestimation.ASGE_FSDKError;
import com.arcsoft.genderestimation.ASGE_FSDKFace;
import com.arcsoft.genderestimation.ASGE_FSDKGender;
import com.arcsoft.genderestimation.ASGE_FSDKVersion;
import com.ftp.FsService;
import com.ftp.NsdService;
import com.guo.android_extend.image.ImageConverter;
import com.guo.android_extend.java.AbsLoop;
import com.guo.android_extend.java.ExtByteArrayOutputStream;
import com.guo.android_extend.tools.CameraHelper;
import com.guo.android_extend.widget.CameraFrameData;
import com.guo.android_extend.widget.CameraGLSurfaceView;
import com.guo.android_extend.widget.CameraSurfaceView;
import com.guo.android_extend.widget.CameraSurfaceView.OnCameraListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPFile;

/**
 * Created by gqj3375 on 2017/4/28.
 */

public class DetecterActivity extends Activity implements OnCameraListener, View.OnTouchListener, Camera.AutoFocusCallback {
	private final static String TAG = "DetecterActivity";//this.getClass().getSimpleName();
	private int mWidth, mHeight, mFormat;
	private CameraSurfaceView mSurfaceView;
	private CameraGLSurfaceView mGLSurfaceView;
	private Camera mCamera;
	FRAbsLoop.DetecterReceiver receiver;
	AFT_FSDKVersion version = new AFT_FSDKVersion();
	AFT_FSDKEngine engine = new AFT_FSDKEngine();
	ASAE_FSDKVersion mAgeVersion = new ASAE_FSDKVersion();
	ASAE_FSDKEngine mAgeEngine = new ASAE_FSDKEngine();
	ASGE_FSDKVersion mGenderVersion = new ASGE_FSDKVersion();
	ASGE_FSDKEngine mGenderEngine = new ASGE_FSDKEngine();
	List<AFT_FSDKFace> result = new ArrayList<>();
	List<ASAE_FSDKAge> ages = new ArrayList<>();
	List<ASGE_FSDKGender> genders = new ArrayList<>();
	List<byte[]> mImageNV21List = new ArrayList<>();
	List<AFT_FSDKFace> mAFT_FSDKFaceList = new ArrayList<>();
	List<Bitmap> mFaceMixBmp = new ArrayList<>();
	List<Integer> mFaceMixBmpWidth= new ArrayList<>();
	List<Integer> mFaceMixBmpHeight= new ArrayList<>();

	float maxSameFace=0.0f;
	int mCameraID;
	int mCameraRotate;
	boolean mCameraMirror;
	byte[] mImageNV21 = null;
	byte[] mImageFtp = null;
	FRAbsLoop mFRAbsLoop = null;
	AFT_FSDKFace mAFT_FSDKFace = null;
	Handler mHandler;


	Runnable hide = new Runnable() {
		@Override
		public void run() {
			mTextView.setAlpha(0.5f);
			mImageView.setImageAlpha(128);
			mImageView2.setImageAlpha(128);
		}
	};

	public class FRAbsLoop extends AbsLoop {

		AFR_FSDKVersion version = new AFR_FSDKVersion();
		AFR_FSDKEngine engine = new AFR_FSDKEngine();
		AFR_FSDKFace result = new AFR_FSDKFace();
		List<FaceDB.FaceRegist> mResgist = ((Application)DetecterActivity.this.getApplicationContext()).mFaceDB.mRegister;
		//String registPath = ((Application)DetecterActivity.this.getApplicationContext()).mFaceDB.mDBPath;

		List<ASAE_FSDKFace> face1 = new ArrayList<>();
		List<ASGE_FSDKFace> face2 = new ArrayList<>();

		@Override
		public void setup() {
			AFR_FSDKError error = engine.AFR_FSDK_InitialEngine(FaceDB.appid, FaceDB.fr_key);
			Log.d(TAG, "AFR_FSDK_InitialEngine = " + error.getCode());

			error = engine.AFR_FSDK_GetVersion(version);
			Log.d(TAG, "FR=" + version.toString() + "," + error.getCode()); //(210, 178 - 478, 446), degree = 1　780, 2208 - 1942, 3370
			IntentFilter filter = new IntentFilter("com.ftp.FTPSERVER_DETECT_STARTED");
			receiver = new DetecterReceiver();
			registerReceiver(receiver,filter);
		}
		public  class DetecterReceiver extends BroadcastReceiver {
			@Override
			public void onReceive(Context context, Intent intent) {
				Log.d(TAG, "onReceive broadcast: " + intent.getAction());

				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
					Log.w(TAG, "onReceive: Running pre-JB, version to old for "
							+ "NSD functionality, bailing out");
					return;
				}
				if (intent.getAction().equals(FsService.ACTION_DETECT_STARTED)) {
					Object object = getFtpClientCurrentData();
				}
			}

		}
		//Object object = onDetectImageFromFTP(15);
		//Object object = getFtpClientCurrentData();
		//Object object = onDetectImageFromTestImg();
		int mFaceIndex=0;
		boolean test=true;
		@Override
		public void loop() {
				if(test == true){
					Object object = onDetectImageFromTestImg();
					test = false;
				}
				if(mAFT_FSDKFaceList.size()> mFaceIndex){
					mImageNV21 = mImageFtp.clone();
					mAFT_FSDKFace = mAFT_FSDKFaceList.get(mFaceIndex).clone();
					mFaceIndex++;
					if(mFaceIndex == mAFT_FSDKFaceList.size()){
						mAFT_FSDKFaceList.clear();
						mFaceIndex = 0;
					}
				}
				/*if(mFaceMixBmp.size()>0 && mImageNV21List.size()>0){
					Log.d(TAG,"mImageNV21List size22:"+mImageNV21List.size());
					if(mFaceIndex==mFaceMixBmp.size()){
						mFaceMixBmp.clear();
						mImageNV21List.clear();
						mAFT_FSDKFaceList.clear();
						mFaceMixBmpWidth.clear();
						mFaceMixBmpHeight.clear();
						mFaceIndex = 0;
					}else {
						mImageNV21 = mImageNV21List.get(mFaceIndex).clone();
						mAFT_FSDKFace = mAFT_FSDKFaceList.get(mFaceIndex).clone();
						mWidth = mFaceMixBmpWidth.get(mFaceIndex);
						mHeight = mFaceMixBmpHeight.get(mFaceIndex);
					}
				}*/
				if (mImageNV21 != null) {
					long time = System.currentTimeMillis();
					Log.d(TAG,"AFR_FSDK_ExtractFRFeature is ok");
					Log.d(TAG,"mWidth:"+mWidth+" mHeight:"+mHeight+" mAFT_FSDKFace.getRect()："+mAFT_FSDKFace.getRect()+" mAFT_FSDKFace.getDegree()："+mAFT_FSDKFace.getDegree());
					AFR_FSDKError error = engine.AFR_FSDK_ExtractFRFeature(mImageNV21, mWidth, mHeight, AFR_FSDKEngine.CP_PAF_NV21, mAFT_FSDKFace.getRect(), mAFT_FSDKFace.getDegree(), result);
					Log.d(TAG, "AFR_FSDK_ExtractFRFeature cost :" + (System.currentTimeMillis() - time) + "ms");

					Log.d(TAG, "Face=" + result.getFeatureData()[0] + "," + result.getFeatureData()[1] + "," + result.getFeatureData()[2] + "," + error.getCode());
					AFR_FSDKMatching score = new AFR_FSDKMatching();
					float max = 0.0f;
					String name = null;
					for (FaceDB.FaceRegist fr : mResgist) {
						for (AFR_FSDKFace face : fr.mFaceList) {
							error = engine.AFR_FSDK_FacePairMatching(result, face, score);

							Log.d(TAG,  "Score:" + score.getScore() + ", AFR_FSDK_FacePairMatching=" + error.getCode());
							if (max < score.getScore()) {
								max = score.getScore();
								name = fr.mName;
							}
						}
					}

					//age & gender
					face1.clear();
					face2.clear();
					face1.add(new ASAE_FSDKFace(mAFT_FSDKFace.getRect(), mAFT_FSDKFace.getDegree()));
					face2.add(new ASGE_FSDKFace(mAFT_FSDKFace.getRect(), mAFT_FSDKFace.getDegree()));
					ASAE_FSDKError error1 = mAgeEngine.ASAE_FSDK_AgeEstimation_Image(mImageNV21, mWidth, mHeight, AFT_FSDKEngine.CP_PAF_NV21, face1, ages);
					ASGE_FSDKError error2 = mGenderEngine.ASGE_FSDK_GenderEstimation_Image(mImageNV21, mWidth, mHeight, AFT_FSDKEngine.CP_PAF_NV21, face2, genders);
					Log.d(TAG, "ASAE_FSDK_AgeEstimation_Image:" + error1.getCode() + ",ASGE_FSDK_GenderEstimation_Image:" + error2.getCode());
					Log.d(TAG, "age:" + ages.get(0).getAge() + ",gender:" + genders.get(0).getGender());
					final String age = ages.get(0).getAge() == 0 ? "年龄未知" : ages.get(0).getAge() + "岁";
					final String gender = genders.get(0).getGender() == -1 ? "性别未知" : (genders.get(0).getGender() == 0 ? "男" : "女");

					//crop
					byte[] data = mImageNV21;
					YuvImage yuv = new YuvImage(data, ImageFormat.NV21, mWidth, mHeight, null);
					ExtByteArrayOutputStream ops = new ExtByteArrayOutputStream();
					yuv.compressToJpeg(mAFT_FSDKFace.getRect(), 80, ops);
					final Bitmap bmp = BitmapFactory.decodeByteArray(ops.getByteArray(), 0, ops.getByteArray().length);
					onSaveBitmap(bmp,"MixBmp"+mFaceIndex);

					try {
						ops.close();
					} catch (IOException e) {
						e.printStackTrace();
					}

					/*final Bitmap bmp = mFaceMixBmp.get(mFaceIndex);
					onSaveBitmap(bmp,"MixBmp"+mFaceIndex);
					mFaceIndex++;*/


						if (max > 0.6f) {
							//fr success.
							final float max_score = max;
							Log.d(TAG, "fit Score:" + max + ", NAME:" + name);
							final String mNameShow = name;
							mAFT_FSDKFaceList.clear();//clear face list when checked!!
							mFaceIndex =  0;
							mHandler.removeCallbacks(hide);
							mHandler.post(new Runnable() {
								@Override
								public void run() {

									mTextView.setAlpha(1.0f);
									mTextView.setText(mNameShow);
									mTextView.setTextColor(Color.RED);
									mTextView1.setVisibility(View.VISIBLE);
									mTextView1.setText("置信度：" + (float) ((int) (max_score * 1000)) / 1000.0);
									mTextView1.setTextColor(Color.RED);
									mImageView.setRotation(mCameraRotate);
									if (mCameraMirror) {
										mImageView.setScaleY(-1);
									}
									mImageView.setImageAlpha(255);
									mImageView.setImageBitmap(bmp);

							/*mImageView2.setRotation(0);
							mImageView2.setImageAlpha(255);
							mImageView2.setImageBitmap(bmp);*/

								}
							});
						} else {
							final String mNameShow = "未识别";
							DetecterActivity.this.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									mTextView.setAlpha(1.0f);
									mTextView1.setVisibility(View.VISIBLE);
									mTextView1.setText(gender + "," + age);
									mTextView1.setTextColor(Color.RED);
									mTextView.setText(mNameShow);
									mTextView.setTextColor(Color.RED);
									mImageView.setImageAlpha(255);
									mImageView.setRotation(mCameraRotate);
									if (mCameraMirror) {
										mImageView.setScaleY(-1);
									}
									mImageView.setImageBitmap(bmp);
								}
							});
						}
						//maxSamebmp = null;
					//}
					mImageNV21 = null;
				}
			//}
		}

		@Override
		public void over() {
			AFR_FSDKError error = engine.AFR_FSDK_UninitialEngine();
			Log.d(TAG, "AFR_FSDK_UninitialEngine : " + error.getCode());
		}
	}

	void onSaveBitmap(Bitmap bitmap,String name){
		try {
			File file = new File(Environment.getExternalStorageDirectory() + "/"+name + ".jpg");
			FileOutputStream out = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private TextView mTextView;
	private TextView mTextView1;
	private ImageView mImageView;
	private ImageView mImageView2;
	private static FTPClient client;


	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		mCameraID = getIntent().getIntExtra("Camera", 0) == 0 ? Camera.CameraInfo.CAMERA_FACING_BACK : Camera.CameraInfo.CAMERA_FACING_FRONT;
		mCameraRotate = getIntent().getIntExtra("Camera", 0) == 0 ? 90 : 270;
		mCameraMirror = getIntent().getIntExtra("Camera", 0) == 0 ? false : true;
		mWidth = 1280;
		mHeight = 960;
		mFormat = ImageFormat.NV21;
		mHandler = new Handler();

		setContentView(R.layout.activity_camera);
		/**
		mGLSurfaceView = (CameraGLSurfaceView) findViewById(R.id.glsurfaceView);
		mGLSurfaceView.setOnTouchListener(this);
		mSurfaceView = (CameraSurfaceView) findViewById(R.id.surfaceView);
		mSurfaceView.setOnCameraListener(this);
		mSurfaceView.setupGLSurafceView(mGLSurfaceView, true, mCameraMirror, mCameraRotate);
		mSurfaceView.debug_print_fps(true, false);
		 **/

		//snap
		mTextView = (TextView) findViewById(R.id.textView);
		mTextView.setText("");
		mTextView1 = (TextView) findViewById(R.id.textView1);
		mTextView1.setText("");

		mImageView = (ImageView) findViewById(R.id.imageView);
		mImageView2 = (ImageView) findViewById(R.id.imageView2);

		//start detect when receive broadcast,so mark it
		AFT_FSDKError err = engine.AFT_FSDK_InitialFaceEngine(FaceDB.appid, FaceDB.ft_key, AFT_FSDKEngine.AFT_OPF_0_HIGHER_EXT, 16, 5);
		Log.d(TAG, "AFT_FSDK_InitialFaceEngine =" + err.getCode());
		err = engine.AFT_FSDK_GetVersion(version);
		Log.d(TAG, "AFT_FSDK_GetVersion:" + version.toString() + "," + err.getCode());

		ASAE_FSDKError error = mAgeEngine.ASAE_FSDK_InitAgeEngine(FaceDB.appid, FaceDB.age_key);
		Log.d(TAG, "ASAE_FSDK_InitAgeEngine =" + error.getCode());
		error = mAgeEngine.ASAE_FSDK_GetVersion(mAgeVersion);
		Log.d(TAG, "ASAE_FSDK_GetVersion:" + mAgeVersion.toString() + "," + error.getCode());

		ASGE_FSDKError error1 = mGenderEngine.ASGE_FSDK_InitgGenderEngine(FaceDB.appid, FaceDB.gender_key);
		Log.d(TAG, "ASGE_FSDK_InitgGenderEngine =" + error1.getCode());
		error1 = mGenderEngine.ASGE_FSDK_GetVersion(mGenderVersion);
		Log.d(TAG, "ASGE_FSDK_GetVersion:" + mGenderVersion.toString() + "," + error1.getCode());
		mFRAbsLoop = new FRAbsLoop();
		mFRAbsLoop.start();
	}







	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Log.d(TAG,"DetecterActivity onDestroy");
		super.onDestroy();
		mFRAbsLoop.shutdown();
		AFT_FSDKError err = engine.AFT_FSDK_UninitialFaceEngine();
		Log.d(TAG, "AFT_FSDK_UninitialFaceEngine =" + err.getCode());

		ASAE_FSDKError err1 = mAgeEngine.ASAE_FSDK_UninitAgeEngine();
		Log.d(TAG, "ASAE_FSDK_UninitAgeEngine =" + err1.getCode());

		ASGE_FSDKError err2 = mGenderEngine.ASGE_FSDK_UninitGenderEngine();
		Log.d(TAG, "ASGE_FSDK_UninitGenderEngine =" + err2.getCode());
		unregisterReceiver(receiver);
	}

	@Override
	public Camera setupCamera() {
		// TODO Auto-generated method stub
		mCamera = Camera.open(mCameraID);
		try {
			Camera.Parameters parameters = mCamera.getParameters();
			parameters.setPreviewSize(mWidth, mHeight);
			parameters.setPreviewFormat(mFormat);

			for( Camera.Size size : parameters.getSupportedPreviewSizes()) {
				Log.d(TAG, "SIZE:" + size.width + "x" + size.height);
			}
			for( Integer format : parameters.getSupportedPreviewFormats()) {
				Log.d(TAG, "FORMAT:" + format);
			}

			List<int[]> fps = parameters.getSupportedPreviewFpsRange();
			for(int[] count : fps) {
				Log.d(TAG, "T:");
				for (int data : count) {
					Log.d(TAG, "V=" + data);
				}
			}
			//parameters.setPreviewFpsRange(15000, 30000);
			//parameters.setExposureCompensation(parameters.getMaxExposureCompensation());
			//parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
			//parameters.setAntibanding(Camera.Parameters.ANTIBANDING_AUTO);
			//parmeters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
			//parameters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
			//parameters.setColorEffect(Camera.Parameters.EFFECT_NONE);
			mCamera.setParameters(parameters);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (mCamera != null) {
			mWidth = mCamera.getParameters().getPreviewSize().width;
			mHeight = mCamera.getParameters().getPreviewSize().height;
		}
		return mCamera;
	}

	@Override
	public void setupChanged(int format, int width, int height) {

	}

	@Override
	public boolean startPreviewLater() {
		// TODO Auto-generated method stub
		return false;
	}



	public static byte[] getYUV420sp(int inputWidth, int inputHeight,
									 Bitmap scaled) {

		int[] argb = new int[inputWidth * inputHeight];

		scaled.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight);

		byte[] yuv = new byte[inputWidth * inputHeight * 3 / 2];
		encodeYUV420SP(yuv, argb, inputWidth, inputHeight);

		scaled.recycle();

		return yuv;
	}


	private static void encodeYUV420SP(byte[] yuv420sp, int[] argb, int width,
									   int height) {
		// 帧图片的像素大小
		final int frameSize = width * height;
		// ---YUV数据---
		int Y, U, V;
		// Y的index从0开始
		int yIndex = 0;
		// UV的index从frameSize开始
		int uvIndex = frameSize;

		// ---颜色数据---
		int a, R, G, B;
		//
		int argbIndex = 0;
		//

		// ---循环所有像素点，RGB转YUV---
		for (int j = 0; j < height; j++) {
			for (int i = 0; i < width; i++) {

				// a is not used obviously
				a = (argb[argbIndex] & 0xff000000) >> 24;
				R = (argb[argbIndex] & 0xff0000) >> 16;
				G = (argb[argbIndex] & 0xff00) >> 8;
				B = (argb[argbIndex] & 0xff);
				//
				argbIndex++;

				// well known RGB to YUV algorithm
				Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
				U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
				V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;

				//
				Y = Math.max(0, Math.min(Y, 255));
				U = Math.max(0, Math.min(U, 255));
				V = Math.max(0, Math.min(V, 255));

				// NV21 has a plane of Y and interleaved planes of VU each
				// sampled by a factor of 2
				// meaning for every 4 Y pixels there are 1 V and 1 U. Note the
				// sampling is every other
				// pixel AND every other scanline.
				// ---Y---
				yuv420sp[yIndex++] = (byte) Y;
				// ---UV---
				if ((j % 2 == 0) && (i % 2 == 0)) {
					//
					yuv420sp[uvIndex++] = (byte) V;
					//
					yuv420sp[uvIndex++] = (byte) U;
				}
			}
		}
	}



	public FTPClient getFtpClientListData(final int index) throws Exception {
		//final byte[] data=null;

		new Thread(new Runnable() {
			@Override
			public void run() {
				int width,height,i;
				client = new FTPClient();
				client.setCharset("utf-8");
				client.setType(FTPClient.TYPE_BINARY);
				try {
					client.connect("192.168.1.101", 21);
					client.login("ftpuser", "123456");
					Log.i(TAG, "client login ok!");
					client.changeDirectory("picture");
					//FTPFile[] ftpFiles = client.list();
					String filePath;


					//Log.d(TAG,"file length:");
					//for(i=0;i<5;i++){//加载服务器所有文件到本地
						filePath = Environment.getExternalStorageDirectory()
								+ "/Download/"+client.listNames()[client.listNames().length-1];
						//Log.d(TAG, "client.currentDirectory()"+filePath[i]);
						Log.d(TAG, "down!!"+filePath);
						client.download(client.listNames()[client.listNames().length-1],new File(filePath));
					//}

				} catch (Exception e) {
					//Toast.makeText(DetecterActivity.this,"服务器无法连接",Toast.LENGTH_SHORT).show();
					//finish();
					return;
				}
				Bitmap bmp;
				ByteArrayOutputStream baos;
				try {
					String[] file = client.listNames();
					String filePath;
					//for(i=0;i<5;i++){
						filePath = Environment.getExternalStorageDirectory()
								+ "/Download/"+file[client.listNames().length-1];
						bmp = Application.decodeImage(filePath);
						if (bmp == null || bmp.getWidth() <= 0 || bmp.getHeight() <= 0 ) {
							Log.e(TAG, "error");
							//continue;
						} else {
							Log.i(TAG, "bmp width:" + bmp.getWidth() + " bmp height:" + bmp.getHeight());
						}

						baos = new ByteArrayOutputStream();
						bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
						//mImageFtp = baos.toByteArray();
						byte[] YuvData = getYUV420sp(bmp.getWidth(),bmp.getHeight(),bmp);
						AFT_FSDKError err = engine.AFT_FSDK_FaceFeatureDetect(YuvData, bmp.getWidth(), bmp.getHeight(), AFT_FSDKEngine.CP_PAF_NV21, result);
						Log.d(TAG, "AFT_FSDK_FaceFeatureDetect =" + err.getCode());
						Log.d(TAG, "Face=" + result.size());
						if(result.size() == 0){
							Toast.makeText(DetecterActivity.this,"未检测到人脸",Toast.LENGTH_SHORT).show();
							finish();
						}else{
							mImageFtp = YuvData;
							mWidth = bmp.getWidth();
							mHeight = bmp.getHeight();
							mCameraRotate = 0;
							mCameraMirror = false;
						}
						for (AFT_FSDKFace face : result) {
							Log.d(TAG, "Face:" + face.toString());
						}
						if (mImageNV21 == null) {
							if (!result.isEmpty()) {
								mAFT_FSDKFace = result.get(0).clone();//mAFT_FSDKFaceList.add(result.get(0).clone());
								if(mImageFtp != null) {
									Log.d(TAG,"clone ok");
									mImageNV21 = mImageFtp.clone();//mImageNV21List.add(mImageFtp.clone());
									//break;
								}
							} else {
								mHandler.postDelayed(hide, 3000);
							}
						}
						baos.close();
					//}
				}catch(Exception e){
					e.printStackTrace();
				}

			}
		}).start();
		return client;
	}


	public Object getFtpClientCurrentData(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				int width,height,i;
				Bitmap bmp;
				ByteArrayOutputStream baos;
				try {
					/*String[] file = client.listNames();
					String filePath;
					filePath = Environment.getExternalStorageDirectory()
							+ "/Download/"+file[client.listNames().length-1];*/
					String[] file = Environment.getExternalStorageDirectory().list();
					for(i=0;i<file.length;i++){
						Log.d(TAG,"file:"+file[i]);
					}
					i = Environment.getExternalStorageDirectory().listFiles().length;
					Log.d(TAG,"index: "+i);

					String filePath;
					filePath = Environment.getExternalStorageDirectory()+"/"+file[i-1];
					if(!filePath.contains(".jpg")){
						return;
					}
					Log.d(TAG,"getFtpClientCurrentData path:"+filePath);
					bmp = Application.decodeImage(filePath);
					if (bmp == null || bmp.getWidth() <= 0 || bmp.getHeight() <= 0 ) {
						Log.e(TAG, "error");
						//continue;
					} else {
						Log.i(TAG, "bmp width:" + bmp.getWidth() + " bmp height:" + bmp.getHeight());
					}

					baos = new ByteArrayOutputStream();
					bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
					//mImageFtp = baos.toByteArray();
					width = bmp.getWidth();
					height = bmp.getHeight();
					if(width >1920)//face image is 1920*1080
						return;
					byte[] YuvData = getYUV420sp(width,height,bmp);
					AFT_FSDKError err = engine.AFT_FSDK_FaceFeatureDetect(YuvData, width, height, AFT_FSDKEngine.CP_PAF_NV21, result);
					Log.d(TAG, "AFT_FSDK_FaceFeatureDetect =" + err.getCode());
					Log.d(TAG, "Face=" + result.size());
					if(result.size() == 0){
						//Toast.makeText(DetecterActivity.this,"未检测到人脸",Toast.LENGTH_SHORT).show();
						finish();
					}else{
						mImageFtp = YuvData;
						mWidth = width;
						mHeight = height;
						mCameraRotate = 0;
						mCameraMirror = false;
					}
					for (AFT_FSDKFace face : result) {
						Log.d(TAG, "Face:" + face.toString());
					}
					if (mImageNV21 == null) {
						if (!result.isEmpty()) {
							mAFT_FSDKFace = result.get(0).clone();//mAFT_FSDKFaceList.add(result.get(0).clone());
							if(mImageFtp != null) {
								Log.d(TAG,"clone ok");
								mImageNV21 = mImageFtp.clone();//mImageNV21List.add(mImageFtp.clone());
								//break;
							}
						} else {
							mHandler.postDelayed(hide, 3000);
						}
					}
					baos.close();
					//}
				}catch(Exception e){
					e.printStackTrace();
				}

			}
		}).start();
		return null;
	}


	public Object onDetectImageFromTestImg()
	{
		Log.d(TAG,"onDetectImageFromTestImg");
		{
			int width,height,i;
			Bitmap bmp;

			/*synchronized (this)*/{


			try {
				Resources r = DetecterActivity.this.getResources();
				bmp = BitmapFactory.decodeResource(r,R.mipmap.img_8395);

				if (bmp == null || bmp.getWidth() <= 0 || bmp.getHeight() <= 0 ) {
					Log.e(TAG, "error");
					//continue;
				} else {
					Log.i(TAG, "bmp width:" + bmp.getWidth() + " bmp height:" + bmp.getHeight());
				}
				width = bmp.getWidth();
				height = bmp.getHeight();
				byte[] ImageData = new byte[width * height * 3 / 2];
				ImageConverter convert = new ImageConverter();
				convert.initial(width, height, ImageConverter.CP_PAF_NV21);
				if (convert.convert(bmp, ImageData)) {
					Log.d(TAG, "convert ok!");
				}
				convert.destroy();

				AFT_FSDKError err = engine.AFT_FSDK_FaceFeatureDetect(ImageData, width, height, AFT_FSDKEngine.CP_PAF_NV21, result);
				Log.d(TAG, "AFT_FSDK_FaceFeatureDetect =" + err.getCode());
				Log.d(TAG, "Face=" + result.size());
				if(result.size() == 0){
					//mHandler.postDelayed(hide, 3000);
					//Toast.makeText(DetecterActivity.this,"未检测到人脸",Toast.LENGTH_SHORT).show();
					finish();
				}else{
					int k=0;
					Bitmap mixBmp;
					ImageConverter gconvert;
					byte[] tempData;
					ByteArrayOutputStream baos;
					for (AFT_FSDKFace face : result) {
						Log.d(TAG, "Face:" + face.toString());
					}

					for (AFT_FSDKFace rt : result) {
							mAFT_FSDKFaceList.add(rt.clone());
							/*mixBmp = Bitmap.createBitmap(bmp, rt.getRect().left, rt.getRect().top,
									rt.getRect().width(), rt.getRect().height());
							mFaceMixBmpWidth.add(mixBmp.getWidth());
							mFaceMixBmpHeight.add(mixBmp.getHeight());
							mAFT_FSDKFaceList.add(rt.clone());


							if(mixBmp != null){
								Log.d(TAG,"mixBmp create!!");
								Log.d(TAG,"mixBmp width:"+mFaceMixBmpWidth.get(k)+"　mixBmp height:"
												+mFaceMixBmpHeight.get(k));
								mFaceMixBmp.add(mixBmp);
								tempData = new byte[mFaceMixBmpWidth.get(k) *
										mFaceMixBmpHeight.get(k) * 3 / 2];
								gconvert = new ImageConverter();
								gconvert.initial(mFaceMixBmpWidth.get(k), mFaceMixBmpHeight.get(k), ImageConverter.CP_PAF_NV21);
								if (gconvert.convert(mixBmp, tempData)) {
									Log.d(TAG, "mixBmp convert ok!");
									mImageNV21List.add(tempData.clone());
								}
								gconvert.destroy();
								k++;
							}*/
						}
						mImageFtp = ImageData.clone();
						mWidth = width;
						mHeight = height;
						mCameraRotate = 0;
						mCameraMirror = false;
				}

//				if (mImageNV21 == null) {
//					if (!result.isEmpty()) {
//						mAFT_FSDKFace = result.get(0).clone();//mAFT_FSDKFaceList.add(result.get(0).clone());
//						if(mImageFtp != null) {
//							Log.d(TAG,"clone ok");
//							mImageNV21 = mImageFtp.clone();//mImageNV21List.add(mImageFtp.clone());
//							//break;
//						}
//					} else {
//						mHandler.postDelayed(hide, 3000);
//					}
//				}
				//baos.close();
				//}
			}catch(Exception e){
				e.printStackTrace();
			}
			}
	}
		return null;
	}

	public Object onDetectImageFromFTP(final int index) {
		if(mCameraID == 2){
			return null;
		}
		try {
			client = getFtpClientListData(index);
		}catch(Exception e){
			e.printStackTrace();
		}
		/*Rect[] rects = new Rect[result.size()];
		for (int i = 0; i < result.size(); i++) {
			rects[i] = new Rect(result.get(i).getRect());
		}
		//clear result.
		result.clear();*/
		//return the rects for render.
		return null;
	}

	@Override
	public Object onPreview(byte[] data, int width, int height, int format, long timestamp) {
		AFT_FSDKError err = engine.AFT_FSDK_FaceFeatureDetect(data, width, height, AFT_FSDKEngine.CP_PAF_NV21, result);
		Log.d(TAG, "AFT_FSDK_FaceFeatureDetect =" + err.getCode());
		Log.d(TAG, "width=" + width);
		Log.d(TAG, "height=" + height);
		for (AFT_FSDKFace face : result) {
			Log.d(TAG, "Face:" + face.toString());
		}
		if (mImageNV21 == null) {
			if (!result.isEmpty()) {
				mAFT_FSDKFace = result.get(0).clone();
				mImageNV21 = data.clone();
			} else {
				mHandler.postDelayed(hide, 3000);
			}
		}
		//copy rects
		Rect[] rects = new Rect[result.size()];
		for (int i = 0; i < result.size(); i++) {
			rects[i] = new Rect(result.get(i).getRect());
		}
		//clear result.
		result.clear();
		//return the rects for render.
		return rects;
	}

	@Override
	public void onBeforeRender(CameraFrameData data) {

	}

	@Override
	public void onAfterRender(CameraFrameData data) {
		mGLSurfaceView.getGLES2Render().draw_rect((Rect[])data.getParams(), Color.GREEN, 2);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		CameraHelper.touchFocus(mCamera, event, v, this);
		return false;
	}

	@Override
	public void onAutoFocus(boolean success, Camera camera) {
		if (success) {
			Log.d(TAG, "Camera Focus SUCCESS!");
		}
	}
}
