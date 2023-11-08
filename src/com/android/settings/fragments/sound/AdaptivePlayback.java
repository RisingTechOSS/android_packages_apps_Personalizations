package com.android.settings.fragments.sound;

import android.content.Context;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.widget.Switch;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.widget.MainSwitchPreference;
import com.android.settingslib.widget.OnMainSwitchChangeListener;

public class AdaptivePlayback extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, OnMainSwitchChangeListener {

    private static final String TAG = AdaptivePlayback.class.getSimpleName();

    private static final String PREF_KEY_ENABLE = "adaptive_playback_enabled";

    private MainSwitchPreference mEnable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.adaptive_playback_settings);

        mEnable = (MainSwitchPreference) findPreference(PREF_KEY_ENABLE);
        boolean enable = Settings.System.getIntForUser(getContext().getContentResolver(),
                Settings.System.ADAPTIVE_PLAYBACK_ENABLED, 0, UserHandle.USER_CURRENT) != 0;
        mEnable.setChecked(enable);
        mEnable.addOnSwitchChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }

    @Override
    public void onSwitchChanged(Switch switchView, boolean isChecked) {
        mEnable.setChecked(isChecked);
        if (isChecked) {
            Settings.System.putIntForUser(getContext().getContentResolver(),
                Settings.System.ADAPTIVE_PLAYBACK_ENABLED, 1, UserHandle.USER_CURRENT);
        } else {
            Settings.System.putIntForUser(getContext().getContentResolver(),
                Settings.System.ADAPTIVE_PLAYBACK_ENABLED, 0, UserHandle.USER_CURRENT);
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.VIEW_UNKNOWN;
    }
}

