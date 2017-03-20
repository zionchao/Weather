package com.kevin.zhangchao.weather;


import com.kevin.zhangchao.weather.component.OrmLite;
import com.kevin.zhangchao.weather.utils.PLog;
import com.kevin.zhangchao.weather.utils.ToastUtil;
import com.kevin.zhangchao.weather.utils.Util;
import com.litesuits.orm.db.assit.WhereBuilder;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.functions.Func1;

/**
 * Created by ZhangChao on 2017/3/18.
 */

class RetrofitSingleton {

    private OkHttpClient sOkHttpClient;
    private Retrofit sRetrofit;
    private ApiInterface sApiService;

    public static RetrofitSingleton getInstance() {
        return SingletonHolder.INSTANCE;
    }



    private static class SingletonHolder {
        private static final RetrofitSingleton INSTANCE=new RetrofitSingleton();
    }

    private RetrofitSingleton(){
        init();
    }

    private void init() {
        initOkHttp();
        initRetrofit();
        sApiService=sRetrofit.create(ApiInterface.class);
    }

    private void initOkHttp() {
        OkHttpClient.Builder builder=new OkHttpClient.Builder();
        if (BuildConfig.DEBUG){
            HttpLoggingInterceptor loggingInterceptor=new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
            builder.addInterceptor(loggingInterceptor);
        }
        File cacheFile=new File(BaseApplication.getAppCacheDir(),"/NetCache");
        Cache cache=new Cache(cacheFile,1024*1024*50);
        Interceptor cacheInterceptor =new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request=chain.request();
                if (!Util.isNetworkConnected(BaseApplication.getAppContext())){
                    request=request.newBuilder()
                            .cacheControl(CacheControl.FORCE_CACHE)
                            .build();
                }
                Response response=chain.proceed(request);
                Response.Builder newBuilder=response.newBuilder();
                if (Util.isNetworkConnected(BaseApplication.getAppContext())){
                    int maxAge=0;
                    newBuilder.header("Cache-Control","public,max-age="+maxAge);
                }else{
                    int maxStale=60*60^24*28;
                    newBuilder.header("Cache-Control","public,only-if-cached,max-stale="+maxStale);
                }
                return newBuilder.build();
            }
        };
        builder.cache(cache).addInterceptor(cacheInterceptor);
        builder.connectTimeout(15, TimeUnit.SECONDS);
        builder.readTimeout(20,TimeUnit.SECONDS);
        builder.writeTimeout(20,TimeUnit.SECONDS);
        builder.retryOnConnectionFailure(true);
        sOkHttpClient=builder.build();
    }

    private void initRetrofit() {
        sRetrofit=new Retrofit.Builder()
                .baseUrl(ApiInterface.HOST)
                .client(sOkHttpClient)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public ApiInterface getApiService(){
        return sApiService;
    }

    public Observable<Weather> fetchWeather(final String city){
        return sApiService.mWeatherAPI(city, C.KEY)
                .flatMap(new Func1<WeatherAPI, Observable<WeatherAPI>>() {
                    @Override
                    public Observable<WeatherAPI> call(WeatherAPI weatherAPI) {
                       String status=weatherAPI.mHeWeatherDataService30s.get(0).status;
                        if ("no more requests".equals(status))
                        {
                            return Observable.error(new RuntimeException("/(ㄒoㄒ)/~~,API免费次数已用完"));
                        }else if ("unknown city".equals(status)) {
                            return Observable.error(new RuntimeException(String.format("API没有%s", city)));
                        }
                        return Observable.just(weatherAPI);
                    }
                }).map(new Func1<WeatherAPI, Weather>() {
                    @Override
                    public Weather call(WeatherAPI weatherAPI) {
                        return weatherAPI.mHeWeatherDataService30s.get(0);
                    }
                }).compose(RxUtils.<Weather>rxSchedulerHelper());
    }


    public static void disposeFailureInfo(Throwable t) {
        if (t.toString().contains("GaiException") || t.toString().contains("SocketTimeoutException") ||
                t.toString().contains("UnknownHostException")){
            ToastUtil.showShort("网络问题");
        }else if (t.toString().contains("API没有")){
            OrmLite.getInstance().delete(new WhereBuilder(CityORM.class).where("name=?", Util.replaceInfo(t.getMessage())));
            PLog.w(Util.replaceInfo(t.getMessage()));
            ToastUtil.showShort("错误: " + t.getMessage());
        }
        PLog.w(t.getMessage());
    }


    public Observable<VersionAPI> fetchVersion() {
        return sApiService.mVersionAPI(C.API_TOKEN).compose(RxUtils.<VersionAPI>rxSchedulerHelper());
    }
}
