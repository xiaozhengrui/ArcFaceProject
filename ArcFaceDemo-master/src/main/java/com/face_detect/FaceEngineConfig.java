package com.face_detect;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.util.Log;

import com.arc_sdk.FaceDB;
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
import com.arcsoft_face_ui.Application;
import com.ftp_service.FsService;
import com.gpio_ctrl.GpioCtrlService;
import com.guo.android_extend.image.ImageConverter;
import com.guo.android_extend.java.ExtInputStream;
import com.sadhana.sdk.Candidate;
import com.sadhana.sdk.FaceEngine;
import com.sadhana.sdk.LicenseUtil;
import com.sadhana.sdk.Person;
import com.sadhana.sdk.PersonFaceDB;
import com.sadhana.sdk.Util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FaceEngineConfig extends android.app.Application{
    private static final String TAG = "FaceEngineConfig";
    

    //arc engine
    AFR_FSDKEngine mFREngine;
    AFR_FSDKVersion mFRVersion;
    AFR_FSDKFace mResult;
    AFT_FSDKVersion version;
    AFT_FSDKEngine engine;
    ASAE_FSDKVersion mAgeVersion;
    ASAE_FSDKEngine mAgeEngine;
    ASGE_FSDKVersion mGenderVersion;
    ASGE_FSDKEngine mGenderEngine;

    List<AFT_FSDKFace> result = new ArrayList<>();
    List<ASAE_FSDKAge> ages = new ArrayList<>();
    List<ASGE_FSDKGender> genders = new ArrayList<>();
    List<AFT_FSDKFace> mAFT_FSDKFaceList = new ArrayList<>();
    AFT_FSDKFace mAFT_FSDKFace = null;
    public List<FaceDB.FaceRegist> mRegister;
    public List<PersonFaceDB.FaceRegist> mPersonRegister;
    boolean mUpgrade;

    int mFaceIndex=0;
    private int mWidth, mHeight;
    long time;
    byte[] mImageNV21 = null;
    byte[] mImageFtp = null;


    //shangbang engine
    private static FaceEngine mFaceEngine = null;// = new FaceEngine();
    private LicenseUtil.ActivateResult mActivateResult;

    public static final int CONFIG_ARC = 0;
    public static final int CONFIG_SHANBANG = 1;
    private  int configMsg = 0;
    String mDBPath;
    Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public FaceEngineConfig(int configMsg,Context mContext){
        this.configMsg = configMsg;
        this.mContext = mContext;
        this.mDBPath = mContext.getExternalCacheDir().getPath();
    }

    public static FaceEngine getFaceEngineInstance(){
        return mFaceEngine;
    }

    public void onActivate() {
        /**
         *  第一步：激活SDK，激活只需在App首次使用时进行。
         */
        if(configMsg == CONFIG_SHANBANG){
            mFaceEngine = new FaceEngine();
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    LicenseUtil licenseUtil = new LicenseUtil();
                    mActivateResult = licenseUtil.activate("shanbang_test", "123456");
                }
            });
            t.start();
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (mActivateResult.getError() == 0) {
                Log.d(TAG,"shangBang 激活成功!");
            } else {
                Log.d(TAG,String.format("激活失败，原因: %s", mActivateResult.getMessage()));
                //Toast.makeText(this, String.format("激活失败，原因: %s", mActivateResult.getMessage()), Toast.LENGTH_LONG).show();
            }
        }else if(configMsg == CONFIG_ARC){
            mFREngine = new AFR_FSDKEngine();
            mResult = new AFR_FSDKFace();
            mFRVersion = new AFR_FSDKVersion();
            AFR_FSDKError error = mFREngine.AFR_FSDK_InitialEngine(FaceDB.appid, FaceDB.fr_key);
            if (error.getCode() != AFR_FSDKError.MOK) {
                Log.e(TAG, "AFR_FSDK_InitialEngine fail! error code :" + error.getCode());
            } else {
                mFREngine.AFR_FSDK_GetVersion(mFRVersion);
                Log.d(TAG,"arcEngine 激活成功!");
                Log.d(TAG, "AFR_FSDK_GetVersion=" + mFRVersion.toString());
            }
        }
    }

    public void onInitialize() {
        /**
         *  第二步：初始化人脸识别引擎。此步骤需要在每次App使用时进行。
         */
        //mDBPath = this.getApplicationContext().getExternalCacheDir().getPath();
        Log.d(TAG,"mDBPath == :"+mDBPath);
        if(configMsg == CONFIG_SHANBANG){
            mPersonRegister = ((Application)mContext.getApplicationContext()).mPersonFaceDB.mRegister;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ((Application)mContext.getApplicationContext()).mPersonFaceDB.loadFaces();
                }
            }).start();
            int error = mFaceEngine.initialize(mContext.getAssets(), "sadhana_base.model");
            if (error == 0) {
                Log.d(TAG,"初始化人脸引擎成功!");
                //Toast.makeText(this, "初始化人脸引擎成功!", Toast.LENGTH_LONG).show();
            } else {
                Log.d(TAG,String.format("初始化人脸引擎失败，错误码: %d", error));
                //Toast.makeText(this, String.format("初始化人脸引擎失败，错误码: %d", error), Toast.LENGTH_LONG).show();
            }
        }else if(configMsg == CONFIG_ARC){
            mRegister = ((Application)mContext.getApplicationContext()).mFaceDB.mRegister;
            new Thread(new Runnable() {
            @Override
            public void run() {
                ((Application)mContext.getApplicationContext()).mFaceDB.loadFaces();
                //mark http register,xiao 2018.9.23
                //HttpFaceRegister register = new HttpFaceRegister(FsService.this);
                //register.getImageFromHttp();
            }
            }).start();

            mUpgrade = false;
            version = new AFT_FSDKVersion();
            engine = new AFT_FSDKEngine();
            mAgeVersion = new ASAE_FSDKVersion();
            mAgeEngine = new ASAE_FSDKEngine();
            mGenderVersion = new ASGE_FSDKVersion();
            mGenderEngine = new ASGE_FSDKEngine();
            AFT_FSDKError err = engine.AFT_FSDK_InitialFaceEngine(FaceDB.appid, FaceDB.ft_key, AFT_FSDKEngine.AFT_OPF_0_HIGHER_EXT, 16, 25);
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
        }
    }


    public void onReleaseEngine(){
        if(configMsg == CONFIG_ARC){
            AFT_FSDKError err = engine.AFT_FSDK_UninitialFaceEngine();
            Log.d(TAG, "AFT_FSDK_UninitialFaceEngine =" + err.getCode());

            ASAE_FSDKError err1 = mAgeEngine.ASAE_FSDK_UninitAgeEngine();
            Log.d(TAG, "ASAE_FSDK_UninitAgeEngine =" + err1.getCode());

            ASGE_FSDKError err2 = mGenderEngine.ASGE_FSDK_UninitGenderEngine();
            Log.d(TAG, "ASGE_FSDK_UninitGenderEngine =" + err2.getCode());

            AFR_FSDKError error = mFREngine.AFR_FSDK_UninitialEngine();
            Log.d(TAG, "AFR_FSDK_UninitialEngine : " + error.getCode());
        }else if(configMsg == CONFIG_SHANBANG){
            mFaceEngine.release();
        }
    }

    public boolean onIdentify(String imgPath) {
        if(configMsg == CONFIG_SHANBANG) {
            /**
             *  第三步：正常的识别操作，此例子中提供了一个sqlite的数据库，里面存放了预先
             *  注册的人脸特征，然后用一张现场照片来进行识别。
             */

            // 从数据库中加载特征集合
//            List<Person> list = loadFace();
//            Person[] persons = new Person[list.size()];
//            list.toArray(persons);
            byte[] feature =null;
            try {
                // 提取现场照中人脸特征
                //InputStream is = getAssets().open("test.jpg");

                BitmapFactory.Options op = new BitmapFactory.Options();
                op.inJustDecodeBounds = false;
                Bitmap bmp = BitmapFactory.decodeFile(imgPath, op);

                byte[] bgr = Util.getPixelsBGR(bmp);
                int width = bmp.getWidth();
                int height = bmp.getHeight();
                feature = mFaceEngine.extractFeature(bgr, width, height);
                if (feature == null) {
                    Log.e(TAG, "Extract feature failed");
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            for(PersonFaceDB.FaceRegist regist:mPersonRegister){
                Person[] persons = new Person[regist.mFaceList.size()];
                regist.mFaceList.toArray(persons);
                // 进行识别
                Candidate candidate = mFaceEngine.identify(feature, persons, 0.9f);
                if(candidate == null){
                    Log.d(TAG, String.format("未找到人员 ID!!"));
                }else{
                    Log.d(TAG, String.format("找到人员 ID = %s , 相似度: %f", candidate.getId(), candidate.getSimilarity()));
                    if(candidate.getSimilarity() > 0.9f){
                        GpioCtrlService.openTheDoor();
                        return true;
                    }
                }
            }
            return false;
        }else if(configMsg == CONFIG_ARC){
            Log.d(TAG,"人脸检测开始!!!");
            int width,height;
            Bitmap bmp;

            List<FaceDB.FaceRegist> mResgist = ((Application)mContext.getApplicationContext()).mFaceDB.mRegister;

            List<ASAE_FSDKFace> face1 = new ArrayList<>();
            List<ASGE_FSDKFace> face2 = new ArrayList<>();

            if(!imgPath.contains(".jpg")){
                return false;
            }
            Log.d(TAG,"getFtpClientCurrentData path:"+imgPath);
            //long time = System.currentTimeMillis();
            BitmapFactory.Options op = new BitmapFactory.Options();
            op.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imgPath,op);
            Log.d(TAG,"pre_decode w:"+op.outWidth+" h:"+op.outHeight+" mime:"+op.outMimeType);
            op.inSampleSize = calculateInSampleSize(op, 1920, 1080);
            op.inJustDecodeBounds = false;
            bmp = BitmapFactory.decodeFile(imgPath,op);
            bmp = BitmapFactory.decodeFile(imgPath);
            if(bmp == null){
                return false;
            }
            Log.d(TAG,"bmp.getWidth():"+bmp.getWidth()+" bmp.getHeight():"+bmp.getHeight());
            width = bmp.getWidth();
            height = bmp.getHeight();

            byte[] ImageData = new byte[width * height * 3 / 2];
            ImageConverter convert = new ImageConverter();
            convert.initial(width, height, ImageConverter.CP_PAF_NV21);
            if (convert.convert(bmp, ImageData)) {
                Log.d(TAG, "convert ok!");
            }
            bmp = null;
            convert.destroy();//so reason cause the leap,so mark it,2018.10.14

            AFT_FSDKError err = engine.AFT_FSDK_FaceFeatureDetect(ImageData, width, height, AFT_FSDKEngine.CP_PAF_NV21, result);
            Log.d(TAG, "AFT_FSDK_FaceFeatureDetect =" + err.getCode());
            Log.d(TAG, "Face=" + result.size());
            if(result.size() == 0){
                //Message msg = new Message();
                //msg.what = 0x002;
                //handler.sendMessage(msg);
            }else{
                for (AFT_FSDKFace rt : result) {
                    mAFT_FSDKFaceList.add(rt.clone());
                }
                result.clear();
                mImageFtp = ImageData.clone();
                mWidth = width;
                mHeight = height;
            }
            Log.d(TAG,"detect face cost:"+(System.currentTimeMillis()-time)+"ms");
            if(mAFT_FSDKFaceList.size()> mFaceIndex){
                mImageNV21 = mImageFtp.clone();
                mAFT_FSDKFace = mAFT_FSDKFaceList.get(mFaceIndex).clone();
                mFaceIndex++;
                if(mFaceIndex == mAFT_FSDKFaceList.size()){
                    mAFT_FSDKFaceList.clear();
                    mFaceIndex = 0;
                }
            }
            if (mImageNV21 != null) {
                long time1 = System.currentTimeMillis();
                time = System.currentTimeMillis();
                Log.d(TAG,"AFR_FSDK_ExtractFRFeature is ok");
                AFR_FSDKError mError = mFREngine.AFR_FSDK_ExtractFRFeature(mImageNV21, mWidth, mHeight, AFR_FSDKEngine.CP_PAF_NV21, mAFT_FSDKFace.getRect(), mAFT_FSDKFace.getDegree(), mResult);
                Log.d(TAG, "AFR_FSDK_ExtractFRFeature cost :" + (System.currentTimeMillis() - time1) + "ms");
                Log.d(TAG, "Face=" + mResult.getFeatureData()[0] + "," + mResult.getFeatureData()[1] + "," + mResult.getFeatureData()[2] + "," + mError.getCode());
                AFR_FSDKMatching score = new AFR_FSDKMatching();
                float max = 0.0f;
                String name = null;
                time1 = System.currentTimeMillis();
                for (FaceDB.FaceRegist fr : mResgist) {
                    for (AFR_FSDKFace face : fr.mFaceList) {
                        mError = mFREngine.AFR_FSDK_FacePairMatching(mResult, face, score);

                        Log.d(TAG,  "Score:" + score.getScore() + ", AFR_FSDK_FacePairMatching=" + mError.getCode());
                        if (max < score.getScore()) {
                            max = score.getScore();
                            name = fr.mName;
                        }
                    }
                }
                mResgist.clear();
                Log.d(TAG, "AFR_FSDK_FacePairMatching cost :" + (System.currentTimeMillis() - time1) + "ms");
                //age & gender
                face1.clear();
                face2.clear();
                face1.add(new ASAE_FSDKFace(mAFT_FSDKFace.getRect(), mAFT_FSDKFace.getDegree()));
                face2.add(new ASGE_FSDKFace(mAFT_FSDKFace.getRect(), mAFT_FSDKFace.getDegree()));
                ASAE_FSDKError error0 = mAgeEngine.ASAE_FSDK_AgeEstimation_Image(mImageNV21, mWidth, mHeight, AFT_FSDKEngine.CP_PAF_NV21, face1, ages);
                ASGE_FSDKError error2 = mGenderEngine.ASGE_FSDK_GenderEstimation_Image(mImageNV21, mWidth, mHeight, AFT_FSDKEngine.CP_PAF_NV21, face2, genders);
                Log.d(TAG, "ASAE_FSDK_AgeEstimation_Image:" + error0.getCode() + ",ASGE_FSDK_GenderEstimation_Image:" + error2.getCode());
                Log.d(TAG, "age:" + ages.get(0).getAge() + ",gender:" + genders.get(0).getGender());
                final String age = ages.get(0).getAge() == 0 ? "年龄未知" : ages.get(0).getAge() + "岁";
                final String gender = genders.get(0).getGender() == -1 ? "性别未知" : (genders.get(0).getGender() == 0 ? "男" : "女");

                //crop
                byte[] data = mImageNV21;
                Log.d(TAG,"rect 1:"+mWidth+" "+mHeight);
                Log.d(TAG,"rect 2:"+mAFT_FSDKFace.getRect());
                Rect rect = new Rect(0, 0, mWidth, mHeight);
                Log.d(TAG, "max fit Score:" + max);
                if (max > 0.6f) {
                    //fr success.
                    final float max_score = max;
                    Log.d(TAG, "fit Score:" + max + ", NAME:" + name);
                    final String mNameShow = name;
                    mAFT_FSDKFaceList.clear();//clear face list when checked!!
                    mFaceIndex =  0;
                    Log.d(TAG, "check face ok cost :" + (System.currentTimeMillis() - time) + "ms");
                    GpioCtrlService.openTheDoor();
                    return true;
                }
                mImageNV21 = null;
                mImageFtp = null;
            }
        }
        return false;
    }


    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // 源图片的高度和宽度
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            // 计算出实际宽高和目标宽高的比率
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
            // 一定都会大于等于目标的宽和高。
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }


}
