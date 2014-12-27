package com.app.androidsms.controller;

import android.content.Context;
import android.media.AudioManager;
import android.os.Vibrator;

/**
 * �����龰ģʽ
 * @author luo-PC
 *
 */
public class ProfilesController {
	private static String TAG = ProfilesController.class.getSimpleName();
	private static ProfilesController sInstance;
	private Context mContext;
	private AudioManager mAudioManager;
	private Vibrator vibrator;
	long []vibratorPattern = {1000,1000}; //stop start stop start ms����Ϊ��λ��ֹͣ�Ϳ�ʼ��

	public static synchronized ProfilesController get(Context cxt) {
        if (sInstance == null) 
            sInstance = new ProfilesController(cxt);
        return sInstance;
    }
	
	private ProfilesController(Context cxt){
		this.mContext = cxt; 
		mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
		vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
	}
	
	/**
	 * ����������С
	 */
	public void setVolume(int index)
	{
		//streamType��������, index������С, flags
		mAudioManager.setStreamVolume(AudioManager.STREAM_RING, index, 0);
	}
	
	/**
	 * ��ʼ��
	 * from index 0 to repeat the pattern
	 */
	public void startVibrator()
	{
		vibrator.vibrate(vibratorPattern, 0);  
	}
	
	/**
	 * ȡ����
	 */
	public void stopVibrator()
	{
		vibrator.cancel();
	}
	
}
