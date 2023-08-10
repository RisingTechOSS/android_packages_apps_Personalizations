/**
 * Copyright (C) 2017 The Android Open Source Project
 * Copyright (C) 2020 The "Best Improved Cherry Picked Rom" Project
 * Copyright (C) 2020 Project Fluid
 * Copyright (C) 2023 the RisingOS android project
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

import android.app.ActivityManager
import android.content.Context
import android.graphics.Point
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.SystemProperties
import android.util.DisplayMetrics
import android.view.WindowManager
import com.android.internal.os.PowerProfile
import com.android.internal.util.MemInfoReader
import java.math.BigDecimal
import java.math.RoundingMode

object SpecUtils {
    private const val CPU_MODEL_PROPERTY = "ro.rising.chipset"
    private const val FALLBACK_CPU_MODEL_PROPERTY = "ro.board.platform"
    private val GB2MB = BigDecimal(1024)

    fun getTotalInternalMemorySize(): String {
        val stat = StatFs(Environment.getDataDirectory().path)
        val total = (stat.blockSizeLong * stat.blockCountLong) / 1073741824.0
        return when {
            total <= 16 -> "16"
            total <= 32 -> "32"
            total <= 64 -> "64"
            total <= 128 -> "128"
            total <= 256 -> "256"
            total <= 512 -> "512"
            else -> "512+"
        }
    }

    fun getTotalRAM(): String {
        val memInfoReader = MemInfoReader()
        memInfoReader.readMemInfo()
        val totalMem = memInfoReader.getTotalSize()
        val totalMemGiB = totalMem.toDouble() / (1024 * 1024 * 1024)
        return BigDecimal(totalMemGiB).setScale(0, RoundingMode.UP).toString() + " GB"
    }

    fun getDeviceName(): String = Build.MODEL

    fun getProcessorModel(): String =
        SystemProperties.get(CPU_MODEL_PROPERTY).takeIf { it.isNotEmpty() }
            ?: SystemProperties.get(FALLBACK_CPU_MODEL_PROPERTY).takeIf { it.isNotEmpty() }
            ?: "unknown"

    fun getScreenRes(context: Context): String {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return Point().apply { windowManager.defaultDisplay.getSize(this) }
            .let { "${it.x} x ${it.y + getNavigationBarHeight(windowManager)}" }
    }

    private fun getNavigationBarHeight(wm: WindowManager): Int {
        val metrics = DisplayMetrics()
        wm.defaultDisplay.run {
            getMetrics(metrics)
            val usableHeight = metrics.heightPixels
            getRealMetrics(metrics)
            return metrics.heightPixels - usableHeight
        }
    }

    fun getBatteryCapacity(context: Context): Int {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val capacity = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        return if (capacity == Integer.MIN_VALUE || capacity <= 1100) {
            PowerProfile(context).batteryCapacity.toInt()
        } else {
            capacity
        }
    }   
}
