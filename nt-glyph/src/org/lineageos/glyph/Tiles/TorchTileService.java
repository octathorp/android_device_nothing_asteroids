/*
 * Copyright (C) 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.glyph.Tiles;

import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import org.lineageos.glyph.R;
import org.lineageos.glyph.Constants.Constants;
import org.lineageos.glyph.Manager.StatusManager;
import org.lineageos.glyph.Utils.FileUtils;
import org.lineageos.glyph.Utils.ResourceUtils;

/** Quick settings tile: Glyph **/
public class TorchTileService extends TileService {

    @Override
    public void onStartListening() {
        super.onStartListening();
        updateState();
    }

    private void updateState() {
        boolean enabled = getEnabled();
        getQsTile().setContentDescription(enabled ?
                getString(R.string.glyph_accessibility_quick_settings_on) :
                getString(R.string.glyph_accessibility_quick_settings_off));
        getQsTile().setState(enabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        getQsTile().updateTile();
    }

    @Override
    public void onClick() {
        super.onClick();
        setEnabled(!getEnabled());
        updateState();
    }

    private boolean getEnabled() {
        return StatusManager.isAllLedActive();
    }

    private void setEnabled(boolean enabled) {
        StatusManager.setAllLedsActive(enabled);
        FileUtils.writeAllLed(enabled ? Constants.getMaxBrightness() : 0);
        if (StatusManager.isEssentialLedActive() && !enabled)
            FileUtils.writeSingleLed(
                ResourceUtils.getInteger("glyph_settings_notifs_essential_led"),
                Constants.getMaxBrightness( )/ 100 * 7);
    }
}
