/*
 * Copyright (C) 2021 Project Radiant
 * Copyright (C) 2023 the risingOS Android Project
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

package com.rising.utils

import android.content.Context
import android.os.UserHandle
import android.provider.Settings
import android.util.AttributeSet
import android.widget.ImageView
import java.util.concurrent.ThreadLocalRandom

class bannerHeaderView @JvmOverloads constructor(
    context: Context, 
    attrs: AttributeSet? = null, 
    defStyleAttr: Int = 0
) : ImageView(context, attrs, defStyleAttr) {

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val contentResolver = context.contentResolver
        val mImageDrawable = Settings.System.getIntForUser(contentResolver, "settings_header_image", 1, UserHandle.USER_CURRENT)
        val randomBanner = Settings.System.getIntForUser(contentResolver, "settings_header_image_random", 1, UserHandle.USER_CURRENT) == 1
        val imageNumber = if (randomBanner) ThreadLocalRandom.current().nextInt(1, 98) else mImageDrawable
        val resId = resources.getIdentifier("banner_$imageNumber", "drawable", "com.android.settings")
        setImageResource(resId)
    }
}

