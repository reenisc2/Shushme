package com.example.android.shushme;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;

public class SettingsFragment extends PreferenceFragmentCompat implements
     SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "SettingsFragment";

    private void setPreferenceSummary(Preference preference, Object value) {
        String stringValue = value.toString();
        Log.i(TAG, "in setPreferenceSummary  " + stringValue + ",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,");
        Log.i(TAG, "in setPreferenceSummary  " + preference.getKey() + ",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,");
        preference.setSummary(stringValue);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // Add 'general' preferences, defined in the XML file
        Log.i(TAG, "onCreatePreferences...........................................");
        addPreferencesFromResource(R.xml.pref_settings);

        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        PreferenceScreen prefScreen = getPreferenceScreen();
        int count = prefScreen.getPreferenceCount();
        Log.i(TAG, "Preference count: " + count + "///////////////////////////////");
        for (int i = 0; i < count; i++) {
            Preference p = prefScreen.getPreference(i);
            String key = p.getKey();
            Log.i(TAG, "In loop over preference count, and the key is...." + key + "   ......");
            if (key.equals("radius")) {
                float value = Float.parseFloat(sharedPreferences.getString(key,
                        Constants.GEOFENCE_RADIUS_DEFAULT));
                setPreferenceSummary(p, value);
            } else if (key.equals("updates")) {
                int value = Integer.parseInt(sharedPreferences.getString(key,
                        Constants.GEOFENCE_NOTIFICATION_FREQUENCY_DEFAULT));
                setPreferenceSummary(p, value);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // unregister the preference change listener
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        // register the preference change listener
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Activity activity = getActivity();
        Log.i(TAG, "onSharedPreferenceChanged_____________________________________");

        Preference preference = findPreference(key);
        if (null != preference) {
            if (key.equals(ShushmePreferences.PREF_RADIUS)) {
                setPreferenceSummary(preference, Float.parseFloat(
                        sharedPreferences.getString(key, Constants.GEOFENCE_RADIUS_DEFAULT)));
            } else if (key.equals(ShushmePreferences.PREF_NOTIFICATIONS)) {
                setPreferenceSummary(preference, Integer.parseInt(
                        sharedPreferences.getString(key, Constants.GEOFENCE_NOTIFICATION_FREQUENCY_DEFAULT)));
            }
        }
    }
}
