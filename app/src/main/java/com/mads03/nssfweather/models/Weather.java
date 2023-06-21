package com.mads03.nssfweather.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "weather")
public final class Weather {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "weather_id")
    private int weatherId;

    @Nullable
    @ColumnInfo(name = "short_desc")
    private String shortDesc;

    @Nullable
    @ColumnInfo(name = "description")
    private String description;

    @Nullable
    @ColumnInfo(name = "temperature")
    private String temperature;

    @Nullable
    @ColumnInfo(name = "min")
    private String min;

    @Nullable
    @ColumnInfo(name = "max")
    private String max;

    @Nullable
    @ColumnInfo(name = "pressure")
    private String pressure;

    @Nullable
    @ColumnInfo(name = "humidity")
    private String humidity;

    @Nullable
    @ColumnInfo(name = "lon")
    private String lon;

    @Nullable
    @ColumnInfo(name = "lat")
    private String lat;

    @Nullable
    @ColumnInfo(name = "date")
    private String date;

    @Nullable
    @ColumnInfo(name = "is_current")
    private String isCurrent;

    @Nullable
    @ColumnInfo(name = "last_update")
    private String lastUpdate;

    /**
     * Open weather api variables
     * */

    @Ignore
    private String dt;

    @Ignore
    private String main;

    public int getWeatherId() {
        return weatherId;
    }

    public void setWeatherId(int weatherId) {
        this.weatherId = weatherId;
    }

    public String getShortDesc() {
        return shortDesc;
    }

    public void setShortDesc(String shortDesc) {
        this.shortDesc = shortDesc;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getMin() {
        return min;
    }

    public void setMin(String min) {
        this.min = min;
    }

    public String getMax() {
        return max;
    }

    public void setMax(String max) {
        this.max = max;
    }

    public String getPressure() {
        return pressure;
    }

    public void setPressure(String pressure) {
        this.pressure = pressure;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getIsCurrent() {
        return isCurrent;
    }

    public void setIsCurrent(String isCurrent) {
        this.isCurrent = isCurrent;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getDt() {
        return dt;
    }

    public void setDt(String dt) {
        this.dt = dt;
    }

    public String getMain() {
        return main;
    }

    public void setMain(String main) {
        this.main = main;
    }
}
