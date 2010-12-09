package com.quakewarning;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

public class ControlPanel extends Activity {
	private SensorManager sensor_manager;
	private Sensor accelerometer;
	private SensorEventListener accelerometer_listener;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	this.sensor_manager = (SensorManager) getSystemService(SENSOR_SERVICE);
        this.accelerometer = sensor_manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.accelerometer_listener = new SensorEventListener() {
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {}

			@Override
			public void onSensorChanged(SensorEvent event) {
				String v = String.format("%1$f , %1$f , %1$f", event.values[0], event.values[1], event.values[2]);
				TextView tv = (TextView) findViewById(R.id.Acceleration);
				tv.setText(v);
			}
		};
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        CheckBox enable_monitoring = (CheckBox) findViewById(R.id.EnableMonitoring);
        
        enable_monitoring.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton button_view, boolean is_checked) {
				ControlPanel ctx = (ControlPanel) button_view.getContext();
				
				if(is_checked) {
					ctx.sensor_manager.registerListener(ctx.accelerometer_listener, ctx.accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
				} else {
					ctx.sensor_manager.unregisterListener(ctx.accelerometer_listener);
				}
			}
		});
    }
    
    @Override
    public void onStart() {
    	super.onStart();
    	//new AlertDialog.Builder(this).setMessage("onStart").show();
    }
}