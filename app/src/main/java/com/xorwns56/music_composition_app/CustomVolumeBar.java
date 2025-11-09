package com.xorwns56.music_composition_app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;

/**
 * Created by xorwns56 on 2019-07-26.
 */

public class CustomVolumeBar extends AppCompatImageView{
    private int max = 100;
    private int progress = max;
    Paint fontPaint = new Paint();
    Paint barPaint = new Paint();
    Rect rect = new Rect();

    public CustomVolumeBar(Context context) {
        super(context);
        init();
    }

    public CustomVolumeBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(fontPaint.getTextSize()<canvas.getHeight()/1.5f) fontPaint.setTextSize(canvas.getHeight()/1.5f);
        canvas.drawRect(0,0,canvas.getWidth()*this.progress/this.max,canvas.getHeight(),barPaint);
        Drawable volume = getResources().getDrawable(R.drawable.volume);
        volume.setBounds(10,10,canvas.getHeight()-10,canvas.getHeight()-10);
        volume.draw(canvas);
        drawText(canvas,fontPaint,String.valueOf(this.progress));
    }

    private void drawText(Canvas canvas, Paint paint, String text) {
        paint.getTextBounds(text, 0, text.length(), rect);
        float x = canvas.getWidth() / 2f - rect.width() / 2f - rect.left;
        float y = canvas.getHeight() / 2f + rect.height() / 2f - rect.bottom;
        canvas.drawText(text, x, y, paint);
    }

    private void init(){
        fontPaint.setFakeBoldText(true);
        fontPaint.setAntiAlias(true);
        fontPaint.setColor(Color.parseColor("#f5ebeb"));
        fontPaint.setShadowLayer(5.0f, 10.0f, 10.0f, Color.BLACK);
        barPaint.setColor(Color.parseColor("#449def"));
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
