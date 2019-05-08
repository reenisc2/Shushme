package com.example.android.shushme;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * Class to encapsulate the points that are selected as the centers of geofences
 * for the location activity
 */

public class Centers {
    private LatLng mLatLng = null;
    private float mRad = 0;
    private Circle mCircle = null;
    private Marker mMarker = null;
    private String mPid = null;
    private boolean hasChanged = false;
    private boolean newlyAdded = false;
    private boolean mEnabled = true;

    public Centers (String pId, LatLng latLng) {
        mPid = pId;
        mLatLng = latLng;

    }

    public Centers (String pId, LatLng latLng, float rad) {
        mPid = pId;
        mLatLng = latLng;
        mRad = rad;
    }

    public Centers (String pId, LatLng latLng, float rad, Circle circle) {
        mPid = pId;
        mLatLng = latLng;
        mRad = rad;
        mCircle = circle;
    }

    public void setLatLng(LatLng v) {
        mLatLng = v;
    }

    public LatLng getLatLng() {
        return mLatLng;
    }

    public void setRad(float rad) {
        mRad = rad;
        hasChanged = true;
    }

    public float getRad() {
        return mRad;
    }

    public void setCircle(Circle circle) {
        mCircle = circle;
    }

    public Circle getCircle() {
        return mCircle;
    }

    public void setMarker(Marker marker) {
        mMarker = marker;
    }

    public boolean hasMarker() {
        return mMarker != null;
    }

    public Marker getMarker() {
        return mMarker;
    }

    public boolean hasChanged() {
        return hasChanged;
    }

    public String getPid() {
        return mPid;
    }

    public void setHasChanged() { hasChanged = true; }

    public void setNewlyAdded() { newlyAdded = true; }

    public boolean isNewlyAdded() { return newlyAdded; }

    public void setEnabled(boolean enabled) { mEnabled = enabled; }

    public boolean getEnabled() { return mEnabled; }
}
