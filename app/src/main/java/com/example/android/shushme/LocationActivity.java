package com.example.android.shushme;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;

import java.util.ArrayList;
import java.util.List;

public class LocationActivity extends AppCompatActivity
    implements OnMapReadyCallback,
        OnMyLocationButtonClickListener,
        OnMyLocationClickListener,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnPoiClickListener {

    private GoogleMap mMap;
    private LatLng mNewPos = Constants.HOME;
    private int mNearestCenter = -1;
    private float mNewRad = 0;
    private static final String TAG = LocationActivity.class.getSimpleName();
    private FusedLocationProviderClient mLocationProvider;
    private List<Centers> mCenters = new ArrayList<>();
    private boolean mDraggingMarker = false;
    private int newLocationsAdded = 0;

    public LocationActivity() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        int count = intent.getIntExtra(Constants.PLACE_COUNT, 0);
        if (count > 0) {
            for (int idx = 0; idx < count; idx++) {
                mCenters.add(new Centers(intent.getStringExtra(Constants.PID + idx),
                                         intent.getParcelableExtra(Constants.LAT_LNG + idx),
                                         intent.getFloatExtra(Constants.RAD + idx, 100)));
                mCenters.get(idx).setEnabled(intent.getBooleanExtra(Constants.ENABLED + idx, true));
            }
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setContentView(R.layout.activity_location);

        mLocationProvider = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        try {
            mapFragment.getMapAsync(this);
        } catch (NullPointerException e) {
            Toast.makeText(this, "Map retrieval failed", Toast.LENGTH_LONG).show();
        }

    }

    public void onMapReady(GoogleMap map) {
        mMap = map;
        CameraPosition target = CameraPosition.builder().target(Constants.HOME).zoom(14).build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(target));
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        mMap.setOnMarkerDragListener(this);
        mMap.setOnPoiClickListener(this);
        enableMyLocation();
        if (mCenters != null && mCenters.size() > 0) {
            for (int i = 0; i < mCenters.size(); i++) {
                mNewRad = mCenters.get(i).getRad();
                float alpha = mCenters.get(i).getEnabled() ? 1.0f : 0.5f;
                CircleOptions circleOptions = new CircleOptions().center(mCenters.get(i).getLatLng()).radius(mNewRad).strokeColor(0xffff0000).strokeWidth(4);
                mMap.addMarker(new MarkerOptions().position(mCenters.get(i).getLatLng()).alpha(alpha));
                Circle c = mMap.addCircle(circleOptions);
                mCenters.get(i).setCircle(c);
                mNearestCenter = i;
                placeMarker(Utils.calculateMarkerPos(mCenters.get(i).getLatLng(), mNewRad));
            }
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Log.d(TAG, "onMyLocationButtonClick");
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Log.d(TAG, "onMyLocationClick");
    }

    @Override
    public void onPoiClick(PointOfInterest poi) {
        if (duplicateSelection(poi)) return;
        mNewRad = Float.parseFloat(Constants.GEOFENCE_RADIUS_DEFAULT);
        CircleOptions circleOptions = new CircleOptions().center(poi.latLng).radius(mNewRad).strokeColor(0xffff0000).strokeWidth(4).clickable(true);
        mMap.addMarker(new MarkerOptions().position(poi.latLng));
        Circle c = mMap.addCircle(circleOptions);
        mNewPos = poi.latLng;
        mCenters.add(new Centers(poi.placeId, poi.latLng, mNewRad, c));
        mNearestCenter = mCenters.size() - 1;
        mCenters.get(mNearestCenter).setNewlyAdded();
        mCenters.get(mNearestCenter).setEnabled(true);
        LatLng initialMarkerPos = Utils.calculateMarkerPos(mNewPos, mNewRad);
        placeMarker(initialMarkerPos);
        newLocationsAdded++;
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        mNewPos = marker.getPosition();
        mNewRad = Utils.calculateRadius(mNewPos, mCenters.get(mNearestCenter).getLatLng());
        marker.setTitle(String.format("Radius: %.1f", mNewRad));
        marker.showInfoWindow();
        updateRadiusAndCircle();
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        for (int i = 0; i < mCenters.size(); i++) {
            if (mCenters.get(i).getMarker().equals(marker)) {
                mNearestCenter = i;
                break;
            }
        }
        mDraggingMarker = true;
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        mNewPos = marker.getPosition();
        mNewRad = Utils.calculateRadius(mNewPos, mCenters.get(mNearestCenter).getLatLng());
        updateRadiusAndCircle();
        mDraggingMarker = false;
        mCenters.get(mNearestCenter).setHasChanged();
    }

    private void placeMarker(LatLng latLng) {
        MarkerOptions markerOptions = new MarkerOptions();
        float alpha = mCenters.get(mNearestCenter).getEnabled() ? 1.0f : 0.5f;
        markerOptions.position(latLng).draggable(true).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).alpha(alpha);
        markerOptions.title(String.format("Radius: %.1f", mNewRad));
        markerOptions.snippet("Drag marker to resize geofence circle.");
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        if (mCenters.get(mNearestCenter).hasMarker()) {
            mCenters.get(mNearestCenter).getMarker().remove();
        }
        Marker m = mMap.addMarker(markerOptions);
        m.showInfoWindow();
        mCenters.get(mNearestCenter).setMarker(m);
        updateRadiusAndCircle();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        Bundle bundle = new Bundle();
        bundle.putDouble("Lat", mNewPos.latitude);
        bundle.putDouble("Lng", mNewPos.longitude);
        Intent resultIntent = new Intent();
        resultIntent.putExtras(bundle);
        setResult(RESULT_OK, resultIntent);
        finish();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        backAction();
    }

    @Override
    public boolean onSupportNavigateUp() {
        backAction();
        return true;
    }
    private void enableMyLocation() {
        try {
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
            }
        } catch (SecurityException e) {
            Toast.makeText(this, "No permissions!", Toast.LENGTH_LONG).show();
        }
    }

    private void backAction() {
        Intent resultIntent = new Intent();
        int changed = getChangedLocationsCount();
        if (newLocationsAdded == 0 && changed == 0) {
            setResult(RESULT_OK);
        }
        ReturnLists returnLists = updatePlaceIdList();
        if (returnLists.mPoi != null) {
            Bundle bundle = new Bundle();
            bundle.putStringArrayList("PlaceIds", returnLists.mPoi);
            bundle.putFloatArray("Rads", convertFloatList(returnLists.mNewPoiRads));
            bundle.putBooleanArray("News", convertBoolList(returnLists.mNew));
            resultIntent.putExtras(bundle);
            setResult(RESULT_OK, resultIntent);
        } else {
            setResult(RESULT_OK);
        }
        finish();
    }


    private int getChangedLocationsCount() {
        if (mCenters.size() == 0) return 0;
        int count = 0;
        for (int i = 0; i < mCenters.size(); i++) {
            if (!mCenters.get(i).isNewlyAdded() && mCenters.get(i).hasChanged()) {
                count += 1;
            }
        }
        return count;
    }
    private class ReturnLists {
        public ArrayList<String> mPoi;
        public ArrayList mNewPoiRads;
        public ArrayList mNew;

        private ReturnLists(ArrayList<String> mPoi, ArrayList newRads, ArrayList news) {
            this.mPoi = mPoi;
            this.mNewPoiRads = newRads;
            this.mNew = news;
        }
    }

    private ReturnLists updatePlaceIdList() {
        ArrayList<String> mPoi = new ArrayList<>();
        ArrayList mNewPoiRads = new ArrayList<>();
        ArrayList mNew = new ArrayList<>();
        for (int i = 0; i < mCenters.size(); i++) {
            if (mCenters.get(i).hasChanged() || mCenters.get(i).isNewlyAdded()) {
                mPoi.add(mCenters.get(i).getPid());
                mNewPoiRads.add(mCenters.get(i).getRad());
                mNew.add(mCenters.get(i).isNewlyAdded());
            }
        }
        return new ReturnLists(mPoi, mNewPoiRads, mNew);
    }

    private float[] convertFloatList(ArrayList newRads) {
        float[] floatArray = new float[newRads.size()];

        for (int i = 0; i < newRads.size(); i++) {
            floatArray[i] = (float) newRads.get(i);
        }
        return floatArray;
    }

    private boolean[] convertBoolList(ArrayList news) {
        boolean[] boolArray = new boolean[news.size()];
        for (int i = 0; i < news.size(); i++) {
            boolArray[i] = (boolean) news.get(i);
        }
        return boolArray;
    }

    private boolean duplicateSelection(PointOfInterest poi) {
        for (int i = 0; i < mCenters.size(); i++) {
            if (mCenters.get(i).getPid().equals(poi.placeId)) {
                return true;
            }
        }
        return false;
    }

    private void updateRadiusAndCircle() {
        mCenters.get(mNearestCenter).setRad(mNewRad);
        Circle existingCircle = mCenters.get(mNearestCenter).getCircle();
        if (existingCircle != null) {
            existingCircle.remove();
        }
        CircleOptions circleOptions = new CircleOptions()
                .center(mCenters.get(mNearestCenter).getLatLng())
                .radius(mCenters.get(mNearestCenter).getRad())
                .strokeColor(0xffff0000).strokeWidth(4).clickable(true);
        Circle c = mMap.addCircle(circleOptions);
        mCenters.get(mNearestCenter).setCircle(c);
    }
}
