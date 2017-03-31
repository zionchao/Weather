package com.kevin.zhangchao.weather.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by zhangchao_a on 2017/3/31.
 * 简单仪表盘面板绘制
 */

public class MyPanel extends View {

    public int start_angel=150;

    public int during=240;

    public Paint paintCircle;
    public Paint paintArc;
    public Paint paintText;
    private int mWidth;
    private int mHeight;

    public MyPanel(Context context) {
        this(context,null);
    }

    public MyPanel(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,-1);
    }

    public MyPanel(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);

    }

    private void init(Context context) {
        paintCircle=new Paint(Paint.ANTI_ALIAS_FLAG);
        paintCircle.setColor(Color.GRAY);

        paintArc=new Paint(Paint.ANTI_ALIAS_FLAG);
        paintArc.setColor(Color.RED);
        paintArc.setStyle(Paint.Style.STROKE);
        paintText=new Paint(Paint.ANTI_ALIAS_FLAG);
        paintText.setColor(Color.BLACK);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSize=MeasureSpec.getSize(widthMeasureSpec);
        int widthMode= MeasureSpec.getMode(widthMeasureSpec);
        int heightSize=MeasureSpec.getSize(heightMeasureSpec);
        int heightMode=MeasureSpec.getMode(heightMeasureSpec);
        int width,height;
        if (widthMode==MeasureSpec.EXACTLY){
            width=widthSize;
        }else{
            width=300;
        }
        if (heightMode==MeasureSpec.EXACTLY){
            height=heightSize;
        }else{
            height=300;
        }
        setMeasuredDimension(300,300);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth=getWidth();
        mHeight=getHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(mWidth/2,mHeight/2);
        drawArc(canvas);
        drawPanel(canvas);
        drawText(canvas);
    }

    private void drawArc(Canvas canvas) {
        RectF rectF=new RectF(-mWidth/2,-mHeight/2,mWidth/2,mHeight/2);
        canvas.drawArc(rectF,start_angel,during,false,paintArc);
    }

    /**
     * 可以先画图，然后再更加画出的效果和实际的效果实际的差距进行旋转
     * @param canvas
     */
    private void drawPanel(Canvas canvas) {
        canvas.save();
        canvas.rotate(-(180-start_angel+90),0,0);
        float step=during/(31*1.0f);
        for (int i=0;i<32;i++)
        {
            canvas.save();
            canvas.rotate(step*i,0,0);
            canvas.drawLine(0,-mHeight/2+5,0,-mHeight/2+15,paintText);
            if (i%5==0)
            {
                String text=i+"";
                float length=paintText.measureText(text);
                canvas.drawText(i+"",-length/2,-mHeight/2+20,paintText);
            }
            canvas.restore();
        }
        canvas.restore();

    }

    private void drawText(Canvas canvas) {
        String text="i am have finished";
        float textWidth=paintText.measureText(text);
        canvas.drawText(text,-textWidth/2,mHeight/2-20,paintText);
    }
}
