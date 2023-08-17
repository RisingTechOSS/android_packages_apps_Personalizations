/*
 * Copyright (C) 2016-2023 crDroid Android Project
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
package com.rising.settings.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.preference.Preference.OnPreferenceChangeListener;
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

import com.android.internal.util.rising.systemUtils;

@SearchIndexable
public class Miscellaneous extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    public static final String TAG = "Miscellaneous";

    private static final String SMART_CHARGING = "smart_charging";
    private static final String SMART_PIXELS = "smart_pixels";
    private static final String HIDE_IME_SPACE = "hide_ime_space_enable";

    private Preference mSmartPixels;
    private Preference mHideImeSpace;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.rising_settings_misc);

        PreferenceScreen prefScreen = getPreferenceScreen();

        mHideImeSpace = findPreference(HIDE_IME_SPACE);
        mHideImeSpace.setOnPreferenceChangeListener(this);

        mSmartPixels = (Preference) prefScreen.findPreference(SMART_PIXELS);
        boolean mSmartPixelsSupported = getActivity().getResources().getBoolean(
                com.android.internal.R.bool.config_supportSmartPixels);
        if (!mSmartPixelsSupported) {
            prefScreen.removePreference(mSmartPixels);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mHideImeSpace) {
            systemUtils.showSystemRestartDialog(getActivity());
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }

    /**
     * For search
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.rising_settings_misc) {

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);

                    boolean mSmartPixelsSupported = context.getResources().getBoolean(
                            com.android.internal.R.bool.config_supportSmartPixels);
                    if (!mSmartPixelsSupported) {
                        keys.add(SMART_PIXELS);
                    }

                    return keys;
                }
            };
}
