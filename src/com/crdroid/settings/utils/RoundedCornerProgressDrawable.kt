/*
 * Copyright (C) 2023 the RisingOS Android Project
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
package com.crdroid.settings.utils

import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.DrawableWrapper
import android.graphics.drawable.InsetDrawable

class RoundedCornerProgressDrawable(drawable: Drawable? = null) : InsetDrawable(drawable, 0) {

    override fun getChangingConfigurations(): Int {
        return super.getChangingConfigurations() or ActivityInfo.CONFIG_DENSITY
    }

    override fun getConstantState(): ConstantState {
        return RoundedCornerState(super.getConstantState())
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        onLevelChange(level)
    }

    override fun onLayoutDirectionChanged(layoutDirection: Int): Boolean {
        onLevelChange(level)
        return super.onLayoutDirectionChanged(layoutDirection)
    }

    override fun onLevelChange(level: Int): Boolean {
        val drawable = drawable
        if (drawable != null) {
            val bounds = drawable.bounds
            val height = bounds.height()
            val progressWidth = (bounds.width() - height) * level / 10000
            drawable.setBounds(bounds.left, bounds.top, bounds.left + (height + progressWidth), bounds.bottom)
            return super.onLevelChange(progressWidth)
        }
        return super.onLevelChange(level)
    }

    private inner class RoundedCornerState(private val wrappedState: Drawable.ConstantState) :
        Drawable.ConstantState() {

        override fun getChangingConfigurations(): Int {
            return wrappedState.changingConfigurations
        }

        override fun newDrawable(): Drawable {
            return newDrawable(null, null)
        }

        override fun newDrawable(resources: Resources?, theme: Resources.Theme?): Drawable {
            val drawable = wrappedState.newDrawable(resources, theme)
            if (drawable is DrawableWrapper) {
                return RoundedCornerProgressDrawable(drawable.drawable)
            }
            throw IllegalArgumentException("Drawable must be DrawableWrapper")
        }
    }
}

