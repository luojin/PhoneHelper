package com.app.androidsms;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.app.androidsms.controller.ContactsController;
import com.app.androidsms.controller.ContactsController.QueryContactFinish;
import com.app.androidsms.controller.PhoneController;
import com.app.androidsms.controller.SMSController;
import com.app.androidsms.controller.PhoneController.OnCallChange;
import com.app.androidsms.controller.SMSController.SendTaskDone;
import com.app.androidsms.custom.widgets.CircleButton;
import com.app.androidsms.custom.widgets.DragLayout;
import com.app.androidsms.custom.widgets.DragLayout.DragListener;
import com.app.androidsms.util.Constants;
import com.app.androidsms.util.NameNumberPair;
import com.app.androidsms.util.UserInfoPref;
import com.app.androidsms.util.WhiteListManager;
import com.nineoldandroids.view.ViewHelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * main activity manage the application
 * @author luo-PC
 *
 */
public class AppActivity extends Activity{
	/**
	 * views
	 */
	private DragLayout 	dl;
	private ListView 		lv;
	private ImageView 	iv_icon;
	private CircleButton 	controlBtn;
	private TextView 		logTV, controlBtnTV, nameTV, phoneTV;
	private EditText 		messageBody;
	
	/**
	 * variables
	 */
	private String name, phone;
	private int mCurrentPosition = 0;
	private Map<String, String> mNumberNameMap;
	
	/**
	 * controllers
	 */
	private SMSController 		mSMSController;
	private PhoneController 		mPhoneController;
	private ContactsController mContactsController;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.app_fragment);
		
		initialize();
	}

	/**
	 * get view and initialize activity
	 */
	private void initialize() {
		initDragLayout();
		
		/**
		 * get widgets
		 */
		nameTV 		= (TextView)findViewById(R.id.name);
		phoneTV 		= (TextView)findViewById(R.id.phone);
		iv_icon 			= (ImageView) findViewById(R.id.iv_icon);
		lv 					= (ListView) findViewById(R.id.lv);	
		messageBody = (EditText) findViewById(R.id.smsBody);
		logTV 			= (TextView) findViewById(R.id.log);
		controlBtn 	= (CircleButton) findViewById(R.id.controlbtn);
		controlBtnTV = (TextView) findViewById(R.id.controlbtnTV);
		
		getUserInfo();
		iv_icon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				/**
				 * click the icon open drag layout
				 */
				dl.open();
			}
		});
		
		/**
		 * set adapter of sidebar
		 * sidebar menus
		 */
		lv.setAdapter(new ArrayAdapter<String>(AppActivity.this,
				R.layout.item_text, getResources().getStringArray(R.array.menu_array) ));
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				/**
				 * drag layout closed and record click position
				 */
				dl.close();
				mCurrentPosition = position;
			}
		});
		
		/**
		 * message management
		 */
		mSMSController = SMSController.get(getApplicationContext());
		mSMSController.registerSMSReceiver();
		mSMSController.setOnSendTaskDoneReceiver(new SendTaskDone() {
			@Override
			public void OnSendTaskDone(List<NameNumberPair> mNameNumberPairList) {
				if( mNameNumberPairList==null ) return;
				
				String names = "";
				for( int m=0; m<mNameNumberPairList.size(); m++)
				{
					if(mNameNumberPairList.get(m).getName()!=null)
						names+= (mNameNumberPairList.get(m).getName()+Constants.STRING_DIVIDER);
					else if( mNameNumberPairList.get(m).getNumber()!=null)
						names+= (mNameNumberPairList.get(m).getNumber()+Constants.STRING_DIVIDER);
				}
				setLogger( "end send msg to: "+names);
			}
		});
		
		/**
		 * phone management
		 */
		mPhoneController = PhoneController.get(getApplicationContext());
		mPhoneController.setmOnEndCall(new OnCallChange() {
			@Override
			public void OnComingCall(String inComingNumber) {
				// TODO Auto-generated method stub
				setLogger("拦截来电: " + inComingNumber);
				
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
			public void OnCallOffhook() {
			}
		});
		
		/**
		 * control monitor or not
		 */
		controlBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if( mPhoneController.isMonitoring()){
					mPhoneController.setMonitoring(false);
					controlBtnTV.setText("start");controlBtn.setColor( getResources().getColor(R.color.control_btn_stop));
					setLogger("stop monitor");
				}
				else{
					mPhoneController.setMonitoring(true);
					controlBtnTV.setText("stop");controlBtn.setColor( getResources().getColor(R.color.control_btn_start));
					setLogger("start monitor");
				}
			}
		});
		
		/**
		 * query contacts databse
		 */
		controlBtn.setVisibility(View.INVISIBLE);
		mContactsController = ContactsController.get(getApplicationContext());       
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
				//reset position
				mCurrentPosition = 0;
			}

			@Override
			public void onClose() {
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
		//reset position
		mCurrentPosition = 0;
		
		if( mContactsController.getNumberNameMap()==null ){
			controlBtn.setVisibility(View.INVISIBLE);
		}else{
			controlBtn.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * the icon will shake while DragLayout closed
	 */
	private void shake() {
		iv_icon.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
	}
	
	/**
	 * show operation log 
	 * divided by Constants.STRING_DIVIDER
	 * @param newLine
	 */
	private void setLogger(String newLine)
	{
		if( newLine==null || newLine.trim().length()==0) return;
		
		String setText = logTV.getText().toString();
		String []newLines = newLine.split(Constants.STRING_DIVIDER);
		for(int k=0; k<newLines.length; k++)
			setText = getCurrentTime() + ": " +newLines[k] + "\n" + setText;
			
		logTV.setText( setText);
	}
	
	/**
	 * get current time 
	 * @return current time string
	 */
	private String getCurrentTime()
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
		return sdf.format(new Date());
	}
	
	/**
	 * get user information
	 * including name and phone number
	 */
	private void getUserInfo()
	{
		name = UserInfoPref.get(getApplicationContext()).getString(Constants.PREF_NAME);
		phone = UserInfoPref.get(getApplicationContext()).getString(Constants.PREF_PHONE);
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
					String result = bundle.getString("result");
					if( result==null ) return;
					
					String []addItem = result.split(Constants.STRING_DIVIDER);
					if( addItem.length==2){
						NameNumberPair item = new NameNumberPair(addItem[0], addItem[1]);
						WhiteListManager.get(getApplicationContext()).addPrefPair(item);
						setLogger("授权给： " + addItem[0] );
					}else{
						setLogger("二维码内容: " + result );
					}
				}
				break;
			case Constants.GET_SETTINGS:
				Bundle bundle = data.getExtras();
				name = bundle.getString(Constants.PREF_NAME);
				phone = bundle.getString(Constants.PREF_PHONE);
				getUserInfo();
				break;
			default:
					break;
		}
    }	

}
