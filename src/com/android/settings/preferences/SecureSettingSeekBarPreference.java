/*
 * Copyright (C) 2016-2019 crDroid Android Project
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
package com.android.settings.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.android.settings.R;

import com.android.settings.preferences.SecureSettingsStore;
import com.android.settings.preferences.CustomSeekBarPreference;

public class SecureSettingSeekBarPreference extends CustomSeekBarPreference {

    private Position position;

    public SecureSettingSeekBarPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setPreferenceDataStore(new SecureSettingsStore(context.getContentResolver()));
        init(context, attrs);
    }

    public SecureSettingSeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPreferenceDataStore(new SecureSettingsStore(context.getContentResolver()));
        init(context, attrs);
    }

    public SecureSettingSeekBarPreference(Context context) {
        super(context, null);
        setPreferenceDataStore(new SecureSettingsStore(context.getContentResolver()));
        init(context, null);
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
                return R.layout.preference_custom_seekbar_top;
            case BOTTOM:
                return R.layout.preference_custom_seekbar_bottom;
            case MIDDLE:
                return R.layout.preference_custom_seekbar_middle;
            default:
                return R.layout.preference_custom_seekbar_middle;
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
