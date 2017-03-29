package com.kevin.zhangchao.weather.ui;

import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by ZhangChao on 2017/3/18.
 */

public class BaseFragment extends Fragment{
    public BaseFragment() {
        super();
    }

    public void safeSetTitle(String title) {
        ActionBar appBarLayout=((AppCompatActivity)getActivity()).getSupportActionBar();
        if (appBarLayout!=null)
            appBarLayout.setTitle(title);
    }

}
