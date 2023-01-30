/*
 * Copyright (C) 2023 riceDroid Android Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.crdroid.settings.fragments.misc;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import java.util.List;

import lineageos.providers.LineageSettings;

@SearchIndexable
public class ScarletSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    public static final String TAG = "ScarletSettings";

    private static final String SYS_AGGRESIVE_IDLE_MODE = "scarlet_aggressive_idle_mode";
    private static final String SYS_BOOST = "scarlet_system_boost";
    private static final String SYS_MANAGER = "scarlet_system_manager";
    private static final String SYS_SYSTEM_BGT = "persist.sys.bgt.enable";
    private static final String SYS_RENDER_BOOST_THREAD = "persist.sys.perf.topAppRenderThreadBoost.enable";
    private static final String SYS_COMPACTION = "persist.sys.appcompact.enable_app_compact";
    private static final String SYS_SYSTEM_BOOST = "persist.sys.perf.systemboost.enable";
    private static final String SYS_INTERACTION_MAX = "persist.sys.powerhal.interaction.max";
    private static final String SYS_INTERACTION_MAX_DEFAULT = "persist.sys.powerhal.interaction.max_default";
    private static final String SYS_INTERACTION_MAX_BOOST = "persist.sys.powerhal.interaction.max_boost";
    private static final int SYS_POWER_BOOST_TIMEOUT_MS_DEFAULT = Integer.parseInt(SystemProperties.get(SYS_INTERACTION_MAX_DEFAULT, "200"));
    private static final int SYS_POWER_SYSBOOST_TIMEOUT_MS = Integer.parseInt(SystemProperties.get(SYS_INTERACTION_MAX_BOOST, "2000"));
    private static final int SYS_POWER_INTERACTION_MAX_DURATION = Integer.parseInt(SystemProperties.get(SYS_INTERACTION_MAX, "2000"));

    private SwitchPreference mSysAggresiveMode;
    private SwitchPreference mSysBoost;
    private SwitchPreference mSysManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.scarlet_settings);

        final PreferenceScreen prefScreen = getPreferenceScreen();
        final Resources res = getResources();

        mSysAggresiveMode = (SwitchPreference) prefScreen.findPreference(SYS_AGGRESIVE_IDLE_MODE);
        mSysAggresiveMode.setOnPreferenceChangeListener(this);
        mSysBoost = (SwitchPreference) prefScreen.findPreference(SYS_BOOST);
        mSysBoost.setOnPreferenceChangeListener(this);
        mSysManager = (SwitchPreference) prefScreen.findPreference(SYS_MANAGER);
        mSysManager.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mSysAggresiveMode) {
            boolean val = (Boolean) newValue;
            if (!val) {
                disableAggressiveModeFeatures(getContext());
            }
            return true;
        } else if (preference == mSysBoost) {
            boolean enable = (Boolean) newValue;
            SystemProperties.set(SYS_RENDER_BOOST_THREAD, enable ? "true" : "false");
            SystemProperties.set(SYS_SYSTEM_BGT, enable ? "true" : "false");
            SystemProperties.set(SYS_COMPACTION, enable ? "false" : "true");
            SystemProperties.set(SYS_SYSTEM_BOOST, enable ? "true" : "false");
            SystemProperties.set(SYS_INTERACTION_MAX, enable ? String.valueOf(SYS_POWER_SYSBOOST_TIMEOUT_MS) : String.valueOf((SYS_POWER_BOOST_TIMEOUT_MS_DEFAULT)));
            return true;
        } else if (preference == mSysManager) {
            boolean val = (Boolean) newValue;
            if (!val) {
                disableSysteManagerFeatures(getContext());
                disableAggressiveModeFeatures(getContext());
            }
            return true;
        }
        return false;
    }

    public static void disableSysteManagerFeatures(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();

        Settings.System.putIntForUser(resolver,
                Settings.System.SCARLET_SYSTEM_MANAGER, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.SCARLET_SYSTEM_BOOST, 0, UserHandle.USER_CURRENT);
    }

    public static void disableAggressiveModeFeatures(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();

        Settings.Secure.putIntForUser(resolver,
                Settings.Secure.SCARLET_AGGRESSIVE_IDLE_MODE, 0, UserHandle.USER_CURRENT);
        Settings.Secure.putIntForUser(resolver,
                Settings.Secure.SCARLET_AGGRESSIVE_IDLE_MODE_WIFI_TOGGLE, 0, UserHandle.USER_CURRENT);
        Settings.Secure.putIntForUser(resolver,
                Settings.Secure.SCARLET_AGGRESSIVE_IDLE_MODE_BLUETOOTH_TOGGLE, 0, UserHandle.USER_CURRENT);
        Settings.Secure.putIntForUser(resolver,
                Settings.Secure.SCARLET_AGGRESSIVE_IDLE_MODE_CELLULAR_TOGGLE, 0, UserHandle.USER_CURRENT);
        Settings.Secure.putIntForUser(resolver,
                Settings.Secure.SCARLET_AGGRESSIVE_IDLE_MODE_LOCATION_TOGGLE, 0, UserHandle.USER_CURRENT);
        Settings.Secure.putIntForUser(resolver,
                Settings.Secure.SCARLET_AGGRESSIVE_IDLE_MODE_RINGER_MODE, 0, UserHandle.USER_CURRENT);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CRDROID_SETTINGS;
    }

    /**
     * For search
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.scarlet_settings) {

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    final Resources res = context.getResources();

                    return keys;
                }
            };
}
