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

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;

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
        
        enable_monitoring.setChecked(this.settings.getBoolean("enable_monitoring", false));
        
        enable_monitoring.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton button_view, boolean is_checked) {
                ControlPanel ctx = (ControlPanel) button_view.getContext();
                SharedPreferences.Editor settings_editor = ctx.settings.edit();
                
                if(is_checked) {
                    startService(new Intent(ctx, AccelerometerWatcher.class));
                } else {
                    if (!settings.getBoolean("enabled_plugged_monitoring", false) || !settings.getBoolean("plugged_in", false)) {
                        stopService(new Intent(ctx, AccelerometerWatcher.class));
                    }
                }
                
                settings_editor.putBoolean("enable_monitoring", is_checked);
                settings_editor.commit();
            }
        });
        
        CheckBox enable_plugged_monitoring = (CheckBox) findViewById(R.id.EnabledPluggedMonitoring);
        
        enable_plugged_monitoring.setChecked(this.settings.getBoolean("enable_plugged_monitoring", false));
        
        enable_plugged_monitoring.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            
            @Override
            public void onCheckedChanged(CompoundButton button_view, boolean is_checked) {
                ControlPanel ctx = (ControlPanel) button_view.getContext();
                SharedPreferences.Editor settings_editor = ctx.settings.edit();
                
                if (!is_checked && !settings.getBoolean("enabled_monitoring", false)) {
                    stopService(new Intent(ctx, AccelerometerWatcher.class));
                } else if (is_checked && settings.getBoolean("plugged_in", false)) {
                    startService(new Intent(ctx, AccelerometerWatcher.class));
                }

                settings_editor.putBoolean("enable_plugged_monitoring", is_checked);
                settings_editor.commit();
            }
        });
        
        Spinner rate_selector = (Spinner) findViewById(R.id.sampling_rate_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.sample_rates_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rate_selector.setAdapter(adapter);
        
        this.initializeSettings();
        
        Button apply_settings = (Button) findViewById(R.id.apply_settings);
        apply_settings.setOnClickListener(new ApplySettingsListener());
        
        Button cancel_settings = (Button) findViewById(R.id.cancel_settings);
        cancel_settings.setOnClickListener(new CancelSettingsListener());
    }
    
    private void initializeSettings() {
        Spinner rate_selector = (Spinner) findViewById(R.id.sampling_rate_spinner);
        int rate = this.settings.getInt("accelerometer_rate", SensorManager.SENSOR_DELAY_NORMAL);
        int pos;
        switch(rate) {
        case SensorManager.SENSOR_DELAY_UI:
            pos = 0;
            break;
        case SensorManager.SENSOR_DELAY_NORMAL:
            pos = 1;
            break;
        case SensorManager.SENSOR_DELAY_GAME:
            pos = 2;
            break;
        case SensorManager.SENSOR_DELAY_FASTEST:
            pos = 3;
            break;
        default:
            pos = 1;
        }
        rate_selector.setSelection(pos);
        
        EditText sensitivity = (EditText) findViewById(R.id.jolt_sensitivity);
        sensitivity.setText(Float.toString(settings.getFloat("jolt_sensitivity", 100)));
        
        EditText jolt_timeout = (EditText) findViewById(R.id.jolt_timeout);
        jolt_timeout.setText(Long.toString(settings.getLong("jolt_timeout", 1000)));
        
        EditText jolt_threashold = (EditText) findViewById(R.id.jolt_threashold);
        jolt_threashold.setText(Integer.toString(settings.getInt("jolt_threashold", 3)));
    }
    
    public class CancelSettingsListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            initializeSettings();

            SharedPreferences settings = getSharedPreferences(getString(R.string.app_name), 0);
            
            if (settings.getBoolean("enabled_monitoring", false) || (settings.getBoolean("enabled_plugged_monitoring", false) && settings.getBoolean("plugged_in", false))) {
                stopService(new Intent(v.getContext(), AccelerometerWatcher.class));
                startService(new Intent(v.getContext(), AccelerometerWatcher.class));
            }
            
        }
    }
    
    public class ApplySettingsListener implements OnClickListener {
        @Override
        public void onClick(View button) {
            SharedPreferences settings = getSharedPreferences(getString(R.string.app_name), 0);
            SharedPreferences.Editor settings_editor = settings.edit();
            
            Spinner rate_selector = (Spinner) findViewById(R.id.sampling_rate_spinner);
            int pos = rate_selector.getSelectedItemPosition();
            int rate;
            
            switch(pos) {
            case 0:
                rate = SensorManager.SENSOR_DELAY_UI;
                break;
            case 1:
                rate = SensorManager.SENSOR_DELAY_NORMAL;
                break;
            case 2:
                rate = SensorManager.SENSOR_DELAY_GAME;
                break;
            case 3:
                rate = SensorManager.SENSOR_DELAY_FASTEST;
                break;
            default:
                rate = SensorManager.SENSOR_DELAY_NORMAL;
            }
            settings_editor.putInt("accelerometer_rate", rate);
            
            EditText sensitivity = (EditText) findViewById(R.id.jolt_sensitivity);
            settings_editor.putFloat("jolt_sensitivity", Float.parseFloat(sensitivity.getText().toString()));
            
            EditText jolt_timeout = (EditText) findViewById(R.id.jolt_timeout);
            settings_editor.putLong("jolt_timeout", Long.parseLong(jolt_timeout.getText().toString()));
            
            EditText jolt_threashold = (EditText) findViewById(R.id.jolt_threashold);
            settings_editor.putInt("jolt_threashold", Integer.parseInt(jolt_threashold.getText().toString()));
    
            settings_editor.commit();
        }    
    }
}