package com.kevin.zhangchao.weather;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.kevin.zhangchao.weather.component.RxBus;
import com.kevin.zhangchao.weather.utils.PLog;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by ZhangChao on 2017/3/14.
 */

public class MainFragment extends Fragment {

    @Bind(R.id.recyclerview)
    RecyclerView mRecyclerView;
    @Bind(R.id.swiprefresh)
    SwipeRefreshLayout mRefreshLayout;
    @Bind(R.id.progressBar)
    ProgressBar mProgressBar;
    @Bind(R.id.iv_erro)
    ImageView mIvError;


    private View view;
    private boolean mIsCreateView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RxBus.getDefault().toObservable(ChangeCityEvent.class).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleSubscriber<ChangeCityEvent>(){
                    @Override
                    public void onNext(ChangeCityEvent changeCityEvent) {
                        if (mRefreshLayout!=null){
                            mRefreshLayout.setRefreshing(true);
                        }
                        load();
                        PLog.d("MainRxBus");
                    }
                });

    }

    public void load(){

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view==null){
            view=inflater.inflate(R.layout.fragment_main,container,false);
            ButterKnife.bind(this,view);
        }
        mIsCreateView=true;
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


}
