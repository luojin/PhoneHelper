package com.app.androidsms.controller;

import java.lang.reflect.Method;

import com.android.internal.telephony.ITelephony;

import android.content.Context;
import android.os.RemoteException;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

/**
 * 控制来电拦截与否
 * @author luo-PC
 *
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
	            public void onCallStateChanged(int state,String number)
	            {  
	            	if( !isMonitoring) return;
	            	
	            	switch(state){
	            	//the calling is coming
		            	case TelephonyManager.CALL_STATE_RINGING:
//		            		try {
//								iTelephony.endCall();
								whenComingCall(number);
//							} catch (RemoteException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
		            		break;
		            //the calling is ended
		            	case TelephonyManager.CALL_STATE_IDLE:
		            		mOnCallChange.OnCallOffhook(number);
		            		break;
	            		default:
	            			break;
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
	
	public void setmOnEndCall(OnCallChange mOnEndCall) {
		this.mOnCallChange = mOnEndCall;
	}
	
	private void whenComingCall(String inComingNumber)
	{
		if( mOnCallChange!=null)
			mOnCallChange.OnComingCall(inComingNumber);
	}

	/**
	 * IsMonitoring && Phone call Intercepted will trigger this callback function
	 * @author luo-PC
	 */
	public interface OnCallChange{
		public void OnComingCall(String inComingNumber);
		public void OnCallOffhook(String offhookNumber);
	}
	
}
