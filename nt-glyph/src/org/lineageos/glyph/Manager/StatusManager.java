/*
 * Copyright (C) 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.glyph.Manager;

public final class StatusManager {

    private static final String TAG = "GlyphStatusManager";
    private static final boolean DEBUG = true;

    private static boolean allLedActive = false;
    private static boolean animationActive = false;
    private static boolean callLedActive = false;
    private static boolean essentialLedActive = false;
    private static boolean volumeLedActive = false;
    private static int volumeLedLast = 0;
    private static boolean volumeLedUpdate = false;

    private static boolean callLedEnabled = false;

    public static boolean isAnimationActive() {
        return animationActive;
    }

    public static void setAnimationActive(boolean status) {
        animationActive = status;
    }

    public static boolean isAllLedActive() {
        return allLedActive;
    }

    public static void setAllLedsActive(boolean status) {
        allLedActive = status;
    }

    public static boolean isCallLedActive() {
        return callLedActive;
    }

    public static void setCallLedActive(boolean status) {
        callLedActive = status;
    }

    public static boolean isEssentialLedActive() {
        return essentialLedActive;
    }

    public static void setEssentialLedActive(boolean status) {
        essentialLedActive = status;
    }

    public static boolean isVolumeLedActive() {
        return volumeLedActive;
    }

    public static void setVolumeLedActive(boolean status) {
        volumeLedActive = status;
    }

    public static int getVolumeLedLast() {
        return volumeLedLast;
    }

    public static void setVolumeLedLast(int last) {
        volumeLedLast = last;
    }

    public static boolean isVolumeLedUpdate() {
        return volumeLedUpdate;
    }

    public static void setVolumeLedUpdate(boolean status) {
        volumeLedUpdate = status;
    }

    public static boolean isCallLedEnabled() {
        return callLedEnabled;
    }

    public static void setCallLedEnabled(boolean status) {
        callLedEnabled = status;
    }

}
