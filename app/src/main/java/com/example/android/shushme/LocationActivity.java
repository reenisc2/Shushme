package com.example.android.shushme;

import android.Manifest;
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
import com.google.android.libraries.places.api.net.PlacesClient;

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
        GoogleMap.OnPoiClickListener {

    private GoogleMap mMap;
    private boolean mapReady = false;
    private LatLng mNewPos = Constants.HOME;
    private static final String TAG = LocationActivity.class.getSimpleName();
    private FusedLocationProviderClient mLocationProvider;
    private PointOfInterest mPoi = null;

    public LocationActivity() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setContentView(R.layout.activity_location);

        mLocationProvider = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    public void onMapReady(GoogleMap map) {
        mMap = map;
        mapReady = true;
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
        } else {
            return;
        }
    }

    @Override
    public void onPoiClick(PointOfInterest poi) {
        float radius = ShushmePreferences.getRadius(this);
        Log.i(TAG, "fetched radius: " + radius);
        CircleOptions circleOptions = new CircleOptions().center(poi.latLng).radius(radius).strokeColor(0xffff0000).strokeWidth(4);
        mMap.addMarker(new MarkerOptions().position(poi.latLng).draggable(true));
        mMap.addCircle(circleOptions);
        mNewPos = poi.latLng;
        mPoi = poi;
        Log.i(TAG, "onPoiClick: " + poi.placeId);
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
    }

    @Override
    public void onMarkerDrag(Marker marker) {
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        mNewPos = marker.getPosition();
        Log.d(TAG, "onMarkerDragEnd "+ mNewPos.latitude + ", " + mNewPos.longitude);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        Log.d(TAG, "onMapClick");
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("New marker");
        mMap.clear();
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.addMarker(markerOptions);
        mNewPos = latLng;
        int instance = mLocationProvider.getInstanceId();
        Log.d(TAG, "instance id: " + instance);
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
    }

    @Override
    public boolean onSupportNavigateUp() {
        Log.d(TAG, "onSupportNavigateUp");
        Intent resultIntent = new Intent();
        if (mPoi != null) {
            Bundle bundle = new Bundle();
            bundle.putString("Name", mPoi.name);
            bundle.putDouble("Lat", mPoi.latLng.latitude);
            bundle.putDouble("Lng", mPoi.latLng.longitude);
            bundle.putString("placeId", mPoi.placeId);
            resultIntent.putExtras(bundle);
            setResult(RESULT_OK, resultIntent);
        } else {
            setResult(RESULT_OK);
        }
        finish();
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
}
