package com.example.android.shushme;

/*
* Copyright (C) 2017 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*  	http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.example.android.shushme.provider.PlaceContract;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        ConnectionCallbacks,
        OnConnectionFailedListener,
        ListItemFragment.ListItemListener {

    // Constants
    public static final String TAG = MainActivity.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST_FINE_LOCATION = 111;
    private static final int PLACE_PICKER_REQUEST = 1;
    private static final int SETTINGS_REQUEST = 2;

    // Member variables
    private PlaceListAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private boolean mGeofencesEnabled;  // true if geofences enabled, false if geofences enabled switch is off
    private GoogleApiClient mClient;
    private Geofencing mGeofencing;
    private PlacesClient mPlacesClient;
    private int mCurrentItemPosition;

    /**
     * Called when the activity is starting
     *
     * @param savedInstanceState The Bundle that contains the data supplied in onSaveInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNotificationChannel();

        // Set up the recycler view
        mRecyclerView = findViewById(R.id.places_list_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new PlaceListAdapter(this, null);

        mAdapter.setOnItemClickListener(new PlaceListAdapter.onItemClickListener() {
            @Override
            public void ItemClicked(View v, int position) {
                mCurrentItemPosition = position;
                DialogFragment newFragment = ListItemFragment.newInstance(
                        mAdapter.getItem(position).getRadius(),
                        mAdapter.getItem(position).getUpdateLocation(),
                        mAdapter.getItem(position).getName());
                newFragment.show(getSupportFragmentManager(), "locations");
            }
        });
        mRecyclerView.setAdapter(mAdapter);
        registerForContextMenu(mRecyclerView);


        // Initialize the switch state and Handle enable/disable switch change
        Switch onOffSwitch = findViewById(R.id.enable_switch);
        mGeofencesEnabled = getPreferences(MODE_PRIVATE).getBoolean(getString(R.string.setting_enabled), false);
        onOffSwitch.setChecked(mGeofencesEnabled);
        onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
                editor.putBoolean(getString(R.string.setting_enabled), isChecked);
                mGeofencesEnabled = isChecked;
                editor.commit();
                if (isChecked) {
                    mGeofencing.registerAllGeofences();
                    Log.i(TAG, "Is checked");
                }
                else  {
                    mGeofencing.unRegisterAllGeofences();
                    restoreRinger();
                    Log.i(TAG, "Is unchecked");
                }
            }

        });

        // Build up the LocationServices API client
        // Uses the addApi method to request the LocationServices API
        // Also uses enableAutoManage to automatically when to connect/suspend the client
        mClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .enableAutoManage(this, this)
                .build();

        mGeofencing = new Geofencing(this, mClient);
        Places.initialize(getApplicationContext(), getString(R.string.api_key));
        mPlacesClient = Places.createClient(this);
    }

    /***
     * Called when the Google API Client is successfully connected
     *
     * @param connectionHint Bundle of data provided to clients by Google Play services
     */
    @Override
    public void onConnected(@Nullable Bundle connectionHint) {
        refreshPlacesData();
        Log.i(TAG, "API Client Connection Successful!");
    }

    /***
     * Called when the Google API Client is suspended
     *
     * @param cause cause The reason for the disconnection. Defined by constants CAUSE_*.
     */
    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "API Client Connection Suspended!");
    }

    /***
     * Called when the Google API Client failed to connect to Google Play Services
     *
     * @param result A ConnectionResult that can be used for resolving the error
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.e(TAG, "API Client Connection Failed!");
    }

    public void refreshPlacesData() {
        Log.i(TAG, "New refreshPlacesData()");
        Uri uri = PlaceContract.PlaceEntry.CONTENT_URI;
        Cursor data = getContentResolver().query(
                uri,
                null,
                null,
                null,
                null);

        if (data == null || data.getCount() == 0) return;
        List<LocationObj> places = new ArrayList<>();
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
        while (data.moveToNext()) {
            String guid = data.getString(data.getColumnIndex(PlaceContract.PlaceEntry.COLUMN_PLACE_ID));
            int mId = data.getInt(data.getColumnIndex(PlaceContract.PlaceEntry._ID));
            float rad = data.getFloat(data.getColumnIndex(PlaceContract.PlaceEntry.COLUMN_RADIUS));
            int update = data.getInt(data.getColumnIndex(PlaceContract.PlaceEntry.COLUMN_UPDATE));
            FetchPlaceRequest request = FetchPlaceRequest.builder(guid, placeFields).build();
            Log.i(TAG, request.getPlaceId());
            mPlacesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                Place place = response.getPlace();
                places.add(new LocationObj(place, rad, mId, update));
                mAdapter.swapPlaces(places);
                mRecyclerView.setAdapter(mAdapter);
                mGeofencing.updateGeofencesList(places);
                if (mGeofencesEnabled) {
                    mGeofencing.registerAllGeofences();
                }
            }).addOnFailureListener((exception) -> {
                Log.i(TAG, "addOnFailureListener");
                if (exception instanceof ApiException) {
                    Log.e(TAG, "Place not found:" + exception.getMessage());
                }
            });
        }
        data.close();
    }

    /***
     * Button Click event handler to handle clicking the "Add new location" Button
     *
     * @param view  The view that was clicked
     */
    public void onAddPlaceButtonClicked(View view) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, getString(R.string.need_location_permission_message), Toast.LENGTH_LONG).show();
            return;
        }

        Intent mapDetailIntent = new Intent(MainActivity.this, LocationActivity.class);
        List<LocationObj> currentPlaces = mAdapter.getPlaces();
        if (currentPlaces == null) {
            mapDetailIntent.putExtra(Constants.PLACE_COUNT, 0);
        } else {
            int count = currentPlaces.size();
            mapDetailIntent.putExtra(Constants.PLACE_COUNT, count);
            for (int i = 0; i < count; i++) {
                LocationObj l = currentPlaces.get(i);
                mapDetailIntent.putExtra(Constants.PID + i, l.getPlace().getId());
                mapDetailIntent.putExtra(Constants.LAT_LNG + i, l.getLatLng());
                mapDetailIntent.putExtra(Constants.RAD + i, l.getRadius());
            }
        }

        startActivityForResult(mapDetailIntent, PLACE_PICKER_REQUEST);
    }

    /***
     * Button Click event handler to handle clicking the "Delete location" Button
     * In the first itereation, this will just pop the last location added off the list.
     *
     * @param view The view that was clicked
     */

    public void onDeletePlaceButtonClicked(View view) {
        Uri uri = PlaceContract.PlaceEntry.CONTENT_URI;
        Cursor data = getContentResolver().query(
                uri,
                null,
                null,
                null,
                null);

        if (data == null || data.getCount() == 0) return;
        boolean deletingLastItem = (data.getCount() == 1);
        data.moveToLast();
        int idx = data.getInt(data.getColumnIndex(PlaceContract.PlaceEntry._ID));
        deleteLocation(idx, deletingLastItem);
        data.close();
    }

    /***
     * Called when the Place Picker Activity returns back with a selected place (or after canceling)
     *
     * @param requestCode The request code passed when calling startActivityForResult
     * @param resultCode  The result code specified by the second activity
     * @param data        The Intent that carries the result data.
     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult");
        if ((requestCode == PLACE_PICKER_REQUEST || requestCode == SETTINGS_REQUEST)
                && resultCode == RESULT_OK) {

            if (requestCode == PLACE_PICKER_REQUEST) {
                if (data == null) {
                    Log.i(TAG, "No place selected");
                    return;
                }

                //String placeID = data.getStringExtra("placeId");
                java.util.ArrayList<String> placeIDs = data.getStringArrayListExtra("PlaceIds");
                float[] rads = data.getFloatArrayExtra("Rads");
                boolean[] newPoi = data.getBooleanArrayExtra("News");
                List oldList = mAdapter.getPlaces();
                Log.i(TAG, "Data Not Null!");

                // Insert a new place into DB
                for (int i = 0; i < placeIDs.size(); i++) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(PlaceContract.PlaceEntry.COLUMN_PLACE_ID, placeIDs.get(i));
                    contentValues.put(PlaceContract.PlaceEntry.COLUMN_RADIUS, rads[i]);
                    contentValues.put(PlaceContract.PlaceEntry.COLUMN_UPDATE,
                            ShushmePreferences.getNotifications(getApplicationContext()));
                    if (newPoi[i]) {
                        getContentResolver().insert(PlaceContract.PlaceEntry.CONTENT_URI, contentValues);
                    } else {
                        String sIdx = "";
                        for (int j = 0; j < oldList.size(); j++) {
                            LocationObj lo = (LocationObj)oldList.get(j);
                            if (lo.getId().equals(placeIDs.get(i))) {
                                sIdx = String.valueOf(lo.getTableIdx());
                                break;
                            }
                        }
                        Uri uri = PlaceContract.BASE_CONTENT_URI.buildUpon()
                                .appendPath(PlaceContract.PATH_PLACES)
                                .appendPath(sIdx).build();
                        getContentResolver().update(uri, contentValues, null, null);
                    }
                }
            } //else {
                restoreRinger();
            //}
            
            // Get live data information
            refreshPlacesData();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");

        // Initialize location permissions checkbox
        CheckBox locationPermissions = findViewById(R.id.location_permission_checkbox);
        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissions.setChecked(false);
        } else {
            locationPermissions.setChecked(true);
            locationPermissions.setEnabled(false);
        }

        // Initialize ringer permissions checkbox
        CheckBox ringerPermissions = findViewById(R.id.ringer_permissions_checkbox);
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Check if the API supports such permission change and check if permission is granted
        if (android.os.Build.VERSION.SDK_INT >= 23 && !nm.isNotificationPolicyAccessGranted()) {
            ringerPermissions.setChecked(false);
        } else {
            ringerPermissions.setChecked(true);
            ringerPermissions.setEnabled(false);
        }
    }

    @TargetApi(23)
    public void onRingerPermissionsClicked(View view) {
        Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
        startActivity(intent);
    }

    public void onLocationPermissionClicked(View view) {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSIONS_REQUEST_FINE_LOCATION);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.shushme, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivityForResult(new Intent(this, SettingsActivity.class), SETTINGS_REQUEST);
            return true;
        }

        if (id == R.id.action_permissions) {
            Log.i(TAG, "selected permissions!--");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showListItemDialog() {
        DialogFragment dialog = new ListItemFragment();
        dialog.show(getSupportFragmentManager(), "ListItemFragment");
    }

    @Override
    public void onDialogPositiveClick(float rad, Integer upd, Boolean checked) {
        Log.i(TAG, "onDialogPositiveClick()");
        Log.i(TAG, mCurrentItemPosition + "   " + rad + "   " + upd + "   " + checked);
        LocationObj lo = mAdapter.getItem(mCurrentItemPosition);
        Log.i(TAG, lo.getName());
        if (checked) {
            deleteLocation(lo.getTableIdx(), mAdapter.getItemCount() == 1);
        } else {
            if (rad > 0 || upd > 0) {
                restoreRinger();
                ContentValues contentValues = new ContentValues();
                contentValues.put(PlaceContract.PlaceEntry.COLUMN_PLACE_ID, lo.getId());
                float newRad = rad > 0 ? rad : lo.getRadius();
                int newUpd = upd > 0 ? upd : lo.getUpdateLocation();
                Log.i(TAG, "udate rate value to be assigned: " + newUpd);
                contentValues.put(PlaceContract.PlaceEntry.COLUMN_RADIUS, newRad);
                contentValues.put(PlaceContract.PlaceEntry.COLUMN_UPDATE, newUpd);
                String sIdx = String.valueOf(lo.getTableIdx());
                Uri uri = PlaceContract.BASE_CONTENT_URI.buildUpon()
                        .appendPath(PlaceContract.PATH_PLACES)
                        .appendPath(sIdx).build();
                getContentResolver().update(uri, contentValues, null, null);
                refreshPlacesData();
            }
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        Log.i(TAG, "onDialogNegativeClick()");
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only in API26 and higher
        // Notification channels are new
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(Constants.CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Restore the ringer to original state before disabling geofences or changing the radius
     */
    private void restoreRinger() {
        Context context = getApplicationContext();
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        GeofenceBroadcastReceiver.sendNotification(context, Geofence.GEOFENCE_TRANSITION_EXIT);
    }

    /**
     * Delete a location entry based on its _ID in the table
     */

    private void deleteLocation(int idx, boolean deletingLastItem) {
        String sIdx = String.valueOf(idx);
        Uri uri = PlaceContract.BASE_CONTENT_URI.buildUpon()
                .appendPath(PlaceContract.PATH_PLACES)
                .appendPath(sIdx).build();
        getContentResolver().delete(uri,
                null,
                null);
        if (deletingLastItem) {
            mAdapter.swapPlaces(new ArrayList<>());
            mGeofencing.unRegisterAllGeofences();
            restoreRinger();
        } else {
            refreshPlacesData();
        }

    }
}
