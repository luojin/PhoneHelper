package com.app.androidsms;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.ImageView;

public class MyQRCode extends ActionBarActivity{
	private ImageView myqrcode;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_myqrcode);
		
		myqrcode = (ImageView) findViewById(R.id.myqrcode);
		
		new GetQRCodeTask().execute();
	}
	
	private class GetQRCodeTask extends AsyncTask<Object, Object, Bitmap>
	{
		@Override
		protected Bitmap doInBackground(Object... params) {
			// TODO Auto-generated method stub
			Bitmap bm = null;
			try {
				 bm = Create2DCode("15902090538");
			} catch (WriterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return bm;
		}
		
		@Override
		protected void onPostExecute(Bitmap result) {
			if( result!=null)
				myqrcode.setImageBitmap(result);
		}
		
	}
	
	/**
	 * ���ַ������ɶ�ά��
	 * @param str
	 * @return
	 * @throws WriterException
	 */
	public Bitmap Create2DCode(String str) throws WriterException {
		//���ɶ�ά����,����ʱָ����С,��Ҫ������ͼƬ�Ժ��ٽ�������,������ģ������ʶ��ʧ��
		float density = getResources().getDisplayMetrics().density;
		int codeWidth = (int) (density * 300);
		BitMatrix matrix = new MultiFormatWriter().encode(str,BarcodeFormat.QR_CODE, codeWidth, codeWidth);
		int width = matrix.getWidth();
		int height = matrix.getHeight();
		//��ά����תΪһά��������,Ҳ����һֱ��������
		int[] pixels = new int[width * height];
		for (int y = 0; y < height; y++) 
			for (int x = 0; x < width; x++) 
				if(matrix.get(x, y))
					pixels[y * width + x] = 0xff000000;
		
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		//ͨ��������������bitmap,����ο�api
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		finish();
		overridePendingTransition(R.anim.zoom_in,
			R.anim.slide_right_out);
	}
}
