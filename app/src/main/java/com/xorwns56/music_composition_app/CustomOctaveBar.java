package com.xorwns56.music_composition_app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;

/**
 * Created by xorwns56 on 2019-10-06.
 */

public class CustomOctaveBar extends AppCompatImageView{
    private int max = 8;
    private int progress = 4;
    Paint fontPaint = new Paint();
    Paint bgPaint = new Paint();
    Rect rect = new Rect();

    public CustomOctaveBar(Context context) {
        super(context);
        init();
    }

    public CustomOctaveBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(fontPaint.getTextSize()<canvas.getHeight()/2f) fontPaint.setTextSize(canvas.getHeight()/2f);
        int progress = this.progress;
        fontPaint.getTextBounds("0",0,1,rect);
        float circleY = canvas.getHeight() / 2f;
        float textY = circleY + rect.height() / 2f - rect.bottom;
        float x;
        for(int i=0;i<=max;i++){
            x = canvas.getWidth() / (max+1) * i + canvas.getWidth()/((max+1)*2);
            if(i==progress){
                fontPaint.setColor(Color.parseColor("#f5ebeb"));
                bgPaint.setColor(Color.parseColor("#449def"));
            }
            canvas.drawCircle(x, circleY, circleY, bgPaint);
            canvas.drawText(String.valueOf(i),x,textY,fontPaint);
            if(i==progress){
                fontPaint.setColor(Color.parseColor("#cccccc"));
                bgPaint.setColor(Color.parseColor("#f5ebeb"));
            }
        }
    }

    private void init(){
        fontPaint.setFakeBoldText(true);
        fontPaint.setAntiAlias(true);
        fontPaint.setTextAlign(Paint.Align.CENTER);
        fontPaint.setColor(Color.parseColor("#cccccc"));
        bgPaint.setAntiAlias(true);
        bgPaint.setColor(Color.parseColor("#f5ebeb"));
    }

    public int getProgress(){
        return this.progress;
    }

    public void setProgress(int progress){
        if(progress<0) progress = 0;
        if(progress>this.max) progress = this.max;
        this.progress = progress;
        this.invalidate();
    }

    public int getMax(){
        return this.max;
    }
}
