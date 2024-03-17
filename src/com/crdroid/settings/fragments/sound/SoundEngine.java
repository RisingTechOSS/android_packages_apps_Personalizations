/*
 * Copyright (C) 2024 the risingOS Android Project
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

package com.crdroid.settings.fragments.sound;

import android.content.ContentResolver;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.card.MaterialCardView;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;

import com.android.internal.graphics.ColorUtils;
import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.widget.LayoutPreference;

import com.crdroid.settings.preferences.CustomSeekBarPreference;

import android.util.SparseIntArray;
import java.util.HashMap;

public class SoundEngine extends SettingsPreferenceFragment 
    implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "SoundEngine";
    private static final String AUDIO_EFFECT_MODE_KEY = "audio_effect_mode";
    private static final String AUDIO_EFFECT_MODE_ENABLED_KEY = "audio_effect_mode_enabled";

    private static final int MODE_MUSIC = 1;
    private static final int MODE_GAMING = 2;
    private static final int MODE_THEATER = 3;
    private static final int MODE_SMART = 4;

    private MaterialCardView musicCardHolder;
    private MaterialCardView gamingModeCardHolder;
    private MaterialCardView theaterModeCardHolder;
    private MaterialCardView smartModeCardHolder;
    
    private Preference mainSwitcPref;
    private PreferenceScreen screen;
    private LayoutPreference soundEngineMenu;
    
    private final SparseIntArray modeToCardMap = new SparseIntArray();
    private final SparseIntArray modeToImage = new SparseIntArray();
    private final HashMap<Integer, Integer> modeToToastMap = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.sound_engine_settings);
        
        screen = getPreferenceScreen();
        
        mainSwitcPref = screen.findPreference(AUDIO_EFFECT_MODE_ENABLED_KEY);
        mainSwitcPref.setOnPreferenceChangeListener(this);
        
        modeToCardMap.put(MODE_MUSIC, R.id.music_card);
        modeToCardMap.put(MODE_GAMING, R.id.gaming_mode_card);
        modeToCardMap.put(MODE_THEATER, R.id.theater_mode_card);
        modeToCardMap.put(MODE_SMART, R.id.smart_mode_card);

        modeToImage.put(MODE_MUSIC, R.id.music_mode);
        modeToImage.put(MODE_GAMING, R.id.gaming_mode);
        modeToImage.put(MODE_THEATER, R.id.theater_mode);
        modeToImage.put(MODE_SMART, R.id.smart_mode);

        modeToToastMap.put(MODE_MUSIC, R.string.sound_engine_profile_music_activated);
        modeToToastMap.put(MODE_GAMING, R.string.sound_engine_profile_game_activated);
        modeToToastMap.put(MODE_THEATER, R.string.sound_engine_profile_theater_activated);
        modeToToastMap.put(MODE_SMART, R.string.sound_engine_profile_smart_activated);

        final boolean isAudioEffectsEnabled = Settings.System.getIntForUser(getActivity().getContentResolver(),
                AUDIO_EFFECT_MODE_ENABLED_KEY, 0, UserHandle.USER_CURRENT) != 0;

        setUpAudioEngineMenu(isAudioEffectsEnabled);
        highlightSelectedOption((MaterialCardView) soundEngineMenu.findViewById(modeToCardMap.get(getAudioEffectMode())));
    }
    
    private void setUpAudioEngineMenu(boolean enabled) {
        soundEngineMenu = (LayoutPreference) screen.findPreference("sound_engine_profile_menu");
        PreferenceCategory selectProfilesCategory = (PreferenceCategory) screen.findPreference("select_profiles");
        final int fadeFilter = ColorUtils.blendARGB(Color.TRANSPARENT, Color.BLACK, 10 / 100f);
        soundEngineMenu.setVisible(enabled);
        selectProfilesCategory.setVisible(enabled);
        if (enabled) {
            for (int i = 1; i <= MODE_SMART; i++) {
                final int mode = i;
                ImageView cardImage = soundEngineMenu.findViewById(modeToImage.get(mode));
                if (cardImage != null) {
                    cardImage.setColorFilter(fadeFilter, PorterDuff.Mode.SRC_ATOP);
                    cardImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Settings.System.putIntForUser(getActivity().getContentResolver(),
                                    AUDIO_EFFECT_MODE_KEY, mode, UserHandle.USER_CURRENT);
                            highlightSelectedOption((MaterialCardView) soundEngineMenu.findViewById(modeToCardMap.get(mode)));
                            Toast.makeText(getContext(), modeToToastMap.get(mode), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        highlightSelectedOption((MaterialCardView) soundEngineMenu.findViewById(modeToCardMap.get(getAudioEffectMode())));
    }
    
    private int getAudioEffectMode() {
        return Settings.System.getIntForUser(getActivity().getContentResolver(),
                AUDIO_EFFECT_MODE_KEY, 0, UserHandle.USER_CURRENT);
    }

    private void highlightSelectedOption(MaterialCardView selectedCard) {
        for (int i = 0; i < modeToCardMap.size(); i++) {
            int mode = modeToCardMap.keyAt(i);
            MaterialCardView cardHolder = (MaterialCardView) soundEngineMenu.findViewById(modeToCardMap.valueAt(i));
            if (cardHolder != null) {
                cardHolder.setStrokeWidth(0);
            }
        }
        if (selectedCard != null) {
            int borderWidth = getResources().getDimensionPixelSize(R.dimen.selected_card_border_width);
            selectedCard.setStrokeWidth(borderWidth);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mainSwitcPref) {
            final boolean isEnabled = (Boolean) newValue;
            setUpAudioEngineMenu(isEnabled);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CRDROID_SETTINGS;
    }
}
