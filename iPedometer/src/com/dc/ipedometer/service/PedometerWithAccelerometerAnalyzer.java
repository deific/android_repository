package com.dc.ipedometer.service;

import android.content.SharedPreferences;
import android.hardware.SensorEvent;
import android.util.Log;

/**
 * 使用加速计传感器分析计步数据
 * @author chensga
 *
 */
public class PedometerWithAccelerometerAnalyzer implements IStepAnalyzer {
	private String tag = "PedometerWithAccelerometerAnalyzer";
	private float lastValue = 0f;
	private int flag = 1;
	private long lastTime = 0l;
	private static IStepAnalyzer analyzer;
	private SharedPreferences sp;
	// 波动精度
	private float waveScale = 3.2256f;
	private long timeScale = 556l;
	private int jumpCount = 5;
	
	public static IStepAnalyzer getInstance() {
		if (analyzer == null) {
			analyzer = new PedometerWithAccelerometerAnalyzer();
		}
		return analyzer;
	}
	private PedometerWithAccelerometerAnalyzer() {
		
	}
	@Override
	public float analyze(SensorEvent event) {
		
		// 前几次检测不算
		if (jumpCount > 0) {
			jumpCount --;
			return 0;
		}
		
		float[] values = event.values;
		// 计算加速度值  通常在9.8左右
		double value = Math.sqrt(((values[0]*values[0]) + (values[1]*values[1]) + (values[2]*values[2])));
		long timeBlank = event.timestamp - lastTime;
		Log.i(tag, "加速计传感器数据分析，本次计算数据为：" + value);
		
		double tmpcal = lastValue - value;
		int tmpFlag = 0;
		if (tmpcal > 0) {
			tmpFlag = 1;
		} else {
			tmpFlag = -1;
		}
		if (timeBlank > timeScale && ((flag + tmpFlag) == 0) && Math.abs(tmpcal) > waveScale) {
			lastValue = (float)value;
			flag = tmpFlag;
			lastTime = event.timestamp;
			return lastValue;
		} else {
			return 0;
		}
		
	}
	@Override
	public void setSharedPreferences(SharedPreferences sp) {
		this.sp = sp;
	}
}
