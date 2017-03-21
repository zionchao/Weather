package com.kevin.zhangchao.weather;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;

import com.kevin.zhangchao.weather.utils.SharedPreferenceUtil;
import com.kevin.zhangchao.weather.utils.Util;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by ZhangChao on 2017/3/21.
 */

public class AutoUpdateService extends Service{

    private SharedPreferenceUtil mSharedPreferenceUtil;
    private CompositeSubscription mCompositionSubscription;

    // 在生命周期的某个时刻取消订阅。一个很常见的模式就是使用CompositeSubscription来持有所有的Subscriptions，然后在onDestroy()或者onDestroyView()里取消所有的订阅
    private boolean mIsUnSubscribed;
    private Subscription mNetSubscription;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSharedPreferenceUtil= SharedPreferenceUtil.getInstance();
        mCompositionSubscription=new CompositeSubscription();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        synchronized (this){
            unSubscribed();
            if (mIsUnSubscribed){
                unSubscribed();
                mNetSubscription= Observable.interval(mSharedPreferenceUtil.getAutoUpdate(), TimeUnit.HOURS)
                        .doOnRequest(new Action1<Long>() {
                            @Override
                            public void call(Long aLong) {
                                mIsUnSubscribed=false;
                                fetchDataByNetWork();
                            }
                        })
                        .subscribe();
                mCompositionSubscription.add(mNetSubscription);
            }
        }
        return START_REDELIVER_INTENT;
    }

    private void fetchDataByNetWork() {
        String cityName=mSharedPreferenceUtil.getCityName();
        if (cityName!=null){
            cityName= Util.replaceCity(cityName);
        }
        RetrofitSingleton.getInstance().fetchWeather(cityName)
                .subscribe(new Action1<Weather>() {
                    @Override
                    public void call(Weather weather) {

                    }
                });
    }

    private void normalStyleNotification(Weather weather){
        Intent intent=new Intent(AutoUpdateService.this,MainActivity.class);
        PendingIntent pendingIntent=PendingIntent.getActivity(AutoUpdateService.this,
                0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder=new Notification.Builder(AutoUpdateService.this);
        Notification notification=builder.setContentIntent(pendingIntent)
                .setContentTitle(weather.basic.city)
                .setContentText(String.format("%s 当前温度: %s℃ ", weather.now.cond.txt, weather.now.tmp))
                .setSmallIcon(mSharedPreferenceUtil.getInt(weather.now.cond.txt,R.mipmap.none))
                .build();
        notification.flags=mSharedPreferenceUtil.getNotificationModel();
        NotificationManager manager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1,notification);

    }

    private void unSubscribed() {
        mIsUnSubscribed=true;
        mCompositionSubscription.remove(mNetSubscription);
    }

    @Override
    public boolean stopService(Intent name) {
        unSubscribed();
        return super.stopService(name);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unSubscribed();
    }
}
