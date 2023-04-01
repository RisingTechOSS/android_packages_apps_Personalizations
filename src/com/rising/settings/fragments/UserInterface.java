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
package com.rising.settings.fragments;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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

import com.rising.settings.preferences.SystemSettingListPreference;

import com.android.internal.util.rising.ThemeUtils;

import java.util.List;

@SearchIndexable
public class UserInterface extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    public static final String TAG = "UserInterface";

    private static final String KEY_QS_PANEL_STYLE  = "qs_panel_style";
    
    private static final String KG_CUSTOM_CLOCK_COLOR_ENABLED = "kg_custom_clock_color_enabled";
    
    private SystemSettingListPreference mQsStyle;
    private SwitchPreference mKGCustomClockColor;
    private ThemeUtils mThemeUtils;
    private Handler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.rising_settings_ui);
        
        final Context mContext = getActivity().getApplicationContext();
        final ContentResolver resolver = mContext.getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();
        
        mThemeUtils = new ThemeUtils(getActivity());

        mQsStyle = (SystemSettingListPreference) findPreference(KEY_QS_PANEL_STYLE);
        mCustomSettingsObserver.observe();
        
        mKGCustomClockColor = (SwitchPreference) findPreference(KG_CUSTOM_CLOCK_COLOR_ENABLED);
        boolean mKGCustomClockColorEnabled = Settings.Secure.getIntForUser(resolver,
                Settings.Secure.KG_CUSTOM_CLOCK_COLOR_ENABLED, 0, UserHandle.USER_CURRENT) != 0;
        mKGCustomClockColor.setChecked(mKGCustomClockColorEnabled);
        mKGCustomClockColor.setOnPreferenceChangeListener(this);

    }
    
    private CustomSettingsObserver mCustomSettingsObserver = new CustomSettingsObserver(mHandler);
    private class CustomSettingsObserver extends ContentObserver {

        CustomSettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            Context mContext = getContext();
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.QS_PANEL_STYLE),
                    false, this, UserHandle.USER_ALL);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (uri.equals(Settings.System.getUriFor(Settings.System.QS_PANEL_STYLE))) {
                updateQsStyle();
            }
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
	Context mContext = getActivity().getApplicationContext();
	ContentResolver resolver = mContext.getContentResolver();
        if (preference == mQsStyle) {
            mCustomSettingsObserver.observe();
            return true;
        } else if (preference == mKGCustomClockColor) {
            boolean val = (Boolean) newValue;
            Settings.Secure.putIntForUser(resolver,
                Settings.Secure.KG_CUSTOM_CLOCK_COLOR_ENABLED, val ? 1 : 0, UserHandle.USER_CURRENT);
            return true;
	}
        return false;
    }

    private void updateQsStyle() {
        ContentResolver resolver = getActivity().getContentResolver();

        int qsPanelStyle = Settings.System.getIntForUser(getContext().getContentResolver(),
                Settings.System.QS_PANEL_STYLE , 0, UserHandle.USER_CURRENT);

        switch (qsPanelStyle) {
            case 0:
              setQsStyle("com.android.systemui");
              break;
            case 1:
              setQsStyle("com.android.system.qs.outline");
              break;
            case 2:
            case 3:
              setQsStyle("com.android.system.qs.twotoneaccent");
              break;
            case 4:
              setQsStyle("com.android.system.qs.shaded");
              break;
            case 5:
              setQsStyle("com.android.system.qs.cyberpunk");
              break;
            case 6:
              setQsStyle("com.android.system.qs.neumorph");
              break;
            case 7:
              setQsStyle("com.android.system.qs.reflected");
              break;
            case 8:
              setQsStyle("com.android.system.qs.surround");
              break;
            case 9:
              setQsStyle("com.android.system.qs.thin");
              break;
            case 10:
              setQsStyle("com.android.system.qs.twotoneaccenttrans");
              break;
            default:
              break;
        }
    }

    public void setQsStyle(String overlayName) {
        mThemeUtils.setOverlayEnabled("android.theme.customization.qs_panel", overlayName, "com.android.systemui");
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }

    /**
     * For search
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.rising_settings_ui) {

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);

                    return keys;
                }
            };
}
