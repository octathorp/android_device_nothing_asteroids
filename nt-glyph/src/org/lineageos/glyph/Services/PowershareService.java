/*
 * Copyright (C) 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.glyph.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.FileObserver;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import org.lineageos.glyph.Constants.Constants;
import org.lineageos.glyph.Manager.AnimationManager;
import org.lineageos.glyph.Utils.FileUtils;
import org.lineageos.glyph.Utils.ResourceUtils;

public class PowershareService extends Service {

    private static final String TAG = "GlyphPowershareService";
    private static final boolean DEBUG = true;

    private static final String POWERSHARE_ACTIVE = ResourceUtils.getString("glyph_settings_paths_powershare_active_absolute");
    private static final String POWERSHARE_ENABLED = ResourceUtils.getString("glyph_settings_paths_powershare_enabled_absolute");

    private PowershareActiveObserver mPowershareActiveObserver;
    private PowerManager mPowerManager;
    private WakeLock mWakeLock;

    @Override
    public void onCreate() {
        if (DEBUG) Log.d(TAG, "Creating service");
        mPowershareActiveObserver = new PowershareActiveObserver();
        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (DEBUG) Log.d(TAG, "Starting service");
        mFileObserver.startWatching();
        mPowershareActiveObserver.startWatching();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (DEBUG) Log.d(TAG, "Destroying service");
        mFileObserver.stopWatching();
        mPowershareActiveObserver.stopWatching();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void onPowershareEnabled() {
        if (DEBUG) Log.e(TAG, "onPowershareEnabled");
        mPowershareActiveObserver.continueWatching();
    }

    private void onPowershareDisabled() {
        if (DEBUG) Log.e(TAG, "onPowershareDisabled");
        mPowershareActiveObserver.pauseWatching();
    }

    private final FileObserver mFileObserver = new FileObserver(POWERSHARE_ENABLED, FileObserver.MODIFY) {
        @Override
        public void onEvent(int event, String file) {
            this.checkIfPowerShareIsEnabled();
        }

        @Override
        public void startWatching() {
            if (DEBUG) Log.e(TAG, "FileObserver: startWatching");
            this.checkIfPowerShareIsEnabled();
            super.startWatching();
        }

        private void checkIfPowerShareIsEnabled() {
            if (DEBUG) Log.e(TAG, "FileObserver: checkIfPowerShareIsEnabled: " + FileUtils.readLineInt(POWERSHARE_ENABLED));
            if (FileUtils.readLineInt(POWERSHARE_ENABLED) == 1) {
                onPowershareEnabled();
            } else {
                onPowershareDisabled();
            }
        }
    };

    private class PowershareActiveObserver extends Thread {

        private boolean lastState = false;
        private boolean pause = true;
        private boolean ended = false;

        private Object mPowershareActiveObserverLock = new Object();

        public void startWatching() {
            if (DEBUG) Log.e(TAG, "PowershareActiveObserver: startWatching");
            if (super.isAlive()) return;
            super.start();
        }

        public void continueWatching() {
            if (DEBUG) Log.e(TAG, "PowershareActiveObserver: continueWatching");
            if (!pause) return;
            pause = false;
            synchronized (mPowershareActiveObserverLock) {
                mPowershareActiveObserverLock.notify();
            }
        }

        public void pauseWatching() {
            if (DEBUG) Log.e(TAG, "PowershareActiveObserver: pauseWatching");
            if (pause) return;
            lastState = false;
            pause = true;
        }

        public void stopWatching() {
            if (DEBUG) Log.e(TAG, "PowershareActiveObserver: stopWatching");
            if (pause) this.continueWatching();
            ended = true;
        }

        private void updatePowershareState() {
            if (DEBUG) Log.d(TAG, "updatePowershareState: " + FileUtils.readLineInt(POWERSHARE_ACTIVE));
            if (FileUtils.readLineInt(POWERSHARE_ACTIVE) == 1) {
                if (lastState) return;
                lastState = true;
                mWakeLock.acquire(2500);
                AnimationManager.playCsv("powershare", true);
            } else {
                lastState = false;
            }
        }

        @Override
        public void run() {
            if (DEBUG) Log.e(TAG, "PowershareActiveObserver: run");
            while (!ended) {
                synchronized (mPowershareActiveObserverLock) {
                    if (pause) {
                        try {
                            if (DEBUG) Log.d(TAG, "mPowershareActiveObserverLock.wait()");
                            mPowershareActiveObserverLock.wait();
                        } catch (InterruptedException e) { }
                    }
                }
                updatePowershareState();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) { }
            }
        }
    }
}
