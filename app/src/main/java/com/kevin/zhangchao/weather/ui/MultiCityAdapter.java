package com.kevin.zhangchao.weather.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.kevin.zhangchao.weather.R;
import com.kevin.zhangchao.weather.entity.Weather;
import com.kevin.zhangchao.weather.utils.CardCityUIHelper;
import com.kevin.zhangchao.weather.utils.PLog;
import com.kevin.zhangchao.weather.utils.SharedPreferenceUtil;
import com.kevin.zhangchao.weather.utils.Util;

import java.util.List;

import butterknife.Bind;

/**
 * Created by ZhangChao on 2017/3/19.
 */

public class MultiCityAdapter extends RecyclerView.Adapter<MultiCityAdapter.MultiCityViewHolder> {

    private Context mContext;
    private List<Weather> mWeatherList;
    private onMultiCityLongClick onMultiCityLongClick=null;


    public void setOnMultiCityLongClick(onMultiCityLongClick ponMultiCityLongClick){
        this.onMultiCityLongClick=ponMultiCityLongClick;

    }
    public MultiCityAdapter(List<Weather> weatherList) {
        mWeatherList=weatherList;
    }

    @Override
    public MultiCityViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
       mContext=parent.getContext();
        return new MultiCityViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_multicity,parent,false));

    }

    @Override
    public void onBindViewHolder(final MultiCityViewHolder holder, int position) {
        holder.bind(mWeatherList.get(position));
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onMultiCityLongClick.longClick(
                        mWeatherList.get(holder.getAdapterPosition()).basic.city
                );
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mWeatherList.size();
    }

    public boolean isEmpty(){
        return 0==mWeatherList.size();
    }

    class MultiCityViewHolder extends BaseViewHolder<Weather>{
        @Bind(R.id.dialog_city)
        TextView mDialogCity;
        @Bind(R.id.dialog_icon)
        ImageView mDialogIcon;
        @Bind(R.id.dialog_temp)
        TextView mDialogTemp;
        @Bind(R.id.cardView)
        CardView mCardView;

        public MultiCityViewHolder(View itemView) {
            super(itemView);
        }
        @Override
        protected void bind(Weather weather) {
            try {
                mDialogCity.setText(Util.safeText(weather.basic.city));
                mDialogTemp.setText(String.format("%s℃", weather.now.tmp));
            } catch (NullPointerException e) {
                PLog.e(e.getMessage());
            }

            Glide.with(mContext)
                    .load(SharedPreferenceUtil.getInstance().getInt(weather.now.cond.txt, R.mipmap.none
                    ))
                    .asBitmap()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            mDialogIcon.setImageBitmap(resource);
                            mDialogIcon.setColorFilter(Color.WHITE);
                        }
                    });

            int code = Integer.valueOf(weather.now.cond.code);
            CardCityUIHelper cardCityUIHelper = new CardCityUIHelper();
            cardCityUIHelper.applyStatus(code, weather.basic.city, mCardView);

            PLog.d(weather.now.cond.txt + " " + weather.now.cond.code);
        }
    }

    public interface onMultiCityLongClick {
        void longClick(String city);
    }

}
