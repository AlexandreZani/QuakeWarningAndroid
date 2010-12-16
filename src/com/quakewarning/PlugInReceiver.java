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
        ctx.startService(new Intent(ctx, AccelerometerWatcher.class));
    }
    
    private void onUnplug(Context ctx) {
        ctx.stopService(new Intent(ctx, AccelerometerWatcher.class));
    }

}
