package com.app.androidsms;

import com.app.androidsms.util.Constants;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

public class SettingActivity extends ActionBarActivity{
	private SharedPreferences mUserPrefs;
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
		
		mUserPrefs = getSharedPreferences(Constants.PREF_USER_INFO,MODE_PRIVATE);
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
			setPref();
			onBackPressed();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void getPref()
	{
		name = mUserPrefs.getString(Constants.PREF_NAME, "");
		phone = mUserPrefs.getString(Constants.PREF_PHONE, "");
		nameET.setText(name);
		phoneET.setText(phone);
	}
	
	private void setPref()
	{
		name = nameET.getText().toString().trim();
		phone = phoneET.getText().toString().trim();
		mUserPrefs.edit().putString(Constants.PREF_NAME, name).commit();
		mUserPrefs.edit().putString(Constants.PREF_PHONE, phone).commit();
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
