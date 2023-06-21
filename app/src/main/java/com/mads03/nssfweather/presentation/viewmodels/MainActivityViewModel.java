package com.mads03.nssfweather.presentation.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.mads03.nssfweather.data.WeatherRepository;
import com.mads03.nssfweather.models.Weather;

import java.util.List;

public class MainActivityViewModel extends AndroidViewModel {

  //private member variable to hold reference to the repository
  private WeatherRepository mRepository;

  /**
   * True when there are pending network requests
   */
  public LiveData<Boolean> loading;

  //private LiveData member variable to cache the weather
  private LiveData<List<Weather>> mAllWeather;

  //constructor that gets a reference to the repository and gets the categories
  public MainActivityViewModel(Application application) {
    super(application);
    mRepository = new WeatherRepository(application);
    mAllWeather = mRepository.getLiveDataWeather();
  }

  //a getter method for all the weather. This hides the implementation from the UI
  public LiveData<List<Weather>> getAllWeather(){
    return mAllWeather;
  }

  public void manualRefresh(String lat, String lon) {
    mRepository.fetchWeatherUpdate(lat, lon);
  }

  //get a single location weather
  public void getPlaceWeather(String lat, String lon) {
    mRepository.getPlaceWeather(lat, lon);
  }


}
