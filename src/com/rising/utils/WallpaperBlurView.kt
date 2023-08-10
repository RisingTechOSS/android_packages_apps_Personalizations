/*
 * Copyright (C) 2021 Project Radiant
 *               2023 the risingOS Android Project
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
import android.graphics.RenderEffect
import android.graphics.Shader
import android.util.AttributeSet
import android.widget.ImageView

class WallpaperBlurView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ImageView(context, attrs, defStyleAttr) {

    init {
        setRenderEffect(RenderEffect.createBlurEffect(100f, 100f, Shader.TileMode.MIRROR))
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val wallpaperManager = android.app.WallpaperManager.getInstance(context)
        setImageDrawable(wallpaperManager.drawable)
    }
}

