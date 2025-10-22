/*
 * Copyright (C) 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.glyph.Services;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.Handler;
import android.util.Log;

import org.lineageos.glyph.Manager.AnimationManager;

public class VolumeLevelService extends Service {

    private static final String TAG = "GlyphVolumeLevelService";
    private static final boolean DEBUG = true;

    private ContentResolver mContentResolver;
    private VolumeObserver mVolumeObserver;

    @Override
    public void onCreate() {
        if (DEBUG) Log.d(TAG, "Creating service");
        mContentResolver = getContentResolver();
        mVolumeObserver = new VolumeObserver();
        mVolumeObserver.register(mContentResolver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (DEBUG) Log.d(TAG, "Starting service");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (DEBUG) Log.d(TAG, "Destroying service");
        mVolumeObserver.unregister(mContentResolver);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class VolumeObserver extends ContentObserver {
        private AudioManager audioManager;
        private int previousVolume;

        public VolumeObserver() {
            super(new Handler());
        }

        public void register(ContentResolver cr) {
            audioManager = (AudioManager) getSystemService(AudioManager.class);
            previousVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            cr.registerContentObserver(
                android.provider.Settings.System.CONTENT_URI,
                true,
                this);
        }

        public void unregister(ContentResolver cr) {
            cr.unregisterContentObserver(this);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);

            int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            int minVolume = audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC);
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int delta = previousVolume - currentVolume;

            if(delta > 0) {
                if (DEBUG) Log.d(TAG, "Decreased: " + (int) (Math.floor(100D / maxVolume * currentVolume)));
                AnimationManager.playVolume((int) (Math.floor(100D / maxVolume * currentVolume)), false);
            } else if(delta < 0) {
                if (DEBUG) Log.d(TAG, "Increased: " + (int) (Math.floor(100D / maxVolume * currentVolume)));
                AnimationManager.playVolume((int) (Math.floor(100D / maxVolume * currentVolume)), false);
            }
            if (delta != 0) previousVolume=currentVolume;
        }
    }
}
