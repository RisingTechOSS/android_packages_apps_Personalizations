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
import com.rising.settings.preferences.CustomSeekBarPreference;

import com.android.internal.util.rising.systemUtils;

public class SmartCharging extends SettingsPreferenceFragment 
	implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "SmartCharging";
    private static final String SMART_CHARGING_FOOTER = "smart_charging_footer";
    
    private Preference mSmartCharge;
    private Preference mResetStats;
    private CustomSeekBarPreference mStopLevel;
    private CustomSeekBarPreference mResumeLevel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.smart_charging);

        findPreference(SMART_CHARGING_FOOTER).setTitle(R.string.smart_charging_footer);
        
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
            systemUtils.showSystemRestartDialog(getContext());
        } else if (preference == mResumeLevel) {
            systemUtils.showSystemRestartDialog(getContext());
        } else if (preference == mSmartCharge) {
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
