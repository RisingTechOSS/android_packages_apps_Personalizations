/*
 * Copyright (C) 2020-2022 crDroid Android Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rising.settings.fragments.misc;

import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.development.SystemPropPoker;

import com.rising.settings.preferences.CustomSeekBarPreference;

import com.android.internal.util.rising.systemUtils;

public class SmartCharging extends SettingsPreferenceFragment 
	implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "SmartCharging";
    private static final String SMART_CHARGING_FOOTER = "smart_charging_footer";
    
    private Preference mSmartCharge;
    private Preference mSmartAdaptiveCharge;
    private Preference mResetStats;
    private CustomSeekBarPreference mStopLevel;
    private CustomSeekBarPreference mResumeLevel;
    boolean isPixelDevice;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.smart_charging);

        findPreference(SMART_CHARGING_FOOTER).setTitle(R.string.smart_charging_footer);

        String manufacturerProp = SystemProperties.get("ro.product.manufacturer");
        String brandProp = SystemProperties.get("ro.product.brand");
        isPixelDevice = brandProp.toLowerCase().contains("google") && manufacturerProp.toLowerCase().contains("google");

        mSmartAdaptiveCharge = findPreference("smart_adaptive_charging");
        mSmartAdaptiveCharge.setOnPreferenceChangeListener(this);

        mSmartCharge = findPreference("smart_charging");
        mSmartCharge.setOnPreferenceChangeListener(this);

        mResetStats = findPreference("smart_charging_reset_stats");
        mResetStats.setOnPreferenceChangeListener(this);

        mStopLevel = (CustomSeekBarPreference) findPreference("smart_charging_level");
        mStopLevel.setOnPreferenceChangeListener(this);

        mResumeLevel = (CustomSeekBarPreference) findPreference("smart_charging_resume_level");
        mResumeLevel.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mStopLevel) {
            if (isPixelDevice) {
            	int stopLevel = (int) newValue;
        	SystemProperties.set("persist.vendor.charge.stop.level", String.valueOf(stopLevel));
        	SystemPropPoker.getInstance().poke();
            }
            systemUtils.showSystemRestartDialog(getContext());
        } else if (preference == mResumeLevel) {
            if (isPixelDevice) {
            	int startLevel = (int) newValue;
        	SystemProperties.set("persist.vendor.charge.start.level", String.valueOf(startLevel));
        	SystemPropPoker.getInstance().poke();
            }
            systemUtils.showSystemRestartDialog(getContext());
        } else if (preference == mSmartCharge) {
            if (isPixelDevice) {
            	boolean enableBattDefender = Boolean.valueOf(newValue.toString());
        	SystemProperties.set("vendor.battery.defender.disable", enableBattDefender ? "0" : "1");
        	SystemPropPoker.getInstance().poke();
            }
            systemUtils.showSystemRestartDialog(getContext());
        } else if (preference == mSmartAdaptiveCharge) {
            if (isPixelDevice) {
            	int chargeLimit = (int) newValue;
        	SystemProperties.set("persist.vendor.adaptive.charge.soc", String.valueOf(chargeLimit));
        	SystemPropPoker.getInstance().poke();
	    }
            systemUtils.showSystemRestartDialog(getContext());
        } else if (preference == mResetStats) {
            systemUtils.showSystemRestartDialog(getContext());
        }
        return true;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }
}
