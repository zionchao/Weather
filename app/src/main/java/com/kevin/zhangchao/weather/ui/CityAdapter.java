package com.kevin.zhangchao.weather.ui;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kevin.zhangchao.weather.R;

import java.util.ArrayList;

import butterknife.Bind;

/**
 * Created by ZhangChao on 2017/3/19.
 */

class CityAdapter extends RecyclerView.Adapter<CityAdapter.CityViewHolder>{

    private final Context mContext;
    private final ArrayList<String> mDataList;
    private OnRecyclerViewItemClickListener mOnItemClickListener;

    CityAdapter(Context context, ArrayList<String> dataList){
        this.mContext=context;
        this.mDataList=dataList;
    }

    @Override
    public CityViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CityViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_city, parent, false));
    }

    @Override
    public void onBindViewHolder(CityViewHolder holder, final int position) {
        holder.bind(mDataList.get(position));
        holder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onItemClick(v,position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    class CityViewHolder extends BaseViewHolder<String>{

        @Bind(R.id.item_city)
        TextView mItemCity;
        @Bind(R.id.cardView)
        CardView mCardView;
        public CityViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void bind(String s) {
            mItemCity.setText(s);
        }
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener){
        this.mOnItemClickListener=listener;
    }

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, int pos);
    }
}
