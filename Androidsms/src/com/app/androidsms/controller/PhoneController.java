package com.app.androidsms.controller;

import java.lang.reflect.Method;

import com.android.internal.telephony.ITelephony;
import com.app.androidsms.util.Constants;
import com.app.androidsms.util.WhiteListManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.RemoteException;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * 控制来电拦截与否
 * 监听模式：
 * 					所有电话都拦截，自动回复短信
 * 非监听模式：
 * 					白名单中的来电，自动更改情景模式为声音100+震动
 * @author luo-PC
 */
public class PhoneController {
	private static String TAG = PhoneController.class.getSimpleName();
	private static PhoneController sInstance;
	private Context 					mContext;
    private boolean					 	isMonitoring 			= false;  
    private PhoneStateListener 	phoneListener 		= null;
    private ITelephony 				iTelephony 			= null;
    private TelephonyManager 	telephonyManager 	= null;  
    private OnCallChange				mOnCallChange		= null;
	
	public static synchronized PhoneController get(Context cxt) {
        if (sInstance == null) 
            sInstance = new PhoneController(cxt);
        return sInstance;
    }
	
	private PhoneController(Context cxt){
		this.mContext = cxt; 
		initValue(); 
	}
	
	/**
	 * 初始化
	 */
	private void initValue()
	{
		telephonyManager = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);  
        try {
            Method getITelephonyMethod = telephonyManager.getClass().getDeclaredMethod("getITelephony");
            getITelephonyMethod.setAccessible(true);
            iTelephony  = (ITelephony) getITelephonyMethod.invoke(telephonyManager);
		} catch (Exception e) {
		            e.printStackTrace();
		}
        setPhoneListener();
	}
	
	/**
	 * 设置监听器，回调函数
	 */
	private void setPhoneListener()
    {
    	if( phoneListener==null)
    	{
	    	phoneListener = new PhoneStateListener(){  
	            @Override  
	            public void onCallStateChanged(int state,String number)
	            {  
	            	switch(state){
	            	//the calling is coming
	            	case TelephonyManager.CALL_STATE_RINGING:
	            		if( isMonitoring() ){
		            		try {
								iTelephony.endCall();
								whenComingCall( number);
							} catch (RemoteException e) {
								e.printStackTrace();
							}
	            		}else if( inWhiteList(number) ){
	            			//if白名单的来电，自动切换情景模式
	            			ProfilesController.get(mContext.getApplicationContext()).getInitProfile();
	            			ProfilesController.get(mContext.getApplicationContext()).RingAndVibrate();
	            		}
	            		break;
		            //the calling is ended
	            	case TelephonyManager.CALL_STATE_IDLE:
	            		//CALL_STATE_IDLE 是拿不到电话号码的
	            		whenEndCall();
	            		
	            		if( !isMonitoring() ){
	            			//if白名单的来电, 恢复情景模式
	            			ProfilesController.get(mContext.getApplicationContext()).resetProfile();
	            		}
	            		break;
            		default:
            			break;
	            	}
	            }  
	        };  
    	}

        telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);  
    }
	
	/**
	 * 判断是否在白名单内
	 * @param number
	 * @return boolean
	 */
	private boolean inWhiteList(String number)
	{
		return WhiteListManager.get(mContext).getPrefName(number)!=null;
	}

	/**
	 * 是否监听模式
	 * @return boolean
	 */
	public boolean isMonitoring() {
		return isMonitoring;
	}

	/**
	 * 设置监听模式
	 * @param isMonitoring
	 */
	public void setMonitoring(boolean isMonitoring) {
		this.isMonitoring = isMonitoring;
	}
	
	/**
	 * 设置监听器，回调函数
	 * @param mOnEndCall
	 */
	public void setmOnEndCall(OnCallChange mOnEndCall) {
		this.mOnCallChange = mOnEndCall;
	}
	
	/**
	 * 来电回调
	 * @param inComingNumber
	 */
	private void whenComingCall(String inComingNumber){
		if( mOnCallChange!=null)
			mOnCallChange.OnComingCall(inComingNumber);
	}
	
	/**
	 * 结束来电回调
	 */
	private void whenEndCall()
	{
		if( mOnCallChange!=null)
			mOnCallChange.OnCallOffhook();
	}

	/**
	 * IsMonitoring && Phone call Intercepted will trigger this callback function
	 * @author luo-PC
	 */
	public interface OnCallChange{
		public void OnComingCall(String inComingNumber);
		public void OnCallOffhook();
	}
	
}
