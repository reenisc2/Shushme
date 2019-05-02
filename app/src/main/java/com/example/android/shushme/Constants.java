package com.example.android.shushme;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

public final class Constants {

    private Constants() {
    }

    public static final String PACKAGE_NAME = "com.google.android.gms.location.Geofence";
    public static final String SHARED_PREFERENCES = PACKAGE_NAME + ".SHARED_PREFERENCES_NAME";
    public static final String GEOFENCES_ADDED_KEY = PACKAGE_NAME + "GEOFENCES_ADDED_KEY";
    public static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;

    /**
     * For this sample, geofences expire after twelve hours.
     */
    public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000;
    //public static final float GEOFENCE_RADIUS_IN_METERS = 1609; // 1 mile, 1.6 km
    public static final String GEOFENCE_RADIUS_DEFAULT = "100"; // 1 mile, 1.6 km
    public static final String GEOFENCE_NOTIFICATION_FREQUENCY_DEFAULT = "300"; // 60sec *5min

    /**
     * Map for storing information about possible local fenced areas for shushme if that gets revived
     */
    public static final LatLng DEFAULT_LOCATION = new LatLng(42.824506, -73.960259); // scotia
    public static final LatLng HOME = new LatLng(42.458728, -71.529123);

    public static final String CHANNEL_ID = "Shushme notifications";

    /**
     * Names of extras passed between MainActivity and LocationActivity
     */
    public static final String PLACE_COUNT = "placeCount";
    public static final String LAT_LNG = "latng_";
    public static final String RAD = "rad_";
    public static final String PID = "pid_";
    public static final String ENABLED = "enabled_";

    // initial bearing for radius marker when POI clicked - degrees clockwise from North
    public static final double INITIAL_BEARING = 0.0;
    public static final double EARTH_RADIUS_METERS = 6378 * 1000;

}

