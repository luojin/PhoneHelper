package com.app.androidsms.controller;

import java.lang.reflect.Method;

import com.android.internal.telephony.ITelephony;

import android.content.Context;
import android.os.RemoteException;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

/**
 * ���������������
 * ����ģʽ��
 * 					���е绰�����أ��Զ��ظ�����
 * �Ǽ���ģʽ��
 * 					�������е����磬�Զ������龰ģʽΪ����100+��
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
	            	switch(state){
	            	//the calling is coming
	            	case TelephonyManager.CALL_STATE_RINGING:
	            		if( isMonitoring() ){
		            		try {
								iTelephony.endCall();
								whenComingCall( number);
								//send message
							} catch (RemoteException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
	            		}else{
	            			//if�����������磬�Զ��л��龰ģʽ
	            			ProfilesController.get(mContext.getApplicationContext()).getInitProfile();
	            			ProfilesController.get(mContext.getApplicationContext()).RingAndVibrate();
	            		}
	            		break;
		            //the calling is ended
	            	case TelephonyManager.CALL_STATE_IDLE:
	            		whenEndCall(number);
	            		
	            		if( !isMonitoring() )
	            		{
	            			//if������������, �ָ��龰ģʽ
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
	
	private void whenEndCall(String inComingNumber)
	{
		if( mOnCallChange!=null)
			mOnCallChange.OnCallOffhook(inComingNumber);
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
