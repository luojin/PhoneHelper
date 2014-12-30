package com.app.androidsms;

import java.util.ArrayList;
import java.util.List;

import com.app.androidsms.adapter.SelectedAdapter;
import com.app.androidsms.util.Constants;
import com.app.androidsms.util.NameNumberPair;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

public class MultiMessage extends ActionBarActivity{
	
	private List<NameNumberPair> mNameNumberPair;
	private int []selected_contactId_list = null;
	private ListView selectedLV;
	private SelectedAdapter mSelectedAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_multi_msg);
		
		ActionBar actionBar = getSupportActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    actionBar.setTitle(R.string.multi_send_msg);
	    
	    selectedLV = (ListView) findViewById(R.id.selected_contact_lv);
	    mNameNumberPair = new ArrayList<NameNumberPair>();
	    mSelectedAdapter = new SelectedAdapter(getApplicationContext(), mNameNumberPair);
	    selectedLV.setAdapter(mSelectedAdapter);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.multi_msg, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch( item.getItemId() ){
		case android.R.id.home: 
				onBackPressed();
			break;
		case R.id.choose_to_send:
			Intent intent = new Intent(MultiMessage.this, ChooseContacts.class);
			intent.putExtra("selected_contactId_list", selected_contactId_list);
			startActivityForResult(intent, Constants.INTENT_GET_CONTACT);
			overridePendingTransition(
					R.anim.slide_right_in, R.anim.zoom_out);
			break;
		case R.id.multi_send:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if( data==null) return;
		
		if( requestCode==Constants.INTENT_GET_CONTACT){
			String [] selected_name_list = data.getStringArrayExtra( "selected_name_list");
			String [] selected_number_list = data.getStringArrayExtra( "selected_number_list");
			selected_contactId_list = data.getIntArrayExtra("selected_contactId_list");
			mNameNumberPair = new ArrayList<NameNumberPair>();
			
			if( selected_name_list==null || selected_number_list==null || selected_contactId_list==null) {
				mSelectedAdapter.updateListView(mNameNumberPair);
				return;
			}
			
			for( int k=0; k<selected_name_list.length; k++)
			{
				NameNumberPair item = new NameNumberPair(selected_name_list[k], selected_number_list[k]);
				mNameNumberPair.add(item);
			}
			mSelectedAdapter.updateListView(mNameNumberPair);
		}
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		finish();
		overridePendingTransition(R.anim.zoom_in,
			R.anim.slide_right_out);
	}

}
