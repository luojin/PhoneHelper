package com.app.androidsms;

import java.util.Random;

import com.app.androidsms.controller.PhoneController;
import com.app.androidsms.controller.ProfilesController;
import com.app.androidsms.controller.SMSController;
import com.app.androidsms.controller.PhoneController.OnCallChange;
import com.app.androidsms.controller.SMSController.SendTaskDone;
import com.app.androidsms.custom.widgets.CircleButton;
import com.app.androidsms.custom.widgets.DragLayout;
import com.app.androidsms.custom.widgets.DragLayout.DragListener;
import com.nineoldandroids.view.ViewHelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class AppActivity extends Activity{
	private DragLayout dl;
	private ListView lv;
	private ImageView iv_icon, iv_bottom;
	private CircleButton controlBtn;
	private TextView logTV, controlBtnTV;
	private EditText messageBody;
	
	private SMSController mSMSController;
	private PhoneController mPhoneController;
	private ProfilesController mProfilesController;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.app_fragment);
		
		initDragLayout();
		initView();
	}

	private void initDragLayout() {
		dl = (DragLayout) findViewById(R.id.dl);
		dl.setDragListener(new DragListener() {
			@Override
			public void onOpen() {
				lv.smoothScrollToPosition(new Random().nextInt(30));
			}

			@Override
			public void onClose() {
				shake();
			}

			@Override
			public void onDrag(float percent) {
				ViewHelper.setAlpha(iv_icon, 1 - percent);
			}
		});
	}

	private void initView() {
		
		iv_icon = (ImageView) findViewById(R.id.iv_icon);
		iv_icon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dl.open();
			}
		});
		
		iv_bottom = (ImageView) findViewById(R.id.iv_bottom);
		
		lv = (ListView) findViewById(R.id.lv);	
		lv.setAdapter(new ArrayAdapter<String>(AppActivity.this,
				R.layout.item_text, getResources().getStringArray(R.array.menu_array) ));
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				Toast.makeText(getApplicationContext(), "click " + position, Toast.LENGTH_SHORT).show();
			}
		});
		
		messageBody = (EditText) findViewById(R.id.smsBody);
		logTV = (TextView) findViewById(R.id.log);
		
		mSMSController = SMSController.get(getApplicationContext());
		mSMSController.registerSMSReceiver();
		mSMSController.setOnSendTaskDoneReceiver(new SendTaskDone() {
			@Override
			public void OnSendTaskDone(String[] SentNameList) {
				// TODO Auto-generated method stub
				String names = "";
				for( int m=0; m<SentNameList.length; m++)
					names+= (SentNameList[m]+"///");
				setLogger( "end send msg to: "+names);
			}
		});
		
		mPhoneController = PhoneController.get(getApplicationContext());
		mProfilesController = ProfilesController.get(getApplicationContext());
		mPhoneController.setmOnEndCall(new OnCallChange() {
			@Override
			public void OnComingCall(String inComingNumber) {
				// TODO Auto-generated method stub
				setLogger(inComingNumber+" Intercepted");
				
				String sms =  messageBody.getText().toString();
				sms = (sms==null || sms.trim().length()==0) ? "i will call you later" :sms;
				if( inComingNumber!=null && inComingNumber.trim().length()!=0)
				{
					String []nameList = inComingNumber.split("///");
					mSMSController.sendSMSMulti(nameList, sms);
					setLogger( "begin send msg to: "+inComingNumber);
				}
				
//				setLogger(" start vibrator");
//				mProfilesController.startVibrator();
//				mProfilesController.setVolume(100);
			}

			@Override
			public void OnCallOffhook(String offhookNumber) {
				// TODO Auto-generated method stub
				setLogger(" stop vibrator");
				
//				mProfilesController.stopVibrator();
//				mProfilesController.setVolume(0);
			}
		});
		
		controlBtn = (CircleButton) findViewById(R.id.controlbtn);
		controlBtnTV = (TextView) findViewById(R.id.controlbtnTV);
		controlBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if( mPhoneController.isMonitoring())
				{
					mPhoneController.setMonitoring(false);
					controlBtnTV.setText("start");controlBtn.setColor( getResources().getColor(R.color.control_btn_stop));
					setLogger("stop monitor");
				}
				else
				{
					mPhoneController.setMonitoring(true);
					controlBtnTV.setText("stop");controlBtn.setColor( getResources().getColor(R.color.control_btn_start));
					setLogger("start monitor");
				}
				
//				Intent intent = new Intent();
//				intent.setClass(AppActivity.this, MyQRCode.class);
////				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
////				startActivityForResult(intent, SCANNIN_GREQUEST_CODE);
//				startActivity(intent);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private void shake() {
		iv_icon.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
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

}
