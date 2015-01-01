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
	 * ��ʼ�����ݿ��ѯ����
	 * ��ѯͨѶ¼���ݿ�
	 */
	private void beginQuery() {
		Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI; // ��ϵ��Uri��
		// ��ѯ���ֶ�
		String[] projection = { ContactsContract.CommonDataKinds.Phone._ID,
				ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
				ContactsContract.CommonDataKinds.Phone.DATA1, "sort_key",
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
				ContactsContract.CommonDataKinds.Phone.PHOTO_ID,
				ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY };
		// ����sort_key�����ԃ
		mContactQueryHandler.startQuery(0, null, uri, projection, null, null,
				"sort_key COLLATE LOCALIZED asc");
	}
	
	/**
	 * ��ȡͨѶ¼�б�
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
	 * ��ȡͨѶ¼- ���绰���룺���֡� -�б�
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
	 * ��ѯͨѶ¼�����ص�����
	 */
	private class ContactQueryHandler extends AsyncQueryHandler {
		public ContactQueryHandler(ContentResolver cr) {
			super(cr);
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			if (cursor != null && cursor.getCount() > 0) {
				contactIdMap = new HashMap<Integer, ContactBean>();
				
				cursor.moveToFirst(); // �α��ƶ�����һ��
				for (int i = 0; i < cursor.getCount(); i++) {
					cursor.moveToPosition(i);
					String name = cursor.getString(1);
					String number = cursor.getString(2).replace(" ", "");
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
						
						contactIdMap.put(contactId, contact);
					}
				}
				
				onQueryDone();
			}

			super.onQueryComplete(token, cookie, cursor);
		}
	}
	
	/**
	 * ��ѯ��ϻص�
	 */
	private void onQueryDone()
	{
		for (QueryContactFinish item : list) 
			item.onQueryContactFinish();
	}
	
	/**
	 * ɾ���������QueryContactFinish�ص�������
	 */
	public void removeQueryContactFinish()
	{
		if( list==null || list.size()==0 ) return;
		
		list.remove(list.size()-1);
		Log.e(TAG, "remove query contact finish " + list.size());
	}
	
	/**
	 * ����һ��QueryContactFinish�ص�������
	 * @param item
	 */
	public void addQueryContactFinish(QueryContactFinish item)
	{
		list.add(item);
		Log.e(TAG, "add query contact finish");
	}
	
	/**
	 * ��ѯ�����ص�interface
	 * @author luo-PC
	 *
	 */
	public interface QueryContactFinish{
		public void onQueryContactFinish();
	}

}
