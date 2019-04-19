package com.example.android.shushme;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;

/** The locationObj will encapsulate the information for each place. That is, the Place
 * object plus the radius for that object, plus whatever other information we may want at
 * some point. For example, location update rate, expieration of the fence
 */

public class LocationObj {
    private Place mPlace;
    private float mRadius;
    private int mTableIdx;
    private int mUpdateLocation;

    public LocationObj() {
    }

    public LocationObj(Place place) {
        mPlace = place;
        mRadius = 150;
        mUpdateLocation = 300;
    }

    public LocationObj(Place place, float radius, int update) {
        mPlace = place;
        mRadius = radius;
        mUpdateLocation = update;
    }

    public LocationObj(Place place, float radius, int id, int update) {
        mPlace = place;
        mRadius = radius;
        mTableIdx = id;
        mUpdateLocation = update;
    }

    public String getId() {
        return mPlace.getId();
    }

    public LatLng getLatLng() {
        return mPlace.getLatLng();
    }

    public Place getPlace() {
        return mPlace;
    }

    public float getRadius() {
        return mRadius;
    }

    public void setPlace(Place place) {
        mPlace = place;
    }

    public void setRadius(float radius) {
        mRadius = radius;
    }

    public String getName() {
        return mPlace.getName();
    }

    public String getAddress() {
        return mPlace.getAddress();
    }

    public int getTableIdx() {
        return mTableIdx;
    }

    public void setTableIdx(int id) {
        mTableIdx = id;
    }

    public void setUpdateLocation(int update) { mUpdateLocation = update; }

    public int getUpdateLocation() { return mUpdateLocation; }
}
