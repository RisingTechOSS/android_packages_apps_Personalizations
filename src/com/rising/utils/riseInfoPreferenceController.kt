/*
 * Copyright (C) 2020 Wave-OS
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

package com.rising.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemProperties
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.rising.utils.SpecUtils
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import com.android.settings.R
import com.android.settingslib.core.AbstractPreferenceController
import com.android.settingslib.widget.LayoutPreference

class riseInfoPreferenceController(context: Context) : AbstractPreferenceController(context) {

    private val defaultFallback = mContext.getString(R.string.device_info_default)

    private fun getPropertyOrDefault(propName: String): String {
        return SystemProperties.get(propName, defaultFallback)
    }

    private fun getDeviceName(): String {
        val device = getPropertyOrDefault(PROP_RISING_DEVICE)
        return if (device.isEmpty() || device == defaultFallback) "${Build.MANUFACTURER} ${Build.MODEL}" else device
    }

    private fun getRisingBuildVersion(): String {
        return getPropertyOrDefault(PROP_RISING_BUILD_VERSION)
    }

    private fun getRisingVersion(): String {
        return getPropertyOrDefault(PROP_RISING_VERSION) + " | " +
               getPropertyOrDefault(PROP_RISING_VERSION_CODE) + " | " +
               getPropertyOrDefault(PROP_RISING_BUILD_TYPE)
    }

    private fun getRisingReleaseType(): String {
        val releaseType = getPropertyOrDefault(PROP_RISING_RELEASETYPE)
        return releaseType.substring(0, 1).uppercase() +
               releaseType.substring(1).lowercase()
    }

    private fun getRisingBuildStatus(releaseType: String): String {
        return mContext.getString(if (releaseType == "official") R.string.build_is_official_title else R.string.build_is_community_title)
    }

    private fun getRisingMaintainer(releaseType: String): String {
        val risingMaintainer = getPropertyOrDefault(PROP_RISING_MAINTAINER)
        if (risingMaintainer.equals("Unknown", ignoreCase = true)) {
            return mContext.getString(R.string.unknown_maintainer)
        }
        return mContext.getString(R.string.maintainer_summary, risingMaintainer)
    }

    override fun displayPreference(screen: PreferenceScreen) {
        super.displayPreference(screen)

        val releaseType = getPropertyOrDefault(PROP_RISING_RELEASETYPE).lowercase()
        val risingMaintainer = getRisingMaintainer(releaseType)
        val isOfficial = releaseType == "official"
        
        val risingInfoPreference = screen.findPreference<LayoutPreference>(KEY_RISING_INFO)!!
        val deviceInfoPreference = screen.findPreference<LayoutPreference>(KEY_DEVICE_INFO)!!

        screen.findPreference<Preference>(KEY_BUILD_VERSION)!!.summary = getRisingBuildVersion()
        screen.findPreference<Preference>(KEY_RISING_VERSION)!!.summary = getRisingVersion()
        screen.findPreference<Preference>(KEY_RISING_DEVICE)!!.summary = getDeviceName()

        risingInfoPreference.apply {
            findViewById<TextView>(R.id.chipset_summary).text = SpecUtils.getProcessorModel()
            findViewById<TextView>(R.id.cust_storage_summary).text = "${SpecUtils.getTotalInternalMemorySize()}GB ROM + ${SpecUtils.getTotalRAM()} RAM"
            findViewById<TextView>(R.id.cust_battery_summary).text = "${SpecUtils.getBatteryCapacity(mContext)} mAh"
            findViewById<TextView>(R.id.cust_display_summary).text = SpecUtils.getScreenRes(mContext)
        }

        deviceInfoPreference.apply {
            findViewById<TextView>(R.id.maintainer).text = risingMaintainer
            findViewById<TextView>(R.id.hashtag).text = getRisingBuildStatus(releaseType)
            findViewById<ImageView>(R.id.rom_status_icon).setImageResource(if (isOfficial) R.drawable.verified else R.drawable.unverified)
        }

        val clickMap = mapOf(
            R.id.battery_info to Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.Settings\$PowerUsageSummaryActivity")),
            R.id.chipset_info to Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.Settings\$DevRunningServicesActivity")),
            R.id.display_info to Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.Settings\$DisplaySettingsActivity")),
            R.id.storage_info to Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.Settings\$StorageDashboardActivity"))
        )

        clickMap.forEach { (id, intent) ->
            risingInfoPreference.findViewById<LinearLayout>(id)?.setOnClickListener {
                mContext.startActivity(intent)
            }
        }
    }

    override fun isAvailable(): Boolean {
        return true
    }

    override fun getPreferenceKey(): String {
        return KEY_RISING_INFO
    }

    companion object {
        private const val KEY_RISING_INFO = "rising_info"
        private const val KEY_DEVICE_INFO = "my_device_info_header"
        private const val KEY_STORAGE = "storage"
        private const val KEY_CHIPSET = "chipset"
        private const val KEY_BATTERY = "battery"
        private const val KEY_DISPLAY = "display"
        private const val KEY_RISING_DEVICE = "rising_device"
        private const val KEY_RISING_VERSION = "rising_version"
        private const val KEY_BUILD_VERSION = "rising_build_version"

        private const val PROP_RISING_VERSION = "ro.rising.version"
        private const val PROP_RISING_VERSION_CODE = "ro.rising.code"
        private const val PROP_RISING_RELEASETYPE = "ro.rising.releasetype"
        private const val PROP_RISING_MAINTAINER = "ro.rising.maintainer"
        private const val PROP_RISING_DEVICE = "ro.rising.device"
        private const val PROP_RISING_BUILD_TYPE = "ro.rising.packagetype"
        private const val PROP_RISING_BUILD_VERSION = "ro.rising.build.version"
    }
}
