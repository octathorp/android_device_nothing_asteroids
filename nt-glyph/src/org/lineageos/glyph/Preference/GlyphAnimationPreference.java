/*
 * Copyright (C) 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.glyph.Preference;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.FrameLayout;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.lineageos.glyph.R;
import org.lineageos.glyph.Constants.Constants;
import org.lineageos.glyph.Utils.ResourceUtils;

public class GlyphAnimationPreference extends Preference {

    private final String TAG = "GlyphAnimationPreference";
    private final boolean DEBUG = true;

    private Activity mActivity;

    private String animationName;
    private boolean animationTerminated;
    private boolean animationPaused = true;
    private int animationTimeBetween = 0;
    private String[] animationSlugs;
    private ImageView[] animationImgs;

    private View mRootView;
    private final View.OnClickListener mClickListener = v -> performClick(v);

    public GlyphAnimationPreference(Context context) {
        super(context);
        setActivity(context);
        setLayout(R.layout.glyph_settings_preview);
    }
    public GlyphAnimationPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setActivity(context);
        setLayout(R.layout.glyph_settings_preview);
    }
    public GlyphAnimationPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setActivity(context);
        setLayout(R.layout.glyph_settings_preview);
    }
    public GlyphAnimationPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        setActivity(context);
        setLayout(defStyleRes);
    }

    private void setLayout(int layoutResource) {
        setLayoutResource(R.layout.glyph_settings_preview_frame);
        mRootView = LayoutInflater.from(getContext())
                .inflate(layoutResource, null, false);
        setShouldDisableView(false);
    }

    private void setActivity(Context context) {
        if (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                mActivity = (Activity) context;
            } else {
                setActivity(((ContextWrapper) context).getBaseContext());
            }
        }
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        holder.itemView.setOnClickListener(mClickListener);

        final boolean selectable = isSelectable();
        holder.itemView.setFocusable(isSelectable());
        holder.itemView.setClickable(isSelectable());

        FrameLayout layout = (FrameLayout) holder.itemView;
        layout.removeAllViews();
        ViewGroup parent = (ViewGroup) mRootView.getParent();
        if (parent != null) {
            parent.removeView(mRootView);
        }
        layout.addView(mRootView);
    }

    @Override
    public void onAttached() {
        super.onAttached();
        if (DEBUG) Log.d(TAG, "onAttached");
        startAnimation();
    }

    @Override
    public void onDetached() {
        super.onDetached();
        if (DEBUG) Log.d(TAG, "onDetached");
        stopAnimation();
    }

    private void startAnimation() {
        animationSlugs = ResourceUtils.getStringArray("glyph_settings_animations_slugs");
        animationImgs = new ImageView[animationSlugs.length];
        for (int i = 0; i < animationSlugs.length; i++) {
            animationImgs[i] = (ImageView) mRootView.findViewById(
                ResourceUtils.getIdentifier("preview_device_" + animationSlugs[i], "id"));
        }
        animationThread.start();
    }

    private void stopAnimation() {
        animationTerminated = true;
        animationThread.interrupt();
    }

    public void updateAnimation(boolean play) {
        updateAnimation(play, animationName, 0);
    }

    public void updateAnimation(boolean play, String name) {
        updateAnimation(play, name, 0);
    }

    public void updateAnimation(boolean play, String name, int time) {
        animationTimeBetween = time;
        animationName = name;
        animationPaused = !play;
        animationThread.interrupt();
    }

    Thread animationThread = new Thread() {
        @Override
        public void run() {
            while (!animationTerminated) {
                while (animationPaused) {}
                if (DEBUG) Log.d(TAG, "Displaying animation | name: " + animationName);
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                        ResourceUtils.getAnimation(animationName)))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        long start = System.currentTimeMillis();
                        line = line.replace(" ", "");
                        line = line.endsWith(",") ? line.substring(0, line.length() - 1) : line;
                        String[] split = line.split(",");
                        if (Constants.getDevice().equals("Spacewar") && split.length == 5) { // Phone (1) pattern on Phone (1)
                            mActivity.runOnUiThread(() -> {
                                for (int i = 0; i < animationSlugs.length; i++) {
                                    setGlyphsDrawable(animationImgs[i], Integer.parseInt(split[i]));
                                }
                            });
                        } else if (Constants.getDevice().equals("Pong") && split.length == 5) { // Phone (1) pattern on Phone (2)
                            mActivity.runOnUiThread(() -> {
                                    setGlyphsDrawable(animationImgs[0], Integer.parseInt(split[0]));
                                    setGlyphsDrawable(animationImgs[1], Integer.parseInt(split[0]));
                                    setGlyphsDrawable(animationImgs[2], Integer.parseInt(split[1]));
                                    setGlyphsDrawable(animationImgs[3], Integer.parseInt(split[2]));
                                    setGlyphsDrawable(animationImgs[4], Integer.parseInt(split[2]));
                                    setGlyphsDrawable(animationImgs[5], Integer.parseInt(split[2]));
                                    setGlyphsDrawable(animationImgs[6], Integer.parseInt(split[2]));
                                    setGlyphsDrawable(animationImgs[7], Integer.parseInt(split[2]));
                                    setGlyphsDrawable(animationImgs[8], Integer.parseInt(split[2]));
                                    setGlyphsDrawable(animationImgs[9], Integer.parseInt(split[3]));
                                    setGlyphsDrawable(animationImgs[10], Integer.parseInt(split[4]));
                            });
                        } else if (Constants.getDevice().equals("Pong") && split.length == 33) { // Phone (2) pattern on Phone (2)
                            mActivity.runOnUiThread(() -> {
                                    setGlyphsDrawable(animationImgs[0], Integer.parseInt(split[0]));
                                    setGlyphsDrawable(animationImgs[1], Integer.parseInt(split[1]));
                                    setGlyphsDrawable(animationImgs[2], Integer.parseInt(split[2]));
                                    setGlyphsDrawable(animationImgs[3], Integer.parseInt(split[3]));
                                    setGlyphsDrawable(animationImgs[4], Integer.parseInt(split[19]));
                                    setGlyphsDrawable(animationImgs[5], Integer.parseInt(split[20]));
                                    setGlyphsDrawable(animationImgs[6], Integer.parseInt(split[21]));
                                    setGlyphsDrawable(animationImgs[7], Integer.parseInt(split[22]));
                                    setGlyphsDrawable(animationImgs[8], Integer.parseInt(split[23]));
                                    setGlyphsDrawable(animationImgs[9], Integer.parseInt(split[25]));
                                    setGlyphsDrawable(animationImgs[10], Integer.parseInt(split[24]));
                            });
                        } else if (Constants.getDevice().equals("Asteroids") && split.length == 5) { // Phone (1) pattern on Phone (3a)
                            mActivity.runOnUiThread(() -> {
                                    setGlyphsDrawable(animationImgs[0], Integer.parseInt(split[0]));
                                    setGlyphsDrawable(animationImgs[1], Integer.parseInt(split[2]));
                                    setGlyphsDrawable(animationImgs[2], Integer.parseInt(split[4]));
                            });
                        } else if (Constants.getDevice().equals("Asteroids") && split.length == 36) { // Phone (3a) pattern on Phone (3a)
                            mActivity.runOnUiThread(() -> {
                                    setGlyphsDrawable(animationImgs[0], Integer.parseInt(split[0]));
                                    setGlyphsDrawable(animationImgs[1], Integer.parseInt(split[35]));
                                    setGlyphsDrawable(animationImgs[2], Integer.parseInt(split[34]));
                            });
                        } else {
                            if (DEBUG) Log.d(TAG, "Animation line length mismatch | name: " + animationName + " | line: " + line);
                            updateAnimation(false);
                        }
                        long delay = 16666L - (System.currentTimeMillis() - start);
                        Thread.sleep(delay/1000);
                    }
                    Thread.sleep(animationTimeBetween);
                } catch (Exception e) {
                    if (DEBUG) Log.d(TAG, "Exception while displaying animation | name: " + animationName + " | exception: " + e);
                } finally {
                    if (animationPaused) {
                        if (DEBUG) Log.d(TAG, "Pause displaying animation | name: " + animationName);
                        mActivity.runOnUiThread(() -> {
                            for (int i = 0; i < animationSlugs.length; i++) {
                                setGlyphsDrawable(animationImgs[i], 0);
                            }
                        });
                    }
                }
            }
        }
        
        private void setGlyphsDrawable(ImageView imageView, int brightness) {
            if (brightness <= 0) {
                imageView.setAlpha(0.3f);
            } else {
                float brightnessFactor = (float) (0.4 + 0.6 * (brightness / (double) Constants.getMaxBrightness()));
                imageView.setAlpha(brightnessFactor);
            }
        }
    };
}
