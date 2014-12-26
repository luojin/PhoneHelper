package com.app.androidsms;

import com.app.androidsms.controller.SMSController;
import com.app.androidsms.controller.SMSController.SendTaskDone;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {
	
	private Button send;
	private EditText phoneNo, messageBody;
	private SMSController mSMSController;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_main);

		phoneNo = (EditText) findViewById(R.id.mobileNumber);
		messageBody = (EditText) findViewById(R.id.smsBody);
		
		mSMSController = SMSController.get(getApplicationContext());
		mSMSController.registerSMSReceiver();
		mSMSController.setOnSendTaskDoneReceiver(new SendTaskDone() {
			@Override
			public void OnSendTaskDone() {
				// TODO Auto-generated method stub
				send.setVisibility(View.VISIBLE);
			}
		});

		send = (Button) findViewById(R.id.send);
		send.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String number = phoneNo.getText().toString();
				String sms = messageBody.getText().toString();

				if( sms!=null && sms.trim().length()!=0 && number!=null && number.trim().length()!=0)
				{
					send.setVisibility(View.GONE);
					String []nameList = number.split("///");
					mSMSController.sendSMSMulti(nameList, sms);
				}
				else 
					Toast.makeText(getApplicationContext(), "请填写完整表格信息咯", Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mSMSController.unregisterSMSReceiver();
	}
}
