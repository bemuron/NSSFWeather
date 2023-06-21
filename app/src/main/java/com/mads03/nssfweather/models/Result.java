package com.mads03.nssfweather.models;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Result {
    @SerializedName("error")
    private Boolean error;

    @SerializedName("message")
    private String message;
    @SerializedName("access_token")
    private String access_token;

    @SerializedName("custom_token")
    private String custom_token;

    @Nullable
    @SerializedName("weather")
    private List<Weather> weatherUpdate;

    @Nullable
    @SerializedName("list")
    private List<Weather> list;

    public Result(Boolean error, String message) {
        this.error = error;
        this.message = message;
    }

    public Boolean getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getAccess_token() {
        return access_token;
    }

    public String getCustom_token() {
        return custom_token;
    }

    @Nullable
    public List<Weather> getWeatherUpdate() {
        return weatherUpdate;
    }

    public void setWeatherUpdate(@Nullable List<Weather> weatherUpdate) {
        this.weatherUpdate = weatherUpdate;
    }

    @Nullable
    public List<Weather> getList() {
        return list;
    }

    public void setList(@Nullable List<Weather> list) {
        this.list = list;
    }
}
