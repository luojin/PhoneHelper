package com.app.androidsms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.app.androidsms.controller.ContactsController;
import com.app.androidsms.controller.ContactsController.QueryContactFinish;
import com.app.androidsms.controller.PhoneController;
import com.app.androidsms.controller.ProfilesController;
import com.app.androidsms.controller.SMSController;
import com.app.androidsms.controller.PhoneController.OnCallChange;
import com.app.androidsms.controller.SMSController.SendTaskDone;
import com.app.androidsms.custom.widgets.CircleButton;
import com.app.androidsms.custom.widgets.DragLayout;
import com.app.androidsms.custom.widgets.DragLayout.DragListener;
import com.app.androidsms.util.Constants;
import com.app.androidsms.util.NameNumberPair;
import com.nineoldandroids.view.ViewHelper;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.ContactsContract;
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
	private ImageView iv_icon;
	private CircleButton controlBtn;
	private TextView logTV, controlBtnTV;
	private EditText messageBody;
	
	private SMSController mSMSController;
	private PhoneController mPhoneController;
	private ContactsController mContactsController;
	
	private String name, phone;
	private TextView nameTV, phoneTV;
	private int mCurrentPosition = 0;
	private SharedPreferences mUserPrefs = null;
	private Map<String, String> mNumberNameMap;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.app_fragment);
		initialize();
	}

	/**
	 * get view and initialize 
	 */
	private void initialize() {
		initDragLayout();
		
		/**
		 * get widgets
		 */
		nameTV = (TextView)findViewById(R.id.name);
		phoneTV = (TextView)findViewById(R.id.phone);
		iv_icon = (ImageView) findViewById(R.id.iv_icon);
		lv = (ListView) findViewById(R.id.lv);	
		messageBody = (EditText) findViewById(R.id.smsBody);
		logTV = (TextView) findViewById(R.id.log);
		controlBtn = (CircleButton) findViewById(R.id.controlbtn);
		controlBtnTV = (TextView) findViewById(R.id.controlbtnTV);
		
		getUserInfo();
		iv_icon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dl.open();
			}
		});
		
		lv.setAdapter(new ArrayAdapter<String>(AppActivity.this,
				R.layout.item_text, getResources().getStringArray(R.array.menu_array) ));
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				dl.close();
				//set position
				mCurrentPosition = position;
			}
		});
		
		mSMSController = SMSController.get(getApplicationContext());
		mSMSController.registerSMSReceiver();
		mSMSController.setOnSendTaskDoneReceiver(new SendTaskDone() {
			@Override
			public void OnSendTaskDone(List<NameNumberPair> mNameNumberPairList) {
				// TODO Auto-generated method stub
				String names = "";
				for( int m=0; m<mNameNumberPairList.size(); m++)
				{
					if(mNameNumberPairList.get(m).getName()!=null)
						names+= (mNameNumberPairList.get(m).getName()+"///");
					else if( mNameNumberPairList.get(m).getNumber()!=null)
						names+= (mNameNumberPairList.get(m).getNumber()+"///");
				}
				setLogger( "end send msg to: "+names);
			}
		});
		
		mPhoneController = PhoneController.get(getApplicationContext());
		mPhoneController.setmOnEndCall(new OnCallChange() {
			@Override
			public void OnComingCall(String inComingNumber) {
				// TODO Auto-generated method stub
				setLogger(inComingNumber+" Intercepted");
				
				if( mPhoneController.isMonitoring() ){
					mNumberNameMap = mContactsController.getNumberNameMap();
					//send msg
					List<NameNumberPair>  mNameNumberPair = new ArrayList<NameNumberPair>();
					String inComingName = "";
					String msgContent = messageBody.getText().toString().trim();
					if( mNumberNameMap!=null )
						inComingName = mNumberNameMap.get(inComingNumber);
					if( msgContent==null || msgContent.length()==0 )
						msgContent = "我现在有事，迟点给你回电话吧。";
					
					mNameNumberPair.add( new NameNumberPair(inComingName, inComingNumber));
					mSMSController.sendSMSMulti(mNameNumberPair, msgContent);
				}
			}

			@Override
			public void OnCallOffhook(String offhookNumber) {
				if( mPhoneController.isMonitoring())
					setLogger("来电结束：" + offhookNumber);
			}
		});
		
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
			}
		});
		
		controlBtn.setVisibility(View.INVISIBLE);
		mContactsController = ContactsController.get(getApplicationContext());       //begin query contacts database
		mContactsController.addQueryContactFinish(new QueryContactFinish() {
			
			@Override
			public void onQueryContactFinish() {
				// TODO Auto-generated method stub
				controlBtn.setVisibility(View.VISIBLE);
			}
		});
	}
	
	/**
	 * initialize drag layout
	 */
	private void initDragLayout() {
		dl = (DragLayout) findViewById(R.id.dl);
		dl.setDragListener(new DragListener() {
			@Override
			public void onOpen() {
				//reset
				mCurrentPosition = 0;
			}

			@Override
			public void onClose() {
				//do something
				switch(mCurrentPosition){
					case Constants.HOME:
						break;
					case Constants.SCAN:
						Intent intent = new Intent();
						intent.setClass(AppActivity.this, MipcaActivityCapture.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivityForResult(intent, Constants.SCANNIN_GREQUEST_CODE);
						overridePendingTransition(
								R.anim.slide_right_in, R.anim.zoom_out);
						break;
					case Constants.QRCODE:
						Intent intent1 = new Intent();
						intent1.setClass(AppActivity.this, MyQRCode.class);
						startActivity(intent1);
						overridePendingTransition(
								R.anim.slide_right_in, R.anim.zoom_out);
						break;
					case Constants.MULTISENDSMS:
						Intent intent2 = new Intent();
						intent2.setClass(AppActivity.this, MultiMessage.class);
						startActivity(intent2);
						overridePendingTransition(
								R.anim.slide_right_in, R.anim.zoom_out);
						break;
					case Constants.SETTING:
						Intent intent3 = new Intent();
						intent3.setClass(AppActivity.this, SettingActivity.class);
						intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivityForResult(intent3, Constants.GET_SETTINGS);
						overridePendingTransition(
								R.anim.slide_right_in, R.anim.zoom_out);
						break;
					default:
						break;
				}
			}

			@Override
			public void onDrag(float percent) {
				ViewHelper.setAlpha(iv_icon, 1 - percent);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		shake();
	}

	/**
	 * the icon will shake while DragLayout closed
	 */
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
	
	/**
	 * get user information
	 * including name and phone number
	 */
	private void getUserInfo()
	{
		if( mUserPrefs==null)
			mUserPrefs = getSharedPreferences(Constants.PREF_USER_INFO,MODE_PRIVATE);
		
		name = mUserPrefs.getString(Constants.PREF_NAME, "name");
		phone = mUserPrefs.getString(Constants.PREF_PHONE, "phone");
		name = name==""?"name":name;
		phone = phone==""?"phone":phone;
		nameTV.setText(name);
		phoneTV.setText(phone);
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
		case Constants.SCANNIN_GREQUEST_CODE:
			if(resultCode == RESULT_OK){
				Bundle bundle = data.getExtras();
				//显示扫描到的内容
				setLogger("二维码内容: " + bundle.getString("result") );
			}
			break;
		case Constants.GET_SETTINGS:
			Bundle bundle = data.getExtras();
			name = bundle.getString(Constants.PREF_NAME);
			phone = bundle.getString(Constants.PREF_PHONE);
			getUserInfo();
			break;
		}
    }	

}
