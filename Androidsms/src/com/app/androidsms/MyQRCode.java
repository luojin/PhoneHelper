package com.app.androidsms;

import com.app.androidsms.util.Constants;
import com.app.androidsms.util.UserInfoPref;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MyQRCode extends ActionBarActivity{
	private final static String TAG = MyQRCode.class.getSimpleName();
	private ImageView myqrcode;
	private TextView scan_hint;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_myqrcode);
		
		ActionBar actionBar = getSupportActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    actionBar.setTitle(R.string.my_qrcode_title);
		
		myqrcode = (ImageView) findViewById(R.id.myqrcode);
		scan_hint = (TextView) findViewById(R.id.hint);
		
		new GetQRCodeTask().execute();
	}
	
	private class GetQRCodeTask extends AsyncTask<Object, Object, Bitmap>
	{
		@Override
		protected Bitmap doInBackground(Object... params) {
			// TODO Auto-generated method stub
			Bitmap bm = null;
			String name = UserInfoPref.get(getApplicationContext()).getString(Constants.PREF_NAME);
			String phone = UserInfoPref.get(getApplicationContext()).getString(Constants.PREF_PHONE);
			
			try {
				if( name.length()!=0 && phone.length()!=0)
				{
					Log.i(TAG, "name = "+name+" phone= "+phone);
					bm = Create2DCode(name + Constants.STRING_DIVIDER + phone);
				}
				
			} catch (WriterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return bm;
		}
		
		@Override
		protected void onPostExecute(Bitmap result) {
			if( result!=null)
			{
				scan_hint.setText( getResources().getString(R.string.scan_hint));
				myqrcode.setImageBitmap(result);
			}
			else
			{
				scan_hint.setText( getResources().getString(R.string.scan_notset));
				myqrcode.setImageBitmap(null);
			}
		}
		
	}
	
	/**
	 * 用字符串生成二维码
	 * @param str
	 * @return
	 * @throws WriterException
	 */
	public Bitmap Create2DCode(String str) throws WriterException {
		//生成二维矩阵,编码时指定大小,不要生成了图片以后再进行缩放,这样会模糊导致识别失败
		float density = getResources().getDisplayMetrics().density;
		int codeWidth = (int) (density * 300);
		BitMatrix matrix = new MultiFormatWriter().encode(str,BarcodeFormat.QR_CODE, codeWidth, codeWidth);
		int width = matrix.getWidth();
		int height = matrix.getHeight();
		//二维矩阵转为一维像素数组,也就是一直横着排了
		int[] pixels = new int[width * height];
		for (int y = 0; y < height; y++) 
			for (int x = 0; x < width; x++) 
				if(matrix.get(x, y))
					pixels[y * width + x] = 0xff000000;
		
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		//通过像素数组生成bitmap,具体参考api
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		finish();
		overridePendingTransition(R.anim.zoom_in,
			R.anim.slide_right_out);
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
		}
		return super.onOptionsItemSelected(item);
	}
}
