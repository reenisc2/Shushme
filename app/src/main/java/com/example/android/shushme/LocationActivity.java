package com.example.android.shushme;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
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
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LocationActivity extends AppCompatActivity
    implements OnMapReadyCallback,
        OnMyLocationButtonClickListener,
        OnMyLocationClickListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnPoiClickListener,
        GoogleMap.OnCircleClickListener {

    private GoogleMap mMap;
    private LatLng mNewPos = Constants.HOME;
    private int mNearestCenter = -1;
    private float mNewRad = 0;
    private static final String TAG = LocationActivity.class.getSimpleName();
    private FusedLocationProviderClient mLocationProvider;
    private ArrayList<String> mPoi = new ArrayList<>();
    private ArrayList mNewPoiRads = new ArrayList<>();
    private ArrayList mNew = new ArrayList<>();
    private List<Centers> mCenters = new ArrayList<>();

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
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMarkerDragListener(this);
        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnPoiClickListener(this);
        enableMyLocation();
        if (mCenters != null && mCenters.size() > 0) {
            for (int i = 0; i < mCenters.size(); i++) {
                float radius = mCenters.get(i).getRad();
                CircleOptions circleOptions = new CircleOptions().center(mCenters.get(i).getLatLng()).radius(radius).strokeColor(0xffff0000).strokeWidth(4);
                mMap.addMarker(new MarkerOptions().position(mCenters.get(i).getLatLng()));
                Circle c = mMap.addCircle(circleOptions);
                mCenters.get(i).setCircle(c);
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
        mNewPos =  new LatLng(location.getLatitude(), location.getLongitude());
        mMap.clear();
        mMap.animateCamera(CameraUpdateFactory.newLatLng(mNewPos));
        mMap.addMarker(new MarkerOptions().position(mNewPos).draggable(true));
        List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ID);
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.builder(placeFields).build();

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Task<FindCurrentPlaceResponse> placeResponse = Places.createClient(this).findCurrentPlace(request);
            placeResponse.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FindCurrentPlaceResponse response = task.getResult();
                    for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                        Log.i(TAG, String.format("Place '%s' has likelihood: %f; PlaceID: %s",
                                placeLikelihood.getPlace().getName(),
                                placeLikelihood.getLikelihood(),
                                placeLikelihood.getPlace().getId()));
                    }
                } else {
                    Exception exception = task.getException();
                    if (exception instanceof ApiException) {
                        ApiException apiException = (ApiException) exception;
                        Log.e(TAG, "Place not found: " + apiException.getStatusCode());
                    }
                }
            });
        }
    }

    @Override
    public void onPoiClick(PointOfInterest poi) {
        float radius = ShushmePreferences.getRadius(this);
        Log.i(TAG, "fetched radius: " + radius);
        CircleOptions circleOptions = new CircleOptions().center(poi.latLng).radius(radius).strokeColor(0xffff0000).strokeWidth(4).clickable(true);
        mMap.addMarker(new MarkerOptions().position(poi.latLng));
        Circle c = mMap.addCircle(circleOptions);
        mNewPos = poi.latLng;
        mCenters.add(new Centers(poi.placeId, poi.latLng, radius, c));
        mPoi.add(poi.placeId);
        mNewPoiRads.add(radius);
        mNew.add(true);
        Log.i(TAG, "onPoiClick: " + poi.placeId);
    }

    public void onCircleClick(Circle circle) {
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
    }

    @Override
    public void onMarkerDrag(Marker marker) {
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return true;
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        mNewPos = marker.getPosition();
        Log.d(TAG, "onMarkerDragEnd "+ mNewPos.latitude + ", " + mNewPos.longitude);
        calculateRadius(mNewPos, mCenters.get(mNearestCenter).getLatLng());
        mCenters.get(mNearestCenter).setRad(mNewRad);
        Circle existingCircle = mCenters.get(mNearestCenter).getCircle();
        if (existingCircle != null) {
            Log.i(TAG, "Exisiting Circle not null");
            existingCircle.remove();
        }
        CircleOptions circleOptions = new CircleOptions()
                .center(mCenters.get(mNearestCenter).getLatLng())
                .radius(mCenters.get(mNearestCenter).getRad())
                .strokeColor(0xffff0000).strokeWidth(4).clickable(true);
        Circle c = mMap.addCircle(circleOptions);
        mCenters.get(mNearestCenter).setCircle(c);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        Log.d(TAG, "onMapClick");
        findNearestCenter(latLng);
        if (mNearestCenter == -1) {
            Log.d(TAG, "No nearest center found");
            return;
        }
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng).draggable(true).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        markerOptions.title("Radius marker");
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        if (mCenters.get(mNearestCenter).hasMarker()) {
            mCenters.get(mNearestCenter).getMarker().remove();
        }
        Marker m = mMap.addMarker(markerOptions);
        mCenters.get(mNearestCenter).setMarker(m);
        mCenters.get(mNearestCenter).setRad(mNewRad);
        if (mPoi.contains(mCenters.get(mNearestCenter).getPid())) {
            mNewPoiRads.set(mPoi.indexOf((Object) mCenters.get(mNearestCenter).getPid()), mNewRad);
        }
        Log.i(TAG, "index to nearest center: " + mNearestCenter);
        Circle existingCircle = mCenters.get(mNearestCenter).getCircle();
        if (existingCircle != null) {
            Log.i(TAG, "Exisiting Circle not null");
            existingCircle.remove();
        }
        CircleOptions circleOptions = new CircleOptions()
                .center(mCenters.get(mNearestCenter).getLatLng())
                .radius(mCenters.get(mNearestCenter).getRad())
                .strokeColor(0xffff0000).strokeWidth(4).clickable(true);
        Circle c = mMap.addCircle(circleOptions);
        mCenters.get(mNearestCenter).setCircle(c);

        int instance = mLocationProvider.getInstanceId();
    }

    @Override
    public void onMapLongClick (LatLng latLng) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("New marker - long click");
        mMap.clear();
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.addMarker(markerOptions);
        mNewPos = latLng;
        Log.d(TAG, "onMapLongClick");
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
        Log.d(TAG, "onBackPressed");
        backAction();
    }

    @Override
    public boolean onSupportNavigateUp() {
        Log.d(TAG, "onSupportNavigateUp");
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
        if (mPoi == null && changed == 0) {
            Log.i(TAG, "In back action, nothing to send back");
            setResult(RESULT_OK);
        }
        Log.i(TAG, "in backAction, changed is: " + changed);
        if (changed > 0) {
            updatePlaceIdList();
        }
        if (mPoi != null) {
            Bundle bundle = new Bundle();
            bundle.putStringArrayList("PlaceIds", mPoi);
            bundle.putFloatArray("Rads", convertFloatList());
            bundle.putBooleanArray("News", convertBoolList());
            resultIntent.putExtras(bundle);
            setResult(RESULT_OK, resultIntent);
        } else {
            setResult(RESULT_OK);
        }
        finish();
    }

    private void findNearestCenter(LatLng point) {
        if (mCenters.isEmpty()) {
            Log.i(TAG, "latLngs is empty");
            mNearestCenter = -1;
        }
        mNearestCenter = 0;
        mNewRad = calculateRadius(point, mCenters.get(0).getLatLng());
        if (mCenters.size() == 1) {
            return;
        }
        for (int idx = 1; idx < mCenters.size(); idx++) {
            float newDistance = calculateRadius(point, mCenters.get(idx).getLatLng());
            if (newDistance < mNewRad) {
                mNewRad = newDistance;
                mNearestCenter = idx;
            }
        }
    }

    private float calculateRadius(LatLng l1, LatLng l2) {
        Location newPoint = new Location("point");
        newPoint.setLatitude(l1.latitude);
        newPoint.setLongitude(l1.longitude);
        Location center = new Location("center");
        center.setLatitude(l2.latitude);
        center.setLongitude(l2.longitude);
        return newPoint.distanceTo(center);
    }

    private int getChangedLocationsCount() {
        Log.i(TAG, "getChangedLocationsCount, mCenters.size is " + mCenters.size());
        if (mCenters.size() == 0) return 0;
        int count = 0;
        for (int i = 0; i < mCenters.size(); i++) {
            Log.i(TAG, "in getChangedLocationsCount  " + i + "  " + mCenters.get(i).hasChanged());
            if (mCenters.get(i).hasChanged()) count += 1;
        }
        return count;
    }

    private void updatePlaceIdList() {
        Log.i(TAG, "updatePlaceList sizes are: " + mCenters.size());
        for (int i = 0; i < mCenters.size(); i++) {
            Log.i(TAG, "does it contain the id? " + mPoi.contains(mCenters.get(i).getPid()));
            if (mPoi.isEmpty() || !(mPoi.contains(mCenters.get(i).getPid()))) {
                mPoi.add(mCenters.get(i).getPid());
                mNewPoiRads.add(mCenters.get(i).getRad());
                mNew.add(false);
            }
        }
    }
    private float[] convertFloatList() {
        float[] floatArray = new float[mNewPoiRads.size()];

        for (int i = 0; i < mNewPoiRads.size(); i++) {
            floatArray[i] = (float) mNewPoiRads.get(i);
        }
        return floatArray;
    }

    private boolean[] convertBoolList() {
        boolean[] boolArray = new boolean[mNewPoiRads.size()];
        for (int i = 0; i < mNewPoiRads.size(); i++) {
            boolArray[i] = (boolean) mNew.get(i);
        }
        return boolArray;
    }

    private class Centers {
        private LatLng mLatLng = null;
        private float mRad = 0;
        private Circle mCircle = null;
        private Marker mMarker = null;
        private String mPid = null;
        private boolean hasChanged = false;


        private Centers () {
        }

        private Centers (String pId, LatLng latLng) {
            mPid = pId;
            mLatLng = latLng;

        }

        private Centers (String pId, LatLng latLng, float rad) {
            mPid = pId;
            mLatLng = latLng;
            mRad = rad;
        }

        private Centers (String pId, LatLng latLng, float rad, Circle circle) {
            mPid = pId;
            mLatLng = latLng;
            mRad = rad;
            mCircle = circle;
        }

        private void setLatLng(LatLng v) {
            mLatLng = v;
        }

        private LatLng getLatLng() {
            return mLatLng;
        }

        private void setRad(float rad) {
            mRad = rad;
            hasChanged = true;
        }

        private float getRad() {
            return mRad;
        }

        private void setCircle(Circle circle) {
            mCircle = circle;
        }

        private Circle getCircle() {
            return mCircle;
        }

        private void setMarker(Marker marker) {
            mMarker = marker;
        }

        private boolean hasMarker() {
            return mMarker != null;
        }

        private Marker getMarker() {
            return mMarker;
        }

        private boolean hasChanged() {
            return hasChanged;
        }

        private String getPid() {
            return mPid;
        }
    }
}
