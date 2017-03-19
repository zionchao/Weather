package com.kevin.zhangchao.weather;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.kevin.zhangchao.weather.component.RxBus;
import com.kevin.zhangchao.weather.utils.PLog;
import com.kevin.zhangchao.weather.utils.SharedPreferenceUtil;
import com.kevin.zhangchao.weather.utils.ToastUtil;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;

/**
 * Created by ZhangChao on 2017/3/14.
 */

public class MainFragment extends BaseFragment {

    @Bind(R.id.recyclerview)
    RecyclerView mRecyclerView;
    @Bind(R.id.swiprefresh)
    SwipeRefreshLayout mRefreshLayout;
    @Bind(R.id.progressBar)
    ProgressBar mProgressBar;
    @Bind(R.id.iv_erro)
    ImageView mIvError;


    private View view;
    private boolean mIsCreateView;
    private WeatherAdapter mAdapter;
    private Weather mWeather=new Weather();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RxBus.getDefault().toObservable(ChangeCityEvent.class).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleSubscriber<ChangeCityEvent>(){
                    @Override
                    public void onNext(ChangeCityEvent changeCityEvent) {
                        if (mRefreshLayout!=null){
                            mRefreshLayout.setRefreshing(true);
                        }
                        load();
                        PLog.d("MainRxBus");
                    }
                });

    }

    public void load(){
        fetchDataByNetWork()
        .doOnRequest(new Action1<Long>() {
            @Override
            public void call(Long aLong) {
                mRefreshLayout.setRefreshing(true);
            }
        }).doOnError(new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                mIvError.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
                SharedPreferenceUtil.getInstance().setCityName("北京");
                safeSetTitle("找不到城市啦");
            }
        })
        .doOnNext(new Action1<Weather>() {
            @Override
            public void call(Weather weather) {
                mIvError.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            }
        }).doOnTerminate(new Action0() {
            @Override
            public void call() {
                mRefreshLayout.setRefreshing(false);
                mProgressBar.setVisibility(View.GONE);
            }
        }).subscribe(new Subscriber<Weather>() {
            @Override
            public void onCompleted() {
                ToastUtil.showShort(getString(R.string.complete));

            }

            @Override
            public void onError(Throwable e) {
                PLog.e(e.toString());
                RetrofitSingleton.disposeFailureInfo(e);
            }

            @Override
            public void onNext(Weather weather) {
                mWeather.status=weather.status;
                mWeather.aqi=weather.aqi;
                mWeather.basic=weather.basic;
                mWeather.suggestion=weather.suggestion;
                mWeather.now=weather.now;
                mWeather.dailyForecast=weather.dailyForecast;
                mWeather.hourlyForecast=weather.hourlyForecast;
                safeSetTitle(weather.basic.city);
                mAdapter.notifyDataSetChanged();
                normalStyleNotification(weather);
            }
        });
 
    }

    private void normalStyleNotification(Weather weather) {
        Intent intent=new Intent(getActivity(),MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent=PendingIntent.getActivity(getActivity(),
                0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder=new Notification.Builder(getActivity());
        Notification notification=builder.setContentIntent(pendingIntent)
                .setContentTitle(weather.basic.city)
                .setContentText(String.format("%s 当前温度: %s℃ ", weather.now.cond.txt, weather.now.tmp))
                .setSmallIcon(SharedPreferenceUtil.getInstance().getInt(weather.now.cond.txt,R.mipmap.none))
                .build();
        notification.flags=SharedPreferenceUtil.getInstance().getNotificationModel();
        NotificationManager manager= (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1,notification);
    }



    private Observable<Weather> fetchDataByNetWork() {
        String cityName= SharedPreferenceUtil.getInstance().getCityName();
        return RetrofitSingleton.getInstance().fetchWeather(cityName);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view==null){
            view=inflater.inflate(R.layout.fragment_main,container,false);
            ButterKnife.bind(this,view);
        }
        mIsCreateView=true;
        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    private void initView(View view) {
        if (mRefreshLayout!=null){
            mRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                    android.R.color.holo_green_light,
                    android.R.color.holo_orange_light,
                    android.R.color.holo_red_light);
            mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    mRefreshLayout.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            load();
                        }
                    },1000);
                }
            });
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            mAdapter=new WeatherAdapter(mWeather);
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }



    @Override
    public void onDetach() {
        super.onDetach();
    }


}
