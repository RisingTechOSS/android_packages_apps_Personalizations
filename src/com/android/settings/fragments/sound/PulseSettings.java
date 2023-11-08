package com.android.settings.fragments.sound;

import android.content.Context;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class PulseSettings extends SettingsPreferenceFragment {

    private static final String TAG = PulseSettings.class.getSimpleName();

    private static final String PULSE_SMOOTHING_KEY = "pulse_smoothing_enabled";
    private static final String PULSE_RENDER_CATEGORY_SOLID = "pulse_2";
    private static final String PULSE_RENDER_CATEGORY_FADING = "pulse_fading_bars_category";
    private static final String PULSE_COLOR_MODE = "pulse_color_mode";
    private static final String PULSE_RENDER_MODE = "pulse_render_style";
    private static final int RENDER_STYLE_FADING_BARS = 0;
    private static final int RENDER_STYLE_SOLID_LINES = 1;
    private static final int COLOR_TYPE_ACCENT = 0;
    private static final int COLOR_TYPE_USER = 1;
    private static final int COLOR_TYPE_LAVALAMP = 2;
    private static final int COLOR_TYPE_AUTO = 3;

    private static final String PULSE_SETTINGS_FOOTER = "pulse_settings_footer";

    private SwitchPreference mPulseSmoothing;
    private Preference mRenderMode;
    private Preference mColorModePref;
    private Preference mLavaSpeedPref;
    private Preference mFooterPref;

    private PreferenceCategory mFadingBarsCat;
    private PreferenceCategory mSolidBarsCat;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pulse_settings);

        ContentResolver resolver = getActivity().getContentResolver();

        mFadingBarsCat = (PreferenceCategory) findPreference(
                PULSE_RENDER_CATEGORY_FADING);
        mSolidBarsCat = (PreferenceCategory) findPreference(
                PULSE_RENDER_CATEGORY_SOLID);
        mColorModePref = (Preference) findPreference(
                PULSE_COLOR_MODE);
        mRenderMode = (Preference) findPreference(
                PULSE_RENDER_MODE);

        mPulseSmoothing = (SwitchPreference) findPreference(PULSE_SMOOTHING_KEY);

        mFooterPref = findPreference(PULSE_SETTINGS_FOOTER);
        mFooterPref.setTitle(R.string.pulse_help_policy_notice_summary);

        updateAllPrefs();
    }

    private void updateAllPrefs() {
        ContentResolver resolver = getContext().getContentResolver();

        boolean navbarPulse = Settings.Secure.getIntForUser(resolver,
                Settings.Secure.NAVBAR_PULSE_ENABLED, 0, UserHandle.USER_CURRENT) != 0;
        boolean lockscreenPulse = Settings.Secure.getIntForUser(resolver,
                Settings.Secure.LOCKSCREEN_PULSE_ENABLED, 0, UserHandle.USER_CURRENT) != 0;
        boolean ambientPulse = Settings.Secure.getIntForUser(resolver,
                Settings.Secure.AMBIENT_PULSE_ENABLED, 0, UserHandle.USER_CURRENT) != 0;

        mPulseSmoothing.setEnabled(navbarPulse || lockscreenPulse || ambientPulse);

        mColorModePref.setEnabled(navbarPulse || lockscreenPulse || ambientPulse);
        if (navbarPulse || lockscreenPulse || ambientPulse) {
            int colorMode = Settings.Secure.getIntForUser(resolver,
                Settings.Secure.PULSE_COLOR_MODE, COLOR_TYPE_LAVALAMP, UserHandle.USER_CURRENT);
        } else {
            mLavaSpeedPref.setEnabled(false);
        }

        mRenderMode.setEnabled(navbarPulse || lockscreenPulse || ambientPulse);
        if (navbarPulse || lockscreenPulse || ambientPulse) {
            int renderMode = Settings.Secure.getIntForUser(resolver,
                Settings.Secure.PULSE_RENDER_STYLE, RENDER_STYLE_SOLID_LINES, UserHandle.USER_CURRENT);
            updateRenderCategories(renderMode);
        } else {
            mFadingBarsCat.setEnabled(false);
            mSolidBarsCat.setEnabled(false);
        }

        mFooterPref.setEnabled(navbarPulse || lockscreenPulse || ambientPulse);
    }

    private void updateRenderCategories(int mode) {
        mFadingBarsCat.setEnabled(mode == RENDER_STYLE_FADING_BARS);
        mSolidBarsCat.setEnabled(mode == RENDER_STYLE_SOLID_LINES);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.VIEW_UNKNOWN;
    }
}
