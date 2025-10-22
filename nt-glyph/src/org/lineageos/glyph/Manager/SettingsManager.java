/*
 * Copyright (C) 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.glyph.Manager;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.android.internal.util.ArrayUtils;

import java.util.HashSet;
import java.util.Set;

import org.lineageos.glyph.Constants.Constants;
import org.lineageos.glyph.Utils.FileUtils;
import org.lineageos.glyph.Utils.ResourceUtils;

public final class SettingsManager {

    private static final String TAG = "GlyphSettingsManager";
    private static final boolean DEBUG = true;

    private static Context context = Constants.CONTEXT;

    public static boolean enableGlyph(boolean enable) {
        return Settings.Secure.putInt(context.getContentResolver(),
                Constants.GLYPH_ENABLE, enable ? 1 : 0);
    }

    public static boolean isGlyphEnabled() {
        return Settings.Secure.getInt(context.getContentResolver(),
                Constants.GLYPH_ENABLE, 1) != 0;
    }

    public static boolean isGlyphFlipEnabled() {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(Constants.GLYPH_FLIP_ENABLE, false) && isGlyphEnabled();
    }

    public static int getGlyphBrightness() {
        int[] levels = Constants.getBrightnessLevels();
        int brightnessSetting = getGlyphBrightnessSetting();
        return levels[brightnessSetting - 1];
    }

    public static int getGlyphBrightnessSetting() {
        int d = 3; if (FileUtils.readLine("/mnt/vendor/persist/color") == "white") d = 2;
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(Constants.GLYPH_BRIGHTNESS, d);
    }

    public static boolean isGlyphChargingEnabled() {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(Constants.GLYPH_CHARGING_LEVEL_ENABLE, false) && isGlyphEnabled();
    }

    public static boolean isGlyphPowershareEnabled() {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(Constants.GLYPH_CHARGING_POWERSHARE_ENABLE, false) && isGlyphEnabled();
    }

    public static boolean isGlyphCallEnabled() {
        return Settings.Secure.getInt(context.getContentResolver(),
                Constants.GLYPH_CALL_ENABLE, 1) != 0 && isGlyphEnabled();
    }

    public static boolean setGlyphCallEnabled(boolean enable) {
        return Settings.Secure.putInt(context.getContentResolver(),
                Constants.GLYPH_CALL_ENABLE, enable ? 1 : 0);
    }

    public static String getGlyphCallAnimation() {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(Constants.GLYPH_CALL_SUB_ANIMATIONS,
                        ResourceUtils.getString("glyph_settings_call_animations_default"));
    }

    public static boolean isGlyphMusicVisualizerEnabled() {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(Constants.GLYPH_MUSIC_VISUALIZER_ENABLE, false) && isGlyphEnabled();
    }

    public static boolean isGlyphVolumeLevelEnabled() {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(Constants.GLYPH_VOLUME_LEVEL_ENABLE, false) && isGlyphEnabled();
    }

    public static boolean isGlyphNotifsEnabled() {
        return Settings.Secure.getInt(context.getContentResolver(),
                Constants.GLYPH_NOTIFS_ENABLE, 1) != 0 && isGlyphEnabled();
    }

    public static boolean setGlyphNotifsEnabled(boolean enable) {
        return Settings.Secure.putInt(context.getContentResolver(),
                Constants.GLYPH_NOTIFS_ENABLE, enable ? 1 : 0);
    }

    public static String getGlyphNotifsAnimation() {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(Constants.GLYPH_NOTIFS_SUB_ANIMATIONS,
                        ResourceUtils.getString("glyph_settings_notifs_animations_default"));
    }

    public static boolean isGlyphNotifsAppEnabled(String app) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(app, true) && isGlyphNotifsEnabled();
    }

    public static boolean isGlyphNotifsAppEssential(String app) {
        Set<String> selectedValues = PreferenceManager.getDefaultSharedPreferences(context)
                .getStringSet(Constants.GLYPH_NOTIFS_SUB_ESSENTIAL , new HashSet<String>());
        return selectedValues.contains(app) && isGlyphNotifsEnabled();
    }
}
