/*
 * Copyright (C) 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.glyph.Settings;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import androidx.preference.ListPreference;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.android.internal.util.ArrayUtils;
import com.android.settingslib.widget.MainSwitchPreference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lineageos.glyph.R;
import org.lineageos.glyph.Constants.Constants;
import org.lineageos.glyph.Manager.SettingsManager;
import org.lineageos.glyph.Preference.GlyphAnimationPreference;
import org.lineageos.glyph.Utils.ResourceUtils;
import org.lineageos.glyph.Utils.ServiceUtils;

public class NotifsSettingsFragment extends PreferenceFragment implements OnPreferenceChangeListener,
        OnCheckedChangeListener {

    private static final String TAG = "NotifsSettingsFragment";
    private static final boolean DEBUG = false;

    private PreferenceScreen mScreen;

    private MainSwitchPreference mSwitchBar;
    private PreferenceCategory mCategory;

    private List<String> mEssentialApps = new ArrayList<String>();
    private List<String> mEssentialAppsNames = new ArrayList<String>();

    private PackageManager mPackageManager;

    private ListPreference mListPreference;
    private MultiSelectListPreference mMultiSelectListPreference;

    private GlyphAnimationPreference mGlyphAnimationPreference;

    private Handler mHandler = new Handler();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.glyph_notifs_settings);

        mScreen = this.getPreferenceScreen();
        getActivity().setTitle(R.string.glyph_settings_notifs_toggle_title);

        mSwitchBar = (MainSwitchPreference) findPreference(Constants.GLYPH_NOTIFS_SUB_ENABLE);
        mSwitchBar.addOnSwitchChangeListener(this);
        mSwitchBar.setChecked(SettingsManager.isGlyphNotifsEnabled());

        mCategory = (PreferenceCategory) findPreference(Constants.GLYPH_NOTIFS_SUB_CATEGORY);

        mListPreference = (ListPreference) findPreference(Constants.GLYPH_NOTIFS_SUB_ANIMATIONS);
        mListPreference.setOnPreferenceChangeListener(this);
        mListPreference.setEntries(ResourceUtils.getNotificationAnimations());
        mListPreference.setEntryValues(ResourceUtils.getNotificationAnimations());
        if (!ArrayUtils.contains(ResourceUtils.getNotificationAnimations(), mListPreference.getValue())) {
            mListPreference.setValue(ResourceUtils.getString("glyph_settings_notifs_animations_default"));
        }

        mGlyphAnimationPreference = (GlyphAnimationPreference) findPreference(Constants.GLYPH_NOTIFS_SUB_PREVIEW);

        mPackageManager = getActivity().getPackageManager();
        List<ApplicationInfo> mApps = mPackageManager.getInstalledApplications(PackageManager.GET_GIDS);
        Collections.sort(mApps, new ApplicationInfo.DisplayNameComparator(mPackageManager));
        for (ApplicationInfo app : mApps) {
            if(mPackageManager.getLaunchIntentForPackage(app.packageName) != null  && !ArrayUtils.contains(Constants.APPS_TO_IGNORE, app.packageName)) { // apps with launcher intent
                SwitchPreference mSwitchPreference = new SwitchPreference(mScreen.getContext());
                mSwitchPreference.setKey(app.packageName);
                mSwitchPreference.setTitle(" " + app.loadLabel(mPackageManager).toString()); // add this space since the layout looks off otherwise
                mSwitchPreference.setIcon(app.loadIcon(mPackageManager));
                mSwitchPreference.setDefaultValue(true);
                mSwitchPreference.setOnPreferenceChangeListener(this);
                mCategory.addPreference(mSwitchPreference);

                mEssentialApps.add(app.packageName);
                mEssentialAppsNames.add(app.loadLabel(mPackageManager).toString());
            }
        }

        mMultiSelectListPreference = (MultiSelectListPreference) findPreference(Constants.GLYPH_NOTIFS_SUB_ESSENTIAL);
        mMultiSelectListPreference.setOnPreferenceChangeListener(this);
        mMultiSelectListPreference.setEntries(mEssentialAppsNames.toArray(new CharSequence[0]));
        mMultiSelectListPreference.setEntryValues(mEssentialApps.toArray(new CharSequence[0]));

    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mGlyphAnimationPreference.updateAnimation(SettingsManager.isGlyphNotifsEnabled(),
                SettingsManager.getGlyphNotifsAnimation(), 1500);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final String preferenceKey = preference.getKey();

        if (preferenceKey.equals(Constants.GLYPH_NOTIFS_SUB_ANIMATIONS)) {
            mGlyphAnimationPreference.updateAnimation(SettingsManager.isGlyphNotifsEnabled(),
                newValue.toString(), 1500);
        }

        if (preferenceKey.equals(Constants.GLYPH_NOTIFS_SUB_ESSENTIAL)) {
            if (DEBUG) Log.d(TAG, "onPreferenceChange: " + newValue.toString());
        }

        //mHandler.post(() -> ServiceUtils.checkGlyphService());

        return true;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        SettingsManager.setGlyphNotifsEnabled(isChecked);
        ServiceUtils.checkGlyphService();
        mGlyphAnimationPreference.updateAnimation(isChecked,
                SettingsManager.getGlyphNotifsAnimation(), 1500);
    }

}
