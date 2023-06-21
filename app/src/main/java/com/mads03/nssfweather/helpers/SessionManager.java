package com.mads03.nssfweather.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import java.util.HashMap;

public class SessionManager {
	// LogCat tag
	private static String TAG = SessionManager.class.getSimpleName();

	// Shared Preferences
	SharedPreferences pref;

	Editor editor;
	Context _context;

	// Shared pref mode
	int PRIVATE_MODE = 0;

	// Shared preferences file name
	private static final String PREF_NAME = "NSSFWeatherAPPPref";

	private static final String KEY_USER_LOCATION = "userLocation";
    private static final String KEY_USER_LON = "userLong";
    private static final String KEY_USER_LAT = "userLat";

    private static final String LAST_REFRESH_DATE = "lastRefreshDate";

	public SessionManager(Context context) {
		this._context = context;
        this.pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        this.editor = pref.edit();
	}

    //save the user prefs
    public void addUserLocationPrefs(String lon, String lat,String location){

        // user location longitude
        editor.putString(KEY_USER_LON, lon);

        // user location latitude
        editor.putString(KEY_USER_LAT, lat);

        // Storing location in pref
        editor.putString(KEY_USER_LOCATION, location);

        // commit changes
        editor.commit();
    }

    //update last date time of weather update
    public void updateWeatherRefreshTime(String dateTime){

        // Storing name in pref
        editor.putString(LAST_REFRESH_DATE, dateTime);

        // commit changes
        editor.commit();
    }

    //get the last time the weather was updated
    public String getDateLastUpdated(){
        String dateTime = pref.getString(LAST_REFRESH_DATE, null);
        return  dateTime;
    }

    public String getUserLocation(){
        String location = pref.getString(KEY_USER_LOCATION, null);
        return  location;
    }

    /**
     * Clear prefs details
     * */
    public void clearPrefs(){
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();
    }

}
