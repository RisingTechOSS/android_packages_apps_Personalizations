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

import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemProperties
import android.provider.Settings
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.view.LayoutInflater
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import com.android.settings.R
import com.android.settings.preferences.ui.DeviceInfoUtil
import com.android.settingslib.core.AbstractPreferenceController
import com.android.settingslib.DeviceInfoUtils
import com.android.settingslib.widget.LayoutPreference

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
        val swInfoPreference = screen.findPreference<LayoutPreference>(KEY_DEVICE_INFO)!!
        val statusPreference = screen.findPreference<Preference>(KEY_BUILD_STATUS)!!
        val deviceText = swInfoPreference.findViewById<TextView>(R.id.device_name_model)
        val editBtn = swInfoPreference.findViewById<TextView>(R.id.edit_device_name_model)

        statusPreference.setTitle(getRisingBuildStatus(releaseType))
        statusPreference.setSummary(risingMaintainer)
        statusPreference.setIcon(if (isOfficial) R.drawable.verified else R.drawable.unverified)

        val settingsDeviceName = Settings.Global.getString(
            mContext.contentResolver,
            Settings.Global.DEVICE_NAME
        )

        if (!settingsDeviceName.isNullOrBlank()) {
            deviceText.text = settingsDeviceName
        }

        editBtn.setOnClickListener {
            showEditDialog(deviceText)
        }

        hwInfoPreference.apply {
            findViewById<TextView>(R.id.device_chipset).text = getRisingChipset()
            findViewById<TextView>(R.id.device_storage).text = DeviceInfoUtil.getTotalRam() + " | " + DeviceInfoUtil.getStorageTotal(mContext)
            findViewById<TextView>(R.id.device_battery_capacity).text = DeviceInfoUtil.getBatteryCapacity(mContext)
            findViewById<TextView>(R.id.device_resolution).text = DeviceInfoUtil.getScreenResolution(mContext)
        }
    }

    private fun showEditDialog(tv: TextView) {
        val layoutInflater = LayoutInflater.from(mContext)
        val promptView = layoutInflater.inflate(R.layout.edit_text_dialog, null)
        val editText = promptView.findViewById<EditText>(R.id.editText)
        val currentDeviceName = Settings.Global.getString(
            mContext.contentResolver,
            Settings.Global.DEVICE_NAME
        )
        editText.hint = currentDeviceName
        AlertDialog.Builder(mContext)
            .setTitle(R.string.edit_device_name_title)
            .setView(promptView)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val deviceName = editText.text.toString().trim()
                Settings.Global.putString(
                    mContext.contentResolver,
                    Settings.Global.DEVICE_NAME,
                    deviceName
                )
                tv.text = deviceName
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun isAvailable(): Boolean {
        return true
    }

    override fun getPreferenceKey(): String {
        return KEY_DEVICE_INFO
    }

    companion object {
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
