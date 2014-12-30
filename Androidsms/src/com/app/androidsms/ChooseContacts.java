package com.app.androidsms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.app.androidsms.adapter.SortAdapter;
import com.app.androidsms.custom.widgets.SideBar;
import com.app.androidsms.custom.widgets.SideBar.OnTouchingLetterChangedListener;
import com.app.androidsms.util.CharacterParser;
import com.app.androidsms.util.Constants;
import com.app.androidsms.util.ContactBean;
import com.app.androidsms.util.PinyinComparator;

import android.R.integer;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ChooseContacts extends ActionBarActivity{
	private ListView sortListView;
	private SideBar sideBar;
	private TextView dialog, hint;
	private SortAdapter adapter;
	private int counter = 0;
	
	/**
	 * ����ת����ƴ������
	 */
	private CharacterParser characterParser;
	private List<ContactBean> SourceDateList;
	
	/**
	 * ����ƴ��������ListView�����������
	 */
	private PinyinComparator pinyinComparator;
	
	private Map<Integer, ContactBean> contactIdMap = null;
	private AsyncQueryHandler asyncQueryHandler; // �첽��ѯ���ݿ������
	private List<ContactBean> list;
	private int [] old_contact_id_list = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choose_contacts);
		
		ActionBar actionBar = getSupportActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    actionBar.setTitle(R.string.choose_to_send);
	    
	    old_contact_id_list = getIntent().getIntArrayExtra("selected_contactId_list");
	    if( old_contact_id_list!=null)
	    	counter = old_contact_id_list.length;
	    
		initViews();
		asyncQueryHandler = new MyAsyncQueryHandler(getContentResolver());
		init();
	}

	private void initViews() {
		//ʵ��������תƴ����
		characterParser = CharacterParser.getInstance();
		
		pinyinComparator = new PinyinComparator();
		
		sideBar = (SideBar) findViewById(R.id.sidrbar);
		dialog = (TextView) findViewById(R.id.dialog);
		sideBar.setTextView(dialog);
		hint = (TextView) findViewById(R.id.hint);
		hint.setText("��ѡ����: "+counter+"��");
		
		//�����Ҳഥ������
		sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {
			@Override
			public void onTouchingLetterChanged(String s) {
				//����ĸ�״γ��ֵ�λ��
				int position = adapter.getPositionForSection(s.charAt(0));
				if(position != -1){
					sortListView.setSelection(position);
				}
			}
		});
		
		sortListView = (ListView) findViewById(R.id.country_lvcountry);
		sortListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				//����Ҫ����adapter.getItem(position)����ȡ��ǰposition����Ӧ�Ķ���
				ContactBean item = SourceDateList.get(position);
				if( item.isCheck() )
				{
					counter--;
					SourceDateList.get(position).setCheck( false);
				}
				else
				{
					counter++;
					SourceDateList.get(position).setCheck( true);
				}
				hint.setText("��ѡ����: "+counter+"��");
				adapter.updateListView(SourceDateList);
			}
		});
		
		SourceDateList = new ArrayList<ContactBean>();
		// ����a-z��������Դ����
		Collections.sort(SourceDateList, pinyinComparator);
		adapter = new SortAdapter(this, SourceDateList);
		sortListView.setAdapter(adapter);
	}

	/**
	 * ΪListView�������
	 * @param date
	 * @return
	 */
	private List<ContactBean> filledData(List<ContactBean> date){
		List<ContactBean> mSortList = new ArrayList<ContactBean>();
		
		for(int i=0; i<date.size(); i++){
			ContactBean sortModel = date.get(i);
			//����ת����ƴ��
			String pinyin = characterParser.getSelling(date.get(i).getDisplayName());
			String sortString = pinyin.substring(0, 1).toUpperCase();
			
			// ������ʽ���ж�����ĸ�Ƿ���Ӣ����ĸ
			if(sortString.matches("[A-Z]")){
				sortModel.setSortLetters(sortString.toUpperCase());
			}else{
				sortModel.setSortLetters("#");
			}
			
			mSortList.add(sortModel);
		}
		return mSortList;
	}
	
	/**
	 * ����������е�ֵ���������ݲ�����ListView
	 * @param filterStr
	 */
	private void filterData(String filterStr){
		List<ContactBean> filterDateList = new ArrayList<ContactBean>();
		
		if(TextUtils.isEmpty(filterStr)){
			filterDateList = SourceDateList;
		}else{
			filterDateList.clear();
			for(ContactBean sortModel : SourceDateList){
				String name = sortModel.getDisplayName();
				if(name.indexOf(filterStr.toString()) != -1 || characterParser.getSelling(name).startsWith(filterStr.toString())){
					filterDateList.add(sortModel);
				}
			}
		}
		// ����a-z��������
		Collections.sort(filterDateList, pinyinComparator);
		adapter.updateListView(filterDateList);
	}
	
	/**
	 * ��ʼ�����ݿ��ѯ����
	 */
	private void init() {
		Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI; // ��ϵ��Uri��
		// ��ѯ���ֶ�
		String[] projection = { ContactsContract.CommonDataKinds.Phone._ID,
				ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
				ContactsContract.CommonDataKinds.Phone.DATA1, "sort_key",
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
				ContactsContract.CommonDataKinds.Phone.PHOTO_ID,
				ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY };
		// ����sort_key�����ԃ
		asyncQueryHandler.startQuery(0, null, uri, projection, null, null,
				"sort_key COLLATE LOCALIZED asc");
	}
	
	/**
	 * ��ѯͨѶ¼�����ص�����
	 */
	private class MyAsyncQueryHandler extends AsyncQueryHandler {
		public MyAsyncQueryHandler(ContentResolver cr) {
			super(cr);
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			if (cursor != null && cursor.getCount() > 0) {
				contactIdMap = new HashMap<Integer, ContactBean>();
				list = new ArrayList<ContactBean>();
				cursor.moveToFirst(); // �α��ƶ�����һ��
				for (int i = 0; i < cursor.getCount(); i++) {
					cursor.moveToPosition(i);
					String name = cursor.getString(1);
					String number = cursor.getString(2);
					String sortKey = cursor.getString(3);
					int contactId = cursor.getInt(4);
					Long photoId = cursor.getLong(5);
					String lookUpKey = cursor.getString(6);

					if (contactIdMap.containsKey(contactId)) {
						// �޲���
					} else {
						// ������ϵ�˶���
						ContactBean contact = new ContactBean();
						contact.setDisplayName(name);
						contact.setPhoneNum(number);
						contact.setSortKey(sortKey);
						contact.setPhotoId(photoId);
						contact.setLookUpKey(lookUpKey);
						contact.setContactId(contactId);
						contact.setCheck(false);
						list.add( contact);

						contactIdMap.put(contactId, contact);
					}
				}
				if (list.size() > 0) {
					if( old_contact_id_list!=null){
						for(int m=0; m<old_contact_id_list.length; m++){
							int item = old_contact_id_list[m];
							if( contactIdMap.containsKey(item) )
								contactIdMap.get(item).setCheck(true);
						}
					}
					
					SourceDateList = filledData( list);
					// ����a-z��������
					Collections.sort(SourceDateList, pinyinComparator);
					adapter.updateListView(SourceDateList);
				}
			}

			super.onQueryComplete(token, cookie, cursor);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.choose_contact, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch( item.getItemId() ){
		case android.R.id.home: 
			exit(null,null,null);
			break;
		case R.id.cancel:
			exit(null,null,null);
			break;
		case R.id.confirm:
			onChoose();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void onChoose()
	{
		List<String> selected_number_list = new ArrayList<String>();
		List<String> selected_name_list = new ArrayList<String>();
		List<Integer> selected_contactId_list = new ArrayList<Integer>();
		
		for (ContactBean item : SourceDateList) {
			if( item.isCheck()){
				selected_name_list.add(  item.getDisplayName()); 
				selected_number_list.add( item.getPhoneNum());
				selected_contactId_list.add( item.getContactId() );
			}
		}
		
		String []selected_name_list_r    = new String[selected_name_list.size()];
		String []selected_number_list_r = new String[selected_number_list.size()];
		int []selected_contactId_list_r = new int[selected_contactId_list.size()];
		for( int k=0; k<selected_name_list.size(); k++)
		{
			selected_name_list_r[k]    = selected_name_list.get(k);
			selected_number_list_r[k] = selected_number_list.get(k);
			selected_contactId_list_r[k] = selected_contactId_list.get(k);
		}
		
		exit(selected_name_list_r,selected_number_list_r, selected_contactId_list_r);
	}
	
	public void exit(String []selected_name_list, String []selected_number_list, int []selected_contactId_list)
	{
		Intent intent = new Intent();
		intent.putExtra("selected_name_list",  selected_name_list);
		intent.putExtra("selected_number_list",  selected_number_list);
		intent.putExtra("selected_contactId_list", selected_contactId_list);
		setResult(Constants.INTENT_GET_CONTACT, intent);
		
		finish();
		overridePendingTransition(R.anim.zoom_in,
			R.anim.slide_right_out);
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		exit(null,null,null);
	}
}
