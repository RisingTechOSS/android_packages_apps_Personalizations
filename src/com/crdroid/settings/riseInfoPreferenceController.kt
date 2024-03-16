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

package com.crdroid.settings

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Build
import android.os.SystemProperties
import android.provider.Settings
import android.widget.TextView
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import com.android.settings.R
import com.android.settingslib.core.AbstractPreferenceController
import com.android.settingslib.DeviceInfoUtils
import com.android.settingslib.widget.LayoutPreference

import com.crdroid.settings.utils.DeviceInfoUtil

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction

class riseInfoPreferenceController(context: Context) : AbstractPreferenceController(context) {

    private val defaultFallback = mContext.getString(R.string.device_info_default)

    private fun getProp(propName: String): String {
        return SystemProperties.get(propName, defaultFallback)
    }

    private fun getProp(propName: String, customFallback: String): String {
        val propValue = SystemProperties.get(propName)
        return if (propValue.isNotEmpty()) propValue else SystemProperties.get(customFallback, "Unknown")
    }

    private fun getRisingChipset(): String {
        return getProp(PROP_RISING_CHIPSET, "ro.board.platform")
    }

    private fun getDeviceName(): String {
        return "${Build.MANUFACTURER} ${Build.MODEL}"
    }

    private fun getRisingBuildVersion(): String {
        return getProp(PROP_RISING_BUILD_VERSION)
    }

    private fun getRisingSecurity(): String {
        return getProp(PROP_RISING_SECURITY)
    }

    private fun getRisingVersion(): String {
        return SystemProperties.get(PROP_RISING_VERSION, "2.0")
    }

    private fun getRisingBuildStatus(releaseType: String): String {
        return mContext.getString(if (releaseType == "official") R.string.build_is_official_title else R.string.build_is_community_title)
    }

    private fun getRisingMaintainer(releaseType: String): String {
        val risingMaintainer = getProp(PROP_RISING_MAINTAINER)
        if (risingMaintainer.equals("Unknown", ignoreCase = true)) {
            return mContext.getString(R.string.unknown_maintainer)
        }
        return mContext.getString(R.string.maintainer_summary, risingMaintainer)
    }

    override fun displayPreference(screen: PreferenceScreen) {
        super.displayPreference(screen)

        val releaseType = getProp(PROP_RISING_RELEASETYPE).lowercase()
        val codeName = getProp(PROP_RISING_CODE).lowercase()
        val risingMaintainer = getRisingMaintainer(releaseType)
        val isOfficial = releaseType == "official"

        val hwInfoPreference = screen.findPreference<LayoutPreference>(KEY_HW_INFO)!!
        val swInfoPreference = screen.findPreference<LayoutPreference>(KEY_SW_INFO)!!
        val statusPreference = screen.findPreference<Preference>(KEY_BUILD_STATUS)!!
        val aboutHwInfoView: View = hwInfoPreference.findViewById(R.id.about_device_hardware)
        val deviceHardwareCard: View = hwInfoPreference.findViewById<TextView>(R.id.device_hardware)
        val deviceShowcaseCard: View = hwInfoPreference.findViewById<TextView>(R.id.device_showcase_container)

        statusPreference.setTitle(FIRMWARE_NAME + " " + getRisingVersion() + " " + getRisingBuildStatus(releaseType))
        statusPreference.setSummary(risingMaintainer)
        statusPreference.setIcon(if (isOfficial) R.drawable.verified else R.drawable.unverified)

        hwInfoPreference.apply {
            findViewById<TextView>(R.id.device_chipset).text = getRisingChipset()
            findViewById<TextView>(R.id.device_storage).text =
                "${DeviceInfoUtil.getTotalRam()} | ${DeviceInfoUtil.getStorageTotal(mContext)}"
            findViewById<TextView>(R.id.device_battery_capacity).text = DeviceInfoUtil.getBatteryCapacity(mContext)
            findViewById<TextView>(R.id.device_resolution).text = DeviceInfoUtil.getScreenResolution(mContext)
            findViewById<TextView>(R.id.device_showcase).text = getDeviceName()
        }
        
       swInfoPreference.apply {
            findViewById<TextView>(R.id.security_patch_summary).text = getRisingSecurity()
            findViewById<TextView>(R.id.kernel_info_summary).text = DeviceInfoUtils.getFormattedKernelVersion(mContext)
        }
                
        aboutHwInfoView.setOnClickListener {
            if (deviceShowcaseCard.visibility == View.VISIBLE) {
                applyCrossfadeAnimation(deviceHardwareCard, deviceShowcaseCard)
            } else {
                applyCrossfadeAnimation(deviceShowcaseCard, deviceHardwareCard)
            }
        }
    }
    
    private fun applyCrossfadeAnimation(viewToShow: View, viewToHide: View) {
        viewToShow.alpha = 0f
        viewToShow.visibility = View.VISIBLE

        viewToShow.animate()
            .alpha(1f)
            .setDuration(500)
            .setListener(null)

        viewToHide.animate()
            .alpha(0f)
            .setDuration(500)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    viewToHide.visibility = View.GONE
                }
            })
    }

    override fun isAvailable(): Boolean {
        return true
    }

    override fun getPreferenceKey(): String {
        return KEY_DEVICE_INFO
    }

    companion object {
        private const val FIRMWARE_NAME = "RisingUI"
        private const val KEY_KERNEL_INFO = "kernel_version_sw"
        private const val KEY_SW_INFO = "my_device_sw_header"
        private const val KEY_HW_INFO = "my_device_hw_header"
        private const val KEY_DEVICE_INFO = "my_device_info_header"
        private const val KEY_BUILD_STATUS = "rom_build_status"

        private const val PROP_RISING_CODE = "ro.rising.code"
        private const val PROP_RISING_VERSION = "ro.rising.version"
        private const val PROP_RISING_RELEASETYPE = "ro.rising.releasetype"
        private const val PROP_RISING_MAINTAINER = "ro.rising.maintainer"
        private const val PROP_RISING_BUILD_VERSION = "ro.rising.build.version"
        private const val PROP_RISING_CHIPSET = "ro.rising.chipset"
        private const val PROP_RISING_SECURITY = "ro.build.version.security_patch"
    }
}
