/*
 * Copyright (C) 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.glyph.Sensors;

import android.annotation.NonNull;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Consumer;

public class FlipToGlyphSensor implements SensorEventListener {

    private static final boolean DEBUG = true;
    private static final String TAG = "FlipToGlyphSensor";

    private boolean isFlipped = false;
    private final Consumer<Boolean> mOnFlip;

    private SensorManager mSensorManager;
    private Sensor mSensorAccelerometer;
    private Context mContext;

    private Duration mTimeThreshold = Duration.ofMillis(1_000L);;
    private float mAccelerationThreshold = 0.2f;
    private float mZAccelerationThreshold = -9.5f;
    private float mZAccelerationThresholdLenient = mZAccelerationThreshold + 1.0f;
    private float mPrevAcceleration = 0;
    private long mPrevAccelerationTime = 0;
    private boolean mZAccelerationIsFaceDown = false;
    private long mZAccelerationFaceDownTime = 0L;

    private static final float MOVING_AVERAGE_WEIGHT = 0.5f;
    private final ExponentialMovingAverage mCurrentXYAcceleration =
            new ExponentialMovingAverage(MOVING_AVERAGE_WEIGHT);
    private final ExponentialMovingAverage mCurrentZAcceleration =
            new ExponentialMovingAverage(MOVING_AVERAGE_WEIGHT);

    public FlipToGlyphSensor(Context context, @NonNull Consumer<Boolean> onFlip) {
        mContext = context;
        mOnFlip = Objects.requireNonNull(onFlip);
        mSensorManager = mContext.getSystemService(SensorManager.class);
        mSensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER, false);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) return;

        final float x = event.values[0];
        final float y = event.values[1];
        mCurrentXYAcceleration.updateMovingAverage(x * x + y * y);
        mCurrentZAcceleration.updateMovingAverage(event.values[2]);

        final long curTime = event.timestamp;
        if (Math.abs(mCurrentXYAcceleration.mMovingAverage - mPrevAcceleration)
                > mAccelerationThreshold) {
            mPrevAcceleration = mCurrentXYAcceleration.mMovingAverage;
            mPrevAccelerationTime = curTime;
        }
        final boolean moving = curTime - mPrevAccelerationTime <= mTimeThreshold.toNanos();

        final float zAccelerationThreshold =
                isFlipped ? mZAccelerationThresholdLenient : mZAccelerationThreshold;
        final boolean isCurrentlyFaceDown =
                mCurrentZAcceleration.mMovingAverage < zAccelerationThreshold;
        final boolean isFaceDownForPeriod = isCurrentlyFaceDown
                && mZAccelerationIsFaceDown
                && curTime - mZAccelerationFaceDownTime > mTimeThreshold.toNanos();
        if (isCurrentlyFaceDown && !mZAccelerationIsFaceDown) {
            mZAccelerationFaceDownTime = curTime;
            mZAccelerationIsFaceDown = true;
        } else if (!isCurrentlyFaceDown) {
            mZAccelerationIsFaceDown = false;
        }

        if (!moving && isFaceDownForPeriod && !isFlipped) {
            onFlip(true);
        } else if (!isFaceDownForPeriod && isFlipped) {
            onFlip(false);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void onFlip(boolean flipped) {
        if (DEBUG) Log.d(TAG, "Flipped: " + flipped);
        mOnFlip.accept(flipped);
        isFlipped = flipped;
    }

    public void enable() {
        if (DEBUG) Log.d(TAG, "Enabling Sensor");
        mSensorManager.registerListener(this, mSensorAccelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL,
                    mContext.getResources().getInteger(
                        com.android.internal.R.integer.config_flipToScreenOffMaxLatencyMicros));
    }

    public void disable() {
        if (DEBUG) Log.d(TAG, "Disabling Sensor");
        onFlip(false);
        mSensorManager.unregisterListener(this, mSensorAccelerometer);
    }

    private final class ExponentialMovingAverage {
        private final float mAlpha;
        private final float mInitialAverage;
        private float mMovingAverage;

        ExponentialMovingAverage(float alpha) {
            this(alpha, 0.0f);
        }

        ExponentialMovingAverage(float alpha, float initialAverage) {
            this.mAlpha = alpha;
            this.mInitialAverage = initialAverage;
            this.mMovingAverage = initialAverage;
        }

        void updateMovingAverage(float newValue) {
            mMovingAverage = newValue + mAlpha * (mMovingAverage - newValue);
        }

        void reset() {
            mMovingAverage = this.mInitialAverage;
        }
    }
}
