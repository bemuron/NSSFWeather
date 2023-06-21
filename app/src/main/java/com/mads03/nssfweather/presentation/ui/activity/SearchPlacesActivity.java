package com.mads03.nssfweather.presentation.ui.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.mads03.nssfweather.R;
import com.mads03.nssfweather.models.Weather;
import com.mads03.nssfweather.presentation.viewmodels.MainActivityViewModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchPlacesActivity extends AppCompatActivity {
    private static final String TAG = SearchPlacesActivity.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 15;
    private final static int AUTO_COMPLETE_REQUEST_CODE = 1;
    private Boolean mLocationPermissionGranted = false;
    private PlacesClient mPlacesClient;
    private MainActivityViewModel mViewModel;

    public static SearchPlacesActivity searchPlacesActivity;

    private TextView placeName, placeTemp, placeLat, placeLon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_places);
        setupActionBar();
        searchPlacesActivity = this;

        getAllWidgets();

        mViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);


        /*Initialize Places.*/
        if (!Places.isInitialized()){
            Places.initialize(this, getString(R.string.google_places_id));
        }
        mPlacesClient = Places.createClient(this);

        try {
            if (mLocationPermissionGranted) {
                updateUserLocation();
            }else{
                getLocationPermission();
            }
        }catch (SecurityException e)  {
            // The user has not granted permission.
            Log.i(TAG, "The user did not grant location permission.");
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void getAllWidgets(){
        placeTemp = findViewById(R.id.currWeatherValue);
        placeLat = findViewById(R.id.latValueTv);
        placeLon = findViewById(R.id.lonTvValue);
        placeName = findViewById(R.id.place_name);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search_places_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_search){
            try {
                if (mLocationPermissionGranted) {
                    getSearchLocation();
                } else {
                    getLocationPermission();
                }
            } catch (SecurityException e) {
                // The user has not granted permission.
                Log.i(TAG, "The user did not grant location permission.");
                Log.e("Exception: %s", e.getMessage());
            }
        }else if (item.getItemId() == android.R.id.home) {
            onBackPressed();    //Call the back button's method
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getSearchLocation(){
        //set the fields to specify which types of place data to
        //return after the user has made a selection
        List<Place.Field> fields = Arrays.asList(Place.Field.NAME,
                Place.Field.LAT_LNG, Place.Field.ADDRESS);

        //start the autocomplete intent
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY,
                fields).build(this);
        startActivityForResult(intent, AUTO_COMPLETE_REQUEST_CODE);
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "Permission callback called ----");
        //if location permission is granted
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            Log.d(TAG, "Location Permission granted ----");

            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
            }
        }
    }

    //save user's location, lat, long
    private void updateUserLocation(){
        showCurrentPlace();
        Log.e(TAG, "Attempt to show user selected location");
    }

    //get the current place
    private void showCurrentPlace() {
        //mDisplayedUserLocation = false;
        if (mLocationPermissionGranted) {
            // Use fields to define the data types to return.
            List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME,
                    Place.Field.LAT_LNG, Place.Field.ID);

            // Use the builder to create a FindCurrentPlaceRequest.
            FindCurrentPlaceRequest request =
                    FindCurrentPlaceRequest.newInstance(placeFields);

            // Get the likely places - that is, the businesses and other points of interest that
            // are the best match for the device's current location.
            @SuppressWarnings("MissingPermission") final Task<FindCurrentPlaceResponse> placeResult =
                    mPlacesClient.findCurrentPlace(request);
            placeResult.addOnCompleteListener (task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    FindCurrentPlaceResponse likelyPlaces = task.getResult();

                    for (PlaceLikelihood placeLikelihood : likelyPlaces.getPlaceLikelihoods()) {

                        // update the edit text with the likely place the user is in
                        //jobLocationEditText.setText(placeLikelihood.getPlace().getName());
                        Log.e(TAG, "Possible user location = "+placeLikelihood.getPlace().getName());
                        Log.e(TAG, "Possible user latitude = "+placeLikelihood.getPlace().getLatLng());
                    }
                    //mDisplayedUserLocation = true;
                }
                else {
                    Log.e(TAG, "Exception: %s", task.getException());
                }
            });
        } else {
            // The user has not granted permission.
            Log.i(TAG, "The user did not grant location permission.");
            // Prompt the user for permission.
            getLocationPermission();
        }
    }

    //get the westher of the selected place
    private void getPlaceWeather(String address){

        Geocoder coder = new Geocoder(this);
        try {
            ArrayList<Address> adresses = (ArrayList<Address>) coder.getFromLocationName(address, 10);
            for(Address add : adresses){
                double longitude = add.getLongitude();
                double latitude = add.getLatitude();

                placeLat.setText(String.valueOf(latitude));
                placeLon.setText(String.valueOf(longitude));

                mViewModel.getPlaceWeather(String.valueOf(latitude), String.valueOf(longitude));

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch(IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //check for our request code
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == AUTO_COMPLETE_REQUEST_CODE) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                Log.i(TAG, "Place: " + place.getName());
                Log.i(TAG, "Place LatLng: " + place.getLatLng());
                getPlaceWeather(String.valueOf(place.getAddress()));
                placeName.setText(String.valueOf(place.getName()));
            }
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            Status status = Autocomplete.getStatusFromIntent(data);
            //handle error
            Log.i(TAG, status.getStatusMessage());
        } else if (resultCode == RESULT_CANCELED) {
            //the user cancelled the operation
        }
    }

    public void onPlaceWeatherGot(Boolean isWeatherUpdated, String temperature, String message){
        if (isWeatherUpdated){
            placeTemp.setText(temperature+" 'C");
            Log.i(TAG, "Place Weather"+ temperature);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                }
            });
        }
    }
}