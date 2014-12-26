package com.app.androidsms.controller;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class SMSController {
	private static String TAG = SMSController.class.getSimpleName();
	private Context mContext;
	private static SMSController sInstance;
	private String SENT = "SMS_SENT", DELIVERED = "SMS_DELIVERED";
	private PendingIntent sentPI, deliveredPI;
	private BroadcastReceiver smsSentReceiver=null, smsDeliveredReceiver=null;
	private String name[], message;
	private int sendIndex=0;
	private SendTaskDone mSendTaskDone=null;
	
	public static synchronized SMSController get(Context cxt) {
        if (sInstance == null) 
            sInstance = new SMSController(cxt);
        return sInstance;
    }
	
	private SMSController(Context cxt)
	{
		this.mContext = cxt;
		sentPI = PendingIntent.getBroadcast(mContext, 0,
				new Intent(SENT), 0);
		deliveredPI = PendingIntent.getBroadcast(mContext, 0,
				new Intent(DELIVERED), 0);
		setValue(0,null,null);
		
		Log.i(TAG, "constructor");
	}
	
	/**
	 * index, name list, message
	 * @param index
	 * @param name
	 * @param msg
	 */
	private void setValue(int index, String[] name, String msg)
	{
		sendIndex=index; this.name=name; this.message=msg;
	}
	
	public void sendSMSMulti(String[] phoneNumber, String msg)
	{
		if( phoneNumber==null|| phoneNumber.length==0 || msg==null || msg.trim().length()==0) 
		{
			Log.i(TAG, "sendSMSMulti value empty"); 
			mSendTaskDone.OnSendTaskDone();
			return; 
		}
		
		setValue(0, phoneNumber, msg);
		sendSMS(name[sendIndex],  message);
	}
	
	//sends an SMS message to another device
	private void sendSMS(String phoneNumber, String message)
	{
		Log.i(TAG, "sendSMS "+phoneNumber+" "+message);
		if( phoneNumber==null || "".equals( phoneNumber) || "".equals(message) ) 
		{
			Log.i(TAG, "sendSMS value empty"); 
			mSendTaskDone.OnSendTaskDone();
			return;
		}
		
		SmsManager smsManager = SmsManager.getDefault();
		smsManager.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
	}
	
	//register BroadcastReceiver
	public void registerSMSReceiver()
	{
		Log.i(TAG, "registerSMSReceiver");
		if( smsSentReceiver==null){
			//begin send
			smsSentReceiver = new BroadcastReceiver(){
				@Override
				public void onReceive(Context arg0, Intent arg1) {
					sendIndex++;
					if( sendIndex<name.length)
						sendSMS(name[sendIndex],  message);
					else{
						setValue(0,null,null);
						mSendTaskDone.OnSendTaskDone();
					}
					
					switch (getResultCode())
					{
					case Activity.RESULT_OK:
						Toast.makeText(mContext, "SMS sent",
								Toast.LENGTH_SHORT).show();
						break;
					case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
						Toast.makeText(mContext, "Generic failure",
								Toast.LENGTH_SHORT).show();
						break;
					case SmsManager.RESULT_ERROR_NO_SERVICE:
						Toast.makeText(mContext, "No service",
								Toast.LENGTH_SHORT).show();
						break;
					case SmsManager.RESULT_ERROR_NULL_PDU:
						Toast.makeText(mContext, "Null PDU",
								Toast.LENGTH_SHORT).show();
						break;
					case SmsManager.RESULT_ERROR_RADIO_OFF:
						Toast.makeText(mContext, "Radio off",
								Toast.LENGTH_SHORT).show();
						break;
					}
				}
			};
		}

		//---create the BroadcastReceiver when the SMS is delivered---
		if( smsDeliveredReceiver==null){
			//finish sending
			smsDeliveredReceiver = new BroadcastReceiver(){
				@Override
				public void onReceive(Context arg0, Intent arg1) {
					switch (getResultCode())
					{
					case Activity.RESULT_OK:
						Toast.makeText(mContext, "SMS delivered",
								Toast.LENGTH_SHORT).show();
						break;
					case Activity.RESULT_CANCELED:
						Toast.makeText(mContext, "SMS not delivered",
								Toast.LENGTH_SHORT).show();
						break;
					}
				}
			}; 
		}

		//---register the two BroadcastReceivers---
		mContext.registerReceiver(smsDeliveredReceiver, new IntentFilter(DELIVERED));      
		mContext.registerReceiver(smsSentReceiver, new IntentFilter(SENT));
	}
	
	//unregister BroadcastReceiver
	public void unregisterSMSReceiver()
	{
		Log.i(TAG, "unregisterSMSReceiver");
		//---unregister the two BroadcastReceivers---
		mContext.unregisterReceiver(smsSentReceiver);
		mContext.unregisterReceiver(smsDeliveredReceiver);    
	}
	
	public void setOnSendTaskDoneReceiver(SendTaskDone obj)
	{
		mSendTaskDone = obj;
	}
	
	/**
	 * send finish callback
	 * @author luo-PC
	 *
	 */
	public interface SendTaskDone{
		public void OnSendTaskDone();
	}

}
