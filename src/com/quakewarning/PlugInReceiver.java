package com.quakewarning;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.os.Vibrator;
import android.util.Log;

public class PlugInReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context ctx, Intent intent) {
		SharedPreferences settings = ctx.getSharedPreferences(ctx.getString(R.string.app_name), 0);
		SharedPreferences.Editor settings_editor = settings.edit();
		if (Intent.ACTION_POWER_CONNECTED.equals(intent.getAction())) {
			settings_editor.putBoolean("plugged_in", true);
		} else if (Intent.ACTION_POWER_DISCONNECTED.equals(intent.getAction())) {
			settings_editor.putBoolean("plugged_in", false);
		}
		settings_editor.commit();

		if (!settings.getBoolean("enable_plugged_monitoring", false) || settings.getBoolean("enable_monitoring", false)) {
			return;
		}
		
		if (Intent.ACTION_POWER_CONNECTED.equals(intent.getAction())) {
			this.onPlugIn(ctx);
		} else if (Intent.ACTION_POWER_DISCONNECTED.equals(intent.getAction())) {
			this.onUnplug(ctx);
		}
	}
	
	private void onPlugIn(Context ctx) {
		Intent intent = new Intent(ctx, AccelerometerWatcher.class);
		intent.putExtra(ctx.getString(R.string.extra_sensor_delay), SensorManager.SENSOR_DELAY_GAME);
		intent.putExtra(ctx.getString(R.string.extra_jolt_sensitivity), new Float(100));
		intent.putExtra(ctx.getString(R.string.extra_jolt_timeout), new Long(1000));
		intent.putExtra(ctx.getString(R.string.extra_jolt_threashold), new Integer(5));
		ctx.startService(intent);
	}
	
	private void onUnplug(Context ctx) {
		ctx.stopService(new Intent(ctx, AccelerometerWatcher.class));
	}

}
