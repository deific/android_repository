package com.dc.ipedometer.service;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.dc.common.dbutil.DBUtil;

/**
 * 计步服务
 * @author chensga
 *
 */
public class PedometerService extends Service implements SensorEventListener {
	protected String tag = "PedometerService";
	/**
	 * 步数
	 */
	private int stepCount = 0;
	
	private SharedPreferences sp;
	
	/**
	 * 
	 */
	private PedometerBinder binder = new PedometerBinder();
	/**
	 * 传感器
	 */
	private SensorManager sensorMng;
	/**
	 * 数据分析器
	 */
	private IStepAnalyzer stepAnalyzer;
	/**
	 * 数据库工具
	 */
	private DBUtil dbUtil;
	/**
	 * 是否正计数
	 */
	public boolean isAnalyzing = false;
    
	@Override
	public IBinder onBind(Intent arg0) {
		Log.i(tag, "绑定服务！");
		// 注册监听
		sensorMng.registerListener(this, sensorMng.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
		return binder;
	}
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(tag, "启动服务！");
		// 启动传感器监听
        sensorMng = (SensorManager) this.getSystemService(SENSOR_SERVICE);

        sp = getSharedPreferences("ipedometer", MODE_WORLD_WRITEABLE);
        
        dbUtil = new DBUtil(this,DBUtil.DB_NAME,null,1);
        dbUtil.createDBAndTable();

	}
	@Override
	public boolean onUnbind(Intent intent) {
		Log.i(tag, "解除服务绑定！");
		// 解除监听
		sensorMng.unregisterListener(this);
		return super.onUnbind(intent);
	}
	
	@Override
	public void onDestroy() {
		Log.i(tag, "注销服务！");
		sensorMng.unregisterListener(this);
		super.onDestroy();
	}

	public class PedometerBinder extends Binder {
		public void setStepCount(int step) {
			stepCount = step;
		}
		public int getStepCount() {
			return stepCount;
		}
		public void setAnalyzing(boolean analyzing) {
			isAnalyzing = analyzing;
		}
		public boolean getAnalyzing() {
			return isAnalyzing;
		}
		
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// 如果计数
		if (isAnalyzing) {
			
			if (stepAnalyzer == null ) {
				switch(event.sensor.getType()) {
				case Sensor.TYPE_ACCELEROMETER:
					stepAnalyzer = PedometerWithAccelerometerAnalyzer.getInstance();
					break;
				default:
					stepAnalyzer = PedometerWithAccelerometerAnalyzer.getInstance();
				}
			}
			float result = stepAnalyzer.analyze(event);
			if (result > 0) {
				this.stepCount += 1;
			}
			
			saveData(event, result);
		}
	}
	/**
	 * 记录传感器数据
	 * @param event
	 */
	public void saveData(SensorEvent event, float result) {
		// 记录数据库
		ContentValues data = new ContentValues();
		data.put("x", event.values[0]);
		data.put("y", event.values[1]);
		data.put("z", event.values[2]);
		data.put("timetamp", event.timestamp);
		data.put("cal", result);
		dbUtil.getWritableDatabase().insert(DBUtil.TABLE_NAME, null, data);
		Log.i(tag,"已向数据库记录数据！数据路径：" + dbUtil.getWritableDatabase().getPath());
	}

}
