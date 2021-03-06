package com.kevin.zhangchao.weather.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;

/**
 * Created by ZhangChao on 2017/3/14.
 */

public class FirstActivity extends Activity {

    private SwitchHandler mHandler=new SwitchHandler(this);
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        super.onCreate(savedInstanceState);
        mHandler.sendEmptyMessageDelayed(1,1000);
    }

    private static class SwitchHandler extends Handler{

        private WeakReference<FirstActivity> mWeakReference;

        SwitchHandler(FirstActivity activity){
            mWeakReference=new WeakReference<FirstActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            FirstActivity activity=mWeakReference.get();
            if (activity!=null){
                MainActivity.launch(activity);
                activity.finish();
            }
        }
    }
}
