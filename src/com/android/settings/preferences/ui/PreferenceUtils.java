/*
 * Copyright (C) 2023 The risingOS Android Project
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

package com.android.settings.preferences.ui;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceGroup;
import android.util.Log;

import com.android.settingslib.widget.IllustrationPreference;
import com.android.settingslib.widget.UsageProgressBarPreference;
import com.android.settingslib.widget.LayoutPreference;
import com.android.settingslib.widget.FooterPreference;

import com.android.settings.notification.IncreasingRingVolumePreference;
import com.android.settings.fuelgauge.batteryusage.PowerGaugePreference;
import com.android.settings.widget.CardPreference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.android.settings.R;

public class PreferenceUtils {
    private static final String TAG = "PreferenceUtils";

    public static void setupExtraPreferences(
            List<String> topPrefs,
            List<String> middlePrefs,
            List<String> bottomPrefs,
            List<String> soloPrefs,
            PreferenceGroup screen) {
        setupExtraPreferences(topPrefs, middlePrefs, bottomPrefs, soloPrefs, screen, false);
    }

    public static void setupExtraPreferences(
            List<String> topPrefs,
            List<String> middlePrefs,
            List<String> bottomPrefs,
            List<String> soloPrefs,
            PreferenceGroup screen,
            boolean forceThemeMiddle) {
        if (screen == null || 
            (topPrefs.isEmpty() && middlePrefs.isEmpty() 
            && bottomPrefs.isEmpty() && soloPrefs.isEmpty())) {
            return;
        }
        List<Preference> allPreferences = getAllPreferences(screen);
        for (Preference preference : allPreferences) {
            String key = preference.getKey();
            if (key != null) {
                int layoutResource = getLayoutResourceForKey(key, topPrefs, middlePrefs, bottomPrefs, soloPrefs, forceThemeMiddle);
                if (layoutResource != 0 && !getExcludedPrefClass().contains(preference.getClass())) {
                    preference.setLayoutResource(layoutResource);
                }
            }
        }
    }
    
    private static List<Class<?>> getExcludedPrefClass() {
        List<Class<?>> exclusionList = Arrays.asList(
            CardPreference.class,
            FooterPreference.class,
            IllustrationPreference.class,
            IncreasingRingVolumePreference.class,
            LayoutPreference.class,
            PowerGaugePreference.class,
            PreferenceCategory.class,
            UsageProgressBarPreference.class
        );
        return exclusionList;
    }

    private static int getLayoutResourceForKey(String key, List<String> topPrefs, List<String> middlePrefs,
                                                List<String> bottomPrefs, List<String> soloPrefs,
                                                boolean forceThemeMiddle) {
        if (!topPrefs.isEmpty() && topPrefs.contains(key)) {
            return R.layout.top_level_preference_top_card;
        } else if (!middlePrefs.isEmpty() && middlePrefs.contains(key)) {
            return R.layout.top_level_preference_middle_card;
        } else if (!bottomPrefs.isEmpty() && bottomPrefs.contains(key)) {
            return R.layout.top_level_preference_bottom_card;
        } else if (!soloPrefs.isEmpty() && soloPrefs.contains(key)) {
            return R.layout.top_level_preference_solo_card;
        } else if (forceThemeMiddle && middlePrefs.isEmpty()) {
            return R.layout.top_level_preference_middle_card;
        } else {
            return 0;
        }
    }

    public static void setLayoutResources(List<Preference> preferences) {
        int minOrder = Integer.MAX_VALUE;
        int maxOrder = Integer.MIN_VALUE;
        int topCardLayoutResId = R.layout.top_level_preference_top_card;
        int middleCardLayoutResId = R.layout.top_level_preference_middle_card;
        int bottomCardLayoutResId = R.layout.top_level_preference_bottom_card;

        for (Preference preference : preferences) {
            if (preference.isVisible()) {
                int order = preference.getOrder();
                if (order < minOrder) {
                    minOrder = order;
                }
                if (order > maxOrder) {
                    maxOrder = order;
                }
            }
        }

        for (Preference preference : preferences) {
            if (preference.isVisible() && !getExcludedPrefClass().contains(preference.getClass())) {
                int order = preference.getOrder();
                if (order == minOrder) {
                    preference.setLayoutResource(topCardLayoutResId);
                } else if (order == maxOrder) {
                    preference.setLayoutResource(bottomCardLayoutResId);
                } else {
                    preference.setLayoutResource(middleCardLayoutResId);
                }
            }
        }
    }

    private static void logPreferenceKey(Preference preference) {
        String key = preference.getKey();
        if (key != null) {
            Log.d(TAG, "Preference Key: " + key);
        }
    }

    public static List<Preference> getAllPreferences(PreferenceGroup preferenceGroup) {
        List<Preference> preferences = new ArrayList<>();
        for (int i = 0; i < preferenceGroup.getPreferenceCount(); i++) {
            Preference preference = preferenceGroup.getPreference(i);
            if (preference.isVisible()) {
                preferences.add(preference);
                if (preference instanceof PreferenceGroup) {
                    preferences.addAll(getAllPreferences((PreferenceGroup) preference));
                }
                logPreferenceKey(preference);
            }
        }
        return preferences;
    }
}
