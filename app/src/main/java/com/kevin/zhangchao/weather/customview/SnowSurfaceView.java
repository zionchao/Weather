package com.kevin.zhangchao.weather.customview;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.kevin.zhangchao.weather.R;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CancellationException;

/**
 * Created by zhangchao_a on 2017/3/30.
 */

public class SnowSurfaceView extends SurfaceView implements SurfaceHolder.Callback,Runnable{

    private Bitmap bitmap_snows[]=new Bitmap[5];
    private Bitmap bitmap_bg;
    private Thread thread;

    private boolean IsRunning=true;
    private int left;
    private int top;
    private float screenWidth;
    private float screenHeight;
    private boolean floag=true;

    private static Random random=new Random();

    private int dx=1;
    private int dy=3;

    private int sleepTime;
    private int offset=0;

    private ArrayList<Snow> snowflake_xxl=new ArrayList<>();
    private ArrayList<Snow> snowflake_xl=new ArrayList<>();
    private ArrayList<Snow> snowflake_l=new ArrayList<>();
    private ArrayList<Snow> snowflake_m=new ArrayList<>();
    private ArrayList<Snow> snowflake_s=new ArrayList<>();

    private SurfaceHolder holder;

    private int SNOW_COUNT=20;

    public SnowSurfaceView(Context context) {
        super(context);
        this.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        ));
        holder=getHolder();
        holder.addCallback(this);
        holder.setFormat(PixelFormat.RGBA_8888);
        getViewSize(context);
        loadSnowImage();
        addRandomSnow();
    }

    private void getViewSize(Context context) {
        DisplayMetrics metrics=new DisplayMetrics();
        WindowManager windowManager= (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        this.screenWidth=metrics.widthPixels;
        this.screenHeight=metrics.heightPixels;
    }

    private void loadSnowImage() {
        bitmap_snows[0]= BitmapFactory.decodeResource(getResources(),
                R.mipmap.snowflake_l);
        bitmap_snows[1] = BitmapFactory.decodeResource(getResources(),
                R.mipmap.snowflake_s);
        bitmap_snows[2] = BitmapFactory.decodeResource(getResources(),
                R.mipmap.snowflake_m);
        bitmap_snows[3] = BitmapFactory.decodeResource(getResources(),
                R.mipmap.snowflake_xl);
        bitmap_snows[4] = BitmapFactory.decodeResource(getResources(),
                R.mipmap.snowflake_xxl);
        bitmap_bg = BitmapFactory.decodeResource(getResources(),
                R.mipmap.bg14_day_snow);
    }

    private void addRandomSnow() {
        for (int i=0;i<SNOW_COUNT;i++){
            snowflake_xxl.add(new Snow(
                    bitmap_snows[4],random.nextFloat()*screenWidth,
                    random.nextFloat()*screenHeight,7f,1-random.nextFloat()*2));
            snowflake_xl.add(new Snow(
                    bitmap_snows[3],random.nextFloat()*screenWidth,
                    random.nextFloat()*screenHeight,5f,1-random.nextFloat()*2
            ));
            snowflake_m.add(new Snow(bitmap_snows[2], random.nextFloat()
                    * screenWidth, random.nextFloat() * screenHeight, 3f,
                    1 - random.nextFloat() * 2));
            snowflake_s.add(new Snow(bitmap_snows[1], random.nextFloat()
                    * screenWidth, random.nextFloat() * screenHeight, 2f,
                    1 - random.nextFloat() * 2));
            snowflake_l.add(new Snow(bitmap_snows[0], random.nextFloat()
                    * screenWidth, random.nextFloat() * screenHeight, 2f,
                    1 - random.nextFloat() * 2));
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread=new Thread(this);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode==KeyEvent.KEYCODE_BACK){
            IsRunning=false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        IsRunning=false;
    }

    @Override
    public void run() {
        while(IsRunning){
            Canvas canvas=null;
            synchronized (this){
                try {
                    canvas=holder.lockCanvas();
                    if (canvas!=null){
                        drawSnow(canvas);
                        Snow snow;
                        for (int i=0;i<SNOW_COUNT;i++){
                            snow=snowflake_xxl.get(i);
                            SnowDown(snow);
                            snow=snowflake_xl.get(i);
                            SnowDown(snow);
                            snow = snowflake_m.get(i);
                            SnowDown(snow);
                            snow = snowflake_s.get(i);
                            SnowDown(snow);
                            snow = snowflake_l.get(i);
                            SnowDown(snow);
                        }
                        Thread.sleep(100);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if (canvas!=null){
                        holder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }
    }

    private void SnowDown(Snow snow) {
        if (snow.x>screenWidth||snow.y>screenHeight){
            snow.y=0;
            snow.x=random.nextFloat()*screenWidth;
        }
        snow.x+=snow.offset;
        snow.y+=snow.speed;
    }

    private void drawSnow(Canvas canvas) {
        Paint paint=new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        RectF rectF=new RectF(0,0,screenWidth,screenHeight);
        canvas.drawBitmap(bitmap_bg,null,rectF,paint);
        Snow snow=null;
        for (int i=0;i<SNOW_COUNT;i++){
            snow=snowflake_xxl.get(i);
            canvas.drawBitmap(snow.bitmap,snow.x,snow.y,paint);
            snow = snowflake_xl.get(i);
            canvas.drawBitmap(snow.bitmap, snow.x, snow.y, paint);
            snow = snowflake_m.get(i);
            canvas.drawBitmap(snow.bitmap, snow.x, snow.y, paint);
            snow = snowflake_s.get(i);
            canvas.drawBitmap(snow.bitmap, snow.x, snow.y, paint);
            snow = snowflake_l.get(i);
            canvas.drawBitmap(snow.bitmap, snow.x, snow.y, paint);;
        }
    }
}
