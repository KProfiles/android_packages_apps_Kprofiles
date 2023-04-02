package com.android.kprofiles.battery;

import static com.android.kprofiles.battery.KprofilesSettingsFragment.INTENT_ACTION;
import static com.android.kprofiles.battery.KprofilesSettingsFragment.KPROFILES_MODES_NODE;
import static com.android.kprofiles.battery.KprofilesSettingsFragment.KPROFILES_MODES_KEY;
import static com.android.kprofiles.battery.KprofilesSettingsFragment.IS_SUPPORTED;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.UserHandle;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import androidx.preference.PreferenceManager;

import com.android.kprofiles.R;
import com.android.kprofiles.utils.FileUtils;

public class KProfilesModesTileService extends TileService {

    private Context mContext;
    private boolean mSelfChange = false;

    @Override
    public void onCreate() {
        if (IS_SUPPORTED) {
            super.onCreate();
            mContext = getApplicationContext();
            return;
        }
        Tile tile = getQsTile();
        tile.setState(Tile.STATE_UNAVAILABLE);
        tile.updateTile();
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
        if (!IS_SUPPORTED) return;
        super.onStartListening();

        // Registering observers
        IntentFilter filter = new IntentFilter();
        filter.addAction(INTENT_ACTION);
        mContext.registerReceiver(mServiceStateReceiver, filter);

        updateTileContent();
    }

    @Override
    public void onStopListening() {
        mContext.unregisterReceiver(mServiceStateReceiver);
        super.onStopListening();
    }

    @Override
    public void onClick() {
        if (!IS_SUPPORTED) return;
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
        updateTileContent(mode);
        super.onClick();
    }

    private void setMode(String mode) {
        FileUtils.writeLine(KPROFILES_MODES_NODE, mode);
        mSelfChange = true;
        Intent intent = new Intent(INTENT_ACTION);
        intent.setFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        prefs.edit().putString(KPROFILES_MODES_KEY, mode).apply();
    }

    private String getMode() {
        final String value = FileUtils.readOneLine(KPROFILES_MODES_NODE);
        return value != null ? value : "0";
    }

    private void updateTileContent() {
        updateTileContent(null);
    }

    private void updateTileContent(String mode) {
        Tile tile = getQsTile();
        if (mode == null) mode = getMode();

        tile.setState(mode != "0" ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
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

    private final BroadcastReceiver mServiceStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!INTENT_ACTION.equals(intent.getAction())) return;
            if (mSelfChange) {
                mSelfChange = false;
                return;
            }
            updateTileContent();
        }
    };
}
