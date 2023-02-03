/*
 * Copyright (C) 2016-2022 crDroid Android Project
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
package com.crdroid.settings.fragments.ui;

import static android.os.UserHandle.USER_CURRENT;
import static android.os.UserHandle.USER_SYSTEM;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.om.IOverlayManager;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import com.android.internal.util.crdroid.Utils;
import com.android.internal.util.crdroid.ThemeUtils;

import com.crdroid.settings.preferences.SystemSettingListPreference;
import com.crdroid.settings.preferences.SystemSettingSwitchPreference;
import com.crdroid.settings.preferences.SystemSettingEditTextPreference;

import java.util.List;

@SearchIndexable
public class LockScreenStyles extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    public static final String TAG = "LockScreenStyles";

    private static final String KG_CUSTOM_CLOCK_COLOR_ENABLED = "kg_custom_clock_color_enabled";

    private SwitchPreference mKGCustomClockColor;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.lockscreen_styles);

	Context mContext = getActivity().getApplicationContext();
	ContentResolver resolver = mContext.getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        mKGCustomClockColor = (SwitchPreference) findPreference(KG_CUSTOM_CLOCK_COLOR_ENABLED);
        boolean mKGCustomClockColorEnabled = Settings.Secure.getIntForUser(resolver,
                Settings.Secure.KG_CUSTOM_CLOCK_COLOR_ENABLED, 0, UserHandle.USER_CURRENT) != 0;
        mKGCustomClockColor.setChecked(mKGCustomClockColorEnabled);
        mKGCustomClockColor.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
	Context mContext = getActivity().getApplicationContext();
	ContentResolver resolver = mContext.getContentResolver();
        if (preference == mKGCustomClockColor) {
            boolean val = (Boolean) newValue;
            Settings.Secure.putIntForUser(resolver,
                Settings.Secure.KG_CUSTOM_CLOCK_COLOR_ENABLED, val ? 1 : 0, UserHandle.USER_CURRENT);
            return true;
        }
        return false;
    }
    
    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CRDROID_SETTINGS;
    }

    /**
     * For search
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.lockscreen_styles) {

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);

                    return keys;
                }
            };
}
