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
import androidx.preference.PreferenceGroup;

import java.util.ArrayList;
import java.util.List;
import android.util.Log;

import com.android.settings.R;

public class PreferenceUtils {
    private static final String TAG = "PreferenceUtils";

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
            if (preference.isVisible()) {
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
