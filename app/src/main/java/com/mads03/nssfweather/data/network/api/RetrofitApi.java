package com.mads03.nssfweather.data.network.api;

import com.mads03.nssfweather.helpers.SessionManager;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;


public class RetrofitApi {
    Retrofit retrofit1;
    SessionManager sessionManager;


    public RetrofitApi() {
        // Session manager
        //sessionManager = new SessionManager(context.getApplicationContext());

        //Here a logging interceptor is created
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        //The logging interceptor will be added to the http client
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        //add the auth token to all requests
        /*httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();

                Response response = chain.proceed(original);
                String authToken = "Bearer " + sessionManager.getUserToken();


                Request request = original.newBuilder()
                        .header("Authorization", authToken)
                        .method(original.method(), original.body()).build();
                return chain.proceed(request);
            }
        });*/
        httpClient.addInterceptor(logging).connectTimeout(30000, TimeUnit.SECONDS).readTimeout(30000, TimeUnit.SECONDS);

        //The Retrofit builder will have the client attached, in order to get connection logs
        this.retrofit1 = new Retrofit.Builder()
                .client(httpClient.build())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(APIUrl.WEATHER_URL)
                .build();
    }

    public Retrofit getRetrofit1() {
        return retrofit1;
    }

    public APIService getRetrofitService(){
        return this.getRetrofit1().create(APIService.class);
    }
}

