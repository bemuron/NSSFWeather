package com.mads03.nssfweather.presentation.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.mads03.nssfweather.R;
import com.mads03.nssfweather.databinding.ActivityMainBinding;
import com.mads03.nssfweather.helpers.DeviceLocationListener;
import com.mads03.nssfweather.models.Weather;
import com.mads03.nssfweather.presentation.adapters.ForecastAdapter;
import com.mads03.nssfweather.presentation.viewmodels.MainActivityViewModel;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements ForecastAdapter.ForecastAdapterListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private DrawerLayout mDrawerLayout;
    private Toolbar toolbar;

    private MainActivityViewModel mViewModel;
    private Boolean mLocationPermissionGranted = false;
    private List<Weather> weatherArrayList;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 15;
    private PlacesClient mPlacesClient;
    private FusedLocationProviderClient mFusedLocationClient;
    private RecyclerView mRecyclerView;
    private LinearLayout linearLayout;
    private DeviceLocationListener mDeviceLocationListener;
    private LocationManager mLocationManager;

    private ForecastAdapter forecastAdapter;
    private String provider;
    private Criteria criteria;
    private ConstraintLayout mainLayout;
    private TextView todayTemp, todayTempDesc, todayMin, todayMax, todayCurrent, lastUpdateTv;
    double lat, lon;
    public static MainActivity mainActivity;

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mainActivity = this;

        setupToolbar();
        setupNavigationDrawer();

        getAllWidgets();
        setAdapter();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mDeviceLocationListener = new DeviceLocationListener();
        mLocationManager = (LocationManager) getSystemService(MainActivity.LOCATION_SERVICE);

        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);   //default
        criteria.setCostAllowed(false);
        // get the best provider depending on the criteria
        provider = mLocationManager.getBestProvider(criteria, false);

        /*Initialize Places.*/
        if (!Places.isInitialized()){
            Places.initialize(this, getString(R.string.google_places_id));
        }
        mPlacesClient = Places.createClient(this);

        mViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);
        mViewModel.getAllWeather().observe(this,
                new Observer<List<Weather>>() {
                    @Override
                    public void onChanged(@Nullable final List<Weather> weather) {
                        if (weather != null){
                            weatherArrayList = weather;
                            forecastAdapter.swapWeatherForecast(weather);
                        }
                    }
                });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDeviceLocation();
                Snackbar.make(view, "Fetching Weather Update. Please Wait", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //ask forlocation access permission
        try {
            if (mLocationPermissionGranted) {
                getDeviceLocation();
                //mViewModel.manualRefresh();
                Log.i(TAG, "Should be fetching weather now.");
            }else{
                getLocationPermission();
            }
        }catch (SecurityException e)  {
            // The user has not granted permission.
            Log.i(TAG, "The user did not grant location permission.");
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void setupNavigationDrawer() {
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackground(R.color.purple_200);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }
    }

    public static MainActivity getInstance(){
        return mainActivity;
    }

    private void getAllWidgets(){
        mRecyclerView = findViewById(R.id.forecast_list);
        todayTemp = findViewById(R.id.today_temp_tv);
        //todayTempDesc = findViewById(R.id.today_temp_text_tv);
        todayTempDesc = findViewById(R.id.today_temp_text_tv);
        todayCurrent = findViewById(R.id.today_current_value);
        todayMax = findViewById(R.id.today_max_value);
        todayMin = findViewById(R.id.today_min_value);
        mainLayout = findViewById(R.id.main_layout);
        lastUpdateTv = findViewById(R.id.last_update_tv);
        linearLayout = findViewById(R.id.current_weather_container);
    }

    //dapter
    private void setAdapter(){
        forecastAdapter = new ForecastAdapter(this,weatherArrayList,this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(forecastAdapter);
    }

    @SuppressLint("MissingPermission")
    private void getDeviceLocation(){
        final Location location = mLocationManager.getLastKnownLocation(provider);
        if (location != null) {

            mDeviceLocationListener.onLocationChanged(location);
            lat = location.getLatitude();
            lon = location.getLongitude();
            Log.i(TAG, "Device lon = "+lon);
            Log.i(TAG, "Device lat = "+lat);

            mViewModel.manualRefresh(String.valueOf(lat), String.valueOf(lon));
        } else {
            Toast.makeText(this, "Please turn on your location", Toast.LENGTH_LONG).show();

            // go to the settings so user can turn on their location
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);

            mLocationManager.requestLocationUpdates(provider, 200, 1, mDeviceLocationListener);

            if(location != null) {
                lat = location.getLatitude();
                lon = location.getLongitude();
                Log.i(TAG, "Device lon = "+lon);
                Log.i(TAG, "Device lat = "+lat);

                mViewModel.manualRefresh(String.valueOf(lat), String.valueOf(lon));

                Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                List<Address> addresses = null;
                try {
                    addresses = geocoder.getFromLocation(lat, lon, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String cityName = addresses.get(0).getLocality();
                String stateName = addresses.get(0).getAdminArea();
                String countryName = addresses.get(0).getCountryName();
                String postalcode = addresses.get(0).getPostalCode();
            }
            else {
                Toast.makeText(MainActivity.this, "Please provide your location", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            Log.d(TAG, "Permission already granted**");
            mViewModel.manualRefresh(String.valueOf(lat), String.valueOf(lon));
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

    //weather inserted callback
    //receive status of weather update
    public void onWeatherUpdated(Boolean isWeatherUpdated, List<Weather> freshWeather, String message){
        if (isWeatherUpdated){
            if (freshWeather != null){
                Log.i(TAG, "Weather updated. Attempting display");
                Log.i(TAG, "Weather size from db = "+freshWeather.size());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        weatherArrayList = freshWeather;
                        forecastAdapter.swapWeatherForecast(weatherArrayList);
                        displayCurrentWeather(freshWeather);
                    }
                });
            }
        }
    }

    //this will pick out today's current weather from the list and display it
    private void displayCurrentWeather(List<Weather> freshWeather){
        for (int i = 0; i < freshWeather.size(); i++) {
            if(freshWeather.get(i).getIsCurrent().equals("1")){
                todayTemp.setText(freshWeather.get(i).getTemperature()+"*c");
                todayTempDesc.setText(freshWeather.get(i).getShortDesc());
                todayMin.setText(freshWeather.get(i).getMin());
                todayCurrent.setText(freshWeather.get(i).getTemperature());
                todayMax.setText(freshWeather.get(i).getMax());
                lastUpdateTv.setText("Last Update: s"+freshWeather.get(i).getLastUpdate());

                switch (freshWeather.get(i).getShortDesc()){
                    case "Rain":
                        linearLayout.setBackgroundResource(R.drawable.sea_rainy);
                        mRecyclerView.setBackgroundColor(getResources().getColor(R.color.rainy));
                        mainLayout.setBackgroundColor(getResources().getColor(R.color.rainy));

                        break;
                    case "Clouds":
                        linearLayout.setBackgroundResource(R.drawable.sea_cloudy);
                        mRecyclerView.setBackgroundColor(getResources().getColor(R.color.cloudy));
                        mainLayout.setBackgroundColor(getResources().getColor(R.color.cloudy));

                        break;
                    case "Clear":
                        linearLayout.setBackgroundResource(R.drawable.sea_sunnypng);
                        mRecyclerView.setBackgroundColor(getResources().getColor(R.color.sunny));
                        mainLayout.setBackgroundColor(getResources().getColor(R.color.sunny));

                        break;
                    default:
                        linearLayout.setBackgroundResource(R.drawable.sea_sunnypng);
                        mRecyclerView.setBackgroundColor(getResources().getColor(R.color.sunny));
                        mainLayout.setBackgroundColor(getResources().getColor(R.color.sunny));

                }

            }
            String updated = freshWeather.get(i).getIsCurrent();
            Log.i(TAG, "Weather is current = "+updated);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {

                    @Override
                    public boolean onNavigationItemSelected(MenuItem item) {
                        // Handle navigation view item clicks here.
                        int id = item.getItemId();

                        if (id == R.id.nav_search_locations) {
                            Intent intent = new Intent(MainActivity.this, SearchPlacesActivity.class);
                            startActivity(intent);
                        } else if (id == R.id.nav_fav_locations) {

                        }

                        //DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                        //drawer.closeDrawer(GravityCompat.START);
                        // Close the navigation drawer when an item is selected.
                        item.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });

    }
}