package com.kevin.zhangchao.weather.customview;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

import com.kevin.zhangchao.weather.R;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by zhangchao_a on 3017/3/30.
 * 太阳
 */

public class SunView extends View {

    private float screenWidth;
    private float screenHeight;

    private Bitmap bitmap_bg;
    private Bitmap bitmap_suns[]=new Bitmap[5];
    private static Random random=new Random();

    private int SUN_COUNT=5;

    private ArrayList<Sun> sunflake_xxl=new ArrayList<>();
    private ArrayList<Sun> sunflake_xl=new ArrayList<>();
    private ArrayList<Sun> sunflake_l=new ArrayList<>();
    private ArrayList<Sun> sunflake_m=new ArrayList<>();
    private ArrayList<Sun> sunflake_s=new ArrayList<>();

    public SunView(Context context) {
        super(context);
        init(context);
    }

    public SunView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);

    }

    public SunView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context){
        getViewSize(context);
        Resources resources=getResources();
        bitmap_bg= BitmapFactory.decodeResource(resources, R.mipmap.yjjc_h_a1);
        bitmap_suns[0]= BitmapFactory.decodeResource(getResources(),
                R.mipmap.yjjc_h_a2);
        bitmap_suns[1] = BitmapFactory.decodeResource(getResources(),
                R.mipmap.yjjc_h_a3);
        bitmap_suns[2] = BitmapFactory.decodeResource(getResources(),
                R.mipmap.yjjc_h_a4);
        bitmap_suns[3] = BitmapFactory.decodeResource(getResources(),
                R.mipmap.yjjc_h_a5);
        bitmap_suns[4] = BitmapFactory.decodeResource(getResources(),
                R.mipmap.yjjc_h_a6);
        addRandomSnow();
    }

    private void addRandomSnow() {
        for (int i=0;i<SUN_COUNT;i++){
            sunflake_xxl.add(new Sun(
                    bitmap_suns[4],-10,
                    50+random.nextFloat()*10,7f,1-random.nextFloat()*2));
            sunflake_xl.add(new Sun(
                     bitmap_suns[3],-5,
                    50+random.nextFloat()*10,5f,1-random.nextFloat()*2
            ));
            sunflake_m.add(new Sun(bitmap_suns[2], 5, 50+random.nextFloat() * 10, 3f,
                    1 - random.nextFloat() * 2));
            sunflake_s.add(new Sun(bitmap_suns[1], 10, 50+random.nextFloat() * 10, 2f,
                    1 - random.nextFloat() * 2));
            sunflake_l.add(new Sun(bitmap_suns[0], 15, 50+random.nextFloat() * 10, 2f,
                    1 - random.nextFloat() * 2));
        }
    }


    private void getViewSize(Context context) {
        DisplayMetrics metrics=new DisplayMetrics();
        WindowManager windowManager= (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        this.screenWidth=metrics.widthPixels;
        this.screenHeight=metrics.heightPixels;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        setMeasuredDimension((int)screenWidth,(int)screenHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint=new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        RectF rectF=new RectF(0,0,screenWidth,screenHeight);
        canvas.drawBitmap(bitmap_bg,null,rectF,paint);
        Sun sun=null;
        for (int i=0;i<SUN_COUNT;i++){
            sun=sunflake_xxl.get(i);
            canvas.drawBitmap(sun.bitmap,sun.x,sun.y,paint);
            moveClode(sun);
            sun = sunflake_xl.get(i);
            canvas.drawBitmap(sun.bitmap, sun.x, sun.y, paint);
            moveClode(sun);

            sun =sunflake_m.get(i);
            canvas.drawBitmap(sun.bitmap, sun.x, sun.y, paint);
            moveClode(sun);

            sun = sunflake_s.get(i);
            canvas.drawBitmap(sun.bitmap, sun.x, sun.y, paint);
            moveClode(sun);

            sun = sunflake_l.get(i);
            canvas.drawBitmap(sun.bitmap, sun.x, sun.y, paint);
            moveClode(sun);

        }

        invalidate();
    }

    private void moveClode(Sun sun) {
        if (sun.x>screenWidth||sun.y>screenHeight){
//            sun.y=0;
            sun.x=-50;
        }
        sun.x+=1;
//        sun.y+=sun.speed;
    }

    public  class Sun{
        private final float y;
        /*
             * 雪花的图片
             */
        Bitmap bitmap;

        /**
         * 雪花开始飘落的横坐标
         */
        float x;

        /**
         * 雪花下落的速度
         */
        float speed;

        /**
         * 雪花下落时偏移的值
         */
        float offset;

        public Sun(Bitmap bitmap, float x, float y,float speed, float offset) {
            this.bitmap = bitmap;
            this.x = x;
            this.y=y;
            this.speed = speed;
            this.offset = offset;
        }
    }
}
