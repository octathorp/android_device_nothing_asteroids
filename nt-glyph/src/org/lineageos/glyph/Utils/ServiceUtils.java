/*
 * Copyright (C) 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.glyph.Utils;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.util.Log;

import org.lineageos.glyph.Constants.Constants;
import org.lineageos.glyph.Manager.SettingsManager;
import org.lineageos.glyph.Services.CallReceiverService;
import org.lineageos.glyph.Services.ChargingService;
import org.lineageos.glyph.Services.FlipToGlyphService;
import org.lineageos.glyph.Services.MusicVisualizerService;
import org.lineageos.glyph.Services.PowershareService;
import org.lineageos.glyph.Services.VolumeLevelService;

public final class ServiceUtils {

    private static final String TAG = "GlyphServiceUtils";
    private static final boolean DEBUG = true;

    private static Context context = Constants.CONTEXT;

    private static void startCallReceiverService() {
        if (DEBUG) Log.d(TAG, "Starting Glyph call receiver service");
        context.startServiceAsUser(new Intent(context, CallReceiverService.class),
                UserHandle.CURRENT);
    }

    private static void stopCallReceiverService() {
        if (DEBUG) Log.d(TAG, "Stopping Glyph call receiver service");
        context.stopServiceAsUser(new Intent(context, CallReceiverService.class),
                UserHandle.CURRENT);
    }

    private static void startChargingService() {
        if (DEBUG) Log.d(TAG, "Starting Glyph charging service");
        context.startServiceAsUser(new Intent(context, ChargingService.class),
                UserHandle.CURRENT);
    }

    private static void stopChargingService() {
        if (DEBUG) Log.d(TAG, "Stopping Glyph charging service");
        context.stopServiceAsUser(new Intent(context, ChargingService.class),
                UserHandle.CURRENT);
    }

    private static void startFlipToGlyphService() {
        if (DEBUG) Log.d(TAG, "Starting Flip to Glyph service");
        context.startServiceAsUser(new Intent(context, FlipToGlyphService.class),
                UserHandle.CURRENT);
    }

    private static void stopFlipToGlyphService() {
        if (DEBUG) Log.d(TAG, "Stopping Flip to Glyph service");
        context.stopServiceAsUser(new Intent(context, FlipToGlyphService.class),
                UserHandle.CURRENT);
    }

    public static void startMusicVisualizerService() {
        if (DEBUG) Log.d(TAG, "Starting Music Visualizer service");
        context.startServiceAsUser(new Intent(context, MusicVisualizerService.class),
                UserHandle.CURRENT);
    }

    protected static void stopMusicVisualizerService() {
        if (DEBUG) Log.d(TAG, "Stopping Music Visualizer service");
        context.stopServiceAsUser(new Intent(context, MusicVisualizerService.class),
                UserHandle.CURRENT);
    }

    private static void startPowershareService() {
        if (DEBUG) Log.d(TAG, "Starting Glyph powershare service");
        context.startServiceAsUser(new Intent(context, PowershareService.class),
                UserHandle.CURRENT);
    }

    private static void stopPowershareService() {
        if (DEBUG) Log.d(TAG, "Stopping Glyph powershare service");
        context.stopServiceAsUser(new Intent(context, PowershareService.class),
                UserHandle.CURRENT);
    }

    public static void startVolumeLevelService() {
        if (DEBUG) Log.d(TAG, "Starting Volume Level service");
        context.startServiceAsUser(new Intent(context, VolumeLevelService.class),
                UserHandle.CURRENT);
    }

    protected static void stopVolumeLevelService() {
        if (DEBUG) Log.d(TAG, "Stopping Volume Listener service");
        context.stopServiceAsUser(new Intent(context, VolumeLevelService.class),
                UserHandle.CURRENT);
    }

    public static void checkGlyphService() {
        if (SettingsManager.isGlyphEnabled()) {
            Constants.setBrightness(SettingsManager.getGlyphBrightness());
            if (SettingsManager.isGlyphChargingEnabled()) {
                startChargingService();
            } else {
                stopChargingService();
            }
            if (SettingsManager.isGlyphPowershareEnabled()) {
                startPowershareService();
            } else {
                stopPowershareService();
            }
            if (SettingsManager.isGlyphCallEnabled()) {
                startCallReceiverService();
            } else {
                stopCallReceiverService();
            }
            if (SettingsManager.isGlyphFlipEnabled()) {
                startFlipToGlyphService();
            } else {
                stopFlipToGlyphService();
            }
            if (SettingsManager.isGlyphMusicVisualizerEnabled()) {
                startMusicVisualizerService();
            } else {
                stopMusicVisualizerService();
            }
            if (SettingsManager.isGlyphVolumeLevelEnabled()) {
                startVolumeLevelService();
            } else {
                stopVolumeLevelService();
            }
        } else {
            stopChargingService();
            stopPowershareService();
            stopCallReceiverService();
            stopFlipToGlyphService();
            stopMusicVisualizerService();
            stopVolumeLevelService();
        }
    }
}
