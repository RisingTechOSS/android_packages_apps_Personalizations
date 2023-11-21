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
 
package com.android.settings.preferences;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.AttributeSet;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import com.android.internal.util.rising.systemUtils;
import com.android.settings.R;
import java.util.Arrays;

import android.util.Log;

import com.android.settings.preferences.ui.AdaptivePreferenceUtils;
import com.android.internal.util.rising.ThemeUtils;

public class SystemListPreference extends ListPreference {

    private static final String SYSTEMUI_RESTART = "systemui";
    private static final String SETTINGS_RESTART = "settings";
    private static final String SYSTEM = "system";
    private static final String SECURE = "secure";
    private static final String GLOBAL = "global";
    private static final String NONE = "none";
    
    private static final String overlayThemeTarget  = "com.android.systemui";

    private Context mContext;
    private TypedArray typedArray = null;
    private String restartLevel;
    private String settingsType;
    private boolean shouldReevaluate = false;
    private boolean shouldAddExtraValue = false;

    public SystemListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        try {
            int layoutRes = AdaptivePreferenceUtils.getLayoutResourceId(context, attrs);
            if (layoutRes != -1) {
                setLayoutResource(layoutRes);
            }
            typedArray = context.obtainStyledAttributes(attrs, R.styleable.SystemPreference);
            settingsType = typedArray.getString(R.styleable.SystemPreference_settings_type);
            if (settingsType == null || settingsType.isEmpty()) {
                settingsType = "system";
            }
            restartLevel = typedArray.getString(R.styleable.SystemPreference_restart_level);
            if (restartLevel == null || restartLevel.isEmpty()) {
                restartLevel = "none";
            }
            shouldReevaluate = typedArray.getBoolean(R.styleable.SystemPreference_reevaluate, false);
        } finally {
            typedArray.recycle();
        }
        final String settingsKey = settingsType;
        final String restartKey = restartLevel;
        CharSequence[] entries = getEntries();
        CharSequence[] entryValues = getEntryValues();
        int settingsValue;
        switch (settingsKey) {
            case SYSTEM:
                settingsValue = Settings.System.getIntForUser(context.getContentResolver(), getKey(), 0, ActivityManager.getCurrentUser());
                break;
            case SECURE:
                settingsValue = Settings.Secure.getIntForUser(context.getContentResolver(), getKey(), 0, ActivityManager.getCurrentUser());
                break;
            case GLOBAL:
                settingsValue = Settings.Global.getInt(context.getContentResolver(), getKey(), 0);
                break;
            default:
                settingsValue = 0;
                break;
        }
        if (settingsValue == -1) {
            settingsValue = 0; // treat negative values as 0 since the array starts from 0
            shouldAddExtraValue = true;
        } 
        String currentEntry = entries[settingsValue].toString();
        setValue(currentEntry);
        setSummary(currentEntry);
        setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int value = Integer.parseInt((String) newValue);
                int index = value == -1 ? 0 : value;
                if (shouldAddExtraValue) {
                    index = index + 1;
                }
                if (index >= 0 && index < entries.length) {
                    String selectedEntry = entries[index].toString();
                    setValue(selectedEntry);
                    setSummary(selectedEntry);
                }
                switch (settingsKey) {
                    case SYSTEM:
                        Settings.System.putIntForUser(context.getContentResolver(), getKey(), value, ActivityManager.getCurrentUser());
                        break;
                    case SECURE:
                        Settings.Secure.putIntForUser(context.getContentResolver(), getKey(), value, ActivityManager.getCurrentUser());
                        break;
                    case GLOBAL:
                        Settings.Global.putInt(context.getContentResolver(), getKey(), value);
                        break;
                    default:
                        break;
                }
                switch (restartKey) {
                    case SYSTEM:
                        systemUtils.showSystemRestartDialog(context);
                        break;
                    case SYSTEMUI_RESTART:
                        systemUtils.showSystemUIRestartDialog(context);
                        break;
                    case SETTINGS_RESTART:
                        systemUtils.showSettingsRestartDialog(context);
                        break;
                    case NONE:
                    default:
                        break;
                }
                ThemeUtils mThemeUtils = new ThemeUtils(context);
                if (shouldReevaluate){
                    if (value == 1) {
                        mThemeUtils.setOverlayEnabled("android.theme.customization.sysui_reevaluate", overlayThemeTarget, overlayThemeTarget);
                    } else {
                        mThemeUtils.setOverlayEnabled("android.theme.customization.sysui_reevaluate", "com.android.system.qs.sysui_reevaluate", overlayThemeTarget);
                    }
                }
                return true;
            }
        });
    }
    
    @Override
    protected void onSetInitialValue(Object defaultValue) {
        super.onSetInitialValue(defaultValue);
        CharSequence[] entries = getEntries();
        CharSequence[] entryValues = getEntryValues();
        int value;
        final String settingsKey = settingsType == null ? "system" : settingsType;
        switch (settingsKey) {
            case SYSTEM:
            default:
                value = Settings.System.getIntForUser(getContext().getContentResolver(), getKey(), 0, ActivityManager.getCurrentUser());
                break;
            case SECURE:
                value = Settings.Secure.getIntForUser(getContext().getContentResolver(), getKey(), 0, ActivityManager.getCurrentUser());
                break;
            case GLOBAL:
                value = Settings.Global.getInt(getContext().getContentResolver(), getKey(), 0);
                break;
        }
        int index = value == -1 ? 0 : value;
        if (shouldAddExtraValue) {
            index = index + 1;
        }
        if (index >= 0 && index < entries.length) {
            String currentEntry = entries[index].toString();
            setValue(currentEntry);
            setSummary(currentEntry);
            setValueIndex(index);
        }
        setEntries(entries);
        setEntryValues(entryValues);
    }
}
