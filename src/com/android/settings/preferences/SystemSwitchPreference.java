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
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.AttributeSet;
import android.widget.Toast;

import androidx.preference.SwitchPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.android.internal.util.rising.systemUtils;

import com.android.settings.R;
import com.android.settings.preferences.ui.AdaptivePreferenceUtils;

import com.android.internal.util.rising.ThemeUtils;

import android.util.Log;

public class SystemSwitchPreference extends SwitchPreference {

    private static final String TAG = "SystemSwitchPreference";

    private static final String PREFS_NAME = "system_store_";
    private static final String SYSTEMUI_RESTART = "systemui";
    private static final String SETTINGS_RESTART = "settings";
    private static final String NONE = "none";
    private static final String SYSTEM = "system";
    private static final String SECURE = "secure";
    private static final String GLOBAL = "global";
    private static final String overlayThemeTarget  = "com.android.systemui";
    private TypedArray typedArray = null;
    String restartLevel;
    String settingsType;
    private boolean shouldReevaluate = false;

    private Context mContext;

    public SystemSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        try {
            typedArray = context.obtainStyledAttributes(attrs, R.styleable.SystemPreference);
            restartLevel = typedArray.getString(R.styleable.SystemPreference_restart_level);
            if (restartLevel == null || restartLevel.isEmpty()) {
                restartLevel = "none";
            }
            settingsType = typedArray.getString(R.styleable.SystemPreference_settings_type);
            if (settingsType == null || settingsType.isEmpty()) {
                settingsType = "system";
            }
            shouldReevaluate = typedArray.getBoolean(R.styleable.SystemPreference_reevaluate, false);
        } finally {
            typedArray.recycle();
        }
        mContext = context;
        int layoutRes = AdaptivePreferenceUtils.getLayoutResourceId(context, attrs);
        if (layoutRes != -1) {
            setLayoutResource(layoutRes);
        }
    }
    
    @Override
    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        if (getKey() != null) {
            init();
        }
    }

    private void init() {
        final String settingsKey = settingsType == null ? "system" : settingsType;
        final String restartKey = restartLevel == null ? "none" : restartLevel;
        final ThemeUtils mThemeUtils = new ThemeUtils(mContext);
        boolean isChecked;
        switch (settingsKey) {
            case SYSTEM:
                isChecked = Settings.System.getIntForUser(mContext.getContentResolver(), getKey(), 0, ActivityManager.getCurrentUser()) != 0;
                break;
            case SECURE:
                isChecked = Settings.Secure.getIntForUser(mContext.getContentResolver(), getKey(), 0, ActivityManager.getCurrentUser()) != 0;
                break;
            case GLOBAL:
                isChecked = Settings.Global.getInt(mContext.getContentResolver(), getKey(), 0) != 0;
                break;
            default:
                isChecked = false;
                break;
        }
        setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean value = (boolean) newValue;
                int intValue = value ? 1 : 0;
                setChecked(value);
                switch (settingsKey) {
                    case SYSTEM:
                        Settings.System.putIntForUser(mContext.getContentResolver(), getKey(), intValue, ActivityManager.getCurrentUser());
                        break;
                    case SECURE:
                        Settings.Secure.putIntForUser(mContext.getContentResolver(), getKey(), intValue, ActivityManager.getCurrentUser());
                        break;
                    case GLOBAL:
                        Settings.Global.putInt(mContext.getContentResolver(), getKey(), intValue);
                        break;
                    default:
                        break;
                }
                switch (restartKey) {
                    case SYSTEM:
                        systemUtils.showSystemRestartDialog(mContext);
                        break;
                    case SYSTEMUI_RESTART:
                        systemUtils.showSystemUIRestartDialog(mContext);
                        break;
                    case SETTINGS_RESTART:
                        systemUtils.showSettingsRestartDialog(mContext);
                        break;
                    case NONE:
                    default:
                        break;
                }
                if (shouldReevaluate) {
                    Toast.makeText(mContext, mContext.getString(R.string.reevaluating_theme), Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                                mThemeUtils.setOverlayEnabled("android.theme.customization.sysui_reevaluate", overlayThemeTarget, overlayThemeTarget);
                                mThemeUtils.setOverlayEnabled("android.theme.customization.sysui_reevaluate", "com.android.system.qs.sysui_reevaluate", overlayThemeTarget);
                        }
                    }, Toast.LENGTH_SHORT + 500L);
                }
                return true;
            }
        });
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                setChecked(isChecked);
            }
        });
    }
}
