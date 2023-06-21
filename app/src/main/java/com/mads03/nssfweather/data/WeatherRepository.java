package com.mads03.nssfweather.data;

import static com.mads03.nssfweather.presentation.ui.activity.MainActivity.mainActivity;
import static com.mads03.nssfweather.presentation.ui.activity.SearchPlacesActivity.searchPlacesActivity;

import android.app.Application;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.mads03.nssfweather.app.MyApplication;
import com.mads03.nssfweather.data.database.WeatherDao;
import com.mads03.nssfweather.data.database.WeatherDatabase;
import com.mads03.nssfweather.data.network.api.APIService;
import com.mads03.nssfweather.data.network.api.APIUrl;
import com.mads03.nssfweather.data.network.api.RetrofitApi;
import com.mads03.nssfweather.helpers.AppExecutors;
import com.mads03.nssfweather.helpers.SessionManager;
import com.mads03.nssfweather.models.Weather;
import com.mads03.nssfweather.presentation.ui.activity.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherRepository {
    private static final String TAG = WeatherRepository.class.getSimpleName();
    private WeatherDao mWeatherDao;
    private AppExecutors mExecutors;
    private MediatorLiveData<List<Weather>> newWeather =
            new MediatorLiveData<>();
    private LiveData<List<Weather>> mAllWeather;
    private List<Weather> weatherList;
    private int mCount;
    private static WeatherRepository sInstance;
    private boolean mInitialized = false;

    private SessionManager session;
    // For Singleton instantiation
    private static final Object LOCK = new Object();

    //constructor that gets a handle to the db and initializes the member
    //variables
    public WeatherRepository(Application application){
        WeatherDatabase db = WeatherDatabase.getDatabase(application);
        session = new SessionManager(MyApplication.getInstance().getApplicationContext());
        mWeatherDao = db.weatherDao();
        mAllWeather = mWeatherDao.getLiveDataWeather();
        mExecutors = AppExecutors.getInstance();
        sInstance = this;
        //LiveData<Weather[]> weather = mFetchWeather.getCurrentWeather();

//        weather.observeForever(newWeather -> mExecutors.diskIO().execute(() -> {
//            //delete all previous weather data from the db, this ensures that if we have new
//            deleteOldData();
//            Log.d(TAG, "Old weather deleted");
//            mWeatherDao.insertWeather(newWeather);
//
//            Log.d(TAG, newWeather.length +" inserted");
//        }));

    }

    public static WeatherRepository getInstance(){
        return  sInstance;
    }

    /*
    public synchronized static WeatherRepository getInstance(
            WeatherDao weatherDao, FetchWeather fetchWeather,
            AppExecutors executors) {
        Log.d(TAG, "Getting the repository");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new WeatherRepository(weatherDao, fetchWeather, executors);
                Log.d(TAG, "Made new repository");
            }
        }
        return sInstance;
    }*/

    public LiveData<List<Weather>> getAllWeather(String lat, String lon){
        mExecutors.diskIO().execute(() -> {
            updateWeather(lat, lon);
        });

        return mAllWeather;
    }

    public LiveData<List<Weather>> getLiveDataWeather(){

        return mAllWeather;
    }

    private boolean isFetchNeeded() {
        mExecutors.diskIO().execute(() ->
                mCount = mWeatherDao.countWeatherInDb());
        Log.e(TAG, "Weather count in db = "+mCount);
        return (mCount < 1);
    }

    private int deleteOldData() {
        mExecutors.diskIO().execute(() -> {
            mWeatherDao.deleteAll();

            mCount = mWeatherDao.countWeatherInDb();
        });
        return mCount;
    }

    public void getPlaceWeather(String lat, String lon) {
        mExecutors.diskIO().execute(() -> {
            getSelectedPlaceWeather(lat, lon);
        });

    }

    public void fetchWeatherUpdate(String lat, String lon) {
        //webDataSource.updateWeatherStatus(lat, lon);
        mExecutors.diskIO().execute(() -> {
            updateWeather(lat, lon);
        });

    }

    //network call to get the current weather and a forecast
    public void updateWeather(String lat, String lon){

        //Defining retrofit api service*/
        APIService service = new RetrofitApi().getRetrofitService();

        //defining the call
        Call<ResponseBody> call = service.getWeatherForecast2(lat, lon, APIUrl.API_TOKEN, "40");

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Weather weather;
                int forecastCount = 0;
                boolean isCurrentWeatherAdded = false;
                String alreadyAddedDate = "";
                //get today date
                Date c = Calendar.getInstance().getTime();
                System.out.println("Current time => " + c);

                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String dateToday = df.format(c);

                SimpleDateFormat myFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm aa", Locale.ENGLISH);
                String lastUpdate = myFormat.format(c);

                if(response.isSuccessful()){
                    if(deleteOldData() == 0){
                        try {
                            String data=response.body().string();
                            JSONObject json=new JSONObject(data);
                            JSONArray list=json.getJSONArray("list");

                            //loop through the json data and get out today's weather and forecast for 5 days
                            for (int i = 0; i < list.length(); i++) {
                                weather = new Weather();
                                JSONObject object = list.getJSONObject(i);

                                //remove the time part and compare to today date
                                String dt = object.getString("dt_txt");
                                String weatherDate = dt.substring(0, dt.indexOf(" "));
                                if (weatherDate.equals(dateToday)){
                                    if(!isCurrentWeatherAdded){
                                        weather.setDate(weatherDate);

                                        JSONArray weatherArray = object.getJSONArray("weather");
                                        JSONObject weatherObject = weatherArray.getJSONObject(0);
                                        weather.setDescription(weatherObject.getString("description"));
                                        weather.setShortDesc(weatherObject.getString("main"));

                                        JSONObject mainObject = object.getJSONObject("main");
                                        weather.setTemperature(mainObject.getString("temp"));
                                        weather.setMin(mainObject.getString("temp_min"));
                                        weather.setMax(mainObject.getString("temp_max"));
                                        weather.setHumidity(mainObject.getString("humidity"));
                                        weather.setPressure(mainObject.getString("pressure"));

                                        weather.setIsCurrent("1");
                                        weather.setLastUpdate(lastUpdate);
                                        Weather finalWeather = weather;
                                        mExecutors.diskIO().execute(() -> {
                                            mWeatherDao.insertWeather(finalWeather);
                                            mCount = mWeatherDao.countWeatherInDb();
                                            Log.e(TAG, "Weather count in db = "+mCount);
                                        });

                                        isCurrentWeatherAdded = true;
                                    }

                                }else{
                                    if(!weatherDate.equals(alreadyAddedDate)){
                                        if (forecastCount <= 4){
                                            weather.setDate(weatherDate);

                                            JSONArray weatherArray = object.getJSONArray("weather");
                                            JSONObject weatherObject = weatherArray.getJSONObject(0);
                                            weather.setDescription(weatherObject.getString("description"));
                                            weather.setShortDesc(weatherObject.getString("main"));

                                            JSONObject mainObject = object.getJSONObject("main");
                                            weather.setTemperature(mainObject.getString("temp"));
                                            weather.setMin(mainObject.getString("temp_min"));
                                            weather.setMax(mainObject.getString("temp_max"));
                                            weather.setHumidity(mainObject.getString("humidity"));
                                            weather.setPressure(mainObject.getString("pressure"));

                                            weather.setIsCurrent("0");
                                            weather.setLastUpdate(lastUpdate);
                                            Weather finalWeather = weather;
                                            mExecutors.diskIO().execute(() -> {
                                                mWeatherDao.insertWeather(finalWeather);
                                                mCount = mWeatherDao.countWeatherInDb();
                                                Log.e(TAG, "Weather count in db = "+mCount);
                                            });
                                            alreadyAddedDate = weatherDate;
                                            forecastCount ++;
                                        }

                                    }
                                }

                            }

                            Log.i(TAG, " returned successfully "+ list);
                        } catch (IOException | JSONException e) {
                            throw new RuntimeException(e);
                        }

                        mExecutors.diskIO().execute(() -> {
                            weatherList = mWeatherDao.getAllWeather();
                            mainActivity.onWeatherUpdated(true, weatherList, "Weather updated");
                        });
                    }


                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                Log.e(TAG, t.getMessage());
                mainActivity.onWeatherUpdated(false, null, "Weather NOT updated");
            }
        });
    }

    //network call to get a single place weather
    public void getSelectedPlaceWeather(String lat, String lon){

        //Defining retrofit api service*/
        APIService service = new RetrofitApi().getRetrofitService();

        //defining the call
        Call<ResponseBody> call = service.getCurrentWeather(lat, lon, APIUrl.API_TOKEN);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                String temp = "";

                if(response.isSuccessful()){
                    if(deleteOldData() == 0){
                        try {
                            String data = response.body().string();
                            //Log.i(TAG, "json data = "+ data);
                            JSONObject json = new JSONObject(data);

                            JSONObject mainObject = json.getJSONObject("main");
                            temp = mainObject.getString("temp");

                        } catch (IOException | JSONException e) {
                            throw new RuntimeException(e);
                        }

                        searchPlacesActivity.onPlaceWeatherGot(true, temp,
                                "Got Place Weather");
                    }


                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                Log.e(TAG, t.getMessage());
                searchPlacesActivity.onPlaceWeatherGot(true, "",
                        "Did NOT Get Place Weather");
            }
        });
    }

}
