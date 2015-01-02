package com.app.androidsms.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * white list management
 * @author luo-PC
 *
 */
public class WhiteListManager {
	private final static String TAG = WhiteListManager.class.getSimpleName();
	private static WhiteListManager sInstance;
	private Context 					mContext;
	private SharedPreferences    mWhiteListPref;
	
	public static synchronized WhiteListManager get(Context cxt) {
        if (sInstance == null) 
            sInstance = new WhiteListManager(cxt);
        return sInstance;
    }
	
	private WhiteListManager(Context cxt){
		this.mContext = cxt; 
		mWhiteListPref = mContext.getSharedPreferences(Constants.PREF_WHITE_LIST,Context.MODE_PRIVATE);
	}
	
	/**
	 * get user name
	 * @param number
	 * @return name
	 */
	public String getPrefName(String number){
		return mWhiteListPref.getString(number, null);
	}
	
	/**
	 * add name number pair
	 * @param item
	 * @return boolean
	 */
	public boolean addPrefPair(NameNumberPair item){
		if( item==null || item.getName()==null || item.getNumber()==null )return false;
		
		mWhiteListPref.edit().putString(item.getNumber(), item.getName()).commit();
		Log.i(TAG, "name list size = "+mWhiteListPref.getAll().size());

		return true;
	}

}
