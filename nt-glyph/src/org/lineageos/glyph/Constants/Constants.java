/*
 * Copyright (C) 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.glyph.Constants;

import android.content.Context;

import org.lineageos.glyph.Utils.ResourceUtils;

public final class Constants {

    private static final String TAG = "GlyphConstants";
    private static final boolean DEBUG = true;

    public static Context CONTEXT;

    private static String device = null;

    private static int brightness = -1;
    private static int brightnessMax = -1;
    private static int[] brightnessLevels = null;
    private static int[] supportedAnimationPatternLengths = null;

    public static final String GLYPH_ENABLE = "glyph_enable";
    public static final String GLYPH_FLIP_ENABLE = "glyph_settings_flip_toggle";
    public static final String GLYPH_BRIGHTNESS = "glyph_settings_brightness";
    public static final String GLYPH_CHARGING_CATEGORY = "glyph_settings_charging";
    public static final String GLYPH_CHARGING_LEVEL_ENABLE = "glyph_settings_charging_level";
    public static final String GLYPH_CHARGING_POWERSHARE_ENABLE = "glyph_settings_charging_powershare";
    public static final String GLYPH_CALL_CATEGORY = "glyph_settings_call";
    public static final String GLYPH_CALL_ENABLE = "glyph_settings_call_toggle";
    public static final String GLYPH_CALL_SUB_PREVIEW = "glyph_settings_call_sub_preview";
    public static final String GLYPH_CALL_SUB_ANIMATIONS = "glyph_settings_call_sub_animations";
    public static final String GLYPH_CALL_SUB_ENABLE = "glyph_settings_call_sub_toggle";
    public static final String GLYPH_MUSIC_VISUALIZER_ENABLE = "glyph_settings_music_visualizer_toggle";
    public static final String GLYPH_NOTIFS_ENABLE = "glyph_settings_notifs_toggle";
    public static final String GLYPH_NOTIFS_SUB_PREVIEW = "glyph_settings_notifs_sub_preview";
    public static final String GLYPH_NOTIFS_SUB_ANIMATIONS = "glyph_settings_notifs_sub_animations";
    public static final String GLYPH_NOTIFS_SUB_ESSENTIAL = "glyph_settings_notifs_sub_essential";
    public static final String GLYPH_NOTIFS_SUB_CATEGORY = "glyph_settings_notifs_sub";
    public static final String GLYPH_NOTIFS_SUB_ENABLE = "glyph_settings_notifs_sub_toggle";
    public static final String GLYPH_VOLUME_LEVEL_ENABLE = "glyph_settings_volume_level_toggle";

    public static final String[] APPS_TO_IGNORE = {
        "android",
        "com.android.traceur",
        //"com.google.android.dialer",
        "com.google.android.setupwizard",
        "dev.kdrag0n.dyntheme.privileged.sys"
    };
    public static final String[] NOTIFS_TO_IGNORE = {
        "com.google.android.dialer:phone_incoming_call",
        "com.google.android.dialer:phone_ongoing_call",
        "com.android.systemui:BAT"
    };

    public static String getDevice() {
        if (device == null)
            device = ResourceUtils.getString("glyph_settings_device");

        return device;
    }

    public static boolean setBrightness(int b) {
        if (b > ResourceUtils.getInteger("glyph_settings_brightness_max"))
            return false;

        brightness = b;
        return true;
    }

    public static int getBrightness() {
        if (brightness == -1)
            brightness = ResourceUtils.getInteger("glyph_settings_brightness_max");

        return brightness;
    }

    public static int getMaxBrightness() {
        if (brightnessMax == -1)
            brightnessMax = ResourceUtils.getInteger("glyph_settings_brightness_max");

        return brightnessMax;
    }

    public static int[] getBrightnessLevels() {
        if (brightnessLevels == null)
            brightnessLevels = ResourceUtils.getIntArray("glyph_settings_brightness_levels");

        return brightnessLevels;
    }

    public static int[] getSupportedAnimationPatternLengths() {
        if (supportedAnimationPatternLengths == null)
            supportedAnimationPatternLengths = ResourceUtils.getIntArray("glyph_settings_animations_supported_pattern_lengths");

        return supportedAnimationPatternLengths;
    }

}
