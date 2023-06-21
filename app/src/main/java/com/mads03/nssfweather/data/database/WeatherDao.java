package com.mads03.nssfweather.data.database;

import android.database.Cursor;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.mads03.nssfweather.models.Weather;

import java.util.List;

@Dao
public interface WeatherDao {

    /*
    insert exercises into db
     */
    @Insert
    void insertWeather(Weather weather);
    //void insertWeather(List<Weather> weatherList);

    @Query("Delete from weather")
    void deleteAll();

    @Query("select * from weather where weather_id = :weatherId")
    LiveData<List<Weather>> getSingleWeather(int weatherId);

    @Query("SELECT * FROM weather")
    List<Weather> getAllWeather();

    @Query("SELECT * FROM weather")
    LiveData<List<Weather>> getLiveDataWeather();

    @Query("SELECT COUNT(weather_id) FROM weather")
    int countWeatherInDb();
}
