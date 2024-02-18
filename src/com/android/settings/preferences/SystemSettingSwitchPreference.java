/*
 * Copyright (C) 2016-2018 crDroid Android Project
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
package com.android.settings.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.provider.Settings;
import android.os.UserHandle;
import android.util.AttributeSet;

import com.android.settings.R;

import lineageos.preference.SelfRemovingSwitchPreference;

import lineageos.providers.LineageSettings;

import com.android.settings.preferences.ui.AdaptivePreferenceUtils;

public class SystemSettingSwitchPreference extends SelfRemovingSwitchPreference {

    private boolean isLineageSettings;

    public SystemSettingSwitchPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
         isLineageSettings = getLineageAttribute(context, attrs);
         init(context, attrs);
    }

    public SystemSettingSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
         isLineageSettings = getLineageAttribute(context, attrs);
         init(context, attrs);
    }

    public SystemSettingSwitchPreference(Context context) {
        super(context);
    }

    @Override
    protected boolean isPersisted() {
        return isLineageSettings ? LineageSettings.System.getString(getContext().getContentResolver(), getKey()) != null : Settings.System.getString(getContext().getContentResolver(), getKey()) != null;
    }

    @Override
    protected void putBoolean(String key, boolean value) {
        if (isLineageSettings) {
            LineageSettings.System.putInt(getContext().getContentResolver(), key, value ? 1 : 0);
        } else {
            Settings.System.putIntForUser(getContext().getContentResolver(), key, value ? 1 : 0, UserHandle.USER_CURRENT);
        }
    }

    @Override
    protected boolean getBoolean(String key, boolean defaultValue) {
        if (isLineageSettings) {
            return LineageSettings.System.getInt(getContext().getContentResolver(),
                    key, defaultValue ? 1 : 0) != 0;
        } else {
            return Settings.System.getIntForUser(getContext().getContentResolver(),
                    key, defaultValue ? 1 : 0, UserHandle.USER_CURRENT) != 0;
        }
    }

    private boolean getLineageAttribute(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AdaptivePreference);
        boolean isLineage = typedArray.getBoolean(R.styleable.AdaptivePreference_isLineageSettings, false);
        typedArray.recycle();

        return isLineage;
    }

    private void init(Context context, AttributeSet attrs) {
        int layoutRes = AdaptivePreferenceUtils.getLayoutResourceId(context, attrs);
        if (layoutRes != -1) {
            setLayoutResource(layoutRes);
        }
    }
}
