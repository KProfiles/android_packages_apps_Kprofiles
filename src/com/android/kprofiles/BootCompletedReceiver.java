/*
 * Copyright (C) 2015 The CyanogenMod Project
 *               2017-2019 The LineageOS Project
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

package com.android.kprofiles;

import static com.android.kprofiles.battery.KprofilesSettingsFragment.IS_SUPPORTED;
import static com.android.kprofiles.battery.KprofilesSettingsFragment.KPROFILES_AUTO_KEY;
import static com.android.kprofiles.battery.KprofilesSettingsFragment.KPROFILES_AUTO_NODE;
import static com.android.kprofiles.battery.KprofilesSettingsFragment.KPROFILES_MODES_KEY;
import static com.android.kprofiles.battery.KprofilesSettingsFragment.KPROFILES_MODES_NODE;
import static com.android.kprofiles.battery.KprofilesSettingsFragment.ON;
import static com.android.kprofiles.battery.KprofilesSettingsFragment.OFF;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.android.kprofiles.utils.FileUtils;

public class BootCompletedReceiver extends BroadcastReceiver {
    private static final boolean DEBUG = false;
    private static final String TAG = "KProfiles";

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (DEBUG)
            Log.d(TAG, "Received boot completed intent");

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (FileUtils.fileExists(KPROFILES_AUTO_NODE)) {
            boolean kProfilesAutoEnabled = sharedPrefs.getBoolean(KPROFILES_AUTO_KEY, false);
            FileUtils.writeLine(KPROFILES_AUTO_NODE, kProfilesAutoEnabled ? ON : OFF);
        }
        if (IS_SUPPORTED) {
            String kProfileMode = sharedPrefs.getString(KPROFILES_MODES_KEY, FileUtils.readOneLine(KPROFILES_MODES_NODE));
            FileUtils.writeLine(KPROFILES_MODES_NODE, kProfileMode);
        }
    }
}
