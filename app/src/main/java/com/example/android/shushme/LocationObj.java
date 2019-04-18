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

    public LocationObj() {
    }

    public LocationObj(Place place) {
        mPlace = place;
        mRadius = 150;
    }

    public LocationObj(Place place, float radius) {
        mPlace = place;
        mRadius = radius;
    }

    public LocationObj(Place place, float radius, int id) {
        mPlace = place;
        mRadius = radius;
        mTableIdx = id;
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
}
