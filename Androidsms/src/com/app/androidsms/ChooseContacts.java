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
import com.app.androidsms.util.ContactBean;
import com.app.androidsms.util.PinyinComparator;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
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
	 * 汉字转换成拼音的类
	 */
	private CharacterParser characterParser;
	private List<ContactBean> SourceDateList;
	
	/**
	 * 根据拼音来排列ListView里面的数据类
	 */
	private PinyinComparator pinyinComparator;
	
	private Map<Integer, ContactBean> contactIdMap = null;
	private AsyncQueryHandler asyncQueryHandler; // 异步查询数据库类对象
	private List<ContactBean> list;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choose_contacts);
		initViews();
		
		// 实例化
		asyncQueryHandler = new MyAsyncQueryHandler(getContentResolver());
		init();
	}

	private void initViews() {
		//实例化汉字转拼音类
		characterParser = CharacterParser.getInstance();
		
		pinyinComparator = new PinyinComparator();
		
		sideBar = (SideBar) findViewById(R.id.sidrbar);
		dialog = (TextView) findViewById(R.id.dialog);
		sideBar.setTextView(dialog);
		hint = (TextView) findViewById(R.id.hint);
		hint.setText("共选择了: "+counter+"人");
		
		//设置右侧触摸监听
		sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {
			@Override
			public void onTouchingLetterChanged(String s) {
				//该字母首次出现的位置
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
				//这里要利用adapter.getItem(position)来获取当前position所对应的对象
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
				hint.setText("共选择了: "+counter+"人");
				adapter.updateListView(SourceDateList);
			}
		});
		
		SourceDateList = new ArrayList<ContactBean>();
		// 根据a-z进行排序源数据
		Collections.sort(SourceDateList, pinyinComparator);
		adapter = new SortAdapter(this, SourceDateList);
		sortListView.setAdapter(adapter);
	}

	/**
	 * 为ListView填充数据
	 * @param date
	 * @return
	 */
	private List<ContactBean> filledData(List<ContactBean> date){
		List<ContactBean> mSortList = new ArrayList<ContactBean>();
		
		for(int i=0; i<date.size(); i++){
			ContactBean sortModel = date.get(i);
			//汉字转换成拼音
			String pinyin = characterParser.getSelling(date.get(i).getDisplayName());
			String sortString = pinyin.substring(0, 1).toUpperCase();
			
			// 正则表达式，判断首字母是否是英文字母
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
	 * 根据输入框中的值来过滤数据并更新ListView
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
		// 根据a-z进行排序
		Collections.sort(filterDateList, pinyinComparator);
		adapter.updateListView(filterDateList);
	}
	
	/**
	 * 初始化数据库查询参数
	 */
	private void init() {
		Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI; // 联系人Uri；
		// 查询的字段
		String[] projection = { ContactsContract.CommonDataKinds.Phone._ID,
				ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
				ContactsContract.CommonDataKinds.Phone.DATA1, "sort_key",
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
				ContactsContract.CommonDataKinds.Phone.PHOTO_ID,
				ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY };
		// 按照sort_key升序查詢
		asyncQueryHandler.startQuery(0, null, uri, projection, null, null,
				"sort_key COLLATE LOCALIZED asc");
	}
	
	/**
	 * 查询通讯录结束回调函数
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
				cursor.moveToFirst(); // 游标移动到第一项
				for (int i = 0; i < cursor.getCount(); i++) {
					cursor.moveToPosition(i);
					String name = cursor.getString(1);
					String number = cursor.getString(2);
					String sortKey = cursor.getString(3);
					int contactId = cursor.getInt(4);
					Long photoId = cursor.getLong(5);
					String lookUpKey = cursor.getString(6);

					if (contactIdMap.containsKey(contactId)) {
						// 无操作
					} else {
						// 创建联系人对象
						ContactBean contact = new ContactBean();
						contact.setDesplayName(name);
						contact.setPhoneNum(number);
						contact.setSortKey(sortKey);
						contact.setPhotoId(photoId);
						contact.setLookUpKey(lookUpKey);
						contact.setCheck(false);
						list.add( contact);

						contactIdMap.put(contactId, contact);
					}
				}
				if (list.size() > 0) {
					SourceDateList = filledData( list);
					// 根据a-z进行排序
					Collections.sort(SourceDateList, pinyinComparator);
					adapter.updateListView(SourceDateList);
				}
			}

			super.onQueryComplete(token, cookie, cursor);
		}
	}
}
