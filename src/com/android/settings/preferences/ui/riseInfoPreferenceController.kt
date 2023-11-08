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

package com.android.settings.preferences.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemProperties
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import com.android.settings.R
import com.android.settingslib.core.AbstractPreferenceController
import com.android.settingslib.DeviceInfoUtils
import com.android.settingslib.widget.LayoutPreference

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction

class riseInfoPreferenceController(context: Context) : AbstractPreferenceController(context) {

    private val defaultFallback = mContext.getString(R.string.device_info_default)

    private fun getPropertyOrDefault(propName: String): String {
        return SystemProperties.get(propName, defaultFallback)
    }

    private fun getDeviceName(): String {
        return "${Build.MANUFACTURER} ${Build.MODEL}"
    }

    private fun getRisingBuildVersion(): String {
        return getPropertyOrDefault(PROP_RISING_BUILD_VERSION)
    }

    private fun getRisingStorage(): String {
        return SystemProperties.get(PROP_RISING_RAM, "0gb") + "/" + SystemProperties.get(PROP_RISING_STORAGE, "0gb")
    }

    private fun getRisingChipset(): String {
        return getPropertyOrDefault(PROP_RISING_CHIPSET)
    }

    private fun getRisingBattery(): String {
        return getPropertyOrDefault(PROP_RISING_BATTERY)
    }
    
    private fun getRisingResolution(): String {
        return getPropertyOrDefault(PROP_RISING_DISPLAY)
    }

    private fun getRisingSecurity(): String {
        return getPropertyOrDefault(PROP_RISING_SECURITY)
    }

    private fun getRisingVersion(): String {
        return SystemProperties.get(PROP_RISING_VERSION, "2.0")
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
        val codeName = getPropertyOrDefault(PROP_RISING_CODE).lowercase()
        val risingMaintainer = getRisingMaintainer(releaseType)
        val isOfficial = releaseType == "official"
        
        val hwInfoPreference = screen.findPreference<LayoutPreference>(KEY_HW_INFO)!!
        val swInfoPreference = screen.findPreference<LayoutPreference>(KEY_SW_INFO)!!
        val sw2InfoPreference = screen.findPreference<LayoutPreference>(KEY_SW2_INFO)!!
        val deviceInfoPreference = screen.findPreference<LayoutPreference>(KEY_DEVICE_INFO)!!
        val aboutHwInfoView: View = hwInfoPreference.findViewById(R.id.about_device_hardware)
        val hwInfoView: View = hwInfoPreference.findViewById(R.id.device_hardware)
        val phoneImage: View = hwInfoPreference.findViewById(R.id.phone_image_container)
        val blurView: View = hwInfoPreference.findViewById(R.id.blurView)

        deviceInfoPreference.apply {
            findViewById<TextView>(R.id.firmware_version).text = "risingUI " + getRisingVersion() + " | " + codeName
            findViewById<TextView>(R.id.firmware_build_summary).text = risingMaintainer
            findViewById<TextView>(R.id.build_variant_title).text = getRisingBuildStatus(releaseType)
        }

        hwInfoPreference.apply {
            findViewById<TextView>(R.id.device_name).text = getDeviceName()
            findViewById<TextView>(R.id.device_chipset).text = getRisingChipset()
            findViewById<TextView>(R.id.device_storage).text = getRisingStorage()
            findViewById<TextView>(R.id.device_battery_capacity).text = getRisingBattery()
            findViewById<TextView>(R.id.device_resolution).text = getRisingResolution()
            findViewById<TextView>(R.id.device_name_model).text = getDeviceName()
        }

        aboutHwInfoView.setOnClickListener {
            if (hwInfoView.visibility == View.VISIBLE) {
                hwInfoView.visibility = View.GONE
                blurView.visibility = View.GONE
                phoneImage.visibility = View.VISIBLE
            } else {
                hwInfoView.visibility = View.VISIBLE
                blurView.visibility = View.VISIBLE
                phoneImage.visibility = View.GONE
            }
        }

        swInfoPreference.apply {
            findViewById<TextView>(R.id.android_version_summary).text = mContext.getString(R.string.device_info_platform_version)
        }
        
        sw2InfoPreference.apply {
            findViewById<TextView>(R.id.security_patch_summary).text = getRisingSecurity()
            findViewById<TextView>(R.id.kernel_info_summary).text = DeviceInfoUtils.getFormattedKernelVersion(mContext)
        }

        val clickMap = mapOf(
            R.id.android_version_details to Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.Settings\$FirmwareVersionActivity"))
           // R.id.chipset_info to Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.Settings\$DevRunningServicesActivity")),
          //  R.id.display_info to Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.Settings\$DisplaySettingsActivity")),
           // R.id.storage_info to Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.Settings\$StorageDashboardActivity"))
       )

        clickMap.forEach { (id, intent) ->
            swInfoPreference.findViewById<View>(id)?.setOnClickListener {
                mContext.startActivity(intent)
            }
       }
    }

    override fun isAvailable(): Boolean {
        return true
    }

    override fun getPreferenceKey(): String {
        return KEY_DEVICE_INFO
    }

    companion object {
        private const val KEY_HW_INFO = "my_device_hw_header"
        private const val KEY_SW_INFO = "my_device_sw_header"
        private const val KEY_SW2_INFO = "my_device_sw2_header"
        private const val KEY_DEVICE_INFO = "my_device_info_header"
        
        private const val KEY_STORAGE = "device_storage"
        private const val KEY_CHIPSET = "device_chipset"
        private const val KEY_BATTERY = "device_battery_capacity"
        private const val KEY_DISPLAY = "device_resolution"

        private const val PROP_RISING_CODE = "ro.rising.code"
        private const val PROP_RISING_VERSION = "ro.rising.version"
        private const val PROP_RISING_RELEASETYPE = "ro.rising.releasetype"
        private const val PROP_RISING_MAINTAINER = "ro.rising.maintainer"
        private const val PROP_RISING_DEVICE = "ro.rising.device"
        private const val PROP_RISING_BUILD_TYPE = "ro.rising.packagetype"
        private const val PROP_RISING_BUILD_VERSION = "ro.rising.build.version"
        private const val PROP_RISING_CHIPSET = "ro.rising.chipset"
        private const val PROP_RISING_STORAGE = "ro.rising.storage"
        private const val PROP_RISING_RAM = "ro.rising.ram"
        private const val PROP_RISING_BATTERY = "ro.rising.battery"
        private const val PROP_RISING_DISPLAY = "ro.rising.display_resolution"
        private const val PROP_RISING_SECURITY = "ro.build.version.security_patch"
    }
}
