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

package com.cannedshroud.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.cannedshroud.settings.utils.FileUtils;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

public class BootCompletedReceiver extends BroadcastReceiver {
    private static final boolean DEBUG = false;
    private static final String TAG = "KProfiles";
    private static final String KPROFILES_AUTO_KEY = "kprofiles_auto";
    private static final String KPROFILES_AUTO_NODE = "/sys/module/kprofiles/parameters/auto_kprofiles";
    private static final String KPROFILES_MODES_KEY = "kprofiles_modes";
    private static final String KPROFILES_MODES_NODE = "/sys/module/kprofiles/parameters/mode";

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (DEBUG)
            Log.d(TAG, "Received boot completed intent");
	try {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        boolean kProfileaAutoEnabled = sharedPrefs.getBoolean(KPROFILES_AUTO_KEY, false);
        FileUtils.writeLine(KPROFILES_AUTO_NODE, kProfileaAutoEnabled ? "1" : "0");
        int kProfileMode = sharedPrefs.getInt(KPROFILES_MODES_KEY, 0);
        FileUtils.writeLine(KPROFILES_MODES_NODE, kProfileMode > 0 ? "1" : "0");

    }
}