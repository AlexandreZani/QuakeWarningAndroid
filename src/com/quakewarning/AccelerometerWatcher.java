/*
 * Copyright 2010 Alexandre Zani (Alexandre.Zani@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.quakewarning;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

public class AccelerometerWatcher extends Service {
	private SensorManager sensor_manager;
	private Sensor accelerometer;
	private SensorEventListener accelerometer_listener;
	private int down = -1;
	private boolean watching = false;
	private float[] last_values = null;
	private float sensitivity;	// Minimum magnitude of jolt
	private long last_jolt;	// Time of the last jolts
	private long jolt_timeout;	// Maximum time between jolts before counter gets reset
	private int jolt_counter;	// Number of jolts that did not timeout
	private int jolt_threashold; // Minimum number of jolts before an earthquake is registered
	
	public class LocalBinder extends Binder {
		AccelerometerWatcher getService() {
			return AccelerometerWatcher.this;
		}
	}
	
	private final IBinder binder = new LocalBinder();

	@Override
	public IBinder onBind(Intent arg0) {
		return this.binder;
	}
	
	@Override
	public void onCreate() {
    	this.sensor_manager = (SensorManager) getSystemService(SENSOR_SERVICE);
        this.accelerometer = sensor_manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.accelerometer_listener = new SensorEventListener() {
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {}

			@Override
			public void onSensorChanged(SensorEvent event) {
				if (last_values == null) {
					last_values = new float[3];
					last_values[0] = event.values[0];
					last_values[1] = event.values[1];
					last_values[2] = event.values[2];
					return;
				}
				
				float dist = (last_values[0] - event.values[0]) * (last_values[0] - event.values[0]);
				dist += (last_values[1] - event.values[1]) * (last_values[1] - event.values[1]);
				dist += (last_values[2] - event.values[2]) * (last_values[2] - event.values[2]);
				
				last_values[0] = event.values[0];
				last_values[1] = event.values[1];
				last_values[2] = event.values[2];
				
				if (dist >= sensitivity) {
					long now = System.currentTimeMillis();
					if (now - last_jolt < jolt_timeout) {
						jolt_counter += 1;
					} else {
						jolt_counter = 1;
					}
					last_jolt = now;
					
					// By using == instead of >= we get only 1 earthquake event
					// per timeout period
					if (jolt_counter == jolt_threashold) {
						// EARTHQUAKE!!!!!!!!!!!!!!!!!!!!!
						Log.v(this.getClass().getName(), "EARTHQUAKE!!!!!!!!!!!!!!");
						Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
				        if(vibrator != null) {
				        	vibrator.vibrate(100);
				        }
					}
				}
			}
		};
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		SharedPreferences settings = getSharedPreferences(getString(R.string.app_name), 0);
		int delay = settings.getInt("accelerometer_rate", SensorManager.SENSOR_DELAY_NORMAL);
		
		this.jolt_counter = 0;
		this.last_jolt = 0;

		this.sensitivity = settings.getFloat("jolt_sensitivity", 100);
		this.jolt_timeout = settings.getLong("jolt_timeout", 1000);
		this.jolt_threashold = settings.getInt("jolt_threashold", 3);
		
		this.startWatcher(delay);
		
		return START_STICKY;
	}
	
	public void startWatcher(int delay) {
		if(this.watching)
			return;
		
		this.sensor_manager.registerListener(this.accelerometer_listener, this.accelerometer, delay);
		
		// Display the notification that the quake catcher is running
		int icon = R.drawable.icon;
		CharSequence tickerText = "Earthquake Watcher";
		long when = System.currentTimeMillis();
		
		Notification notification =  new Notification(icon, tickerText, when);
		
		Context ctx = getApplicationContext();
		CharSequence content_title = getString(R.string.app_name);
		CharSequence content_text = getString(R.string.accelerometer_notification);
		Intent notification_intent = new Intent(ctx, ControlPanel.class);
		
		PendingIntent pending_intent = PendingIntent.getActivity(ctx, 0, notification_intent, 0);
		notification.setLatestEventInfo(ctx, content_title, content_text, pending_intent);
		
		this.startForeground(R.string.accelerometer_notification, notification);
	}
	
	public void stopWatcher() {
		if (this.watching)
			return;
		
		this.watching = false;
		this.sensor_manager.unregisterListener(this.accelerometer_listener);
		this.stopForeground(true);
	}
	
	@Override
	public void onDestroy() {
		this.stopWatcher();
	}
}
