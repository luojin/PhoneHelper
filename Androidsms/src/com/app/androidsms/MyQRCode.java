package com.app.androidsms;

import java.util.Hashtable;

import com.app.androidsms.util.Constants;
import com.app.androidsms.util.ImageUtil;
import com.app.androidsms.util.UserInfoPref;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 我的二维码
 * @author luo-PC
 *
 */
public class MyQRCode extends ActionBarActivity{
	private final static String TAG = MyQRCode.class.getSimpleName();
	private ImageView myqrcode;
	private TextView scan_hint;
	private Bitmap     myqrcodeBM;
	
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
				if( name.length()!=0 && phone.length()!=0){
					Log.i(TAG, "name = "+name+" phone= "+phone);
					bm = Create2DCode(name + Constants.STRING_DIVIDER + phone);
				}
			} catch (WriterException e) {
				e.printStackTrace();
			}
			return bm;
		}
		
		@Override
		protected void onPostExecute(Bitmap result) {
			if( result!=null){
				myqrcodeBM = result;
				scan_hint.setText( getResources().getString(R.string.scan_hint));
				myqrcode.setImageBitmap(result);
			}else{
				scan_hint.setText( getResources().getString(R.string.scan_notset));
				myqrcode.setImageBitmap(null);
				myqrcodeBM = null;
			}
		}
	}
	
	/**
	 * 用字符串生成二维码
	 * @param str
	 * @return Bitmap
	 * @throws WriterException
	 */
	public Bitmap Create2DCode(String str) throws WriterException {
		//生成二维矩阵,编码时指定大小,不要生成了图片以后再进行缩放,这样会模糊导致识别失败
		Hashtable<EncodeHintType,String> hints = new Hashtable<EncodeHintType,String>();
		//设置编码，避免中文乱码
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        
		float density = getResources().getDisplayMetrics().density;
		int codeWidth = (int) (density * 300);
		BitMatrix matrix = new MultiFormatWriter().encode(str,BarcodeFormat.QR_CODE, codeWidth, codeWidth, hints);
		int width = matrix.getWidth();
		int height = matrix.getHeight();
		//二维矩阵转为一维像素数组,也就是一直横着排了
		int[] pixels = new int[width * height];
		for (int y = 0; y < height; y++) 
			for (int x = 0; x < width; x++) 
				if(matrix.get(x, y))
					pixels[y * width + x] = 0xff000000;
				else
					pixels[y * width + x] = 0xffffffff;
		
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		//通过像素数组生成bitmap,具体参考api
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}
	
	@Override
	public void onBackPressed() {
		finish();
		overridePendingTransition(R.anim.zoom_in,
			R.anim.slide_right_out);
	}
	
	/**
	 * save qr_code asyntask
	 * @author luo-PC
	 *
	 */
	private class SaveQRCodeTask extends AsyncTask<Object, Object, Boolean>
	{
		@Override
		protected Boolean doInBackground(Object... params) {
			if( myqrcodeBM==null)
				return false;
			
			String filepath = ImageUtil.saveImage(myqrcodeBM);
			if( filepath==null )
				return false;
			else
				ImageUtil.scanPhotos(getApplicationContext(), filepath);
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if( result.booleanValue()==true){
				Toast.makeText(getApplicationContext(), "已保存到相册", Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(getApplicationContext(), "保存失败", Toast.LENGTH_SHORT).show();
			}
		}
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.my_qrcode, menu);
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
		case R.id.qrcode_saveto_album:
			new SaveQRCodeTask().execute();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
}
