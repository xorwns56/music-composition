package com.xorwns56.music_composition_app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;

import java.util.List;

public class InstrumentImageView extends AppCompatImageView {
    Paint rectPaint = new Paint();
    Paint fontPaint = new Paint();
    Paint fontPaint2 = new Paint();

    public InstrumentImageView(Context context) {
        super(context);
        init();
    }

    public InstrumentImageView(Context context, AttributeSet attr) {
        super(context, attr);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Resources resources = (Resources)this.getTag(R.id.resources);
        Setting setting = (Setting)this.getTag(R.id.setting);
        if(resources==null||setting==null) return;
        List<Track> trackList = resources.getTrackList();
        if(trackList.size()==0) return;
        int noteHeight = setting.getNoteHeight();
        int drawPosition = resources.getDrawPositionY();
        int drawPos = drawPosition/noteHeight;
        int gap = drawPosition%noteHeight;
        int selected = resources.getSelectedTrack();
        int wholeHeight = trackList.size()*noteHeight;
        int maxHeight = wholeHeight-drawPosition;
        if(maxHeight>getHeight()) maxHeight = getHeight();
        if(fontPaint.getTextSize()>getWidth()/13f||fontPaint.getTextSize()<getWidth()/13f) fontPaint.setTextSize(getWidth()/13f);

        Drawable d = getResources().getDrawable(R.drawable.blue_track_title,null);
        for(int i=drawPos;i<(drawPosition+maxHeight)/noteHeight+((drawPosition+maxHeight)%noteHeight==0?0:1);i++) {
            if(i>=trackList.size()) break;
            Track track = trackList.get(i);
            d.setBounds(0,(i-drawPos)*noteHeight-gap+(int)(0.1f*noteHeight), getWidth(),(i-drawPos)*noteHeight+noteHeight- gap - (int)(0.1f*noteHeight));
            d.draw(canvas);
            if(selected==i){
                Drawable yellowStroke = getResources().getDrawable(R.drawable.cyan_stroke);
                yellowStroke.setBounds(0,(i-drawPos)*noteHeight-gap+(int)(0.1f*noteHeight), getWidth(),(i-drawPos)*noteHeight+noteHeight- gap - (int)(0.1f*noteHeight));
                yellowStroke.draw(canvas);
            }
            canvas.drawText("Track "+(i+1), 5, (i-drawPos)*noteHeight+noteHeight*0.25f- gap, fontPaint2);
            int instType = track.getInstType();
            canvas.drawText((instType+1)+". "+StrArray.INST_GROUP[instType<0?0:instType/8+1][instType<0?0:instType%8], getWidth()/2, (i-drawPos)*noteHeight+noteHeight/2 + fontPaint.getTextSize()/3 - gap, fontPaint);
        }
        if(wholeHeight>getHeight()) {
            float scrollBarSize = (float)getHeight()/wholeHeight*getHeight();
            float scrollBarPos = (float) drawPosition / (wholeHeight - getHeight()) * (getHeight() - scrollBarSize) ;
            canvas.drawRect(getWidth()*0.95f, scrollBarPos, getWidth(), scrollBarSize + scrollBarPos, rectPaint);
        }

    }

    public void init(){
        rectPaint.setColor(Color.BLACK);
        rectPaint.setAlpha(100);

        fontPaint.setFakeBoldText(true);
        fontPaint.setAntiAlias(true);
        fontPaint.setColor(Color.parseColor("#f5ebeb"));
        fontPaint.setTextAlign(Paint.Align.CENTER);
        fontPaint.setShadowLayer(5.0f, 10.0f, 10.0f, Color.BLACK);

        fontPaint2.setFakeBoldText(true);
        fontPaint2.setAntiAlias(true);
        fontPaint2.setColor(Color.parseColor("#3b577d"));
        fontPaint2.setAlpha(150);
        fontPaint2.setTextSize(18f);
    }
}
