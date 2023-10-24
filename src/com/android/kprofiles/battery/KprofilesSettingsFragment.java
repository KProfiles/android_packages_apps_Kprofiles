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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;

import com.android.kprofiles.R;
import com.android.kprofiles.utils.FileUtils;

public class KprofilesSettingsFragment extends PreferenceFragment implements
        OnPreferenceChangeListener {

    private SwitchPreference kProfilesAutoPreference;
    private ListPreference kProfilesModesPreference;
    private Preference kProfilesModesInfo;
    private boolean mSelfChange = false;

    public static final String INTENT_ACTION = "com.android.kprofiles.battery.KPROFILE_CHANGED";
    public static final String KPROFILES_MODES_NODE = "/sys/kernel/kprofiles/kp_mode";
    public static final String KPROFILES_AUTO_KEY = "kprofiles_auto";
    public static final String KPROFILES_AUTO_NODE = "/sys/module/kprofiles/parameters/auto_kp";
    public static final String KPROFILES_MODES_KEY = "kprofiles_modes";
    public static final String KPROFILES_MODES_INFO = "pref_kprofiles_modes_info";
    public static final String ON = "Y";
    public static final String OFF = "N";
    public static final boolean IS_SUPPORTED = FileUtils.fileExists(KPROFILES_MODES_NODE);

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
        if (IS_SUPPORTED) {
            kProfilesModesPreference.setEnabled(true);
            kProfilesModesPreference.setOnPreferenceChangeListener(this);
        } else {
            kProfilesModesPreference.setSummary(R.string.kprofiles_not_supported);
            kProfilesModesPreference.setEnabled(false);
        }
        kProfilesModesInfo = (Preference) findPreference(KPROFILES_MODES_INFO);
        kProfilesModesInfo.setEnabled(IS_SUPPORTED);

        updateValues();

        // Registering observers
        IntentFilter filter = new IntentFilter();
        filter.addAction(INTENT_ACTION);
        getContext().registerReceiver(mServiceStateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
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
    public void onResume() {
        super.onResume();
        if (kProfilesAutoPreference == null || kProfilesModesPreference == null
                || kProfilesModesInfo == null) return;
        updateValues();
    }

    @Override
    public void onDestroy() {
        getContext().unregisterReceiver(mServiceStateReceiver);
        super.onDestroy();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final String key = preference.getKey();
        switch (key) {
            case KPROFILES_AUTO_KEY:
                final boolean enabled = (Boolean) newValue;
                try {
                    FileUtils.writeLine(KPROFILES_AUTO_NODE, enabled ? ON : OFF);
                } catch(Exception e) { }
                break;
            case KPROFILES_MODES_KEY:
                final String value = (String) newValue;
                try {
                    FileUtils.writeLine(KPROFILES_MODES_NODE, value);
                    updateTitle(value);
                    mSelfChange = true;
                    Intent intent = new Intent(INTENT_ACTION);
                    intent.setFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
                    getContext().sendBroadcastAsUser(intent, UserHandle.CURRENT);
                } catch(Exception e) { }
                break;
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

    private String modesDesc(String mode) {
        if (mode == null) mode = "0";
        String descrpition = null;
        if (!IS_SUPPORTED) return getString(R.string.kprofiles_not_supported);
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
            default:
                descrpition = getString(R.string.kprofiles_modes_none_description);
                break;
        }
        return descrpition;
    }

    private void updateTitle(String value) {
        Handler.getMain().post(() -> {
            kProfilesModesInfo.setTitle(
                String.format(getString(R.string.kprofiles_modes_description),
                    modesDesc(value)));
        });
    }

    private void updateValues() {
        if (FileUtils.fileExists(KPROFILES_AUTO_NODE)) {
            final String value = FileUtils.readOneLine(KPROFILES_AUTO_NODE);
            kProfilesAutoPreference.setChecked(value != null && value.equals(ON));
        }

        if (IS_SUPPORTED) {
            final String value = FileUtils.readOneLine(KPROFILES_MODES_NODE);
            kProfilesModesPreference.setValue(value != null ? value : "0");
            updateTitle(value);
        }
    }

    private final BroadcastReceiver mServiceStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!INTENT_ACTION.equals(intent.getAction())) return;
            if (mSelfChange) {
                mSelfChange = false;
                return;
            }
            updateValues();
        }
    };
}
