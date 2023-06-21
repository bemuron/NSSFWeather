package com.mads03.nssfweather.data.network.api;


import com.mads03.nssfweather.models.Result;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface APIService {

    //getting the current weather
//    @GET("weather?units=metric")
//    Call<Result> getCurrentWeather(
//            @Query("q") String location,
//            @Query("APPID") String appId);

    @GET("weather?units=metric")
    Call<ResponseBody> getCurrentWeather(
            @Query("lat") String latitude,
            @Query("lon") String longitude,
            @Query("APPID") String appId);

    //get the weather forecast for the location
    @GET("forecast?units=metric")
    Call<ResponseBody> getWeatherForecast2(
            @Query("lat") String latitude,
            @Query("lon") String longitude,
            @Query("APPID") String appId,
            @Query("cnt") String cnt);
}
