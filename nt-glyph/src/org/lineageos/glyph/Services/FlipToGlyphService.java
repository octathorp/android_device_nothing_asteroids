/*
 * Copyright (C) 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.glyph.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import org.lineageos.glyph.Manager.AnimationManager;
import org.lineageos.glyph.Sensors.FlipToGlyphSensor;

public class FlipToGlyphService extends Service {

    private static final String TAG = "FlipToGlyphService";
    private static final boolean DEBUG = true;

    private boolean isFlipped;
    private int ringerMode;

    private AudioManager mAudioManager;
    private FlipToGlyphSensor mFlipToGlyphSensor;
    private PowerManager mPowerManager;
    private WakeLock mWakeLock;

    @Override
    public void onCreate() {
        if (DEBUG) Log.d(TAG, "Creating service");

        mFlipToGlyphSensor = new FlipToGlyphSensor(this, this::onFlip);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (DEBUG) Log.d(TAG, "Starting service");
        mFlipToGlyphSensor.enable();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (DEBUG) Log.d(TAG, "Destroying service");
        mFlipToGlyphSensor.disable();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void onFlip(boolean flipped) {
        if (flipped == isFlipped) return;
        if (DEBUG) Log.d(TAG, "Flipped: " + flipped);
        if (flipped) {
            mWakeLock.acquire(2500);
            AnimationManager.playCsv("flip");
            ringerMode = mAudioManager.getRingerModeInternal();
            mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_SILENT);
        } else {
            mAudioManager.setRingerModeInternal(ringerMode);
        }
        isFlipped = flipped;
    }
}
