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

package com.android.settings.preferences.ui

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.util.AttributeSet
import android.widget.ImageView

import com.android.internal.graphics.ColorUtils
import androidx.core.content.ContextCompat

import com.android.settings.R

class BlurWallpaperView : ImageView {

    private val handler = Handler()

    private val wallpaperChecker = object : Runnable {
        override fun run() {
            setWallpaperPreview()
            handler.postDelayed(this, 2000)
        }
    }

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    private fun init(context : Context) {
        setWallpaperPreview()
        setRenderEffect(RenderEffect.createBlurEffect(125f, 125f, Shader.TileMode.CLAMP))
        val color = ContextCompat.getColor(context , R.color.user_crd_bg)
        val fadeFilter = ColorUtils.blendARGB(Color.TRANSPARENT, color, 60 / 100f)
        setColorFilter(fadeFilter, PorterDuff.Mode.SRC_ATOP)
        handler.postDelayed(wallpaperChecker, 2000)
    }

    private fun setWallpaperPreview() {
        val wallpaperManager = WallpaperManager.getInstance(context)
        val wallpaperDrawable: Drawable? = wallpaperManager.drawable
        wallpaperDrawable?.let {
            val wallpaperBitmap = drawableToBitmap(it)
            setImageBitmap(wallpaperBitmap)
        }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeCallbacks(wallpaperChecker)
    }
}
