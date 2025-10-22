/*
 * Copyright (C) 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.glyph.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.android.internal.util.ArrayUtils;

import org.lineageos.glyph.Constants.Constants;
import org.lineageos.glyph.Manager.AnimationManager;
import org.lineageos.glyph.Manager.SettingsManager;
import org.lineageos.glyph.Manager.StatusManager;

public class NotificationService extends NotificationListenerService
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "GlyphNotification";
    private static final boolean DEBUG = true;

    private NotificationManager mNotificationManager;
    private PowerManager mPowerManager;
    private WakeLock mWakeLock;

    private ContentResolver mContentResolver;
    private SettingObserver mSettingObserver;

    private SharedPreferences mSharedPreferences;

    @Override
    public void onCreate() {
        if (DEBUG) Log.d(TAG, "Creating service");
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        mContentResolver = getContentResolver();
        mSettingObserver = new SettingObserver();
        mSettingObserver.register(mContentResolver);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (DEBUG) Log.d(TAG, "Starting service");
        onNotificationUpdated();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (DEBUG) Log.d(TAG, "Destroying service");
        AnimationManager.stopEssential();
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        mSettingObserver.unregister(mContentResolver);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn){
        if (Constants.CONTEXT == null) return;
        if (DEBUG) Log.d(TAG, "onNotificationPosted");
        if (!SettingsManager.isGlyphNotifsEnabled()) return;
        String packageName = sbn.getPackageName();
        String packageChannelID = sbn.getNotification().getChannelId();
        int packageImportance = -1;
        boolean packageCanBypassDnd = false;
        int interruptionFilter = mNotificationManager.getCurrentInterruptionFilter();
        try {
            Context packageContext = createPackageContext(packageName, 0);
            NotificationManager packageNotificationManager = (NotificationManager) packageContext.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel packageChannel = packageNotificationManager.getNotificationChannel(packageChannelID);
            if (packageChannel != null) {
                packageImportance = packageChannel.getImportance();
                packageCanBypassDnd = packageChannel.canBypassDnd();
            }
        } catch (PackageManager.NameNotFoundException e) {}
        if (DEBUG) Log.d(TAG, "onNotificationPosted: package:" + packageName + " | channel id: " + packageChannelID + " | importance: " + packageImportance + " | can bypass dnd: " + packageCanBypassDnd);
        if (SettingsManager.isGlyphNotifsAppEnabled(packageName)
                        && !sbn.isOngoing()
                        && !ArrayUtils.contains(Constants.APPS_TO_IGNORE, packageName)
                        && !ArrayUtils.contains(Constants.NOTIFS_TO_IGNORE, packageName + ":" + packageChannelID)
                        && (packageImportance >= NotificationManager.IMPORTANCE_DEFAULT || packageImportance == -1)
                        && (interruptionFilter <= NotificationManager.INTERRUPTION_FILTER_ALL || packageCanBypassDnd)) {
            mWakeLock.acquire(2500);
            AnimationManager.playCsv(SettingsManager.getGlyphNotifsAnimation());
        }
        if (SettingsManager.isGlyphNotifsAppEssential(packageName)
                        && !sbn.isOngoing()
                        && !ArrayUtils.contains(Constants.APPS_TO_IGNORE, packageName)
                        && !ArrayUtils.contains(Constants.NOTIFS_TO_IGNORE, packageName + ":" + packageChannelID)
                        && (packageImportance >= NotificationManager.IMPORTANCE_DEFAULT || packageImportance == -1)
                        && (interruptionFilter <= NotificationManager.INTERRUPTION_FILTER_ALL || packageCanBypassDnd)
                        && mNotificationManager.isNotificationPolicyAccessGranted()) {
            AnimationManager.playEssential();
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn){
        if (DEBUG) Log.d(TAG, "onNotificationRemoved: package:" + sbn.getPackageName() + " | channel id: " + sbn.getNotification().getChannelId());
        onNotificationUpdated();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preference, String key) {
        if (key.equals("glyph_settings_notifs_sub_essential")) {
            if (DEBUG) Log.d(TAG, "onSharedPreferenceChanged: glyph_settings_notifs_sub_essential");
            onNotificationUpdated();
        }
    }

    private void onNotificationUpdated() {
        if (DEBUG) Log.d(TAG, "onNotificationUpdated");
        boolean playEssential = false;
        if (SettingsManager.isGlyphNotifsEnabled()) {
            if (!mNotificationManager.isNotificationPolicyAccessGranted()) return;
            StatusBarNotification[] activeNotifications = getActiveNotifications();
            for (StatusBarNotification sbn : activeNotifications) {
                String packageName = sbn.getPackageName();
                String packageChannelID = sbn.getNotification().getChannelId();
                int packageImportance = -1;
                boolean packageCanBypassDnd = false;
                int interruptionFilter = mNotificationManager.getCurrentInterruptionFilter();
                try {
                    Context packageContext = createPackageContext(packageName, 0);
                    NotificationManager packageNotificationManager = (NotificationManager) packageContext.getSystemService(Context.NOTIFICATION_SERVICE);
                    NotificationChannel packageChannel = packageNotificationManager.getNotificationChannel(packageChannelID);
                    if (packageChannel != null) {
                        packageImportance = packageChannel.getImportance();
                        packageCanBypassDnd = packageChannel.canBypassDnd();
                    }
                } catch (PackageManager.NameNotFoundException e) {}
                if (DEBUG) Log.d(TAG, "onNotificationUpdated: package:" + packageName + " | channel id: " + packageChannelID + " | importance: " + packageImportance + " | can bypass dnd: " + packageCanBypassDnd);
                if (SettingsManager.isGlyphNotifsAppEssential(packageName)
                                && !sbn.isOngoing()
                                && !ArrayUtils.contains(Constants.APPS_TO_IGNORE, packageName)
                                && !ArrayUtils.contains(Constants.NOTIFS_TO_IGNORE, packageName + ":" + packageChannelID)
                                && (packageImportance >= NotificationManager.IMPORTANCE_DEFAULT || packageImportance == -1)
                                && (interruptionFilter <= NotificationManager.INTERRUPTION_FILTER_ALL || packageCanBypassDnd)) {
                    if (DEBUG) Log.d(TAG, "onNotificationUpdated: found essential notification | package:" + packageName);
                    playEssential = true;
                }
            }
        }
        if (playEssential) {
            AnimationManager.playEssential();
        } else {
            AnimationManager.stopEssential();
        }
    }

    private class SettingObserver extends ContentObserver {
        public SettingObserver() {
            super(new Handler());
        }

        public void register(ContentResolver cr) {
            cr.registerContentObserver(Settings.Secure.getUriFor(
                Constants.GLYPH_ENABLE), false, this);
            cr.registerContentObserver(Settings.Secure.getUriFor(
                Constants.GLYPH_NOTIFS_ENABLE), false, this);
        }

        public void unregister(ContentResolver cr) {
            cr.unregisterContentObserver(this);
        }

        @Override
        public void onChange(boolean selfChange) {
            if (DEBUG) Log.d(TAG, "SettingObserver: onChange");
            onNotificationUpdated();
            super.onChange(selfChange);
        }
    }
}
