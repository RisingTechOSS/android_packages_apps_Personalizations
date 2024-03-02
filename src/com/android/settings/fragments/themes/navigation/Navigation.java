/*
 * Copyright (C) 2023 the risingOS Android Project
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

package com.android.settings.fragments.themes.navigation;

import android.content.Context;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.android.internal.util.rising.systemUtils;
import com.android.internal.util.rising.ThemeUtils;
import com.android.settings.preferences.ui.AdaptivePreferenceUtils;

public class Navigation extends SettingsPreferenceFragment 
        implements Preference.OnPreferenceChangeListener {

    private final static String TAG = "Navigation";
    private final static String GESTURE_LENGTH_KEY = "gesture_navbar_length_mode";
    private final static String GESTURE_RADIUS_KEY = "gesture_navbar_radius";

    private Preference gestureLengthPref;
    private Preference gestureRadiusPref;
    private ThemeUtils mThemeUtils;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.navigation_themes);

        mThemeUtils = new ThemeUtils(getActivity());
        
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        gestureLengthPref = findPreference(GESTURE_LENGTH_KEY);
        gestureRadiusPref = findPreference(GESTURE_RADIUS_KEY);
        
        gestureLengthPref.setOnPreferenceChangeListener(this);
        gestureRadiusPref.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int prefValue = (int) newValue;
        if (preference == gestureLengthPref) {
            String category = "android.theme.customization.gesture_pill";
            String overlayName = "com.android.overlay.customization.no_gesture";
            String overlayThemeTarget = "android";
            if (prefValue == 0) {
                mThemeUtils.setOverlayEnabled(category, overlayThemeTarget, overlayThemeTarget);
                mThemeUtils.setOverlayEnabled(category, overlayName, overlayThemeTarget);
            } else {
                mThemeUtils.setOverlayEnabled(category, overlayThemeTarget, overlayThemeTarget);
                AdaptivePreferenceUtils.refreshTheme(getActivity());
            }
        } else if (preference == gestureRadiusPref) {
            systemUtils.showSystemUIRestartDialog(getActivity());
        }
        return true;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.VIEW_UNKNOWN;
    }
}
