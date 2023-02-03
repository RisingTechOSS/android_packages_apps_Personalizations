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
import android.os.SystemProperties;
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
public class SettingsStyles extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    public static final String TAG = "SettingsStyles";

    private static final String SETTINGS_DASHBOARD_STYLE = "settings_dashboard_style";
    private static final String SETTINGS_HEADER_IMAGE = "settings_header_image";
    private static final String SETTINGS_HEADER_TEXT = "settings_header_text";
    private static final String SETTINGS_HEADER_TEXT_ENABLED = "settings_header_text_enabled";
    private static final String USE_STOCK_LAYOUT = "use_stock_layout";
    private static final String ABOUT_PHONE_STYLE = "about_card_style";
    private static final String HIDE_USER_CARD = "hide_user_card";

    private Handler mHandler;
    private SystemSettingListPreference mSettingsDashBoardStyle;
    private SystemSettingListPreference mAboutPhoneStyle;
    private SystemSettingSwitchPreference mUseStockLayout;
    private SystemSettingSwitchPreference mHideUserCard;
    private ThemeUtils mThemeUtils;
    private Preference mSettingsHeaderImage;
    private SystemSettingEditTextPreference mSettingsHeaderText;
    private SystemSettingSwitchPreference mSettingsHeaderTextEnabled;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings_styles);

	Context mContext = getActivity().getApplicationContext();
	ContentResolver resolver = mContext.getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        mSettingsDashBoardStyle = (SystemSettingListPreference) findPreference(SETTINGS_DASHBOARD_STYLE);
        mSettingsDashBoardStyle.setOnPreferenceChangeListener(this);
        mSettingsHeaderImage = findPreference(SETTINGS_HEADER_IMAGE);
        mSettingsHeaderImage.setOnPreferenceChangeListener(this);
        mUseStockLayout = (SystemSettingSwitchPreference) findPreference(USE_STOCK_LAYOUT);
        mUseStockLayout.setOnPreferenceChangeListener(this);
        mAboutPhoneStyle = (SystemSettingListPreference) findPreference(ABOUT_PHONE_STYLE);
        mAboutPhoneStyle.setOnPreferenceChangeListener(this);
        mHideUserCard = (SystemSettingSwitchPreference) findPreference(HIDE_USER_CARD);
        mHideUserCard.setOnPreferenceChangeListener(this);
        mSettingsHeaderText = (SystemSettingEditTextPreference) findPreference(SETTINGS_HEADER_TEXT);
        mSettingsHeaderText.setOnPreferenceChangeListener(this);
        mSettingsHeaderTextEnabled = (SystemSettingSwitchPreference) findPreference(SETTINGS_HEADER_TEXT_ENABLED);
        mSettingsHeaderTextEnabled.setOnPreferenceChangeListener(this);
        
        mThemeUtils = new ThemeUtils(getActivity());
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
                    Settings.System.SETTINGS_DASHBOARD_STYLE),
                    false, this, UserHandle.USER_ALL);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (uri.equals(Settings.System.getUriFor(Settings.System.SETTINGS_DASHBOARD_STYLE))) {
                updateSettingsStyle();
            }
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
	Context mContext = getActivity().getApplicationContext();
	ContentResolver resolver = mContext.getContentResolver();
        if (preference == mSettingsDashBoardStyle) {
            mCustomSettingsObserver.observe();
            return true;
        } else if (preference == mUseStockLayout) {
            Utils.showSettingsRestartDialog(getContext());
            return true;
        } else if (preference == mHideUserCard) {
            Utils.showSettingsRestartDialog(getContext());
            return true;
        } else if (preference == mAboutPhoneStyle) {
            Utils.showSettingsRestartDialog(getContext());
            return true;
        } else if (preference == mSettingsHeaderImage) {
            Utils.showSettingsRestartDialog(getContext());
            return true;
        } else if (preference == mSettingsHeaderTextEnabled) {
            boolean enable = (Boolean) newValue;
            SystemProperties.set("persist.sys.settings.header_text_enabled", enable ? "true" : "false");
            Utils.showSettingsRestartDialog(getContext());
            return true;
        } else if (preference == mSettingsHeaderText) {
            String value = (String) newValue;
            SystemProperties.set("persist.sys.settings.header_text", value);
            Utils.showSettingsRestartDialog(getContext());
            return true;
        }
        return false;
    }

    private void updateSettingsStyle() {
        ContentResolver resolver = getActivity().getContentResolver();

        int settingsPanelStyle = Settings.System.getIntForUser(getContext().getContentResolver(),
                Settings.System.SETTINGS_DASHBOARD_STYLE, 0, UserHandle.USER_CURRENT);

        switch (settingsPanelStyle) {
            case 0:
              setSettingsStyle("com.android.settings");
              break;
            case 1:
              setSettingsStyle("com.android.system.settings.rui");
              break;
            case 2:
              setSettingsStyle("com.android.system.settings.arc");
              break;
            case 3:
              setSettingsStyle("com.android.system.settings.aosp");
              break;
            case 4:
              setSettingsStyle("com.android.system.settings.mt");
              break;
            case 5:
              setSettingsStyle("com.android.system.settings.card");
              break;
            default:
              break;
        }
    }

    public void setSettingsStyle(String overlayName) {
       mThemeUtils.setOverlayEnabled("android.theme.customization.icon_pack.settings", overlayName, "com.android.settings");
    }
    
    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CRDROID_SETTINGS;
    }

    /**
     * For search
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.settings_styles) {

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);

                    return keys;
                }
            };
}
