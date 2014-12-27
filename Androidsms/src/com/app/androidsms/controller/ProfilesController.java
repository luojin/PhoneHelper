package com.app.androidsms.controller;

import android.content.Context;
import android.media.AudioManager;
import android.os.Vibrator;

/**
 * 控制情景模式
 * @author luo-PC
 *
 */
public class ProfilesController {
	private static String TAG = ProfilesController.class.getSimpleName();
	private static ProfilesController sInstance;
	private Context mContext;
	private AudioManager mAudioManager;
	private Vibrator vibrator;
	long []vibratorPattern = {1000,1000}; //stop start stop start ms毫秒为单位的停止和开始震动

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
	 * 设置音量大小
	 */
	public void setVolume(int index)
	{
		//streamType铃声类型, index音量大小, flags
		mAudioManager.setStreamVolume(AudioManager.STREAM_RING, index, 0);
	}
	
	/**
	 * 开始震动
	 * from index 0 to repeat the pattern
	 */
	public void startVibrator()
	{
		vibrator.vibrate(vibratorPattern, 0);  
	}
	
	/**
	 * 取消震动
	 */
	public void stopVibrator()
	{
		vibrator.cancel();
	}
	
}
