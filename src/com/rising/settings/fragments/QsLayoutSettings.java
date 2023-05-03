/*
 * Copyright (C) 2022 The Nameless-AOSP Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.rising.settings.fragments;

import android.database.ContentObserver;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.systemui.qs.QSLayoutUtils;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.android.settingslib.widget.LayoutPreference;

import com.rising.settings.preferences.CustomSeekBarPreference;
import com.rising.settings.preferences.SystemSettingSwitchPreference;
import com.rising.settings.preferences.SystemSettingListPreference;

import com.android.internal.util.rising.ThemeUtils;

public class QsLayoutSettings extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String KEY_QS_HIDE_LABEL = "qs_tile_label_hide";
    private static final String KEY_QS_VERTICAL_LAYOUT = "qs_tile_vertical_layout";
    private static final String KEY_QS_COLUMN_PORTRAIT = "qs_layout_columns";
    private static final String KEY_QS_ROW_PORTRAIT = "qs_layout_rows";
    private static final String KEY_QQS_ROW_PORTRAIT = "qqs_layout_rows";
    private static final String KEY_APPLY_CHANGE_BUTTON = "apply_change_button";
    private static final String QS_PAGE_TRANSITIONS = "custom_transitions_page_tile";
    private static final String KEY_QS_PANEL_STYLE  = "qs_panel_style";
    private static final String KEY_QS_UI_STYLE  = "qs_ui_style";
    private static final String overlayThemeTarget  = "com.android.systemui";

    private Context mContext;

    private CustomSeekBarPreference mQsColumns;
    private CustomSeekBarPreference mQsRows;
    private CustomSeekBarPreference mQqsRows;
    private SystemSettingListPreference mPageTransitions;
    private SystemSettingListPreference mQsStyle;
    private SystemSettingListPreference mQsUI;
    private ThemeUtils mThemeUtils;
    private Handler mHandler;

    private Button mApplyChange;

    private SystemSettingSwitchPreference mHide;
    private SystemSettingSwitchPreference mVertical;

    private int[] currentValue = new int[2];

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        addPreferencesFromResource(R.xml.qs_layout_settings);

	mThemeUtils = new ThemeUtils(getActivity());

        final Context mContext = getActivity().getApplicationContext();
        final ContentResolver resolver = mContext.getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        mQsStyle = (SystemSettingListPreference) findPreference(KEY_QS_PANEL_STYLE);
        mQsUI = (SystemSettingListPreference) findPreference(KEY_QS_UI_STYLE);
        mCustomSettingsObserver.observe();

        mPageTransitions = (SystemSettingListPreference) findPreference(QS_PAGE_TRANSITIONS);
        mPageTransitions.setOnPreferenceChangeListener(this);
        int customTransitions = Settings.System.getIntForUser(resolver,
                Settings.System.CUSTOM_TRANSITIONS_KEY,
                0, UserHandle.USER_CURRENT);
        mPageTransitions.setValue(String.valueOf(customTransitions));
        mPageTransitions.setSummary(mPageTransitions.getEntry());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mQsColumns = (CustomSeekBarPreference) findPreference(KEY_QS_COLUMN_PORTRAIT);
        mQsColumns.setOnPreferenceChangeListener(this);

        mQsRows = (CustomSeekBarPreference) findPreference(KEY_QS_ROW_PORTRAIT);
        mQsRows.setOnPreferenceChangeListener(this);

        mQqsRows = (CustomSeekBarPreference) findPreference(KEY_QQS_ROW_PORTRAIT);
        mQqsRows.setOnPreferenceChangeListener(this);

        mContext = getContext();

        LayoutPreference preference = findPreference(KEY_APPLY_CHANGE_BUTTON);
        mApplyChange = (Button) preference.findViewById(R.id.apply_change);
        mApplyChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mApplyChange.isEnabled()) {
                    final int[] newValue = {
                        mQsRows.getValue() * 10 + mQsColumns.getValue(),
                        mQqsRows.getValue() * 10 + mQsColumns.getValue()
                    };
                    Settings.System.putIntForUser(getContentResolver(),
                            Settings.System.QS_LAYOUT, newValue[0], UserHandle.USER_CURRENT);
                    Settings.System.putIntForUser(getContentResolver(),
                            Settings.System.QQS_LAYOUT, newValue[1], UserHandle.USER_CURRENT);
                    if (QSLayoutUtils.updateLayout(mContext)) {
                        currentValue[0] = newValue[0];
                        currentValue[1] = newValue[1];
                        mApplyChange.setEnabled(false);
                    } else {
                        Settings.System.putIntForUser(getContentResolver(),
                                Settings.System.QS_LAYOUT, currentValue[0], UserHandle.USER_CURRENT);
                        Settings.System.putIntForUser(getContentResolver(),
                                Settings.System.QQS_LAYOUT, currentValue[1], UserHandle.USER_CURRENT);
                        Toast.makeText(mContext, R.string.qs_apply_change_failed, Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        initPreference();

        final boolean hideLabel = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.QS_TILE_LABEL_HIDE, 0, UserHandle.USER_CURRENT) == 1;

        mHide = (SystemSettingSwitchPreference) findPreference(KEY_QS_HIDE_LABEL);
        mHide.setOnPreferenceChangeListener(this);

        mVertical = (SystemSettingSwitchPreference) findPreference(KEY_QS_VERTICAL_LAYOUT);
        mVertical.setEnabled(!hideLabel);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mHide) {
            boolean hideLabel = (Boolean) newValue;
            mVertical.setEnabled(!hideLabel);
        } else if (preference == mQsColumns) {
            int qs_columns = Integer.parseInt(newValue.toString());
            mApplyChange.setEnabled(
                currentValue[0] != mQsRows.getValue() * 10 + qs_columns ||
                currentValue[1] != mQqsRows.getValue() * 10 + qs_columns
            );
        } else if (preference == mQsRows) {
            int qs_rows = Integer.parseInt(newValue.toString());
            mQqsRows.setMax(qs_rows - 1);
            if (mQqsRows.getValue() > qs_rows - 1) {
                mQqsRows.setValue(qs_rows - 1);
            }
            mApplyChange.setEnabled(
                currentValue[0] != qs_rows * 10 + mQsColumns.getValue() ||
                currentValue[1] != mQqsRows.getValue() * 10 + mQsColumns.getValue()
            );
        } else if (preference == mQqsRows) {
            int qqs_rows = Integer.parseInt(newValue.toString());
            mApplyChange.setEnabled(
                currentValue[0] != mQsRows.getValue() * 10 + mQsColumns.getValue() ||
                currentValue[1] != qqs_rows * 10 + mQsColumns.getValue()
            );
	} else if (preference == mPageTransitions) {
            int customTransitions = Integer.parseInt(((String) newValue).toString());
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.CUSTOM_TRANSITIONS_KEY, customTransitions, UserHandle.USER_CURRENT);
            int index = mPageTransitions.findIndexOfValue((String) newValue);
            mPageTransitions.setSummary(
                    mPageTransitions.getEntries()[index]);
        } else if (preference == mQsStyle || preference == mQsUI) {
            mCustomSettingsObserver.observe();
        }
        return true;
    }


    private CustomSettingsObserver mCustomSettingsObserver = new CustomSettingsObserver(mHandler);
    private class CustomSettingsObserver extends ContentObserver {

        CustomSettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            Context mContext = getContext();
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.QS_PANEL_STYLE),
                    false, this, UserHandle.USER_ALL);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.QS_UI_STYLE),
                    false, this, UserHandle.USER_ALL);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (uri.equals(Settings.System.getUriFor(Settings.System.QS_PANEL_STYLE)) || uri.equals(Settings.System.getUriFor(Settings.System.QS_UI_STYLE))) {
                updateQsStyle();
            }
        }
    }

    private void updateQsStyle() {
        ContentResolver resolver = getActivity().getContentResolver();

        boolean isA11Style = Settings.System.getIntForUser(getContext().getContentResolver(),
                Settings.System.QS_UI_STYLE , 1, UserHandle.USER_CURRENT) == 1;

        int qsPanelStyle = Settings.System.getIntForUser(getContext().getContentResolver(),
                Settings.System.QS_PANEL_STYLE , 0, UserHandle.USER_CURRENT);

	String qsUIStyleCategory = "android.theme.customization.qs_ui";
	String qsPanelStyleCategory = "android.theme.customization.qs_panel";

	/// reset all overlays before applying
	resetQsOverlays(qsPanelStyleCategory);
	resetQsOverlays(qsUIStyleCategory);

	if (isA11Style) {
	    setQsStyle("com.android.system.qs.ui.A11", qsUIStyleCategory);
	}

	if (qsPanelStyle == 0) return;

        switch (qsPanelStyle) {
            case 1:
              setQsStyle("com.android.system.qs.outline", qsPanelStyleCategory);
              break;
            case 2:
            case 3:
              setQsStyle("com.android.system.qs.twotoneaccent", qsPanelStyleCategory);
              break;
            case 4:
              setQsStyle("com.android.system.qs.shaded", qsPanelStyleCategory);
              break;
            case 5:
              setQsStyle("com.android.system.qs.cyberpunk", qsPanelStyleCategory);
              break;
            case 6:
              setQsStyle("com.android.system.qs.neumorph", qsPanelStyleCategory);
              break;
            case 7:
              setQsStyle("com.android.system.qs.reflected", qsPanelStyleCategory);
              break;
            case 8:
              setQsStyle("com.android.system.qs.surround", qsPanelStyleCategory);
              break;
            case 9:
              setQsStyle("com.android.system.qs.thin", qsPanelStyleCategory);
              break;
            case 10:
              setQsStyle("com.android.system.qs.twotoneaccenttrans", qsPanelStyleCategory);
              break;
            default:
              break;
        }
    }

    public void resetQsOverlays(String category) {
        mThemeUtils.setOverlayEnabled(category, overlayThemeTarget, overlayThemeTarget);
    }

    public void setQsStyle(String overlayName, String category) {
        mThemeUtils.setOverlayEnabled(category, overlayName, overlayThemeTarget);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }

    private void initPreference() {
        final int index_qs = Settings.System.getIntForUser(getContentResolver(),
            Settings.System.QS_LAYOUT, 42, UserHandle.USER_CURRENT);
        final int index_qqs = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.QQS_LAYOUT, 22, UserHandle.USER_CURRENT);
        mQsColumns.setValue(index_qs % 10);
        mQsRows.setValue(index_qs / 10);
        mQqsRows.setValue(index_qqs / 10);
        mQqsRows.setMax(mQsRows.getValue() - 1);
        currentValue[0] = index_qs;
        currentValue[1] = index_qqs;
    }
}
