package com.example.android.shushme;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.android.shushme.R;

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

        float radius = Float.parseFloat(sp.getString(PREF_RADIUS, Constants.GEOFENCE_RADIUS_DEFAULT));
        return radius;
    }

    public static void setNotifications(Context context, int notifications) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();

        editor.putInt(PREF_NOTIFICATIONS, notifications);
    }

    public static int getNotifications(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        int notifications = Integer.parseInt(sp.getString(PREF_NOTIFICATIONS, "100"));
        return notifications;
    }
}
