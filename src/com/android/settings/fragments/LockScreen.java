/*
 * Copyright (C) 2016-2022 crDroid Android Project
 * Copyright (C) 2024 the risingOS Android Project
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
package com.android.settings.fragments;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import com.android.internal.util.rising.systemUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SearchIndexable
public class LockScreen extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    public static final String TAG = "LockScreen";
    private static final String ALBUM_ART_KEY = "lockscreen_media_metadata";
    private static final String BLUR_RADIUS_KEY = "ls_media_filter_blur_radius";

    private static final String MAIN_WIDGET_1_KEY = "main_custom_widgets1";
    private static final String MAIN_WIDGET_2_KEY = "main_custom_widgets2";
    private static final String EXTRA_WIDGET_1_KEY = "custom_widgets1";
    private static final String EXTRA_WIDGET_2_KEY = "custom_widgets2";
    private static final String EXTRA_WIDGET_3_KEY = "custom_widgets3";
    private static final String EXTRA_WIDGET_4_KEY = "custom_widgets4";
    
    private static final String FORCE_DARK_WP_TEXT_KEY = "force_dark_text_wp";

    private Preference mAlbumArtPref;
    private Preference mBlurRadiusPref;

    private Preference mMainWidget1;
    private Preference mMainWidget2;
    private Preference mExtraWidget1;
    private Preference mExtraWidget2;
    private Preference mExtraWidget3;
    private Preference mExtraWidget4;
    
    private Preference mForceDarkWallpaperTextColor;

    private Map<Preference, String> widgetKeysMap = new HashMap<>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.rising_settings_lockscreen);
		mAlbumArtPref = findPreference(ALBUM_ART_KEY);
		mBlurRadiusPref = findPreference(BLUR_RADIUS_KEY);
		
		mForceDarkWallpaperTextColor = findPreference(FORCE_DARK_WP_TEXT_KEY);

		mAlbumArtPref.setOnPreferenceChangeListener(this);
		mForceDarkWallpaperTextColor.setOnPreferenceChangeListener(this);

		mMainWidget1 = findPreference(MAIN_WIDGET_1_KEY);
		mMainWidget2 = findPreference(MAIN_WIDGET_2_KEY);
		mExtraWidget1 = findPreference(EXTRA_WIDGET_1_KEY);
		mExtraWidget2 = findPreference(EXTRA_WIDGET_2_KEY);
		mExtraWidget3 = findPreference(EXTRA_WIDGET_3_KEY);
		mExtraWidget4 = findPreference(EXTRA_WIDGET_4_KEY);

		List<Preference> widgetPreferences = Arrays.asList(mMainWidget1, mMainWidget2, mExtraWidget1, mExtraWidget2, mExtraWidget3, mExtraWidget4);
		for (Preference widgetPref : widgetPreferences) {
		    widgetPref.setOnPreferenceChangeListener(this);
		    widgetKeysMap.put(widgetPref, "");
		}

		String mainWidgets = Settings.System.getString(getActivity().getContentResolver(), "lockscreen_widgets");
		String extraWidgets = Settings.System.getString(getActivity().getContentResolver(), "lockscreen_widgets_extras");

		setWidgetValues(mainWidgets, mMainWidget1, mMainWidget2);
		setWidgetValues(extraWidgets, mExtraWidget1, mExtraWidget2, mExtraWidget3, mExtraWidget4);
		
		((SwitchPreference) mForceDarkWallpaperTextColor).setChecked(SystemProperties.getBoolean("persist.sys.wallpapercolors.force_dark_text", false));

		updateAlbumArtPref();
	}

    private void updateAlbumArtPref() {
        boolean mAlbumEnabled = Settings.System.getIntForUser(getActivity().getContentResolver(),
                ALBUM_ART_KEY, 0, UserHandle.USER_CURRENT) == 1;
        boolean gradientBlurFilterEnabled = Settings.System.getIntForUser(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_ALBUMART_FILTER, 0, UserHandle.USER_CURRENT) == 5;
        boolean blurFilterEnabled = Settings.System.getIntForUser(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_ALBUMART_FILTER, 0, UserHandle.USER_CURRENT) >= 3 && !gradientBlurFilterEnabled;
        ((SwitchPreference) mAlbumArtPref).setChecked(mAlbumEnabled);
        mBlurRadiusPref.setEnabled(blurFilterEnabled);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mAlbumArtPref) {
            final boolean booleanValue = (boolean) newValue;
            Settings.System.putIntForUser(getActivity().getContentResolver(), ALBUM_ART_KEY, booleanValue ? 1 : 0, UserHandle.USER_CURRENT);
            updateAlbumArtPref();
            SystemProperties.set("persist.sys.lockscreen_live_wallpaper", String.valueOf(!booleanValue));
            systemUtils.showSystemUIRestartDialog(getContext());
            return true;
        } else if (widgetKeysMap.containsKey(preference)) {
            widgetKeysMap.put(preference, String.valueOf(newValue));
            updateWidgetPreferences();
            return true;
        } else if (preference == mForceDarkWallpaperTextColor) {
            final boolean booleanValue = (boolean) newValue;
            SystemProperties.set("persist.sys.wallpapercolors.force_dark_text", String.valueOf(booleanValue));
            systemUtils.showSystemRestartDialog(getContext());
            return true;
        }
        return false;
    }

    private void setWidgetValues(String widgets, Preference... preferences) {
        List<String> widgetList = Arrays.asList(widgets.split(","));
        for (int i = 0; i < preferences.length && i < widgetList.size(); i++) {
            widgetKeysMap.put(preferences[i], widgetList.get(i).trim());
        }
    }

	private void updateWidgetPreferences() {
		List<String> mainWidgetsList = Arrays.asList(widgetKeysMap.get(mMainWidget1), widgetKeysMap.get(mMainWidget2));
		List<String> extraWidgetsList = Arrays.asList(widgetKeysMap.get(mExtraWidget1), widgetKeysMap.get(mExtraWidget2), widgetKeysMap.get(mExtraWidget3), widgetKeysMap.get(mExtraWidget4));

		mainWidgetsList = filterEmptyStrings(mainWidgetsList);
		extraWidgetsList = filterEmptyStrings(extraWidgetsList);

		String mainWidgets = TextUtils.join(",", mainWidgetsList);
		String extraWidgets = TextUtils.join(",", extraWidgetsList);

		Settings.System.putString(getActivity().getContentResolver(), "lockscreen_widgets", mainWidgets);
		Settings.System.putString(getActivity().getContentResolver(), "lockscreen_widgets_extras", extraWidgets);
	}

	private List<String> filterEmptyStrings(List<String> inputList) {
		return inputList.stream().filter(s -> !TextUtils.isEmpty(s)).collect(Collectors.toList());
	}

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.VIEW_UNKNOWN;
    }

    /**
     * For search
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.rising_settings_lockscreen) {

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
                }
            };
}
