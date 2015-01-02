package com.app.androidsms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.app.androidsms.adapter.SortAdapter;
import com.app.androidsms.controller.ContactsController;
import com.app.androidsms.custom.widgets.SideBar;
import com.app.androidsms.custom.widgets.SideBar.OnTouchingLetterChangedListener;
import com.app.androidsms.util.CharacterParser;
import com.app.androidsms.util.Constants;
import com.app.androidsms.util.ContactBean;
import com.app.androidsms.util.PinyinComparator;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Ⱥ������ǰ��ѡ����ϵ��
 * @author luo-PC
 *
 */
public class ChooseContacts extends ActionBarActivity{
	/**
	 * variables
	 */
	private ListView sortListView;
	private SideBar sideBar;
	private TextView dialog, hint;
	private SortAdapter adapter;
	private int counter = 0;
	private Map<Integer, ContactBean> contactIdMap = null;
	private List<ContactBean> list;
	private int [] old_contact_id_list = null;
	private boolean select_all = false;
	private List<ContactBean> SourceDateList;
	
	/**
	 * ����ת����ƴ������
	 */
	private CharacterParser characterParser;
	
	/**
	 * ����ƴ��������ListView�����������
	 */
	private PinyinComparator pinyinComparator;

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
	}

	private void initViews() {
		characterParser = CharacterParser.getInstance();
		pinyinComparator = new PinyinComparator();
		
		sideBar = (SideBar) findViewById(R.id.sidrbar);
		dialog = (TextView) findViewById(R.id.dialog);
		hint = (TextView) findViewById(R.id.hint);
		
		sideBar.setTextView(dialog);
		hint.setText("��ѡ����: "+counter+"��");
		
		//�����Ҳ���ĸ��������
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
				if( item.isCheck() ){
					counter--;
					SourceDateList.get(position).setCheck( false);
				}
				else{
					counter++;
					SourceDateList.get(position).setCheck( true);
				}
				updateListView(counter);
			}
		});
		
		SourceDateList = new ArrayList<ContactBean>();
		adapter = new SortAdapter(this, SourceDateList);
		sortListView.setAdapter(adapter);
		
		/**
		 * ���һ���ص�����
		 * ��ѯͨѶ¼������ص�
		 */
		ContactsController.get(getApplicationContext()).addQueryContactFinish(new ContactsController.QueryContactFinish() {
			
			@Override
			public void onQueryContactFinish() {
				QueryContactFinish();
			}
		});
		QueryContactFinish();
	}
	
	/**
	 * ��ʾ���ݵ��б���
	 */
	private void QueryContactFinish()
	{
		contactIdMap = ContactsController.get(getApplicationContext()).getContactMap();
		if( contactIdMap==null ) {
			return;
		}
		
		list = getContactBeanList();
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
			updateListView(counter);
		}
	}
	
	/**
	 * ��ȡͨѶ¼�б�
	 * @return
	 */
	public List<ContactBean> getContactBeanList()
	{
		if( contactIdMap==null) return null;
		
		List<ContactBean> contactBeanList = new ArrayList<ContactBean>();
		for (Integer key : contactIdMap.keySet()) 
			contactBeanList.add( contactIdMap.get(key));
		
		return contactBeanList;
	}

	/**
	 * ΪListView�����������
	 * @param ԭʼͨѶ¼
	 * @return ������ͨѶ¼
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
			onBackPressed();
			break;
		case R.id.select_all:
			selected_all();
			break;
		case R.id.confirm:
			onChoose();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * ȫѡ����ȫ��ѡ
	 */
	private void selected_all()
	{
		for (int k=0; k<SourceDateList.size(); k++) 
			SourceDateList.get(k).setCheck( !select_all);
		
		if( !select_all)
			counter = SourceDateList.size();
		else
			counter = 0;
		
		updateListView(counter);
		select_all = !select_all;
	}
	
	/**
	 * ˢ���б�
	 * @param counter
	 */
	private void updateListView(int counter)
	{
		hint.setText("��ѡ����: "+counter+"��");
		adapter.updateListView(SourceDateList);
	}
	
	/**
	 * ����ص�����
	 */
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
	
	/**
	 * �˳�activity
	 * @param selected_name_list
	 * @param selected_number_list
	 * @param selected_contactId_list
	 */
	public void exit(String []selected_name_list, String []selected_number_list, int []selected_contactId_list)
	{
		Intent intent = new Intent();
		intent.putExtra("selected_name_list",  selected_name_list);
		intent.putExtra("selected_number_list",  selected_number_list);
		intent.putExtra("selected_contactId_list", selected_contactId_list);
		setResult(Constants.INTENT_GET_CONTACT, intent);
		
		onBackPressed();
	}
	
	@Override
	public void onBackPressed() {
		/**
		 * ɾ��һ��QueryContactFinish�ص�
		 */
		ContactsController.get(getApplicationContext()).removeQueryContactFinish();
		
		finish();
		overridePendingTransition(R.anim.zoom_in,
			R.anim.slide_right_out);
	}
}
