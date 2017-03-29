package com.kevin.zhangchao.weather.ui;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.kevin.zhangchao.weather.R;

/**
 * Created by ZhangChao on 2017/3/21.
 */

public abstract class ToolbarActivity extends BaseActivity {

    protected AppBarLayout mAppBar;
    protected Toolbar mToolbar;
    protected boolean mIsHidden = false;

    abstract protected int provideContentViewId();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.DayTheme);
        super.onCreate(savedInstanceState);
        beforeSetContent();
        setContentView(provideContentViewId());
        mAppBar = (AppBarLayout) findViewById(R.id.appbar_layout);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar == null || mAppBar == null) {
            throw new IllegalStateException(
                    "The subclass of ToolbarActivity must contain a toolbar.");
        }

        mToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onToolarClick();
            }
        });
        setSupportActionBar(mToolbar);
        if (canBack()){
            ActionBar actionBar=getSupportActionBar();
            if (actionBar!=null)
                actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (Build.VERSION.SDK_INT>=21)
            mAppBar.setElevation(10.6f);
    }

    public void onToolarClick() {

    }

    public boolean canBack() {
        return false;
    }

    public Toolbar getToolbar(){
        return mToolbar;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    protected void beforeSetContent() {

    }
}
