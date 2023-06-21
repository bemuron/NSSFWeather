package com.mads03.nssfweather.data.database;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.mads03.nssfweather.models.Weather;

@Database(entities = {Weather.class}, version = 1, exportSchema = false)
public abstract class WeatherDatabase extends RoomDatabase {
    private static final String TAG = WeatherDatabase.class.getSimpleName();

    public abstract WeatherDao weatherDao();
    private static WeatherDatabase INSTANCE;

    public static WeatherDatabase getDatabase (final Context context){
        if (INSTANCE == null){
            synchronized (WeatherDatabase.class){
                if (INSTANCE == null){
                    //create db here
                    Log.i(TAG, "Creating weather db");
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    WeatherDatabase.class, "weather.db")
                            .build();
                    Log.i(TAG, "Finished Creating weather db");
                }
            }
        }
        return INSTANCE;
    }

}
