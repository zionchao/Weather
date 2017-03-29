package com.kevin.zhangchao.weather.network;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kevin.zhangchao.weather.entity.Weather;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZhangChao on 2017/3/18.
 */

public class WeatherAPI {
    @SerializedName("HeWeather data service 3.0") @Expose
    public List<Weather> mHeWeatherDataService30s
            =new ArrayList<>();
}
