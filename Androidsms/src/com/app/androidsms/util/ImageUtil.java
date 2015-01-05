package com.app.androidsms.util;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

public class ImageUtil {
	private static final String TAG = "ImageUtil";
	private volatile static ImageUtil uniqueInstance = null;
	private static final String PATH_SDCARD = Environment.getExternalStorageDirectory().getAbsolutePath();
	private static final String PATH_CAMERA = PATH_SDCARD + "/DCIM/Camera";
	
	private ImageUtil()
	{
	}
    
    public static ImageUtil getInstance()
    {
        if(uniqueInstance == null){
            synchronized(ImageUtil.class) {
                if(uniqueInstance == null) {
                    uniqueInstance = new ImageUtil();
                }
            }
        }
        
        return uniqueInstance;       
    }
    
  //-----get file path from mediaprovider, downloadsprovider, external storageprovider----
  	/**
  	 * Get a file path from a Uri. This will get the the path for Storage Access
  	 * Framework Documents, as well as the _data field for the MediaStore and
  	 * other file-based ContentProviders.
  	 *
  	 * @param context The context.
  	 * @param uri The Uri to query.
  	 * @author paulburke
  	 */
  	@SuppressLint("NewApi")
	public static String getPath(final Context context, final Uri uri) {

  	    final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

  	    // DocumentProvider
  	    if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
  	        // ExternalStorageProvider
  	        if (isExternalStorageDocument(uri)) {
  	            final String docId = DocumentsContract.getDocumentId(uri);
  	            final String[] split = docId.split(":");
  	            final String type = split[0];

  	            if ("primary".equalsIgnoreCase(type)) {
  	                return Environment.getExternalStorageDirectory() + "/" + split[1];
  	            }

  	            // TODO handle non-primary volumes
  	        }
  	        // DownloadsProvider
  	        else if (isDownloadsDocument(uri)) {

  	            final String id = DocumentsContract.getDocumentId(uri);
  	            final Uri contentUri = ContentUris.withAppendedId(
  	                    Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

  	            return getDataColumn(context, contentUri, null, null);
  	        }
  	        // MediaProvider
  	        else if (isMediaDocument(uri)) {
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

  	            return getDataColumn(context, contentUri, selection, selectionArgs);
  	        }
  	    }
  	    // MediaStore (and general)
  	    else if ("content".equalsIgnoreCase(uri.getScheme())) {
  	        return getDataColumn(context, uri, null, null);
  	    }
  	    // File
  	    else if ("file".equalsIgnoreCase(uri.getScheme())) {
  	        return uri.getPath();
  	    }

  	    return null;
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
  	            final int column_index = cursor.getColumnIndexOrThrow(column);
  	            return cursor.getString(column_index);
  	        }
  	    } finally {
  	        if (cursor != null)
  	            cursor.close();
  	    }
  	    return null;
  	}


  	/**
  	 * @param uri The Uri to check.
  	 * @return Whether the Uri authority is ExternalStorageProvider.
  	 */
  	private static boolean isExternalStorageDocument(Uri uri) {
  	    return "com.android.externalstorage.documents".equals(uri.getAuthority());
  	}

  	/**
  	 * @param uri The Uri to check.
  	 * @return Whether the Uri authority is DownloadsProvider.
  	 */
  	private static boolean isDownloadsDocument(Uri uri) {
  	    return "com.android.providers.downloads.documents".equals(uri.getAuthority());
  	}

  	/**
  	 * @param uri The Uri to check.
  	 * @return Whether the Uri authority is MediaProvider.
  	 */
  	private static boolean isMediaDocument(Uri uri) {
  	    return "com.android.providers.media.documents".equals(uri.getAuthority());
  	}
  	
  	/**
  	 * 获取当前时间做为文件名
  	 * @return String
  	 */
  	public static String getRandomDateString() {
        Random random = new Random(System.currentTimeMillis());
        Date date = new Date(System.currentTimeMillis()); 
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US); 
        String fileName = format.format(date) + String.format("%06d", Math.abs(random.nextInt()) % 100000);      
        return fileName;
    } 
  	
  	/**
  	 * create folder
  	 * @param folderPath
  	 * @return folderPath
  	 */
  	public static String createFolder(String folderPath) {
		File folder = new File(folderPath);
		if (!folder.exists())
			folder.mkdirs();
		return folderPath;
	}
  	
  	/**
  	 * get qr_code save path
  	 * @return path
  	 */
  	public static String getQRcodeSavePath() {
		File file = new File(createFolder(PATH_CAMERA), getRandomDateString()+".jpg");
		return file.getAbsolutePath();
	}
	
  	/**
  	 * save image bitmap 
  	 * @param bitmap
  	 */
  	public static String saveImage(Bitmap bitmap) {
  		return saveImage( bitmap,  getQRcodeSavePath());
	}
  	
  	/**
  	 * save image bitmap to specify file path
  	 * @param bitmap
  	 * @param filePath
  	 */
  	public static String saveImage(Bitmap bitmap, String filePath) {
		try {
            FileOutputStream fos = new FileOutputStream(filePath);    		
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
        	filePath = null;
        	e.printStackTrace();
        }finally{
        }
		
		return filePath;
	}
  	
  	/**
	 * scan photo in album
	 * in this way the user can view saved photo in album
	 * @param mContext
	 * @param filePath
	 */
	public static void scanPhotos(Context mContext, String filePath) 
    {
    	if( filePath==null)return;
    	
		 Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		 File file = new File(filePath);
		 if( file==null ) return;
		 
		 Uri uri = Uri.fromFile( file);
		 intent.setData(uri);
		 mContext.sendBroadcast(intent);
	}

}
