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

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import com.android.settings.R
import com.android.settingslib.core.AbstractPreferenceController
import com.android.settingslib.widget.LayoutPreference

class personalizationSettingsController(context: Context) : AbstractPreferenceController(context) {

    override fun displayPreference(screen: PreferenceScreen) {
        super.displayPreference(screen)
        
        val personalizationPreference = screen.findPreference<LayoutPreference>(KEY_PERSONALIZATIONS)!!

        val clickMap = mapOf(
            R.id.themes_card to Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.Settings\$PersonalizationsLockscreenActivity")),
            R.id.system_themes to Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.Settings\$PersonalizationsThemesActivity")),
            R.id.toolbox to Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.Settings\$PersonalizationsToolboxActivity"))
       )

        clickMap.forEach { (id, intent) ->
            personalizationPreference.findViewById<View>(id)?.setOnClickListener {
                mContext.startActivity(intent)
            }
       }
    }

    override fun isAvailable(): Boolean {
        return true
    }

    override fun getPreferenceKey(): String {
        return KEY_PERSONALIZATIONS
    }

    companion object {
        private const val KEY_PERSONALIZATIONS = "personalization_dashboard_quick_access"
    }
}
