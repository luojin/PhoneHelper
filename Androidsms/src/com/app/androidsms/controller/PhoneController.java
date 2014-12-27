package com.app.androidsms.controller;

import java.lang.reflect.Method;

import com.android.internal.telephony.ITelephony;

import android.content.Context;
import android.os.RemoteException;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class PhoneController {
	private static String TAG = PhoneController.class.getSimpleName();
	private static PhoneController sInstance;
	private Context 					mContext;
    private boolean					 	isMonitoring 			= false;  
    private PhoneStateListener 	phoneListener 		= null;
    private ITelephony 				iTelephony 			= null;
    private TelephonyManager 	telephonyManager 	= null;  
    private OnEndCall					mOnEndCall			= null;
	
	public static synchronized PhoneController get(Context cxt) {
        if (sInstance == null) 
            sInstance = new PhoneController(cxt);
        return sInstance;
    }
	
	private PhoneController(Context cxt){
		this.mContext = cxt; 
		initValue(); 
	}
	
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
	
	private void setPhoneListener()
    {
    	if( phoneListener==null)
    	{
	    	phoneListener = new PhoneStateListener(){  
	            @Override  
	            public void onCallStateChanged(int state,String inComingNumber)
	            {  
	                if(isMonitoring && state == TelephonyManager.CALL_STATE_RINGING )
	                {  
	                	try {
							iTelephony.endCall();
							whenEndCall(inComingNumber);
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	                }  
	            }  
	        };  
    	}

        telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);  
    }

	public boolean isMonitoring() {
		return isMonitoring;
	}

	public void setMonitoring(boolean isMonitoring) {
		this.isMonitoring = isMonitoring;
	}
	
	public void setmOnEndCall(OnEndCall mOnEndCall) {
		this.mOnEndCall = mOnEndCall;
	}
	
	private void whenEndCall(String inComingNumber)
	{
		if( mOnEndCall!=null)
			mOnEndCall.OnEndCall(inComingNumber);
	}

	/**
	 * IsMonitoring && Phone call Intercepted will trigger this callback function
	 * @author luo-PC
	 */
	public interface OnEndCall{
		public void OnEndCall(String inComingNumber);
	}
	
}
