package com.kevin.zhangchao.weather;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.kevin.zhangchao.weather.component.OrmLite;
import com.kevin.zhangchao.weather.component.RxBus;
import com.kevin.zhangchao.weather.utils.SharedPreferenceUtil;
import com.kevin.zhangchao.weather.utils.Util;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;

/**
 * Created by ZhangChao on 2017/3/19.
 */

public class ChoiceCityActivity extends AppCompatActivity{

    protected AppBarLayout mAppBar;
    protected Toolbar mToolbar;
    protected boolean mIsHidden = false;

    private RecyclerView mRecyclerview;
    private ProgressBar mProgressBar;

    private ArrayList<String> dataList = new ArrayList<>();
    private Province selectedProvince;
    private City selectedCity;
    private List<Province> provincesList = new ArrayList<>();
    private List<City> cityList;
    private CityAdapter mAdapter;

    public static final int LEVEL_PROVINCE = 1;
    public static final int LEVEL_CITY = 2;
    private int currentLevel;

    private boolean isChecked = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice_city);
        initView();
        initData();

        Intent intent=getIntent();
        isChecked=intent.getBooleanExtra(C.MULTI_CHECK,false);
        if (isChecked&& SharedPreferenceUtil.getInstance().getBoolean("Tips",true)){
            showTips();
        }
    }



    private void initView() {

        mAppBar = (AppBarLayout) findViewById(R.id.appbar_layout);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar == null || mAppBar == null) {
            throw new IllegalStateException(
                    "The subclass of ToolbarActivity must contain a toolbar.");
        }
        mToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                onToolbarClick();
            }
        });
        setSupportActionBar(mToolbar);
        if (canBack()) {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
        }
        if (Build.VERSION.SDK_INT >= 21) {
            mAppBar.setElevation(10.6f);
        }

        mRecyclerview = (RecyclerView) findViewById(R.id.recyclerview);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }


    private void initData() {
        Observable.defer(new Func0<Observable<Integer>>() {
            @Override
            public Observable<Integer> call() {
                DBManager.getInstance().openDatabase();
                return Observable.just(1);
            }
        }).compose(RxUtils.<Integer>rxSchedulerHelper())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        initRecyclerView();
                        queryProvinces();
                    }
                });
    }

    private void initRecyclerView() {
        mRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerview.setHasFixedSize(true);
        mAdapter=new CityAdapter(this,dataList);
        mRecyclerview.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new CityAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, int pos) {
                if (currentLevel==LEVEL_PROVINCE){
                    selectedProvince=provincesList.get(pos);
                    mRecyclerview.smoothScrollToPosition(0);
                    queryCities();
                }else if(currentLevel==LEVEL_CITY){
                    String city= Util.replaceCity(cityList.get(pos).CityName);
                    if (isChecked){
                        OrmLite.getInstance().save(new CityORM(city));
                        RxBus.getDefault().post(new MultiUpdate());
                    }else
                    {
                        SharedPreferenceUtil.getInstance().setCityName(city);
                        RxBus.getDefault().post(new ChangeCityEvent());
                    }
                    quit();
                }
            }
        });
    }

    private void queryCities() {
        mToolbar.setTitle("选择城市");
        dataList.clear();
        mAdapter.notifyDataSetChanged();
        Observable.defer(new Func0<Observable<City>>() {
            @Override
            public Observable<City> call() {
                cityList=WeatherDB.loadCities(
                        DBManager.getInstance().getDatabase(),selectedProvince.ProSort
                );
                return Observable.from(cityList);
            }
        }).map(new Func1<City, String>() {
            @Override
            public String call(City city) {
                return city.CityName;
            }
        }).toList()
                .compose(RxUtils.<List<String>>rxSchedulerHelper())
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        currentLevel=LEVEL_CITY;
                        mAdapter.notifyDataSetChanged();
                        mRecyclerview.smoothScrollToPosition(0);
                    }
                }).subscribe(new SimpleSubscriber<List<String>>() {
            @Override
            public void onNext(List<String> strings) {
                dataList.addAll(strings);
            }
        });

    }

    private void queryProvinces() {
        mToolbar.setTitle("选择省份");
        Observable.defer(new Func0<Observable<Province>>() {
            @Override
            public Observable<Province> call() {
                if (provincesList.isEmpty()){
                    provincesList.addAll(WeatherDB.loadProvinces(
                            DBManager.getInstance().getDatabase()));
                }
                return Observable.from(provincesList);
            }
        }).map(new Func1<Province, String>() {
            @Override
            public String call(Province province) {
                return province.ProName;
            }
        }).toList()
                .compose(RxUtils.<List<String>>rxSchedulerHelper())
                .doOnTerminate(new Action0() {
                    @Override
                    public void call() {
                        mProgressBar.setVisibility(View.GONE);
                    }
                }).doOnCompleted(new Action0() {
            @Override
            public void call() {
                currentLevel=LEVEL_PROVINCE;
                mAdapter.notifyDataSetChanged();
            }
        }).subscribe(new SimpleSubscriber<List<String>>() {
            @Override
            public void onNext(List<String> strings) {
                dataList.addAll(strings);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (currentLevel==LEVEL_PROVINCE){
            quit();
        }else{
            queryProvinces();
            mRecyclerview.smoothScrollToPosition(0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.multi_city_menu,menu);
        menu.getItem(0).setChecked(isChecked);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.multi_check){
            if (isChecked){
                item.setChecked(false);
            }else
                item.setChecked(true);
            isChecked=item.isChecked();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DBManager.getInstance().closeDatabase();
    }

    public boolean canBack() {
        return true;
    }


    private void showTips() {
        new AlertDialog.Builder(this).setTitle("多城市管理模式").setMessage("您现在是多城市管理模式,直接点击即可新增城市.如果暂时不需要添加,"
                + "在右上选项中关闭即可像往常一样操作.\n因为 api 次数限制的影响,多城市列表最多三个城市.(๑′ᴗ‵๑)").setPositiveButton("明白", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

    private void quit() {
        ChoiceCityActivity.this.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public static void launch(Context context) {
        context.startActivity(new Intent(context, ChoiceCityActivity.class));
    }
}
