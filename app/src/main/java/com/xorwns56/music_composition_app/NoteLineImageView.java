package com.xorwns56.music_composition_app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;

import java.util.List;

public class NoteLineImageView extends AppCompatImageView {
    Paint rectPaint = new Paint();
    Paint playLinePaint = new Paint();
    Paint noteLinePaint = new Paint();

    public NoteLineImageView(Context context) {
        super(context);
        init();
    }

    public NoteLineImageView(Context context, AttributeSet attr){
        super(context,attr);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Resources resources = (Resources)this.getTag(R.id.resources);
        Setting setting = (Setting)this.getTag(R.id.setting);
        if(resources==null||setting==null) return;
        int noteWidth = setting.getNoteWidth();
        int noteType = setting.getNoteType();
        int drawPosition = resources.getDrawPositionX();
        int gap = drawPosition%(noteWidth/12*noteType);
        int startLine = resources.getStartPosition();
        int playLine = resources.getPlayPosition();
        int tempoOver = resources.getTempoOver();
        int tempoUnder = resources.getTempoUnder();
        int subCycle = 8/tempoUnder*noteWidth;
        int cycle = subCycle*tempoOver;
        Selected selected = resources.getSelected();

        Drawable triangleDrawable = getResources().getDrawable(R.drawable.triangle);

        for(int i=0-gap;i<=getWidth();i+=noteWidth/12*noteType){
            if((drawPosition+i)%cycle==0){
                canvas.drawLine(i, getHeight() * 0.6f, i, getHeight(), noteLinePaint);
                canvas.drawText(String.valueOf((drawPosition+i)/cycle + 1), i + 5, getHeight()*0.6f - 10, noteLinePaint);
            }else if((drawPosition+i)%subCycle==0){
                canvas.drawLine(i, getHeight() * 0.8f, i, getHeight(), noteLinePaint);
                if(cycle>=getWidth()) canvas.drawText(String.valueOf((drawPosition+i)/cycle + 1) + "." + String.valueOf((drawPosition+i)%cycle/subCycle),i+5,getHeight()*0.75f-3,noteLinePaint);
            }else if((drawPosition+i)%noteWidth==0){
                canvas.drawLine(i, getHeight() * 0.9f, i, getHeight(), noteLinePaint);
            }else{
                canvas.drawLine(i, getHeight() * 0.95f, i, getHeight(), noteLinePaint);
            }
        }
        if(startLine*(noteWidth/12)>=drawPosition-gap&&startLine*(noteWidth/12)<=drawPosition + getWidth()) {
            triangleDrawable.setBounds(startLine*(noteWidth/12)-drawPosition,(int)Math.round(getHeight()*0.6),(startLine+(int)Math.round(noteType*0.8))*(noteWidth/12)-drawPosition,getHeight());
            triangleDrawable.setAlpha(180);
            triangleDrawable.draw(canvas);
        }
        if(playLine>=0&&playLine*(noteWidth/12)>=drawPosition&&playLine*(noteWidth/12)<=drawPosition + getWidth()){
            canvas.drawLine(playLine*(noteWidth/12)-drawPosition, getHeight()*0.6f, playLine*(noteWidth/12)-drawPosition, getHeight(), playLinePaint);
        }
        if(selected!=null) {
            List<Note> selectedNoteList = selected.getSelectedNoteList();
            if (selectedNoteList.size() == 0) {
                int left = (selected.getX() - drawPosition/(noteWidth/12))*(noteWidth/12) - drawPosition%(noteWidth/12);
                int right = left + selected.getSize()*(noteWidth/12);
                if(left<getWidth()&&right>=0){
                    left = Math.max(left, -10);
                    right = Math.min(right, getWidth()+10);
                    canvas.drawRect(left,0,right,getHeight(),rectPaint);
                }
            } else {
                int left = (selectedNoteList.get(0).getStartIndex() - drawPosition/(noteWidth/12))*(noteWidth/12) - drawPosition%(noteWidth/12);
                Note lastNote = selectedNoteList.get(selectedNoteList.size()-1);
                int right = (lastNote.getStartIndex()+lastNote.getSize() - drawPosition/(noteWidth/12))*(noteWidth/12) - drawPosition%(noteWidth/12);
                if(left<getWidth()&&right>=0) {
                    for (Note note : selectedNoteList) {
                        left = (note.getStartIndex() - drawPosition / (noteWidth / 12)) * (noteWidth / 12) - drawPosition % (noteWidth / 12);
                        right = left + note.getSize() * (noteWidth / 12);
                        if (left < getWidth() && right >= 0) {
                            left = Math.max(left, -10);
                            right = Math.min(right, getWidth() + 10);
                            canvas.drawRect(left, 0, right, getHeight(), rectPaint);
                        }
                    }
                }
            }
        }
    }

    public void init(){
        rectPaint.setColor(Color.BLACK);
        rectPaint.setAlpha(30);

        playLinePaint.setColor(Color.RED);
        playLinePaint.setStrokeWidth(4f);
        playLinePaint.setAlpha(180);

        noteLinePaint.setAntiAlias(true);
        noteLinePaint.setColor(Color.parseColor("#f5ebeb"));
        noteLinePaint.setStrokeWidth(1.2f);
        noteLinePaint.setTextSize(20);
    }
}
