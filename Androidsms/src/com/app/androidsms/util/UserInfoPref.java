package com.app.androidsms.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * �û���ϢsharedPreference operation
 * @author luo-PC
 *
 */
public class UserInfoPref {
	private final static String TAG = UserInfoPref.class.getSimpleName();
	private static UserInfoPref sInstance;
	private Context 					mContext;
	private SharedPreferences 	mUserPrefs = null;
	
	public static synchronized UserInfoPref get(Context cxt) {
        if (sInstance == null) 
            sInstance = new UserInfoPref(cxt);
        return sInstance;
    }
	
	private UserInfoPref(Context cxt){
		this.mContext = cxt; 
		mUserPrefs = mContext.getSharedPreferences(Constants.PREF_USER_INFO,Context.MODE_PRIVATE);
	}
	
	/**
	 * ��ȡ
	 * @param key
	 * @return
	 */
	public  String getString(String key){
		return mUserPrefs.getString(key, "");
	}
	
	/**
	 * ���
	 * @param key
	 * @param value
	 */
	public void addString(String key, String value)
	{
		mUserPrefs.edit().putString(key, value).commit();
	}

}
