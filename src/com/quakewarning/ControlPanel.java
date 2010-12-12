package com.quakewarning;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class ControlPanel extends Activity {
	private AccelerometerWatcher bound_service;
	private SharedPreferences settings;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        this.settings = getSharedPreferences(getString(R.string.app_name), 0);
        
        CheckBox enable_monitoring = (CheckBox) findViewById(R.id.EnableMonitoring);
        
        enable_monitoring.setChecked(this.settings.getBoolean("enable_monitoring", true));
        
        enable_monitoring.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton button_view, boolean is_checked) {
				ControlPanel ctx = (ControlPanel) button_view.getContext();
				SharedPreferences.Editor settings_editor = ctx.settings.edit();
				
				if(is_checked) {
					Intent intent = new Intent(ctx, AccelerometerWatcher.class);
					intent.putExtra(getString(R.string.extra_sensor_delay), SensorManager.SENSOR_DELAY_GAME);
					intent.putExtra(getString(R.string.extra_jolt_sensitivity), new Float(500));
					intent.putExtra(getString(R.string.extra_jolt_timeout), new Long(1000));
					intent.putExtra(getString(R.string.extra_jolt_threashold), new Integer(5));
					startService(intent);
				} else {
					stopService(new Intent(ctx, AccelerometerWatcher.class));
				}
				
				settings_editor.putBoolean("enable_monitoring", is_checked);
				settings_editor.commit();
			}
		});
    }
}