package com.app.androidsms;

import com.app.androidsms.controller.PhoneController;
import com.app.androidsms.controller.PhoneController.OnCallChange;
import com.app.androidsms.controller.ProfilesController;
import com.app.androidsms.controller.SMSController;
import com.app.androidsms.controller.SMSController.SendTaskDone;
import com.app.androidsms.custom.widgets.CircleButton;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {
	
	private final static int SCANNIN_GREQUEST_CODE = 1;
	private Button send;
	private CircleButton controlBtn;
	private TextView logTV, controlBtnTV;
	private EditText phoneNo, messageBody;
	private SMSController mSMSController;
	private PhoneController mPhoneController;
	private ProfilesController mProfilesController;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_main);

		phoneNo = (EditText) findViewById(R.id.mobileNumber);
		messageBody = (EditText) findViewById(R.id.smsBody);
		logTV = (TextView) findViewById(R.id.log);
		
		mSMSController = SMSController.get(getApplicationContext());
		mSMSController.registerSMSReceiver();
//		mSMSController.setOnSendTaskDoneReceiver(new SendTaskDone() {
//			@Override
//			public void OnSendTaskDone(String[] SentNameList) {
//				// TODO Auto-generated method stub
//				String names = "";
//				for( int m=0; m<SentNameList.length; m++)
//					names+= (SentNameList[m]+"///");
//				setLogger( "end send msg to: "+names);
//			}
//		});
		
		mPhoneController = PhoneController.get(getApplicationContext());
		mProfilesController = ProfilesController.get(getApplicationContext());
		mPhoneController.setmOnEndCall(new OnCallChange() {
			@Override
			public void OnComingCall(String inComingNumber) {
				// TODO Auto-generated method stub
//				setLogger(inComingNumber+" Intercepted");
//				
//				String sms =  messageBody.getText().toString();
//				sms = (sms==null || sms.trim().length()==0) ? "i will call you later" :sms;
//				if( inComingNumber!=null && inComingNumber.trim().length()!=0)
//				{
//					String []nameList = inComingNumber.split("///");
//					mSMSController.sendSMSMulti(nameList, sms);
//					setLogger( "begin send msg to: "+inComingNumber);
//				}
				setLogger(" start vibrator");
				mProfilesController.startVibrator();
				mProfilesController.setVolume(100);
			}

			@Override
			public void OnCallOffhook(String offhookNumber) {
				// TODO Auto-generated method stub
				setLogger(" stop vibrator");
				mProfilesController.stopVibrator();
				mProfilesController.setVolume(0);
			}
		});
		
		controlBtn = (CircleButton) findViewById(R.id.controlbtn);
		controlBtnTV = (TextView) findViewById(R.id.controlbtnTV);
		controlBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				if( mPhoneController.isMonitoring())
//				{
//					mPhoneController.setMonitoring(false);
//					controlBtnTV.setText("start");controlBtn.setColor( getResources().getColor(R.color.control_btn_stop));
//					setLogger("stop monitor");
//				}
//				else
//				{
//					mPhoneController.setMonitoring(true);
//					controlBtnTV.setText("stop");controlBtn.setColor( getResources().getColor(R.color.control_btn_start));
//					setLogger("start monitor");
//				}
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, MyQRCode.class);
//				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//				startActivityForResult(intent, SCANNIN_GREQUEST_CODE);
				startActivity(intent);
			}
		});

//		send = (Button) findViewById(R.id.send);
//		send.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				String number = phoneNo.getText().toString();
//				String sms = messageBody.getText().toString();
//
//				if( sms!=null && sms.trim().length()!=0 && number!=null && number.trim().length()!=0)
//				{
//					send.setVisibility(View.GONE);
//					String []nameList = number.split("///");
//					mSMSController.sendSMSMulti(nameList, sms);
//				}
//				else 
//					Toast.makeText(getApplicationContext(), "请填写完整表格信息咯", Toast.LENGTH_SHORT).show();
//			}
//		});
	}
	
	/**
	 * divided by ///
	 * @param newLine
	 */
	private void setLogger(String newLine)
	{
		if( newLine==null || newLine.trim().length()==0) return;
		
		String setText = logTV.getText().toString();
		String []newLines = newLine.split("///");
		for(int k=0; k<newLines.length; k++)
			setText+= ("\n"+newLines[k]);
			
		logTV.setText( setText);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mSMSController.unregisterSMSReceiver();
		mProfilesController.stopVibrator();
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
		case SCANNIN_GREQUEST_CODE:
			if(resultCode == RESULT_OK){
				Bundle bundle = data.getExtras();
				//显示扫描到的内容
				//mTextView.setText(bundle.getString("result"));
				setLogger(bundle.getString("result") );
				//显示
				//mImageView.setImageBitmap((Bitmap) data.getParcelableExtra("bitmap"));
			}
			break;
		}
    }	
}
