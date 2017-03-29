package com.kevin.zhangchao.weather.ui;

import android.app.AlertDialog;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
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

import org.w3c.dom.Text;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import rx.Observable;
import rx.functions.Func1;

/**
 * Created by ZhangChao on 2017/3/21.
 */

public class SettingFragment2 extends Fragment {

    private static String TAG = SettingFragment2.class.getSimpleName();
    //private SettingActivity mActivity;
    private SharedPreferenceUtil mSharedPreferenceUtil;

    @Bind(R.id.change_icons)
    LinearLayout mChangeIcons;
    @Bind(R.id.animation_start)
    CheckBox mAnimationOnOff;
    @Bind(R.id.day_night_model)
    SwitchCompat mDayNightModel;

    @Bind(R.id.change_update_time)
    LinearLayout mChangeUpdate;
    @Bind(R.id.notification_model)
    CheckBox mNotificationType;
    @Bind(R.id.clear_cache)
    LinearLayout mClearCache;

    @Bind(R.id.icon_name)
    TextView iconName;
    @Bind(R.id.cache_size)
    TextView cacheSize;
    @Bind(R.id.refreash_interval)
    TextView refreash_interval;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        ButterKnife.bind(this,view);
        ((SettingActivity)getActivity()).setSupportActionBar(toolbar);
        return view;

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (SharedPreferenceUtil.getInstance().getNotificationModel()!= Notification.FLAG_ONGOING_EVENT){
            mNotificationType.setChecked(false);
        }else{
            mNotificationType.setChecked(true);
        }
        mAnimationOnOff.setChecked(SharedPreferenceUtil.getInstance().getMainAnim());
        mSharedPreferenceUtil=SharedPreferenceUtil.getInstance();
        iconName.setText(getResources().getStringArray(R.array.icons)[mSharedPreferenceUtil.getIconType()]);
        refreash_interval.setText(
                mSharedPreferenceUtil.getAutoUpdate() == 0 ? "禁止刷新" : "每" + mSharedPreferenceUtil.getAutoUpdate() + "小时更新");
       cacheSize.setText(FileSizeUtil.getAutoFileOrFilesSize(BaseApplication.getAppCacheDir() + "/NetCache"));

        int model= SharedPreferenceUtil.getInstance().getInt(SharedPreferenceUtil.DYA_NIGHT_MODEL,1);
        if (model==1){
            mDayNightModel.setChecked(false);
        }else{
            mDayNightModel.setChecked(true);
        }

    }


    @OnCheckedChanged({R.id.day_night_model})
    public void onCheckedChangedClick2(SwitchCompat view){
        switch (view.getId()){
            case R.id.day_night_model:
                int model= SharedPreferenceUtil.getInstance().getInt(SharedPreferenceUtil.DYA_NIGHT_MODEL,1);
                if (model==1){
                    view.setChecked(false);
                    SharedPreferenceUtil.getInstance().putInt(SharedPreferenceUtil.DYA_NIGHT_MODEL,0);
                }else{
                    view.setChecked(true);
                    SharedPreferenceUtil.getInstance().putInt(SharedPreferenceUtil.DYA_NIGHT_MODEL,1);
                }

                ((SettingActivity)getActivity()).changeTheme();
                break;
        }
    }


    @OnCheckedChanged({R.id.animation_start,R.id.notification_model})
   public void onCheckedChangedClick(CheckBox view){
        switch (view.getId()){
            case R.id.animation_start:
                SharedPreferenceUtil.getInstance().setMainAnim((boolean)view.isChecked());
                break;
            case R.id.notification_model:
                SharedPreferenceUtil.getInstance().setNotificationModel(
                        (boolean)view.isChecked()?Notification.FLAG_ONGOING_EVENT:Notification.FLAG_AUTO_CANCEL
                );
                break;
            case R.id.day_night_model:
                ((SettingActivity)getActivity()).changeTheme();
                break;
        }
    }

    @OnClick({R.id.change_icons,R.id.change_update_time,R.id.clear_cache,R.id.toolbar})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.change_icons:
                showIconDialog();
                break;
            case R.id.change_update_time:
                showUpdateDialog();
                break;
            case R.id.clear_cache:
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
                                cacheSize.setText(FileSizeUtil.getAutoFileOrFilesSize(BaseApplication.getAppCacheDir() + "/NetCache"));
                                Snackbar.make(getView(),"缓存已清除",Snackbar.LENGTH_SHORT).show();
                            }
                        });
                break;
            case R.id.toolbar:
                Intent intent=new Intent(getActivity(),MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
        }
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
                iconName.setText(radioTypeOne.isChecked() ? iconsText[0] :
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
                refreash_interval.setText(
                        mSharedPreferenceUtil.getAutoUpdate() == 0 ? "禁止刷新" : "每" + mSharedPreferenceUtil.getAutoUpdate() + "小时更新"
                );
                //TODO 不需要先停止再启动吗？
                getActivity().startService(new Intent(getActivity(),AutoUpdateService.class));
                alertDialog.dismiss();

            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
