/*
 * Copyright (C) 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.glyph.Manager;

import android.util.Log;

import com.android.internal.util.ArrayUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.lineageos.glyph.Constants.Constants;
import org.lineageos.glyph.Utils.FileUtils;
import org.lineageos.glyph.Utils.ResourceUtils;

public final class AnimationManager {

    private static final String TAG = "GlyphAnimationManager";
    private static final boolean DEBUG = true;

    private static Future<?> submit(Runnable runnable) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        return executorService.submit(runnable);
    }

    private static boolean check(String name, boolean wait) {
        if (DEBUG) Log.d(TAG, "Playing animation | name: " + name + " | waiting: " + Boolean.toString(wait));

        if (StatusManager.isAllLedActive()) {
            if (DEBUG) Log.d(TAG, "All LEDs are active, exiting animation | name: " + name);
            return false;
        }

        if (StatusManager.isCallLedActive()) {
            if (DEBUG) Log.d(TAG, "Call animation is currently active, exiting animation | name: " + name);
            return false;
        }

        if (StatusManager.isAnimationActive()) {
            long start = System.currentTimeMillis();
            if (name == "volume" && StatusManager.isVolumeLedActive()) {
                if (DEBUG) Log.d(TAG, "There is already a volume animation playing, update");
                StatusManager.setVolumeLedUpdate(true);
                while (StatusManager.isVolumeLedUpdate()) {
                    if (System.currentTimeMillis() - start >= 2500) return false;
                }
            } else if (wait) {
                if (DEBUG) Log.d(TAG, "There is already an animation playing, wait | name: " + name);
                while (StatusManager.isAnimationActive()) {
                    if (System.currentTimeMillis() - start >= 2500) return false;
                }
            } else {
                if (DEBUG) Log.d(TAG, "There is already an animation playing, exiting | name: " + name);
                return false;
            }
        }

        return true;
    }

    private static boolean checkInterruption(String name) {
        if (StatusManager.isAllLedActive()
                || (name != "call" && StatusManager.isCallLedEnabled())
                || (name == "call" && !StatusManager.isCallLedEnabled())
                || (name == "volume" && StatusManager.isVolumeLedUpdate())) {
            return true;
        }
        return false;
    }

    public static void playCsv(String name) {
        playCsv(name, false);
    }

    public static void playCsv(String name, boolean wait) {
        submit(() -> {
            if (!check(name, wait))
                return;

            StatusManager.setAnimationActive(true);

            long start = System.currentTimeMillis();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    ResourceUtils.getAnimation(name)))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (checkInterruption("csv")) throw new InterruptedException();
                    line = line.replace(" ", "");
                    line = line.endsWith(",") ? line.substring(0, line.length() - 1) : line;
                    String[] pattern = line.split(",");
                    if (ArrayUtils.contains(Constants.getSupportedAnimationPatternLengths(), pattern.length)) {
                        updateLedFrame(pattern);
                    } else {
                        if (DEBUG) Log.d(TAG, "Animation line length mismatch | name: " + name + " | line: " + line);
                        throw new InterruptedException();
                    }
                    long delay = 16666L - (System.currentTimeMillis() - start);
                    Thread.sleep(delay/1000);
                }
            } catch (Exception e) {
                if (DEBUG) Log.d(TAG, "Exception while playing animation | name: " + name + " | exception: " + e);
            } finally {
                updateLedFrame(new float[5]);
                StatusManager.setAnimationActive(false);
                if (DEBUG) Log.d(TAG, "Done playing animation | name: " + name);
            }
        });
    }

    public static void playCharging(int batteryLevel, boolean wait) {
        submit(() -> {
            if (!check("charging", wait))
                return;

            StatusManager.setAnimationActive(true);

            boolean batteryDot = ResourceUtils.getBoolean("glyph_settings_battery_dot");
            int[] batteryArray = new int[ResourceUtils.getInteger("glyph_settings_battery_levels_num")];
            int amount = (int) (Math.floor((batteryLevel / 100.0) * (batteryArray.length - (batteryDot ? 2 : 1))) + (batteryDot ? 2 : 1));

            try {
                for (int i = 0; i < batteryArray.length; i++) {
                    if (checkInterruption("charging")) throw new InterruptedException();
                    batteryArray[i] = Constants.getBrightness();
                    if (batteryDot && i == 0) continue;
                    updateLedFrame(batteryArray);
                    Thread.sleep(15);
                }
                for (int i = batteryArray.length - 1; i > amount - 1; i--) {
                    if (checkInterruption("charging")) throw new InterruptedException();
                    batteryArray[i] = 0;
                    updateLedFrame(batteryArray);
                    Thread.sleep(5);
                }
                long start = System.currentTimeMillis();
                while (System.currentTimeMillis() - start <= 2000) {
                    if (checkInterruption("charging")) throw new InterruptedException();
                }
                for (int i = amount - 1; i >= 0; i--) {
                    if (checkInterruption("charging")) throw new InterruptedException();
                    batteryArray[i] = 0;
                    updateLedFrame(batteryArray);
                    Thread.sleep(11);
                }
                long start2 = System.currentTimeMillis();
                while (System.currentTimeMillis() - start2 <= 730) {
                    if (checkInterruption("charging")) throw new InterruptedException();
                }
            } catch (InterruptedException e) {
                if (DEBUG) Log.d(TAG, "Exception while playing animation, interrupted | name: charging");
                if (!StatusManager.isAllLedActive()) {
                    updateLedFrame(new int[batteryArray.length]);
                }
            } finally {
                StatusManager.setAnimationActive(false);
                if (DEBUG) Log.d(TAG, "Done playing animation | name: charging");
            }
        });
    }

    public static void playVolume(int volumeLevel, boolean wait) {
        submit(() -> {
            if (!check("volume", wait))
                return;

            StatusManager.setVolumeLedActive(true);
            StatusManager.setAnimationActive(true);

            int[] volumeArray = new int[ResourceUtils.getInteger("glyph_settings_volume_levels_num")];
            int amount = (int) (Math.floor((volumeLevel / 100D) * (volumeArray.length - 1)) + 1);
            int last = StatusManager.getVolumeLedLast();

            try {
                for (int i = 0; i < volumeArray.length; i++) {
                    if (volumeLevel == 0) {
                        if (checkInterruption("volume")) throw new InterruptedException();
                        StatusManager.setVolumeLedLast(0);
                        updateLedFrame(new int[volumeArray.length]);
                        break;
                    } else if ( i <= amount - 1 && volumeLevel > 0) {
                        if (checkInterruption("volume")) throw new InterruptedException();
                        StatusManager.setVolumeLedLast(i);
                        volumeArray[i] = Constants.getBrightness();
                        if (last == 0) {
                            updateLedFrame(volumeArray);
                            Thread.sleep(15);
                        }
                    }
                }
                if (last != 0) {
                    if (checkInterruption("volume")) throw new InterruptedException();
                    updateLedFrame(volumeArray);
                }
                long start = System.currentTimeMillis();
                while (System.currentTimeMillis() - start <= 1800) {
                    if (checkInterruption("volume")) throw new InterruptedException();
                }
                for (int i = volumeArray.length - 1; i >= 0; i--) {
                    if (checkInterruption("volume")) throw new InterruptedException();
                    if (volumeArray[i] != 0) {
                        StatusManager.setVolumeLedLast(i);
                        volumeArray[i] = 0;
                        updateLedFrame(volumeArray);
                        Thread.sleep(15);
                    }
                }
                long start2 = System.currentTimeMillis();
                while (System.currentTimeMillis() - start2 <= 730) {
                    if (checkInterruption("volume")) throw new InterruptedException();
                }
            } catch (InterruptedException e) {
                if (DEBUG) Log.d(TAG, "Exception while playing animation, interrupted | name: volume");
                if (!StatusManager.isAllLedActive() && !StatusManager.isVolumeLedUpdate()) {
                    updateLedFrame(new int[volumeArray.length]);
                }
            } finally {
                if (!StatusManager.isVolumeLedUpdate()) {
                    StatusManager.setVolumeLedLast(0);
                    StatusManager.setAnimationActive(false);
                    StatusManager.setVolumeLedActive(false);
                }
                StatusManager.setVolumeLedUpdate(false);
                if (DEBUG) Log.d(TAG, "Done playing animation | name: volume");
            }
        });
    }

    public static void playCall(String name) {
        submit(() -> {
            StatusManager.setCallLedEnabled(true);

            if (!check("call: " + name, true))
                return;

            StatusManager.setCallLedActive(true);

            long start = System.currentTimeMillis();

            while (StatusManager.isCallLedEnabled()) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                        ResourceUtils.getCallAnimation(name)))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (checkInterruption("call")) throw new InterruptedException();
                        line = line.replace(" ", "");
                        line = line.endsWith(",") ? line.substring(0, line.length() - 1) : line;
                        String[] pattern = line.split(",");
                        if (ArrayUtils.contains(Constants.getSupportedAnimationPatternLengths(), pattern.length)) {
                            updateLedFrame(pattern);
                        } else {
                            if (DEBUG) Log.d(TAG, "Animation line length mismatch | name: " + name + " | line: " + line);
                            throw new InterruptedException();
                        }
                        long delay = 16666L - (System.currentTimeMillis() - start);
                        Thread.sleep(delay/1000);
                    }
                } catch (Exception e) {
                    if (DEBUG) Log.d(TAG, "Exception while playing animation | name: " + name + " | exception: " + e);
                } finally {
                    if (StatusManager.isAllLedActive()) {
                        if (DEBUG) Log.d(TAG, "All LED active, pause playing animation | name: " + name);
                        while (StatusManager.isAllLedActive()) {}
                    }
                }
            }
            updateLedFrame(new float[5]);
            StatusManager.setCallLedActive(false);
            if (DEBUG) Log.d(TAG, "Done playing animation | name: " + name);
        });
    }

    public static void stopCall() {
        if (DEBUG) Log.d(TAG, "Disabling Call Animation");
        StatusManager.setCallLedEnabled(false);
    }

    public static void playEssential() {
        if (DEBUG) Log.d(TAG, "Playing Essential Animation");
        int led = ResourceUtils.getInteger("glyph_settings_notifs_essential_led");
        if (!StatusManager.isEssentialLedActive()) {
            submit(() -> {
                if (!check("essential", true))
                    return;

                StatusManager.setAnimationActive(true);

                try {
                    if (checkInterruption("essential")) throw new InterruptedException();
                    int[] steps = {1, 2, 4, 7};
                    for (int i : steps) {
                        if (checkInterruption("essential")) throw new InterruptedException();
                        updateLedSingle(led, Constants.getMaxBrightness() / 100 * i);
                        Thread.sleep(25);
                    }
                    Thread.sleep(250);
                } catch (InterruptedException e) {}

                StatusManager.setAnimationActive(false);
                StatusManager.setEssentialLedActive(true);
                if (DEBUG) Log.d(TAG, "Done playing animation | name: essential");
            });
        } else {
            updateLedSingle(led, Constants.getMaxBrightness() / 100 * 7);
            return;
        }

    }

    public static void stopEssential() {
        if (DEBUG) Log.d(TAG, "Disabling Essential Animation");
        StatusManager.setEssentialLedActive(false);
        if (!StatusManager.isAnimationActive() && !StatusManager.isAllLedActive()) {
            int led = ResourceUtils.getInteger("glyph_settings_notifs_essential_led");
            updateLedSingle(led, 0);
        }
    }

    public static void playMusic(String name) {
        submit(() -> {
            float maxBrightness = (float) Constants.getMaxBrightness();
            float[] pattern = new float[5];

            switch (name) {
                case "low":
                    pattern[4] = maxBrightness;
                    break;
                case "mid_low":
                    pattern[3] = maxBrightness;
                    break;
                case "mid":
                    pattern[2] = maxBrightness;
                    break;
                case "mid_high":
                    pattern[0] = maxBrightness;
                    break;
                case "high":
                    pattern[1] = maxBrightness;
                    break;
                default:
                    if (DEBUG) Log.d(TAG, "Name doesn't match any zone, returning | name: " + name);
                    return;
            }

            try {
                updateLedFrame(pattern);
                Thread.sleep(90);
            } catch (Exception e) {
                if (DEBUG) Log.d(TAG, "Exception while playing animation | name: music: " + name + " | exception: " + e);
            } finally {
                updateLedFrame(new float[5]);
                if (DEBUG) Log.d(TAG, "Done playing animation | name: " + name);
            }
        });
    }

    private static void updateLedFrame(String[] pattern) {
        updateLedFrame(Arrays.stream(pattern)
                .mapToInt(Integer::parseInt)
                .toArray());
    }

    private static void updateLedFrame(int[] pattern) {
        float[] floatPattern = new float[pattern.length];
        for (int i = 0; i < pattern.length; i++) {
            floatPattern[i] = (float) pattern[i];
        }
        updateLedFrame(floatPattern);
    }

    private static void updateLedFrame(float[] pattern) {
        if (DEBUG) Log.d(TAG, "Updating pattern: " + pattern);
        if (DEBUG) Log.d(TAG, "Updating pattern length: " + pattern.length);
        float maxBrightness = (float) Constants.getMaxBrightness();
        int essentialLed = ResourceUtils.getInteger("glyph_settings_notifs_essential_led");
        if (StatusManager.isEssentialLedActive()) {
            if (pattern.length == 5) { // Phone (1) pattern
                if (pattern[1] < (maxBrightness / 100 * 7)) {
                    pattern[1] = maxBrightness / 100 * 7;
                }
            } else if (pattern.length == 33) { // Phone (2) pattern
                if (pattern[2] < (maxBrightness / 100 * 7)) {
                    pattern[2] = maxBrightness / 100 * 7;
                }
            }
        }
        for (int i = 0; i < pattern.length; i++) {
            pattern[i] = pattern[i] / maxBrightness * Constants.getBrightness();
        }
        FileUtils.writeFrameLed(pattern);
    }

    private static void updateLedSingle(int led, String brightness) {
        updateLedSingle(led, Float.parseFloat(brightness));
    }

    private static void updateLedSingle(int led, int brightness) {
        updateLedSingle(led, (float) brightness);
    }

    private static void updateLedSingle(int led, float brightness) {
        if (DEBUG) Log.d(TAG, "Updating led | led: " + led + " | brightness: " + brightness);
        float maxBrightness = (float) Constants.getMaxBrightness();
        int essentialLed = ResourceUtils.getInteger("glyph_settings_notifs_essential_led");
        if (StatusManager.isEssentialLedActive()
                && led == essentialLed
                && brightness < (maxBrightness / 100 * 7)) {
            brightness = maxBrightness / 100 * 7;
        }
        FileUtils.writeSingleLed(led, brightness / maxBrightness * Constants.getBrightness());
    }
}
