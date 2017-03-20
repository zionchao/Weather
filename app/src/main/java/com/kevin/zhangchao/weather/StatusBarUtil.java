package com.kevin.zhangchao.weather;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

/**
 * Created by ZhangChao on 2017/3/20.
 */

class StatusBarUtil {

    public static void setImmersiveStatusBar(Activity activity) {
        if (SdkUtil.sdkVersionGe21()){
            activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        if (SdkUtil.sdkVersionEq(19)){
            activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        activity.getWindow()
                .getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE|
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    public static void setImmersiveStatusBarToolbar(Toolbar toolbar,Context context){
        ViewGroup.MarginLayoutParams toolbarLayoutParmas= (ViewGroup.MarginLayoutParams) toolbar.getLayoutParams();
        toolbarLayoutParmas.height=EnvUtil.getStatusBarHeight()+ EnvUtil.getActionBarSize(context);;
        toolbar.setLayoutParams(toolbarLayoutParmas);
        toolbar.setPadding(0,EnvUtil.getStatusBarHeight(),0,0);
        toolbar.requestLayout();
    }
}
