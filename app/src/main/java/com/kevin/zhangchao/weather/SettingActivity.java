package com.kevin.zhangchao.weather;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by zhangchao_a on 2017/3/20.
 */

public class SettingActivity extends ToolbarActivity{

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_setting;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getToolbar().setTitle("设置");
        getFragmentManager().beginTransaction().replace(R.id.framelayout,
                new SettingFragment()).commit();
    }

    public static void launch(Context context) {
        context.startActivity(new Intent(context, SettingActivity.class));
    }

    @Override
    public boolean canBack() {
        return true;
    }
}
