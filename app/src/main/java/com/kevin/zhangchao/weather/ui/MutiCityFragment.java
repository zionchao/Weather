package com.kevin.zhangchao.weather.ui;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.kevin.zhangchao.weather.R;

import com.kevin.zhangchao.weather.databases.CityORM;
import com.kevin.zhangchao.weather.databases.OrmLite;
import com.kevin.zhangchao.weather.entity.Weather;
import com.kevin.zhangchao.weather.network.RetrofitSingleton;
import com.kevin.zhangchao.weather.network.WeatherAPI;
import com.kevin.zhangchao.weather.utils.Constants;
import com.kevin.zhangchao.weather.utils.MultiUpdate;
import com.kevin.zhangchao.weather.utils.RxBus;
import com.kevin.zhangchao.weather.utils.RxUtils;
import com.kevin.zhangchao.weather.utils.SimpleSubscriber;
import com.litesuits.orm.db.assit.WhereBuilder;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Observer;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;

/**
 * Created by ZhangChao on 2017/3/14.
 */

public class MutiCityFragment extends Fragment {

    @Bind(R.id.recyclerview)
    RecyclerView mRecyclerView;
    @Bind(R.id.swiprefresh)
    SwipeRefreshLayout mRefreshLayout;
    @Bind(R.id.empty)
    LinearLayout mLayout;

    private View view;
    private ArrayList<Weather> mWeathers;
    private MultiCityAdapter mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RxBus.getDefault().toObservable(MultiUpdate.class).subscribe(new SimpleSubscriber<MultiUpdate>() {
            @Override
            public void onNext(MultiUpdate multiUpdate) {
                multiLoad();
            }
        });
    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
         if (view==null){
             view=inflater.inflate(R.layout.fragment_multicity,container,false);
             ButterKnife.bind(this,view);
         }
         return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        multiLoad();
    }

    private void initView() {
        mWeathers=new ArrayList<>();
        mAdapter=new MultiCityAdapter(mWeathers);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnMultiCityLongClick(new MultiCityAdapter.onMultiCityLongClick() {
            @Override
            public void longClick(final String city) {
                new AlertDialog.Builder(getActivity()).setMessage("是否删除该城市")
                        .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                OrmLite.getInstance().delete(new WhereBuilder(CityORM.class).where("name=?", city));
//                                OrmLite.OrmTest(CityORM.class);
                                multiLoad();
                                Snackbar.make(getView(),"已经将" + city + "删掉了 Ծ‸ Ծ", Snackbar.LENGTH_LONG)
                                        .setAction("撤销", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                OrmLite.getInstance().save(new CityORM(city));
                                                multiLoad();
                                            }
                                        }).show();

                            }
                        });
            }
        });
        if (mRefreshLayout!=null){
            mRefreshLayout.setColorSchemeResources(
                    android.R.color.holo_orange_light,
                    android.R.color.holo_red_light,
                    android.R.color.holo_green_light,
                    android.R.color.holo_blue_bright
            );
            mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    mRefreshLayout.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            multiLoad();
                        }
                    },1000);
                }
            });
        }
    }

    private void multiLoad() {
        mWeathers.clear();
        Observable.defer(new Func0<Observable<CityORM>>() {
            @Override
            public Observable<CityORM> call() {
                return Observable.from(OrmLite.getInstance().query(CityORM.class));
            }
        }).doOnRequest(new Action1<Long>() {
            @Override
            public void call(Long aLong) {
                mRefreshLayout.setRefreshing(true);
            }
        }).map(new Func1<CityORM, String>() {
            @Override
            public String call(CityORM cityORM) {
                return cityORM.getName();
            }
        }).distinct()
                .flatMap(new Func1<String, Observable<Weather>>() {
                    @Override
                    public Observable<Weather> call(String s) {
                        return RetrofitSingleton.getInstance()
                                .getApiService()
                                .mWeatherAPI(s, Constants.KEY)
                                .map(new Func1<WeatherAPI, Weather>() {
                                    @Override
                                    public Weather call(WeatherAPI weatherAPI) {
                                        return weatherAPI.mHeWeatherDataService30s.get(0);
                                    }
                                }).compose(RxUtils.<Weather>rxSchedulerHelper());
                    }
                }).filter(new Func1<Weather, Boolean>() {
            @Override
            public Boolean call(Weather weather) {
                return !Constants.UNKNOWN_CITY.equals(weather.status);
            }
        }).take(3)
                .doOnTerminate(new Action0() {
                    @Override
                    public void call() {
                        mRefreshLayout.setRefreshing(false);
                    }
                }).subscribe(new Observer<Weather>() {
            @Override
            public void onCompleted() {
                mAdapter.notifyDataSetChanged();
                if (mAdapter.isEmpty()){
                    mLayout.setVisibility(View.VISIBLE);
                }else
                    mLayout.setVisibility(View.GONE);
            }

            @Override
            public void onError(Throwable e) {
                if (mAdapter.isEmpty() && mLayout != null) {
                    mLayout.setVisibility(View.VISIBLE);
                }
                RetrofitSingleton.disposeFailureInfo(e);
            }

            @Override
            public void onNext(Weather weather) {
                mWeathers.add(weather);
            }
        });
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
}
