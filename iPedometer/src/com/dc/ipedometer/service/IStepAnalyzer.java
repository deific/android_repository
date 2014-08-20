package com.dc.ipedometer.service;

import android.content.SharedPreferences;
import android.hardware.SensorEvent;

public interface IStepAnalyzer {
	
	public float analyze(SensorEvent event);
	public void setSharedPreferences(SharedPreferences sp);
}
