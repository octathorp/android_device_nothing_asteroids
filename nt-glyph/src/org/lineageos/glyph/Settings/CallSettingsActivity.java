/*
 * Copyright (C) 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.glyph.Settings;

import android.app.Fragment;
import android.os.Bundle;

import com.android.settingslib.collapsingtoolbar.CollapsingToolbarBaseActivity;

public class CallSettingsActivity extends CollapsingToolbarBaseActivity {

    private CallSettingsFragment mCallSettingsFragment;
    private static final String TAG_GLYPH = "glyphcall";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fragment fragment = getFragmentManager().findFragmentById(com.android.settingslib.collapsingtoolbar.R.id.content_frame);
        if (fragment == null) {
            mCallSettingsFragment = new CallSettingsFragment();
            getFragmentManager().beginTransaction()
                .add(com.android.settingslib.collapsingtoolbar.R.id.content_frame,
                    mCallSettingsFragment, TAG_GLYPH)
                .commit();
        } else {
            mCallSettingsFragment = (CallSettingsFragment) fragment;
        }
    }
}
