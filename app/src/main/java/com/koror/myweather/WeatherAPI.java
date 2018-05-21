package com.koror.myweather;

import com.koror.myweather.pojo.Model;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Koror on 03.05.2018.
 */

public interface WeatherAPI {
    @GET("/data/2.5/weather?")
    Call<Model> getData(@Query("id") Integer city, @Query("APPID") String appid, @Query("units") String units, @Query("lang") String lang);
}
