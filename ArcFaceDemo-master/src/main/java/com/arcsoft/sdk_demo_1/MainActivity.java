package com.arcsoft.sdk_demo_1;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.SQLite.SQLiteDemo;
import com.ftp.ArcFtpActivity;
import com.ftp.ArcFtpList;
import com.ftp.FsService;
import com.http.HttpUtils;
import com.http.CameraSetting;
import com.tcp.MainTcpActivity;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends Activity implements OnClickListener {
	private final String TAG = "sdk_main_log";

	private static final int REQUEST_CODE_IMAGE_CAMERA = 1;
	private static final int REQUEST_CODE_IMAGE_OP = 2;
	private static final int REQUEST_CODE_OP = 3;
	private static final int REQUEST_CODE_FTP_IMAGE_OP = 4;

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.main_test);
		View v = this.findViewById(R.id.button1);
		v.setOnClickListener(this);
		v = this.findViewById(R.id.button2);
		v.setOnClickListener(this);
		v = this.findViewById(R.id.button3);
		v.setOnClickListener(this);
		v = this.findViewById(R.id.SQLenter);
		v.setOnClickListener(this);
		Log.d(TAG, "ArcFtp.getInstance()"+FsService.isRunning());
		/*Intent serverService  = new Intent(this,FsService.class);
		if (!FsService.isRunning()) {
			//warnIfNoExternalStorage();
			startService(serverService);
		}*/
	}



	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(TAG,"requestCode:"+requestCode+" resultCode:"+resultCode);
		if ((requestCode == REQUEST_CODE_IMAGE_OP || requestCode == REQUEST_CODE_FTP_IMAGE_OP)&& resultCode == RESULT_OK) {
			Log.d(TAG,"REQUEST_CODE_FTP_IMAGE_OP");
			String file;
			if(requestCode == REQUEST_CODE_IMAGE_OP) {
				Uri mPath = data.getData();
				Log.d(TAG, "data is ok!!");
				file = getPath(mPath);
				Log.d(TAG, "file path:" + file);
			}else{
				file = data.getStringExtra("localpath");
				Log.d(TAG, "localpath:" + file);
			}
			Bitmap bmp = Application.decodeImage(file);
			if (bmp == null || bmp.getWidth() <= 0 || bmp.getHeight() <= 0 ) {
				Log.e(TAG, "error");
			} else {
				Log.i(TAG, "bmp [" + bmp.getWidth() + "," + bmp.getHeight());
			}
			startRegister(bmp, file);
		}  else if (requestCode == REQUEST_CODE_OP) {
			Log.i(TAG, "RESULT =" + resultCode);
			if (data == null) {
				return;
			}
			Bundle bundle = data.getExtras();
			String path = bundle.getString("imagePath");
			Log.i(TAG, "path="+path);
		} else if (requestCode == REQUEST_CODE_IMAGE_CAMERA && resultCode == RESULT_OK) {
			Uri mPath = ((Application)(MainActivity.this.getApplicationContext())).getCaptureImage();
			String file = getPath(mPath);
			Bitmap bmp = Application.decodeImage(file);
			startRegister(bmp, file);
		}
	}

	@Override
	public void onClick(View paramView) {
		// TODO Auto-generated method stub
		switch (paramView.getId()) {
			case R.id.SQLenter:
				Log.d(TAG,"start SQLite");
				/*Intent intent1 = new Intent().setClass(this, MainTcpActivity.class);
				startActivity(intent1);*/
				startActivity(new Intent(this, SQLiteDemo.class));
				break;
			case R.id.button3:
					/*Intent intent = new Intent();
					intent.setClass(this, ArcFtpActivity.class);
					startActivity(intent);*/
					Thread td = new Thread(new Runnable() {
						@Override
						public void run() {
							HttpUtils httpUtils = new HttpUtils();
							Map<String, String> params = new HashMap<String, String>();
							params.put("username", "admin");
							params.put("password", "admin");
							String result = httpUtils.sendPostMessage(params,"utf-8");
							httpUtils.saveDataToFile(MainActivity.this,result,"camera_setting.html");
							System.out.println("result->"+result);
							MainActivity.this.startActivity(new Intent(MainActivity.this,CameraSetting.class));
						}
					});
					td.start();
				break;
			case R.id.button2:
				if( ((Application)getApplicationContext()).mFaceDB.mRegister.isEmpty() ) {
					Toast.makeText(this, "没有注册人脸，请先注册！", Toast.LENGTH_SHORT).show();
				} else {
					new AlertDialog.Builder(this)
							.setTitle("请选择获取方式")
							.setIcon(android.R.drawable.ic_dialog_info)
							.setItems(new String[]{"后置相机", "前置相机","通过服务器比对"}, new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											startDetector(which);
										}
									})
							.show();
				}
				break;
			case R.id.button1:
				final Intent getImageByFtp = new Intent(this,ArcFtpList.class);
				new AlertDialog.Builder(this)
						.setTitle("请选择注册方式")
						.setIcon(android.R.drawable.ic_dialog_info)
						.setItems(new String[]{"打开图片", "拍摄照片","从服务器获取"}, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								switch (which){
									case 2:
										//Intent getImageByFtp = new Intent("com.ftp.ftplist");
										startActivityForResult(getImageByFtp, REQUEST_CODE_FTP_IMAGE_OP);
										break;
									case 1:
										Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
										ContentValues values = new ContentValues(1);
										values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
										Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
										((Application)(MainActivity.this.getApplicationContext())).setCaptureImage(uri);
										intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
										startActivityForResult(intent, REQUEST_CODE_IMAGE_CAMERA);
										break;
									case 0:
										Intent getImageByalbum = new Intent(Intent.ACTION_GET_CONTENT);
										getImageByalbum.addCategory(Intent.CATEGORY_OPENABLE);
										getImageByalbum.setType("image/jpeg");
										startActivityForResult(getImageByalbum, REQUEST_CODE_IMAGE_OP);
										break;
									default:;
								}
							}
						})
						.show();
				break;
			default:;
		}
	}

	/**
	 * @param uri
	 * @return
	 */
	private String getPath(Uri uri) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			Log.d(TAG,"isDocumentUri:"+DocumentsContract.isDocumentUri(this, uri));
			Log.d(TAG,"uri is:"+uri);
			Log.d(TAG,"uri path is:"+uri.getPath());
			if (DocumentsContract.isDocumentUri(this, uri)) {
				// ExternalStorageProvider
				if (isExternalStorageDocument(uri)) {
					Log.d(TAG,"isExternalStorageDocument");
					final String docId = DocumentsContract.getDocumentId(uri);
					final String[] split = docId.split(":");
					final String type = split[0];

					if ("primary".equalsIgnoreCase(type)) {
						return Environment.getExternalStorageDirectory() + "/" + split[1];
					}

					// TODO handle non-primary volumes
				} else if (isDownloadsDocument(uri)) {
					Log.d(TAG,"isDownloadsDocument");
					final String id = DocumentsContract.getDocumentId(uri);
					final Uri contentUri = ContentUris.withAppendedId(
							Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

					return getDataColumn(this, contentUri, null, null);
				} else if (isMediaDocument(uri)) {
					Log.d(TAG,"isMediaDocument");
					final String docId = DocumentsContract.getDocumentId(uri);
					final String[] split = docId.split(":");
					final String type = split[0];

					Uri contentUri = null;
					if ("image".equals(type)) {
						contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
					} else if ("video".equals(type)) {
						contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
					} else if ("audio".equals(type)) {
						contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
					}

					final String selection = "_id=?";
					final String[] selectionArgs = new String[] {
							split[1]
					};

					return getDataColumn(this, contentUri, selection, selectionArgs);
				}
			}
		}
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor actualimagecursor = this.getContentResolver().query(uri, proj, null, null, null);
		int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		actualimagecursor.moveToFirst();
		String img_path = actualimagecursor.getString(actual_image_column_index);
		String end = img_path.substring(img_path.length() - 4);
		if (0 != end.compareToIgnoreCase(".jpg") && 0 != end.compareToIgnoreCase(".png")) {
			return null;
		}
		return img_path;
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}

	/**
	 * Get the value of the data column for this Uri. This is useful for
	 * MediaStore Uris, and other file-based ContentProviders.
	 *
	 * @param context The context.
	 * @param uri The Uri to query.
	 * @param selection (Optional) Filter used in the query.
	 * @param selectionArgs (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 */
	public static String getDataColumn(Context context, Uri uri, String selection,
									   String[] selectionArgs) {

		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = {
				column
		};

		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
					null);
			if (cursor != null && cursor.moveToFirst()) {
				final int index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}

	/**
	 * @param mBitmap
	 */
	private void startRegister(Bitmap mBitmap, String file) {
		Intent it = new Intent(MainActivity.this, RegisterActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("imagePath", file);
		it.putExtras(bundle);
		startActivityForResult(it, REQUEST_CODE_OP);
	}

	private void startDetector(int camera) {
		Intent it = new Intent(MainActivity.this, DetecterActivity.class);
		it.putExtra("Camera", camera);
		startActivityForResult(it, REQUEST_CODE_OP);
	}


}

