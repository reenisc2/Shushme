package com.example.android.shushme;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Utility methods for shushme.
 */

public class Utils {

    public static int findNearestCenter(LatLng point, List<Centers> mCenters) {
        if (mCenters.isEmpty()) {
            return -1;
        }
        int nearestCenter = 0;
        float newRad = calculateRadius(point, mCenters.get(0).getLatLng());
        if (mCenters.size() == 1) {
            return 1;
        }
        for (int idx = 1; idx < mCenters.size(); idx++) {
            float newDistance = calculateRadius(point, mCenters.get(idx).getLatLng());
            if (newDistance < newRad) {
                newRad = newDistance;
                nearestCenter = idx;
            }
        }
        return nearestCenter;
    }

    public static float calculateRadius(LatLng l1, LatLng l2) {
        Location newPoint = new Location("point");
        newPoint.setLatitude(l1.latitude);
        newPoint.setLongitude(l1.longitude);
        Location center = new Location("center");
        center.setLatitude(l2.latitude);
        center.setLongitude(l2.longitude);
        return newPoint.distanceTo(center);
    }

    public static LatLng calculateMarkerPos(LatLng center, float radius) {
        double bearing = Math.toRadians(Constants.INITIAL_BEARING);
        double centerLat = Math.toRadians(center.latitude);
        double centerLng = Math.toRadians(center.longitude);
        double distance = radius / Constants.EARTH_RADIUS_METERS;

        double a = Math.sin(distance) * Math.cos(centerLat);
        double outerLat = Math.asin(Math.sin(centerLat) * Math.cos(distance) + a * Math.cos(bearing));
        double outerLng = centerLng + Math.atan2(Math.sin(bearing) * a, Math.cos(bearing) - Math.sin(centerLat) * Math.sin(outerLat));

        return new LatLng(Math.toDegrees(outerLat), Math.toDegrees(outerLng));
    }

}
