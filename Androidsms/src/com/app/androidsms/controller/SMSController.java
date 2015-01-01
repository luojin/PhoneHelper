package com.app.androidsms.controller;

import java.util.ArrayList;
import java.util.List;

import com.app.androidsms.util.NameNumberPair;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

/**
 * ¿ØÖÆ¶ÌÐÅ·¢ËÍ
 * @author luo-PC
 *
 */
public class SMSController {
	private static String TAG = SMSController.class.getSimpleName();
	private Context mContext;
	private static SMSController sInstance;
	private String SENT = "SMS_SENT", DELIVERED = "SMS_DELIVERED";
	private PendingIntent sentPI, deliveredPI;
	private BroadcastReceiver smsSentReceiver=null, smsDeliveredReceiver=null;
	private String message;
	private List<NameNumberPair> mNameNumberList;
	private int sendIndex=0;
//	private SendTaskDone mSendTaskDone=null;
	private List<SendTaskDone> mSendTaskDoneList = null;
	
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
	private void setValue(int index, List<NameNumberPair> list, String msg)
	{
		sendIndex=index; this.mNameNumberList=list; this.message=msg;
	}
	
	public void sendSMSMulti(List<NameNumberPair> nameNumberList, String msg)
	{
		if( nameNumberList==null|| nameNumberList.size()==0 || msg==null || msg.trim().length()==0) 
		{
			Log.i(TAG, "sendSMSMulti value empty"); 
			SendTaskDone(null);
			return; 
		}
		
		setValue(0, nameNumberList, msg);
		sendSMS(nameNumberList.get(sendIndex),  message);
	}
	
	//sends an SMS message to another device
	private void sendSMS(NameNumberPair item, String message)
	{
		Log.i(TAG, "sendSMS "+item.getNumber()+" "+message);
		if( item==null || "".equals( item.getNumber()) || "".equals(message) ) 
		{
			Log.i(TAG, "sendSMS value empty"); 
			SendTaskDone(null);
			return;
		}
		
		if( item.getName()!=null && item.getName().length()>0)
			message = item.getName()+", "+message;
		
		SmsManager smsManager = SmsManager.getDefault();
		smsManager.sendTextMessage(item.getNumber(), null, message, sentPI, deliveredPI);
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
					if( sendIndex<mNameNumberList.size())
						sendSMS(mNameNumberList.get(sendIndex),  message);
					else{
						SendTaskDone(mNameNumberList);
						setValue(0,null,null);
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
		if( mSendTaskDoneList==null )
			mSendTaskDoneList =  new ArrayList<SMSController.SendTaskDone>();
		
		mSendTaskDoneList.add(obj);
	}
	
	private void SendTaskDone(List<NameNumberPair> mNameNumberPairList)
	{
		if( mSendTaskDoneList==null ) return;
		
		for (SendTaskDone item : mSendTaskDoneList) {
			item.OnSendTaskDone(mNameNumberPairList);
		}
	}
	
	/**
	 * send task finish will callback this function
	 * args: sent name list, String[]
	 * @author luo-PC
	 *
	 */
	public interface SendTaskDone{
		public void OnSendTaskDone(List<NameNumberPair> mNameNumberPairList);
	}

}
