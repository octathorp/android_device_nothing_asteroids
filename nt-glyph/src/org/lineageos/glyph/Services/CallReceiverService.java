/*
 * Copyright (C) 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.glyph.Services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.concurrent.Executors;

import org.lineageos.glyph.Manager.AnimationManager;
import org.lineageos.glyph.Manager.SettingsManager;

public class CallReceiverService extends Service {

    private static final String TAG = "GlyphCallReceiverService";
    private static final boolean DEBUG = true;

    private AudioManager mAudioManager;

    @Override
    public void onCreate() {
        if (DEBUG) Log.d(TAG, "Creating service");

        mAudioManager = getSystemService(AudioManager.class);
        mAudioManager.addOnModeChangedListener(Executors.newSingleThreadExecutor(), mAudioManagerOnModeChangedListener);
        mAudioManagerOnModeChangedListener.onModeChanged(mAudioManager.getMode());

        IntentFilter callReceiver = new IntentFilter();
        callReceiver.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        registerReceiver(mCallReceiver, callReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (DEBUG) Log.d(TAG, "Starting service");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (DEBUG) Log.d(TAG, "Destroying service");
        this.unregisterReceiver(mCallReceiver);
        mAudioManager.removeOnModeChangedListener(mAudioManagerOnModeChangedListener);
        disableCallAnimation();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void enableCallAnimation() {
        if (DEBUG) Log.d(TAG, "enableCallAnimation");
        AnimationManager.playCall(SettingsManager.getGlyphCallAnimation());
    }

    private void disableCallAnimation() {
        if (DEBUG) Log.d(TAG, "disableCallAnimation");
        AnimationManager.stopCall();
    }

    private final BroadcastReceiver mCallReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
                String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                if(state.equals(TelephonyManager.EXTRA_STATE_RINGING)){
                    if (DEBUG) Log.d(TAG, "EXTRA_STATE_RINGING");
                    enableCallAnimation();
                }
                if ((state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK))){
                    if (DEBUG) Log.d(TAG, "EXTRA_STATE_OFFHOOK");
                    disableCallAnimation();
                }
                if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)){
                    if (DEBUG) Log.d(TAG, "EXTRA_STATE_IDLE");
                    disableCallAnimation();
                }
            }
        }
    };

    private final AudioManager.OnModeChangedListener mAudioManagerOnModeChangedListener = new AudioManager.OnModeChangedListener() {
        @Override
        public void onModeChanged(int mode) {
            if (DEBUG) Log.d(TAG, "mAudioManagerOnModeChangedListener: " + mode);
            if (mode == AudioManager.MODE_RINGTONE) {
                enableCallAnimation();
            } else {
                disableCallAnimation();
            }
        }
    };
}
