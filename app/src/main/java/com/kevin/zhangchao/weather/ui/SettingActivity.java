package com.kevin.zhangchao.weather.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import com.kevin.zhangchao.weather.R;
import com.kevin.zhangchao.weather.utils.SharedPreferenceUtil;

import static com.kevin.zhangchao.weather.utils.SharedPreferenceUtil.DYA_NIGHT_MODEL;

/**
 * Created by zhangchao_a on 2017/3/20.
 */

public class SettingActivity extends AppCompatActivity{

//    @Override
//    protected int provideContentViewId() {
//        return R.layout.activity_setting;
//    }

    private SettingFragment2 fragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int model= SharedPreferenceUtil.getInstance().getInt(SharedPreferenceUtil.DYA_NIGHT_MODEL,1);
        if (model==1){
            setTheme(R.style.NightTheme);
        }else{
            setTheme(R.style.DayTheme);
        }
        setContentView(R.layout.activity_setting);
//        getToolbar().setTitle("设置");
        fragment=new SettingFragment2();
        getSupportFragmentManager().beginTransaction().replace(R.id.framelayout,
                fragment).commit();
    }

    public static void launch(Context context) {
        context.startActivity(new Intent(context, SettingActivity.class));
    }

//    @Override
//    public boolean canBack() {
//        return true;
//    }

    public void changeTheme(){
        showAimi();
       int model= SharedPreferenceUtil.getInstance().getInt(SharedPreferenceUtil.DYA_NIGHT_MODEL,1);
        if (model==1){
            setTheme(R.style.NightTheme);
            SharedPreferenceUtil.getInstance().putInt(SharedPreferenceUtil.DYA_NIGHT_MODEL,0);
        }else{
            setTheme(R.style.DayTheme);
            SharedPreferenceUtil.getInstance().putInt(SharedPreferenceUtil.DYA_NIGHT_MODEL,1);
        }
        refreshToolbar();
        refreshStatusBar();

        if (fragment != null){
            getSupportFragmentManager().beginTransaction().remove(fragment);
        }

        fragment= new SettingFragment2();
        getSupportFragmentManager().beginTransaction().replace(R.id.framelayout,
                fragment).commit();
    }

    private void showAimi() {
        final View decorView=getWindow().getDecorView();
        Bitmap cachaBitmap=getCacheBitmapFromView(decorView);
        if (decorView instanceof ViewGroup &&cachaBitmap!=null){
            final View view=new View(this);
            view.setBackground(new BitmapDrawable(
                    getResources(),cachaBitmap
            ));
            ViewGroup.LayoutParams layoutParams=new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            );
            ((ViewGroup) decorView).addView(view,layoutParams);
            ObjectAnimator objectAnimator=ObjectAnimator.ofFloat(view,"alpha",1f,0f);
            objectAnimator.setDuration(1000);
            objectAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    ((ViewGroup) decorView).removeView(view);
                }
            });
            objectAnimator.start();
        }

    }

    private Bitmap getCacheBitmapFromView(View view) {
        boolean drawingCacheEnable=true;
        view.setDrawingCacheEnabled(drawingCacheEnable);
        view.buildDrawingCache(drawingCacheEnable);
        Bitmap drawingCache=view.getDrawingCache();
        Bitmap bitmap;
        if (drawingCache!=null){
            bitmap=Bitmap.createBitmap(drawingCache);
            view.setDrawingCacheEnabled(false);
        }else{
            bitmap=null;
        }
        return bitmap;

    }

    private void refreshStatusBar() {
        if (Build.VERSION.SDK_INT >= 21) {
            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = getTheme();
            theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
            getWindow().setStatusBarColor(getResources().getColor(typedValue.resourceId));
        }
    }

    private void refreshToolbar() {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getTheme();
        theme.resolveAttribute(R.attr.kevinBackground, typedValue, true);
//        getToolbar().setBackgroundColor(typedValue.resourceId);
        int model= SharedPreferenceUtil.getInstance().getInt(SharedPreferenceUtil.DYA_NIGHT_MODEL,1);
//        if (model==1){
////            setTheme(R.style.NightTheme);
//            getToolbar().setTitleTextColor(getResources().getColor(R.color.color8A9599));
//            getToolbar().setBackgroundColor(getResources().getColor(R.color.color3F3F3F));
//            getToolbar().setPopupTheme(R.style.NightTheme);
//        }else{
////            setTheme(R.style.NightTheme);
//            getToolbar().setTitleTextColor(getResources().getColor(android.R.color.black));
//            getToolbar().setTitleTextColor(getResources().getColor(R.color.colorPrimary));
//            getToolbar().setPopupTheme(R.style.DayTheme);
//
//        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent=new Intent(this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

//    @Override
//    public void onToolarClick() {
//        super.onToolarClick();
//        Intent intent=new Intent(this,MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
//    }
}
