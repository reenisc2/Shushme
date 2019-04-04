package com.example.android.shushme;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public final class ShushmePreferences {

    public static final String PREF_RADIUS = "radius";
    public static final String PREF_NOTIFICATIONS = "updates";

    public static void setRadius(Context context, float radius) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();

        editor.putFloat(PREF_RADIUS, radius);
        editor.apply();
    }

    public static float getRadius(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        return Float.parseFloat(sp.getString(PREF_RADIUS, Constants.GEOFENCE_RADIUS_DEFAULT));
    }

    public static void setNotifications(Context context, int notifications) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();

        editor.putInt(PREF_NOTIFICATIONS, notifications);
        editor.apply();
    }

    public static int getNotifications(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        return Integer.parseInt(sp.getString(PREF_NOTIFICATIONS, "100"));
    }
}
