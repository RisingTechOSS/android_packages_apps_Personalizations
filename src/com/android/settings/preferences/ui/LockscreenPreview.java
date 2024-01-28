/*
 * Copyright (C) 2024 The risingOS Android Project
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

import android.content.Context;
import android.provider.Settings;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.android.settings.R;

public class LockscreenPreview extends LinearLayout {

    public LockscreenPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        String lockscreenWidgets = Settings.System.getString(getContext().getContentResolver(), "lockscreen_widgets");
        String lockscreenExtras = Settings.System.getString(getContext().getContentResolver(), "lockscreen_widgets_extras");
        boolean hasLockscreenWidgets = lockscreenWidgets != null && !lockscreenWidgets.isEmpty();
        boolean hasLockscreenExtras = lockscreenExtras != null && !lockscreenExtras.isEmpty();

        int previewHeight;
        if (hasLockscreenWidgets && hasLockscreenExtras) {
            previewHeight = (int) getResources().getDimension(R.dimen.lockscreen_preview_height_all);
        } else if (hasLockscreenWidgets || hasLockscreenExtras) {
            previewHeight = (int) getResources().getDimension(R.dimen.lockscreen_preview_height_with_widgets);
        } else {
            previewHeight = (int) getResources().getDimension(R.dimen.lockscreen_preview_height_with_no_widgets);
        }
        int newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(previewHeight, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, newHeightMeasureSpec);
    }
}
