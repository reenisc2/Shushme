package com.example.android.shushme;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate-------------");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*
         * Normally, calling setDisplayHomeAsUpEnabled(true) (we do so in onCreate here) as well as
         * declaring the parent activity in the AndroidManifest is all that is required to get the
         * up button working properly. However, in this case, we want to navigate to the previous
         * screen the user came from when the up button was clicked, rather than a single
         * designated Activity in the Manifest.
         *
         * We use the up button's ID (android.R.id.home) to listen for when the up button is
         * clicked and then call onBackPressed to navigate to the previous Activity when this
         * happens.
         */
        int id = item.getItemId();
        Log.i(TAG, "onOptionsItemSelected!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}