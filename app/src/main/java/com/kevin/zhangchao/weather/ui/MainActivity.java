package com.kevin.zhangchao.weather.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.kevin.zhangchao.weather.R;
import com.kevin.zhangchao.weather.customview.SnowSurfaceView;
import com.kevin.zhangchao.weather.customview.SunView;
import com.kevin.zhangchao.weather.utils.CircularAnimUtil;
import com.kevin.zhangchao.weather.utils.Constants;
import com.kevin.zhangchao.weather.utils.RxDrawer;
import com.kevin.zhangchao.weather.utils.RxUtils;
import com.kevin.zhangchao.weather.utils.SharedPreferenceUtil;
import com.kevin.zhangchao.weather.utils.SimpleSubscriber;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    @Bind(R.id.viewPager)
    ViewPager mViewPager;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.tabLayout)
    TabLayout mTabLayout;
    @Bind(R.id.fab)
    FloatingActionButton mFab;
    @Bind(R.id.nav_view)
    NavigationView mNavView;
    @Bind(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int model= SharedPreferenceUtil.getInstance().getInt(SharedPreferenceUtil.DYA_NIGHT_MODEL,1);
        if (model==1){
            setTheme(R.style.NightTheme);
        }else{
            setTheme(R.style.DayTheme);
        }
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initView();
        initDrawer();
        initIcon();
        //TODO 加载数据
    }

    private void initView() {

        View view=new SunView(this);
        this.addContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setSupportActionBar(mToolbar);
        HomePagerAdapter mHomePagerAdapter=new HomePagerAdapter(getSupportFragmentManager());
        mHomePagerAdapter.addTab(new MainFragment(),"主页");
        mHomePagerAdapter.addTab(new MutiCityFragment(),"多城市");
        mViewPager.setAdapter(mHomePagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager,false);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(final int position) {
                mFab.post(new Runnable() {
                    @Override
                    public void run() {
                        mFab.hide(new FloatingActionButton.OnVisibilityChangedListener() {
                            @Override
                            public void onHidden(FloatingActionButton fab) {
                                if (position==1){
                                    mFab.setImageResource(R.drawable.ic_add_24dp);
                                    mFab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MainActivity.this,R.color.colorPrimary)));
                                    mFab.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent(MainActivity.this, ChoiceCityActivity.class);
                                            intent.putExtra(Constants.MULTI_CHECK, true);
                                            CircularAnimUtil.startActivity(MainActivity.this, intent, mFab,
                                                    R.color.colorPrimary);
                                        }
                                    });
                                }else{
                                    mFab.setImageResource(R.drawable.ic_favorite);
                                    mFab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MainActivity.this, R.color.colorAccent)));
                                    mFab.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            showFabDialog();
                                        }
                                    });
                                }
                                fab.show();
                            }
                        });
                    }
                });
                if (!mFab.isShown())
                    mFab.show();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFabDialog();
            }
        });
    }

    private void showFabDialog() {
        new AlertDialog.Builder(MainActivity.this).setTitle("点赞")
                .setMessage("去项目个作者个Star，鼓励下作者")
                .setPositiveButton("好嘞", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Uri uri=Uri.parse("");
                        Intent intent=new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setData(uri);
                        MainActivity.this.startActivity(intent);
                    }
                }).show();
    }

    private void initDrawer() {
        if (mNavView!=null){
            mNavView.setNavigationItemSelectedListener(this);
            mNavView.inflateHeaderView(R.layout.nav_header_main);
            ActionBarDrawerToggle toggle=new ActionBarDrawerToggle(this,mDrawerLayout,
                    R.string.navigation_drawer_open,R.string.navigation_drawer_close);
            mDrawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }

    }

    private void initIcon() {
        if (SharedPreferenceUtil.getInstance().getIconType() == 0) {
            SharedPreferenceUtil.getInstance().putInt("未知", R.mipmap.none);
            SharedPreferenceUtil.getInstance().putInt("晴", R.mipmap.type_one_sunny);
            SharedPreferenceUtil.getInstance().putInt("阴", R.mipmap.type_one_cloudy);
            SharedPreferenceUtil.getInstance().putInt("多云", R.mipmap.type_one_cloudy);
            SharedPreferenceUtil.getInstance().putInt("少云", R.mipmap.type_one_cloudy);
            SharedPreferenceUtil.getInstance().putInt("晴间多云", R.mipmap.type_one_cloudytosunny);
            SharedPreferenceUtil.getInstance().putInt("小雨", R.mipmap.type_one_light_rain);
            SharedPreferenceUtil.getInstance().putInt("中雨", R.mipmap.type_one_light_rain);
            SharedPreferenceUtil.getInstance().putInt("大雨", R.mipmap.type_one_heavy_rain);
            SharedPreferenceUtil.getInstance().putInt("阵雨", R.mipmap.type_one_thunderstorm);
            SharedPreferenceUtil.getInstance().putInt("雷阵雨", R.mipmap.type_one_thunder_rain);
            SharedPreferenceUtil.getInstance().putInt("霾", R.mipmap.type_one_fog);
            SharedPreferenceUtil.getInstance().putInt("雾", R.mipmap.type_one_fog);
        } else {
            SharedPreferenceUtil.getInstance().putInt("未知", R.mipmap.none);
            SharedPreferenceUtil.getInstance().putInt("晴", R.mipmap.type_two_sunny);
            SharedPreferenceUtil.getInstance().putInt("阴", R.mipmap.type_two_cloudy);
            SharedPreferenceUtil.getInstance().putInt("多云", R.mipmap.type_two_cloudy);
            SharedPreferenceUtil.getInstance().putInt("少云", R.mipmap.type_two_cloudy);
            SharedPreferenceUtil.getInstance().putInt("晴间多云", R.mipmap.type_two_cloudytosunny);
            SharedPreferenceUtil.getInstance().putInt("小雨", R.mipmap.type_two_light_rain);
            SharedPreferenceUtil.getInstance().putInt("中雨", R.mipmap.type_two_rain);
            SharedPreferenceUtil.getInstance().putInt("大雨", R.mipmap.type_two_rain);
            SharedPreferenceUtil.getInstance().putInt("阵雨", R.mipmap.type_two_rain);
            SharedPreferenceUtil.getInstance().putInt("雷阵雨", R.mipmap.type_two_thunderstorm);
            SharedPreferenceUtil.getInstance().putInt("霾", R.mipmap.type_two_haze);
            SharedPreferenceUtil.getInstance().putInt("雾", R.mipmap.type_two_fog);
            SharedPreferenceUtil.getInstance().putInt("雨夹雪", R.mipmap.type_two_snowrain);
        }
    }

    public static void launch(Context context) {
        context.startActivity(new Intent(context,MainActivity.class));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        RxDrawer.close(mDrawerLayout)
                .compose(RxUtils.<Void>rxSchedulerHelper(AndroidSchedulers.mainThread()))
                .subscribe(new SimpleSubscriber<Void>() {
                    @Override
                    public void onNext(Void aVoid) {
                        switch (item.getItemId()) {
                            case R.id.nav_set:
                                SettingActivity.launch(MainActivity.this);
                                break;
                            case R.id.nav_about:
                                AboutActivity.launch(MainActivity.this);
                                break;
                            case R.id.nav_city:
                                ChoiceCityActivity.launch(MainActivity.this);
                                break;
                            case R.id.nav_multi_cities:
                                mViewPager.setCurrentItem(1);
                                break;
                        }
                    }
                });
        return false;
    }
}
