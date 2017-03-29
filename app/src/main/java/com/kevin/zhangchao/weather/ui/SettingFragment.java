package com.kevin.zhangchao.weather.ui;

import android.app.AlertDialog;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.kevin.zhangchao.weather.R;
import com.kevin.zhangchao.weather.utils.ChangeCityEvent;
import com.kevin.zhangchao.weather.utils.FileSizeUtil;
import com.kevin.zhangchao.weather.utils.FileUtil;
import com.kevin.zhangchao.weather.utils.ImageLoader;
import com.kevin.zhangchao.weather.utils.RxBus;
import com.kevin.zhangchao.weather.utils.RxUtils;
import com.kevin.zhangchao.weather.utils.SharedPreferenceUtil;
import com.kevin.zhangchao.weather.utils.SimpleSubscriber;

import java.io.File;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by ZhangChao on 2017/3/21.
 */

public class SettingFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener{

    private static String TAG = SettingFragment.class.getSimpleName();
    //private SettingActivity mActivity;
    private SharedPreferenceUtil mSharedPreferenceUtil;
    private Preference mChangeIcons;
    private Preference mChangeUpdate;
    private Preference mClearCache;
    private CheckBoxPreference mNotificationType;
    private CheckBoxPreference mAnimationOnOff;
    private SwitchPreference mDayNightModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.setting);

        mSharedPreferenceUtil=SharedPreferenceUtil.getInstance();
        mChangeIcons=findPreference(SharedPreferenceUtil.CHANGE_ICONS);
        mChangeUpdate=findPreference(SharedPreferenceUtil.AUTO_UPDATE);
        mClearCache=findPreference(SharedPreferenceUtil.CLEAR_CACHE);

        mAnimationOnOff= (CheckBoxPreference) findPreference(SharedPreferenceUtil.ANIM_START);
        mNotificationType= (CheckBoxPreference) findPreference(SharedPreferenceUtil.NOTIFICATION_MODEL);
        mDayNightModel= (SwitchPreference) findPreference(SharedPreferenceUtil.DYA_NIGHT_MODEL);

        if (SharedPreferenceUtil.getInstance().getNotificationModel()!= Notification.FLAG_ONGOING_EVENT){
            mNotificationType.setChecked(false);
        }else{
            mNotificationType.setChecked(true);
        }
        mAnimationOnOff.setChecked(SharedPreferenceUtil.getInstance().getMainAnim());

        mChangeIcons.setSummary(getResources().getStringArray(R.array.icons)[mSharedPreferenceUtil.getIconType()]);
        mChangeUpdate.setSummary(
                mSharedPreferenceUtil.getAutoUpdate() == 0 ? "禁止刷新" : "每" + mSharedPreferenceUtil.getAutoUpdate() + "小时更新");
        mClearCache.setSummary(FileSizeUtil.getAutoFileOrFilesSize(BaseApplication.getAppCacheDir() + "/NetCache"));

        mChangeIcons.setOnPreferenceClickListener(this);
        mChangeUpdate.setOnPreferenceClickListener(this);
        mClearCache.setOnPreferenceClickListener(this);

        mNotificationType.setOnPreferenceChangeListener(this);
        mAnimationOnOff.setOnPreferenceChangeListener(this);
        mDayNightModel.setOnPreferenceChangeListener(this);
    }


    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference==mAnimationOnOff){
            SharedPreferenceUtil.getInstance().setMainAnim((boolean)newValue);
        }else if (mNotificationType==preference){
            SharedPreferenceUtil.getInstance().setNotificationModel(
                    (boolean)newValue?Notification.FLAG_ONGOING_EVENT:Notification.FLAG_AUTO_CANCEL
            );
        }else if (mDayNightModel==preference){
            ((SettingActivity)getActivity()).changeTheme();
        }
        return true;
    }


    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (mChangeIcons==preference){
            showIconDialog();
        }else if (mClearCache==preference){
            ImageLoader.clear(getActivity());
            Observable.just(FileUtil.delete(new File(BaseApplication.getAppCacheDir()+ "/NetCache")))
                    .filter(new Func1<Boolean, Boolean>() {
                        @Override
                        public Boolean call(Boolean aBoolean) {
                            return aBoolean;
                        }
                    }).compose(RxUtils.<Boolean>rxSchedulerHelper())
                    .subscribe(new SimpleSubscriber<Boolean>() {
                        @Override
                        public void onNext(Boolean aBoolean) {
                            mClearCache.setSummary(FileSizeUtil.getAutoFileOrFilesSize(BaseApplication.getAppCacheDir() + "/NetCache"));
                            Snackbar.make(getView(),"缓存已清除",Snackbar.LENGTH_SHORT).show();
                        }
                    });
        }else if (mChangeUpdate==preference){
            showUpdateDialog();
        }
        return true;
    }

    private void showIconDialog() {
        LayoutInflater inflater= (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogLayout=inflater.inflate(R.layout.icon_dialog, (ViewGroup) getActivity().findViewById(R.id.dialog_root));
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity())
                .setView(dialogLayout);
        final  AlertDialog alertDialog=builder.create();
        LinearLayout layoutTypeOne= (LinearLayout) dialogLayout.findViewById(R.id.layout_one);
        layoutTypeOne.setClickable(true);
        LinearLayout layoutTypeTwo = (LinearLayout) dialogLayout.findViewById(R.id.layout_two);
        layoutTypeTwo.setClickable(true);
        final RadioButton radioTypeOne = (RadioButton) dialogLayout.findViewById(R.id.radio_one);
        final RadioButton radioTypeTwo = (RadioButton) dialogLayout.findViewById(R.id.radio_two);
        TextView done = (TextView) dialogLayout.findViewById(R.id.done);

        radioTypeOne.setClickable(false);
        radioTypeTwo.setClickable(false);

        alertDialog.show();

        switch (mSharedPreferenceUtil.getIconType()){
            case 0:
                radioTypeOne.setChecked(true);
                radioTypeTwo.setChecked(false);
                break;
            case 1:
                radioTypeOne.setChecked(false);
                radioTypeTwo.setChecked(true);
                break;
        }

        layoutTypeOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioTypeOne.setChecked(true);
                radioTypeTwo.setChecked(false);
            }
        });

        layoutTypeTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioTypeOne.setChecked(false);
                radioTypeTwo.setChecked(true);
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSharedPreferenceUtil.setIconType(radioTypeOne.isChecked()?0:1);
                String[] iconsText=getResources().getStringArray(R.array.icons);
                mChangeIcons.setSummary(radioTypeOne.isChecked() ? iconsText[0] :
                        iconsText[1]);
                alertDialog.dismiss();

                Snackbar.make(getView(),"切换成功，重启应用生效", BaseTransientBottomBar.LENGTH_INDEFINITE)
                        .setAction("重启", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent=new Intent(getActivity(),MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                getActivity().startActivity(intent);
                                getActivity().finish();
                                RxBus.getDefault().post(new ChangeCityEvent());
                            }
                        }).show();

            }
        });

    }

    private void showUpdateDialog() {
        LayoutInflater inflater= (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogLayout=inflater.inflate(R.layout.update_dialog,null);
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity())
                .setView(dialogLayout);
        final AlertDialog alertDialog=builder.create();
        final SeekBar mSeekBar= (SeekBar) dialogLayout.findViewById(R.id.time_seekbar);
        final TextView tvShowHour= (TextView) dialogLayout.findViewById(R.id.tv_showhour);
        TextView tvDown= (TextView) dialogLayout.findViewById(R.id.done);
        mSeekBar.setMax(24);
        mSeekBar.setProgress(mSharedPreferenceUtil.getAutoUpdate());
        tvShowHour.setText(String.format("每%s小时",mSeekBar.getProgress()));
        alertDialog.show();

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvShowHour.setText(String.format("每%s小时",mSeekBar.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        tvDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSharedPreferenceUtil.setAutoUpdate(mSeekBar.getProgress());
                mChangeUpdate.setSummary(
                        mSharedPreferenceUtil.getAutoUpdate() == 0 ? "禁止刷新" : "每" + mSharedPreferenceUtil.getAutoUpdate() + "小时更新"
                );
                //TODO 不需要先停止再启动吗？
                getActivity().startService(new Intent(getActivity(),AutoUpdateService.class));
                alertDialog.dismiss();

            }
        });
    }
}
