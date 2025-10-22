/*
 * Copyright (C) 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.glyph.Settings;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceScreen;

import com.android.internal.util.ArrayUtils;
import com.android.settingslib.widget.MainSwitchPreference;

import org.lineageos.glyph.R;
import org.lineageos.glyph.Constants.Constants;
import org.lineageos.glyph.Manager.SettingsManager;
import org.lineageos.glyph.Preference.GlyphAnimationPreference;
import org.lineageos.glyph.Utils.ResourceUtils;
import org.lineageos.glyph.Utils.ServiceUtils;

public class CallSettingsFragment extends PreferenceFragment implements OnPreferenceChangeListener,
        OnCheckedChangeListener {

    private PreferenceScreen mScreen;

    private MainSwitchPreference mSwitchBar;

    private ListPreference mListPreference;

    private GlyphAnimationPreference mGlyphAnimationPreference;

    private Handler mHandler = new Handler();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.glyph_call_settings);

        mScreen = this.getPreferenceScreen();
        getActivity().setTitle(R.string.glyph_settings_call_toggle_title);

        mSwitchBar = (MainSwitchPreference) findPreference(Constants.GLYPH_CALL_SUB_ENABLE);
        mSwitchBar.addOnSwitchChangeListener(this);
        mSwitchBar.setChecked(SettingsManager.isGlyphCallEnabled());

        mListPreference = (ListPreference) findPreference(Constants.GLYPH_CALL_SUB_ANIMATIONS);
        mListPreference.setOnPreferenceChangeListener(this);
        mListPreference.setEntries(ResourceUtils.getCallAnimations());
        mListPreference.setEntryValues(ResourceUtils.getCallAnimations());
        if (!ArrayUtils.contains(ResourceUtils.getCallAnimations(), mListPreference.getValue())) {
            mListPreference.setValue(ResourceUtils.getString("glyph_settings_call_animations_default"));
        }

        mGlyphAnimationPreference = (GlyphAnimationPreference) findPreference(Constants.GLYPH_CALL_SUB_PREVIEW);
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mGlyphAnimationPreference.updateAnimation(SettingsManager.isGlyphCallEnabled(),
                SettingsManager.getGlyphCallAnimation());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final String preferenceKey = preference.getKey();

        if (preferenceKey.equals(Constants.GLYPH_CALL_SUB_ANIMATIONS)) {
            mGlyphAnimationPreference.updateAnimation(SettingsManager.isGlyphCallEnabled(),
                newValue.toString());
        }

        //mHandler.post(() -> ServiceUtils.checkGlyphService());

        return true;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        SettingsManager.setGlyphCallEnabled(isChecked);
        ServiceUtils.checkGlyphService();
        mGlyphAnimationPreference.updateAnimation(isChecked,
                SettingsManager.getGlyphCallAnimation());
    }

}
