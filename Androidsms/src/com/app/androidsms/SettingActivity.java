package com.app.androidsms;

import com.app.androidsms.util.Constants;
import com.app.androidsms.util.UserInfoPref;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

/**
 * 设置页面
 * @author luo-PC
 *
 */
public class SettingActivity extends ActionBarActivity{
	private EditText nameET, phoneET;
	private String name, phone;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_setting);
		
		ActionBar actionBar = getSupportActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    actionBar.setTitle(R.string.setting);
		
		nameET = (EditText) findViewById(R.id.name);
		phoneET = (EditText) findViewById(R.id.phone_number);
		
		getPref();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.setting, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch( item.getItemId() ){
		case android.R.id.home: 
			onBackPressed();
			break;
		case R.id.setting_ok:
			if( setPref() )
				onBackPressed();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * 获取已经存在的用户信息
	 */
	private void getPref()
	{
		name = UserInfoPref.get(getApplicationContext()).getString(Constants.PREF_NAME);
		phone = UserInfoPref.get(getApplicationContext()).getString(Constants.PREF_PHONE);
		nameET.setText(name);
		phoneET.setText(phone);
	}
	
	/**
	 * 设置用户信息
	 * @return boolean
	 */
	private boolean setPref()
	{
		name = nameET.getText().toString().trim();
		phone = phoneET.getText().toString().trim();
		if( name.length()==0 || phone.length()==0 ){
			Toast.makeText(getApplicationContext(), "不能为空", Toast.LENGTH_SHORT).show();
			return false;
		}
		
		UserInfoPref.get(getApplicationContext()).addString(Constants.PREF_NAME, name);
		UserInfoPref.get(getApplicationContext()).addString(Constants.PREF_PHONE, phone);
		return true;
	}
	
	@Override
	public void onBackPressed() {
		Intent intent = new Intent();
		intent.putExtra(Constants.PREF_NAME,  name);
		intent.putExtra(Constants.PREF_PHONE,  phone);
		setResult(Constants.GET_SETTINGS, intent);
		
		finish();
		overridePendingTransition(R.anim.zoom_in,
			R.anim.slide_right_out);
	}

}
