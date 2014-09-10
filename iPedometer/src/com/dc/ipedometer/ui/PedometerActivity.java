package com.dc.ipedometer.ui;

import java.util.Timer;
import java.util.TimerTask;

import android.app.ActionBar.Tab;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.dc.ipedometer.R;
import com.dc.ipedometer.service.PedometerService;

public class PedometerActivity extends ActionBarActivity {
    public String tag = "PedometerActivity";
    // ui
	private TextView stepCount;
	private Button btnStart;
	private Button btnPause;
	private Button btnStop;
	private Button btnReset;
	
	// 步数跟新Handler
	private Handler refreshHandler;
	// 刷新时间
	private Timer timer;
	// 与服务交互
	private PedometerService.PedometerBinder binder;
	// 服务连接
	private ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			Log.i(tag, "已连接服务！");
			binder = (PedometerService.PedometerBinder)arg1;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			Log.i(tag, "服务连接已中断！");
			binder = null;
		}
	};
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pedometer_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        // 创建启动服务的Intent
        final Intent intent = new Intent("stepService");
    	bindService(intent, conn, 1);
    	
        stepCount = (TextView)findViewById(R.id.stepCount);
        btnStart = (Button) findViewById(R.id.btn_start);
        btnPause = (Button) findViewById(R.id.btn_pause);
        btnStop = (Button) findViewById(R.id.btn_stop);
        btnReset = (Button) findViewById(R.id.btn_reset);
        
        btnStart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Log.d(tag, "开始自动服务...");
				start();
			}
		});
        btnPause.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				pause();
			}
		});
        
        btnStop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				stop();
			}
		});
        
        btnReset.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				reset();
			}
		});
        
        // 通过handler更新步数
        refreshHandler = new Handler() {
        	@Override
        	public void handleMessage(Message msg) {
        		if (binder != null) {
        			stepCount.setText(getResources().getString(R.string.main_text_step) + String.valueOf(binder.getStepCount()));
        		} else {
        			stepCount.setText(getResources().getString(R.string.main_text_init));
        		}
        	}
        };
    }

    
    public void start() {
    	Log.d(tag, "启动计步服务");

    	// 连接服务
    	if (binder != null) {
    		binder.setAnalyzing(true);
    	}
		
		btnStart.setEnabled(false);
    	btnPause.setEnabled(true);
    	btnStop.setEnabled(true);
    	
    	
    	if (timer == null) {
        	timer = new Timer();
    	}
    	timer.schedule(new TimerTask() {
			@Override
			public void run() {
				refreshHandler.sendEmptyMessage(0);
			}
        }, 0, 1000);
    	Log.d(tag, "页面定时器启动完成");
    }
    
    public void pause() {
    	if (binder != null) {
    		if (binder.getAnalyzing()) {
    			btnPause.setText(getResources().getString(R.string.main_btn_pause_2));
    			binder.setAnalyzing(false);
    		} else {
    			binder.setAnalyzing(true);
    			btnPause.setText(getResources().getString(R.string.main_btn_pause_1));
    		}
    	}
    }
    public void stop() {
    	if (timer != null) {
    		timer.cancel();
    		timer = null;
    	}
    	if (binder != null) {
    		binder.setAnalyzing(false);
    	}
    	
    	btnStart.setEnabled(true);
    	btnPause.setEnabled(false);
    	btnStop.setEnabled(false);
    }
    
    public void reset() {
    	if (binder != null) {
    		binder.setStepCount(0);
    		binder.setAnalyzing(false);
    	}
    	stepCount.setText(getResources().getString(R.string.main_text_step) + "0");
    	stop();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.pedometer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_pedometer, container, false);
            return rootView;
        }
    }

}
