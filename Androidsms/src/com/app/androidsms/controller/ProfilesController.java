package com.app.androidsms.controller;

import android.content.Context;
import android.media.AudioManager;
import android.os.Vibrator;
import android.util.Log;

/**
 * 控制情景模式
 * @author luo-PC
 *
 */
public class ProfilesController {
	private static String TAG = ProfilesController.class.getSimpleName();
	private Context mContext;
	private Vibrator vibrator;
	private AudioManager mAudioManager;
	private static ProfilesController sInstance;
	long []vibratorPattern = {1000,1000}; //stop start stop start ms毫秒为单位的停止和开始震动

	private int originVolume=-1, originMode;
	
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
	 * 获取当前情景模式
	 */
	public void getInitProfile(){
		originVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
		originMode = mAudioManager.getRingerMode();
	}
	
	/**
	 * 恢复情景模式
	 */
	public void resetProfile(){
		if( originVolume==-1) return;
		
		Log.i(TAG, "reset profile when ending call");
		stopVibrator();
		setVolume(originVolume);
		mAudioManager.setRingerMode(originMode);
	}
	
	/**
	 * 声音+震动
	 */
    public void RingAndVibrate() {
    	Log.i(TAG, "change profile when coming call");
    	setVolume( mAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING) );
    	mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
    	startVibrator();
    }
	
	/**
	 * 设置音量大小
	 */
	private void setVolume(int index){
		//streamType铃声类型, index音量大小, flags
		Log.i(TAG, "volume is "+index);
		mAudioManager.setStreamVolume(AudioManager.STREAM_RING, index, 0);
	}
	
	/**
	 * 开始震动
	 * from index 0 to repeat the pattern
	 */
	private void startVibrator(){
		vibrator.vibrate(vibratorPattern, 0);  
	}
	
	/**
	 * 取消震动
	 */
	private void stopVibrator(){
		vibrator.cancel();
	}
	
}
