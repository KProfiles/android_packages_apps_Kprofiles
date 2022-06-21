package com.android.kprofiles.battery;

import android.content.Context;
import android.content.SharedPreferences;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import androidx.preference.PreferenceManager;

import com.android.kprofiles.R;
import com.android.kprofiles.utils.FileUtils;

public class KProfilesModesTileService extends TileService {

    private Context mContext;
    private SharedPreferences mSharedPrefs;

    private static final String KPROFILES_MODES_KEY = "kprofiles_modes";
    private static final String KPROFILES_MODES_NODE = "/sys/module/kprofiles/parameters/kp_mode";

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onTileAdded() {
        super.onTileAdded();
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
    }

    @Override
    public void onStartListening() {
        updateTileContent();
        super.onStartListening();
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
    }

    @Override
    public void onClick() {
        String mode = getMode();
        switch (mode) {
            case "0":
                mode = "1"; // Set mode from none to battery
                break;
            case "1":
                mode = "2"; // Set mode from battery to balanced
                break;
            case "2":
                mode = "3"; // Set mode from balanced to performance
                break;
            case "3":
                mode = "0"; // Set mode from performance to none
                break;
        }
        setMode(mode);
        updateTileContent();
        super.onClick();
    }

    private void setMode(String mode) {
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        mSharedPrefs.edit().putString(KPROFILES_MODES_KEY, mode).apply();
        FileUtils.writeLine(KPROFILES_MODES_NODE, mode);
    }

    private String getMode() {
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        return mSharedPrefs.getString(KPROFILES_MODES_KEY, "0");
    }

    private void updateTileContent() {
        boolean isActive;
        Tile tile = getQsTile();
        String mode = getMode();

        if (mode != "0") {
            isActive = true;
        } else {
            isActive = false;
        }

        tile.setState(isActive ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        switch (mode) {
            case "0":
                tile.setContentDescription(getResources().getString(R.string.kprofiles_modes_none));
                tile.setSubtitle(getResources().getString(R.string.kprofiles_modes_none));
                break;
            case "1":
                tile.setContentDescription(getResources().getString(R.string.kprofiles_modes_battery));
                tile.setSubtitle(getResources().getString(R.string.kprofiles_modes_battery));
                break;
            case "2":
                tile.setContentDescription(getResources().getString(R.string.kprofiles_modes_balanced));
                tile.setSubtitle(getResources().getString(R.string.kprofiles_modes_balanced));
                break;
            case "3":
                tile.setContentDescription(getResources().getString(R.string.kprofiles_modes_performance));
                tile.setSubtitle(getResources().getString(R.string.kprofiles_modes_performance));
                break;
        }
        tile.updateTile();
    }
}
