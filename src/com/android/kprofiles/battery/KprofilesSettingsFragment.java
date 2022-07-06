/*
 * Copyright (C) 2022 CannedShroud
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

package com.android.kprofiles.battery;

import android.app.ActionBar;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import com.android.kprofiles.R;
import com.android.kprofiles.utils.FileUtils;

public class KprofilesSettingsFragment extends PreferenceFragment implements
        OnPreferenceChangeListener {

    private SharedPreferences mSharedPrefs;
    private SwitchPreference kProfilesAutoPreference;
    private ListPreference kProfilesModesPreference;
    private Preference kProfilesModesInfo;
    private static final String KPROFILES_AUTO_KEY = "kprofiles_auto";
    private static final String KPROFILES_AUTO_NODE = "/sys/module/kprofiles/parameters/auto_kprofiles";
    private static final String KPROFILES_MODES_KEY = "kprofiles_modes";
    private static final String KPROFILES_MODES_NODE = "/sys/module/kprofiles/parameters/kp_mode";
    private static final String KPROFILES_MODES_INFO = "pref_kprofiles_modes_info";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.kprofiles_settings);
        final ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        kProfilesAutoPreference = (SwitchPreference) findPreference(KPROFILES_AUTO_KEY);
        if (FileUtils.fileExists(KPROFILES_AUTO_NODE)) {
            kProfilesAutoPreference.setEnabled(true);
            kProfilesAutoPreference.setOnPreferenceChangeListener(this);
        } else {
            kProfilesAutoPreference.setSummary(R.string.kprofiles_not_supported);
            kProfilesAutoPreference.setEnabled(false);
        }
        kProfilesModesPreference = (ListPreference) findPreference(KPROFILES_MODES_KEY);
        if (FileUtils.fileExists(KPROFILES_MODES_NODE)) {
            kProfilesModesPreference.setEnabled(true);
            kProfilesModesPreference.setOnPreferenceChangeListener(this);
            updateTitle();
        } else {
            kProfilesModesPreference.setSummary(R.string.kprofiles_not_supported);
            kProfilesModesPreference.setEnabled(false);
        }
        kProfilesModesInfo = (Preference) findPreference(KPROFILES_MODES_INFO);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = LayoutInflater.from(getContext()).inflate(R.layout.kprofiles,
                container, false);
        ((ViewGroup) view).addView(super.onCreateView(inflater, container, savedInstanceState));
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (KPROFILES_AUTO_KEY.equals(preference.getKey())) {
            try {
                FileUtils.writeLine(KPROFILES_AUTO_NODE, (Boolean) newValue ? "Y" : "N");
            } catch(Exception e) { }

        } else if (KPROFILES_MODES_KEY.equals(preference.getKey())) {
            try {
                FileUtils.writeLine(KPROFILES_MODES_NODE, (String) newValue);
                updateTitle();
            } catch(Exception e) { }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().onBackPressed();
            return true;
        }
        return false;
    }

    private String modesDesc() {
        String mode = getCurrentKProfilesMode();
        String descrpition = null;
        switch (mode) {
            case "0":
                descrpition = getString(R.string.kprofiles_modes_none_description);
                break;
            case "1":
                descrpition = getString(R.string.kprofiles_modes_battery_description);
                break;
            case "2":
                descrpition = getString(R.string.kprofiles_modes_balanced_description);
                break;
            case "3":
                descrpition = getString(R.string.kprofiles_modes_performance_description);
                break;
        }
        return descrpition;
    }

    private void updateTitle() {
        Handler.getMain().post(() -> {
            kProfilesModesInfo.setTitle(
                String.format(getString(R.string.kprofiles_modes_description),
                    modesDesc()));
        });
    }

    private String getCurrentKProfilesMode() {
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        return mSharedPrefs.getString(KPROFILES_MODES_KEY, "0");
    }
}
