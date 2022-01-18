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

package com.cannedshroud.settings.battery;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;
import androidx.preference.ListPreference;

import com.cannedshroud.settings.R;
import org.lineageos.settings.utils.FileUtils;

public class KprofilesSettingsFragment extends PreferenceFragment implements
        OnPreferenceChangeListener {

    private SwitchPreference kProfilesAutoPreference;
    private ListPreference kProfilesModesPreference;
    private static final String KPROFILES_AUTO_KEY = "kprofiles_auto";
    private static final String KPROFILES_AUTO_NODE = "/sys/module/kprofiles/parameters/auto_kprofiles";
    private static final String KPROFILES_MODES_KEY = "kprofiles_modes";
    private static final String KPROFILES_MODES_NODE = "/sys/module/kprofiles/parameters/mode";
    private static final String KPROFILES_MODES_DEFAULT = "2";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.kprofiles_settings);
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
        } else {
            kProfilesModesPreference.setSummary(R.string.kprofiles_not_supported);
            kProfilesModesPreference.setEnabled(false);
        }
        
    }
    
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (KPROFILES_AUTO_KEY.equals(preference.getKey())) {
            try {
                FileUtils.writeLine(KPROFILES_AUTO_NODE, (Boolean) newValue ? "1" : "0");
            } catch(Exception e) { }

        } else if (KPROFILES_MODES_KEY.equals(preference.getKey())) {
            try {
                FileUtils.writeLine(KPROFILES_MODES_NODE, (String) newValue);
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

}