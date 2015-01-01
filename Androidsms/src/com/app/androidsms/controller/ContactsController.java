package com.app.androidsms.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.app.androidsms.util.ContactBean;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

/**
 * manage contacts
 * @author luo-PC
 *
 */
public class ContactsController {
	private static String TAG = ContactsController.class.getSimpleName();
	private static ContactsController sInstance;
	private Context mContext;
	private Map<Integer, ContactBean> contactIdMap = null;
	private ContactQueryHandler mContactQueryHandler;
	private List<QueryContactFinish> list = new ArrayList<ContactsController.QueryContactFinish>();
	
	public static synchronized ContactsController get(Context cxt) {
        if (sInstance == null) 
            sInstance = new ContactsController(cxt);
        return sInstance;
    }
	
	private ContactsController(Context cxt){
		this.mContext = cxt; 
		mContactQueryHandler = new ContactQueryHandler(mContext.getContentResolver());
		beginQuery();
	}
	
	/**
	 * 初始化数据库查询参数
	 * 查询通讯录数据库
	 */
	private void beginQuery() {
		Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI; // 联系人Uri；
		// 查询的字段
		String[] projection = { ContactsContract.CommonDataKinds.Phone._ID,
				ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
				ContactsContract.CommonDataKinds.Phone.DATA1, "sort_key",
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
				ContactsContract.CommonDataKinds.Phone.PHOTO_ID,
				ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY };
		// 按照sort_key升序查詢
		mContactQueryHandler.startQuery(0, null, uri, projection, null, null,
				"sort_key COLLATE LOCALIZED asc");
	}
	
	/**
	 * 获取通讯录列表
	 * @return Map<Integer, ContactBean>
	 */
	public Map<Integer, ContactBean> getContactMap()
	{
		resetCheck();
		return contactIdMap;
	}
	
	/**
	 * reset checked to false
	 */
	private void resetCheck()
	{
		if( contactIdMap==null || contactIdMap.size()==0) return;
		
		for (Integer key : contactIdMap.keySet()) 
			contactIdMap.get(key).setCheck(false);
	}
	
	/**
	 * 获取通讯录- “电话号码：名字” -列表
	 * @return Map<String, String>
	 */
	public Map<String, String> getNumberNameMap()
	{
		if( contactIdMap==null) return null;
		
		Map<String, String> NumberNameMap = new HashMap<String, String>();
		for (Integer key : contactIdMap.keySet()) 
			NumberNameMap.put(contactIdMap.get(key).getPhoneNum(), contactIdMap.get(key).getDisplayName());
		
		return NumberNameMap;
	}
	
	/**
	 * 查询通讯录结束回调函数
	 */
	private class ContactQueryHandler extends AsyncQueryHandler {
		public ContactQueryHandler(ContentResolver cr) {
			super(cr);
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			if (cursor != null && cursor.getCount() > 0) {
				contactIdMap = new HashMap<Integer, ContactBean>();
				
				cursor.moveToFirst(); // 游标移动到第一项
				for (int i = 0; i < cursor.getCount(); i++) {
					cursor.moveToPosition(i);
					String name = cursor.getString(1);
					String number = cursor.getString(2).replace(" ", "");
					String sortKey = cursor.getString(3);
					int contactId = cursor.getInt(4);
					Long photoId = cursor.getLong(5);
					String lookUpKey = cursor.getString(6);

					if (contactIdMap.containsKey(contactId)) {
						// 无操作
					} else {
						// 创建联系人对象
						ContactBean contact = new ContactBean();
						contact.setDisplayName(name);
						contact.setPhoneNum(number);
						contact.setSortKey(sortKey);
						contact.setPhotoId(photoId);
						contact.setLookUpKey(lookUpKey);
						contact.setContactId(contactId);
						contact.setCheck(false);
						
						contactIdMap.put(contactId, contact);
					}
				}
				
				onQueryDone();
			}

			super.onQueryComplete(token, cookie, cursor);
		}
	}
	
	/**
	 * 查询完毕回调
	 */
	private void onQueryDone()
	{
		for (QueryContactFinish item : list) 
			item.onQueryContactFinish();
	}
	
	/**
	 * 删除最后加入的QueryContactFinish回调监听器
	 */
	public void removeQueryContactFinish()
	{
		if( list==null || list.size()==0 ) return;
		
		list.remove(list.size()-1);
		Log.e(TAG, "remove query contact finish " + list.size());
	}
	
	/**
	 * 增加一个QueryContactFinish回调监听器
	 * @param item
	 */
	public void addQueryContactFinish(QueryContactFinish item)
	{
		list.add(item);
		Log.e(TAG, "add query contact finish");
	}
	
	/**
	 * 查询结束回掉interface
	 * @author luo-PC
	 *
	 */
	public interface QueryContactFinish{
		public void onQueryContactFinish();
	}

}
