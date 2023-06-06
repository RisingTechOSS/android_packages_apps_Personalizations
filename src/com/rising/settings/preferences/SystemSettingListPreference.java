/*
 * Copyright (C) 2016-2018 crDroid Android Project
 * Copyright (C) 2023 the risingOS Android Project
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
package com.rising.settings.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import androidx.preference.ListPreference;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.provider.Settings;

import com.android.settings.R;

import lineageos.preference.SelfRemovingListPreference;

import lineageos.providers.LineageSettings;

public class SystemSettingListPreference extends SelfRemovingListPreference {

    private boolean mAutoSummary = false;
    private Position position;
    private boolean isLineageSettings;

    public SystemSettingListPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        isLineageSettings = getLineageAttribute(context, attrs);
        init(context, attrs);
        if (!isLineageSettings) {
            setPreferenceDataStore(new SystemSettingsStore(context.getContentResolver()));
        }
    }

    public SystemSettingListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        isLineageSettings = getLineageAttribute(context, attrs);
        init(context, attrs);
        if (!isLineageSettings) {
            setPreferenceDataStore(new SystemSettingsStore(context.getContentResolver()));
        }
    }

    public SystemSettingListPreference(Context context) {
        super(context);
        isLineageSettings = getLineageAttribute(context, null);
        init(context, null);
        if (!isLineageSettings) {
            setPreferenceDataStore(new SystemSettingsStore(context.getContentResolver()));
        }
    }

    @Override
    protected boolean isPersisted() {
        if (isLineageSettings) {
            return LineageSettings.System.getString(getContext().getContentResolver(), getKey()) != null;
        } else {
            return Settings.System.getString(getContext().getContentResolver(), getKey()) != null;
        }
    }

    @Override
    protected void putString(String key, String value) {
        if (isLineageSettings) {
            LineageSettings.System.putString(getContext().getContentResolver(), key, value);
        } else {
            Settings.System.putStringForUser(getContext().getContentResolver(), key, value, UserHandle.USER_CURRENT);
        }
    }

    @Override
    protected String getString(String key, String defaultValue) {
        if (isLineageSettings) {
            return LineageSettings.System.getString(getContext().getContentResolver(),
                    key, defaultValue);
        } else {
            return Settings.System.getStringForUser(getContext().getContentResolver(),
                    key, UserHandle.USER_CURRENT);
        }
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);
        if (mAutoSummary || TextUtils.isEmpty(getSummary())) {
            setSummary(getEntry(), true);
        }
    }

    @Override
    public void setSummary(CharSequence summary) {
        setSummary(summary, false);
    }

    private void setSummary(CharSequence summary, boolean autoSummary) {
        mAutoSummary = autoSummary;
        super.setSummary(summary);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        // This is what default ListPreference implementation is doing without respecting
        // real default value:
        //setValue(restoreValue ? getPersistedString(mValue) : (String) defaultValue);
        // Instead, we better do
        setValue(restoreValue ? getPersistedString((String) defaultValue) : (String) defaultValue);
    }

    public int getIntValue(int defValue) {
        return getValue() == null ? defValue : Integer.valueOf(getValue());
    }
    
    private boolean getLineageAttribute(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AdaptivePreference);
        boolean isLineage = typedArray.getBoolean(R.styleable.AdaptivePreference_isLineageSettings, false);
        typedArray.recycle();

        return isLineage;
    }

    private void init(Context context, AttributeSet attrs) {
        // Retrieve and set the layout resource based on position
        // otherwise do not set any layout
        position = getPosition(context, attrs);
        if (position != null) {
            int layoutResId = getLayoutResourceId(position);
            setLayoutResource(layoutResId);
        }
    }

    private Position getPosition(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AdaptivePreference);
        String positionAttribute = typedArray.getString(R.styleable.AdaptivePreference_position);
        typedArray.recycle();

        Position positionFromAttribute = Position.fromAttribute(positionAttribute);
        if (positionFromAttribute != null) {
            return positionFromAttribute;
        }

        return null;
    }

    private int getLayoutResourceId(Position position) {
        switch (position) {
            case TOP:
                return R.layout.arc_card_about_top;
            case BOTTOM:
                return R.layout.arc_card_about_bottom;
            case MIDDLE:
                return R.layout.arc_card_about_middle;
            default:
                return R.layout.arc_card_about_middle;
        }
    }

    private enum Position {
        TOP,
        MIDDLE,
        BOTTOM;

        public static Position fromAttribute(String attribute) {
            if (attribute != null) {
                switch (attribute.toLowerCase()) {
                    case "top":
                        return TOP;
                    case "bottom":
                        return BOTTOM;
                    case "middle":
                        return MIDDLE;
                        
                }
            }
            return null;
        }
    }
}
