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
import android.view.View;
import android.widget.SeekBar;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.res.TypedArrayUtils;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.android.internal.util.rising.systemUtils;
import com.android.settings.R;

import com.android.settings.widget.SeekBarPreference;
import com.android.settings.preferences.ui.AdaptivePreferenceUtils;

public class SystemSeekBarPreference extends SeekBarPreference {

    private static final String SYSTEMUI_RESTART = "systemui";
    private static final String SETTINGS_RESTART = "settings";
    private static final String SYSTEM = "system";
    private static final String SECURE = "secure";
    private static final String GLOBAL = "global";
    private static final String NONE = "none";

    private Context mContext;
    private SeekBar mSeekBar;
    private TypedArray typedArray = null;
    private String restartLevel;
    private String settingsType;
    private int minValue;

    public SystemSeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs, TypedArrayUtils.getAttr(context,
                R.attr.preferenceStyle,
                android.R.attr.preferenceStyle));
        mContext = context;
        typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.SystemPreference);
        restartLevel = typedArray.getString(R.styleable.SystemPreference_restart_level);
        if (restartLevel == null || restartLevel.isEmpty()) {
            restartLevel = "none";
        }
        settingsType = typedArray.getString(R.styleable.SystemPreference_settings_type);
        if (settingsType == null || settingsType.isEmpty()) {
            settingsType = "system";
        }
        minValue = typedArray.getInteger(R.styleable.SystemPreference_minValue, 0);
        typedArray.recycle();
        int layoutRes = AdaptivePreferenceUtils.getLayoutResourceId(context, attrs);
        if (layoutRes != -1) {
            setLayoutResource(layoutRes);
        }
    }
    
    @Override
    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        mSeekBar = (SeekBar) view.findViewById(com.android.internal.R.id.seekbar);
        mSeekBar.setVisibility(View.VISIBLE);
        init();
    }

    private void init() {
        if (mSeekBar == null) {
            return;
        }
        final String settingsKey = settingsType == null ? "system" : settingsType;
        final String restartKey = restartLevel == null ? "none" : restartLevel;
        int currentValue;
        switch (settingsKey) {
            case SYSTEM:
                currentValue = Settings.System.getIntForUser(mContext.getContentResolver(), getKey(), 0, ActivityManager.getCurrentUser());
                break;
            case SECURE:
                currentValue = Settings.Secure.getIntForUser(mContext.getContentResolver(), getKey(), 0, ActivityManager.getCurrentUser());
                break;
            case GLOBAL:
                currentValue = Settings.Global.getInt(mContext.getContentResolver(), getKey(), 0);
                break;
            default:
                currentValue = 0;
                break;
        }
        if (currentValue < minValue) {
            currentValue = minValue;
        }
        setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int value = (int) newValue;
                if (value < minValue) {
                    value = minValue;
                }
                mSeekBar.setProgress(value);
                switch (settingsKey) {
                    case SYSTEM:
                        Settings.System.putIntForUser(mContext.getContentResolver(), getKey(), value, ActivityManager.getCurrentUser());
                        break;
                    case SECURE:
                        Settings.Secure.putIntForUser(mContext.getContentResolver(), getKey(), value, ActivityManager.getCurrentUser());
                        break;
                    case GLOBAL:
                        Settings.Global.putInt(mContext.getContentResolver(), getKey(), value);
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

                return true;
            }
        });
        mSeekBar.setProgress(currentValue);
    }
}
