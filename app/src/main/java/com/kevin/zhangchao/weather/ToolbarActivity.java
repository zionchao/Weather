package com.kevin.zhangchao.weather;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.Toolbar;

/**
 * Created by ZhangChao on 2017/3/21.
 */

public abstract class ToolbarActivity extends BaseActivity{

    protected AppBarLayout mAppBar;
    protected Toolbar mToolbar;
    protected boolean mIsHidden = false;

    abstract protected int provideContentViewId();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        beforeSetContent();
        setContentView(provideContentViewId());
        mAppBar = (AppBarLayout) findViewById(R.id.appbar_layout);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar == null || mAppBar == null) {
            throw new IllegalStateException(
                    "The subclass of ToolbarActivity must contain a toolbar.");
        }
    }

    public Toolbar getToolbar(){
        return mToolbar;
    }

    protected void beforeSetContent() {

    }
}
