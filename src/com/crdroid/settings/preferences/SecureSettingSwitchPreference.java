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
package com.crdroid.settings.preferences;

import android.content.Context;
import android.provider.Settings;
import android.os.UserHandle;
import android.util.AttributeSet;

import lineageos.preference.SelfRemovingSwitchPreference;

import com.crdroid.settings.utils.AdaptivePreferenceUtils;

import lineageos.providers.LineageSettings;

public class SecureSettingSwitchPreference extends SelfRemovingSwitchPreference {

    private boolean isLineageSettings;

    public SecureSettingSwitchPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public SecureSettingSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SecureSettingSwitchPreference(Context context) {
        super(context);
        init(context, null);
    }
    
    private void init(Context context, AttributeSet attrs) {
        isLineageSettings = AdaptivePreferenceUtils.isLineageSettings(context, attrs);
        int layoutRes = AdaptivePreferenceUtils.getLayoutResourceId(context, attrs);
        if (layoutRes != -1) {
            setLayoutResource(layoutRes);
        }
    }

    @Override
    protected boolean isPersisted() {
        return isLineageSettings 
            ? LineageSettings.Secure.getString(getContext().getContentResolver(), getKey()) != null 
            : Settings.Secure.getString(getContext().getContentResolver(), getKey()) != null;
    }

    @Override
    protected void putBoolean(String key, boolean value) {
        if (isLineageSettings) {
            LineageSettings.Secure.putInt(getContext().getContentResolver(), key, value ? 1 : 0);
        } else {
            Settings.Secure.putIntForUser(getContext().getContentResolver(), key, value ? 1 : 0, UserHandle.USER_CURRENT);
        }
    }

    @Override
    protected boolean getBoolean(String key, boolean defaultValue) {
        if (isLineageSettings) {
            return LineageSettings.Secure.getInt(getContext().getContentResolver(),
                    key, defaultValue ? 1 : 0) != 0;
        } else {
            return Settings.Secure.getIntForUser(getContext().getContentResolver(),
                    key, defaultValue ? 1 : 0, UserHandle.USER_CURRENT) != 0;
        }
    }
}
