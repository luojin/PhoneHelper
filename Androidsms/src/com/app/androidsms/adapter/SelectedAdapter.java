package com.app.androidsms.adapter;

import java.util.List;
import com.app.androidsms.R;
import com.app.androidsms.util.NameNumberPair;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * 在通讯录中已经选择的用户
 * @author luo-PC
 *
 */
public class SelectedAdapter extends BaseAdapter{
	private Context mContext;
	private List<NameNumberPair> mList;
	
	public SelectedAdapter(Context cxt, List<NameNumberPair> list){
		mContext = cxt;
		mList = list;
	}
	
	/**
	 * 当ListView数据发生变化时,调用此方法来更新ListView
	 * @param list
	 */
	public void updateListView(List<NameNumberPair> list){
		mList = list;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		NameNumberPair item = mList.get(position);
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.contact_selected_item, null);
			viewHolder.tvName = (TextView) convertView.findViewById(R.id.name);
			viewHolder.tvNumber = (TextView) convertView.findViewById(R.id.phonenumber);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		viewHolder.tvName.setText(item.getName());
		viewHolder.tvNumber.setText(item.getNumber());
		
		return convertView;
	}
	
	final static class ViewHolder {
		TextView tvName;
		TextView tvNumber;
	}

}
