package com.app.androidsms.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

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
	
	public String getPrefName(String number)
	{
		return mWhiteListPref.getString(number, null);
	}
	
	public boolean addPrefPair(NameNumberPair item)
	{
		if( item==null || item.getName()==null || item.getNumber()==null )return false;
		
		mWhiteListPref.edit().putString(item.getNumber(), item.getName()).commit();
		Log.i(TAG, "name list size = "+mWhiteListPref.getAll().size());

		return true;
	}

}
