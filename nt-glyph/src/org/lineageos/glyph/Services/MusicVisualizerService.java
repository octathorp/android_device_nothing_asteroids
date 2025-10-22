/*
 * Copyright (C) 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.glyph.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.audiofx.Visualizer;
import android.os.IBinder;
import android.util.Log;

import org.lineageos.glyph.Manager.AnimationManager;

public class MusicVisualizerService extends Service {

    private static final String TAG = "GlyphMusicVisualizerService";
    private static final boolean DEBUG = true;

    private Visualizer mVisualizer;
    private int bufferSize;
    private boolean isRecording = false;

    private double mRunningSoundAvg[];             // Total sound energy in one second  (0=low, 1=mid low, 2=mid, 3=mid high, 4=high)
    private double mCurrentAvgEnergyOneSec[];      // Average sound energy in one second (0=low, 1=mid low, 2=mid, 3=mid high, 4=high)
    private int mNumberOfSamplesInOneSec;          // Number of samples in one second
    private long mSystemTimeStartSec;              // System time at the start of a one second interval

    // Define the max value for a frequency band
    private static final int LOW_FREQUENCY = 200;
    private static final int MID_LOW_FREQUENCY = 500;
    private static final int MID_FREQUENCY = 1500;
    private static final int MID_HIGH_FREQUENCY = 5000;
    private static final int HIGH_FREQUENCY = 10000;

    @Override
    public void onCreate() {
        if (DEBUG) Log.d(TAG, "Creating service");

        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // Create a visualizer with the audio session ID (0) which takes the entire output mix
        mVisualizer = new Visualizer(0);

        // Set the capture size to the maximum available
        bufferSize = Visualizer.getCaptureSizeRange()[1];
        mVisualizer.setCaptureSize(bufferSize);

        mVisualizer.setDataCaptureListener(
            new Visualizer.OnDataCaptureListener() {
                @Override
                public void onWaveFormDataCapture(
                        Visualizer visualizer, byte[] waveform, int samplingRate) {
                }

                @Override
                public void onFftDataCapture(
                        Visualizer visualizer, byte[] fft, int samplingRate) {
                    if (mAudioManager.isMusicActive()) {
                        if (DEBUG) Log.d(TAG, "Music is active");
                        processAudioFFT(fft);
                    }
                }
            }, Visualizer.getMaxCaptureRate() / 2, false, true
        );

        mVisualizer.setEnabled(true);

        // Initialize instance variables
        mRunningSoundAvg = new double[5];
        mCurrentAvgEnergyOneSec = new double[5];
        mCurrentAvgEnergyOneSec[0] = -1;
        mCurrentAvgEnergyOneSec[1] = -1;
        mCurrentAvgEnergyOneSec[2] = -1;
        mCurrentAvgEnergyOneSec[3] = -1;
        mCurrentAvgEnergyOneSec[4] = -1;

        // Set the start time for the current one second interval
        mSystemTimeStartSec = System.currentTimeMillis();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (DEBUG) Log.d(TAG, "Starting service");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (DEBUG) Log.d(TAG, "Destroying service");
        mVisualizer.setEnabled(false);
        mVisualizer.release();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void processAudioFFT(byte[] audioBytes) {
    if (mVisualizer == null) {
        return;
    }

    try {
        // Ensure the Visualizer is enabled before accessing its properties
        if (!mVisualizer.getEnabled()) {
            return;
        }

        // The first byte is the DC component of the FFT result (real only)
        int energySum = Math.abs(audioBytes[0]);

        // Calculate the average instantaneous energy of the low frequency band
        int k = 2;
        double captureSize = mVisualizer.getCaptureSize() / 2.0;
        int sampleRate = mVisualizer.getSamplingRate() / 2000;
        double nextFrequency = (k / 2.0 * sampleRate) / captureSize;

        // Sum the energy in the low frequency band
        while (nextFrequency < LOW_FREQUENCY) {
            // Calculate the energy of the current frequency
            energySum += Math.sqrt(audioBytes[k] * audioBytes[k] + audioBytes[k + 1] * audioBytes[k + 1]);

            // Increment the frequency index
            k += 2;
            nextFrequency = (k / 2.0 * sampleRate) / captureSize;
        }

        // Calculate the average energy in the low frequency band
        double sampleAvgAudioEnergy = energySum / (k / 2.0);

        // Accumulate the low frequency band energy over time
        mRunningSoundAvg[0] += sampleAvgAudioEnergy;

        // Check for a beat in the low frequency band
        // A beat occurs when the average sound energy of a sample is greater than
        // the average sound energy of a one second part of a song
        // Also make sure the mCurrentAvgEnergy has been set, otherwise its -1 before its first pass
        if( (sampleAvgAudioEnergy >  mCurrentAvgEnergyOneSec[0]) && (mCurrentAvgEnergyOneSec[0] > 0) ) {
            if (DEBUG) Log.d(TAG, "Low frequency band beat detected");
            AnimationManager.playMusic("low");
        }

        energySum = 0;

        // Sum the energy in the mid-low frequency band
        while (nextFrequency < MID_LOW_FREQUENCY) {
            // Calculate the energy of the current frequency
            energySum += Math.sqrt(audioBytes[k] * audioBytes[k] + audioBytes[k + 1] * audioBytes[k + 1]);

            // Increment the frequency index
            k += 2;
            nextFrequency = (k / 2.0 * sampleRate) / captureSize;
        }

        // Calculate the average energy in the mid-low frequency band
        sampleAvgAudioEnergy = energySum / (k / 2.0);

        // Accumulate the mid low frequency band energy over time
        mRunningSoundAvg[1] += sampleAvgAudioEnergy;

        // Check for a beat in the mid-low frequency band
        if((sampleAvgAudioEnergy >  mCurrentAvgEnergyOneSec[1]) && (mCurrentAvgEnergyOneSec[1] > 0)) {
            if (DEBUG) Log.d(TAG, "Mid-low frequency band beat detected");
            AnimationManager.playMusic("mid_low");
        }

        energySum = 0;

        // Sum the energy in the mid frequency band
        while (nextFrequency < MID_FREQUENCY) {
            // Calculate the energy of the current frequency
            energySum += Math.sqrt(audioBytes[k] * audioBytes[k] + audioBytes[k + 1] * audioBytes[k + 1]);

            // Increment the frequency index
            k += 2;
            nextFrequency = (k / 2.0 * sampleRate) / captureSize;
        }

        // Calculate the average energy in the mid frequency band
        sampleAvgAudioEnergy = energySum / (k / 2.0);

        // Accumulate the mid frequency band energy over time
        mRunningSoundAvg[2] += sampleAvgAudioEnergy;

        // Check for a beat in the mid frequency band
        if( (sampleAvgAudioEnergy >  mCurrentAvgEnergyOneSec[2]) && (mCurrentAvgEnergyOneSec[2] > 0) ) {
            if (DEBUG) Log.d(TAG, "Mid frequency band beat detected");
            AnimationManager.playMusic("mid");
        }

        energySum = 0;

        // Sum the energy in the mid-high frequency band
        while (nextFrequency < MID_HIGH_FREQUENCY) {
            // Calculate the energy of the current frequency
            energySum += Math.sqrt(audioBytes[k] * audioBytes[k] + audioBytes[k + 1] * audioBytes[k + 1]);

            // Increment the frequency index
            k += 2;
            nextFrequency = (k / 2.0 * sampleRate) / captureSize;
        }

        // Calculate the average energy in the mid-high frequency band
        sampleAvgAudioEnergy = energySum / (k / 2.0);

        // Accumulate the mid high-frequency band energy over time
        mRunningSoundAvg[3] += sampleAvgAudioEnergy;

        // Check for a beat in the mid-high frequency band
        if( (sampleAvgAudioEnergy >  mCurrentAvgEnergyOneSec[3]) && (mCurrentAvgEnergyOneSec[3] > 0) ) {
            if (DEBUG) Log.d(TAG, "Mid-high frequency band beat detected");
            AnimationManager.playMusic("mid_high");
        }

        // Second Byte: Only imaginary part of the last frequency (include in highs)
        energySum = Math.abs(audioBytes[1]);

        // Sum the energy in the high frequency band
        while (nextFrequency < HIGH_FREQUENCY) {
            // Calculate the energy of the current frequency
            energySum += Math.sqrt(audioBytes[k] * audioBytes[k] + audioBytes[k + 1] * audioBytes[k + 1]);

            // Increment the frequency index
            k += 2;
            nextFrequency = (k / 2.0 * sampleRate) / captureSize;
        }

        // Calculate the average energy in the high frequency band
        sampleAvgAudioEnergy = energySum / (k / 2.0);

        // Accumulate the high frequency band energy over time
        mRunningSoundAvg[4] += sampleAvgAudioEnergy;

        // Check for a beat in the high frequency band
        if( (sampleAvgAudioEnergy >  mCurrentAvgEnergyOneSec[4]) && (mCurrentAvgEnergyOneSec[4] > 0) ) {
            if (DEBUG) Log.d(TAG, "High frequency band beat detected");
            AnimationManager.playMusic("high");
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - mSystemTimeStartSec >= 1000) {
            mCurrentAvgEnergyOneSec[0] = mRunningSoundAvg[0] / mNumberOfSamplesInOneSec;
            mCurrentAvgEnergyOneSec[1] = mRunningSoundAvg[1] / mNumberOfSamplesInOneSec;
            mCurrentAvgEnergyOneSec[2] = mRunningSoundAvg[2] / mNumberOfSamplesInOneSec;
            mCurrentAvgEnergyOneSec[3] = mRunningSoundAvg[3] / mNumberOfSamplesInOneSec;
            mCurrentAvgEnergyOneSec[4] = mRunningSoundAvg[4] / mNumberOfSamplesInOneSec;


            // Reset the running energy sum and sample count
            mRunningSoundAvg[0] = 0;
            mRunningSoundAvg[1] = 0;
            mRunningSoundAvg[2] = 0;
            mRunningSoundAvg[3] = 0;
            mRunningSoundAvg[4] = 0;
            mNumberOfSamplesInOneSec = 0;

            // Update the start time for the next one-second interval
            mSystemTimeStartSec = currentTime;
        }
        mNumberOfSamplesInOneSec++;
    } catch (IllegalStateException e) {
        Log.e(TAG, "Error accessing Visualizer state: " + e.getMessage());
    }

    }
}
