/*
 * Copyright (C) 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.glyph.Tiles;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import org.lineageos.glyph.Settings.SettingsActivity;

public class TileActivity extends Activity {

    private static final boolean DEBUG = true;
    private static final String TAG = "TileActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (DEBUG) Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        ComponentName sourceClass = getIntent().getParcelableExtra(Intent.EXTRA_COMPONENT_NAME);
        if (DEBUG) Log.d(TAG, "sourceClass: " + sourceClass.getClassName());
        if (sourceClass.getClassName().equals("org.lineageos.glyph.Tiles.GlyphTileService")
            || sourceClass.getClassName().equals("org.lineageos.glyph.Tiles.TorchTileService")) {
            openActivitySafely(new Intent(this, SettingsActivity.class));
        } else {
            finish();
        }
    }

    private void openActivitySafely(Intent dest) {
        if (DEBUG) Log.d(TAG, "openActivitySafely");
        try {
            dest.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
            finish();
            startActivity(dest);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "No activity found for " + dest);
            finish();
        }
    }
}
