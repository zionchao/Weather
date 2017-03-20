package com.kevin.zhangchao.weather;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

import com.kevin.zhangchao.weather.utils.SharedPreferenceUtil;
import com.kevin.zhangchao.weather.utils.ToastUtil;
import com.kevin.zhangchao.weather.utils.Util;

/**
 * Created by ZhangChao on 2017/3/20.
 */

class CheckVersion {

    public static void checkVersion(final Context context, boolean force) {
        RetrofitSingleton.getInstance().fetchVersion()
                .subscribe(new SimpleSubscriber<VersionAPI>() {
                    @Override
                    public void onNext(VersionAPI versionAPI) {
                        String firVersionName=versionAPI.versionShort;
                        //TODO 应该对比当前保存的版本号，跳过此版本时保存了版本号
                        String currentVersoinName= Util.getVersion(context);
                        if (currentVersoinName.compareTo(firVersionName)<0)
                        {
                            showUpdateDialog(versionAPI,context);
                        }else{
                            ToastUtil.showShort("已经是最新版本(⌐■_■)");
                        }
                    }
                });
    }


    private static void showUpdateDialog(final VersionAPI versionAPI, final Context context) {
        String title = "发现新版" + versionAPI.name + "版本号：" + versionAPI.versionShort;
        new AlertDialog.Builder(context).setTitle(title)
                .setMessage(versionAPI.changelog)
                .setPositiveButton("下载", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Uri uri= Uri.parse(versionAPI.updateUrl);
                        Intent intent=new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setData(uri);
                        context.startActivity(intent);
                    }
                }).setNegativeButton("跳过此版本", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferenceUtil.getInstance().putString("version",versionAPI.versionShort);
            }
        }).show();

    }
}
