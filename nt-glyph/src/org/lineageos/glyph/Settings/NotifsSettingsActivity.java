/*
 * Copyright (C) 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.glyph.Settings;

import android.app.Fragment;
import android.os.Bundle;

import com.android.settingslib.collapsingtoolbar.CollapsingToolbarBaseActivity;

public class NotifsSettingsActivity extends CollapsingToolbarBaseActivity {

    private NotifsSettingsFragment mNotifsSettingsFragment;
    private static final String TAG_GLYPH = "glyphnotifs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fragment fragment = getFragmentManager().findFragmentById(com.android.settingslib.collapsingtoolbar.R.id.content_frame);
        if (fragment == null) {
            mNotifsSettingsFragment = new NotifsSettingsFragment();
            getFragmentManager().beginTransaction()
                .add(com.android.settingslib.collapsingtoolbar.R.id.content_frame,
                    mNotifsSettingsFragment, TAG_GLYPH)
                .commit();
        } else {
            mNotifsSettingsFragment = (NotifsSettingsFragment) fragment;
        }
    }
}
