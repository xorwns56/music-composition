package com.xorwns56.music_composition_app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;

import java.util.List;

public class NoteTableImageView extends AppCompatImageView {
    Paint startLinePaint = new Paint();
    Paint playLinePaint = new Paint();
    Paint noteLinePaint = new Paint();
    Paint fontPaint = new Paint();

    public NoteTableImageView(Context context) {
        super(context);
        init();
    }

    public NoteTableImageView(Context context, AttributeSet attr){
        super(context,attr);
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

        int drawPositionX = resources.getDrawPositionX();
        int drawPositionY = resources.getDrawPositionY();
        int noteWidth = setting.getNoteWidth();
        int noteHeight = setting.getNoteHeight();
        int noteType = setting.getNoteType();
        int rectHeight = noteHeight/2;
        int drawPosX = drawPositionX/(noteWidth/12);
        int drawPosY = drawPositionY/noteHeight;
        int gapX = drawPositionX%(noteWidth/12);
        int gapY = drawPositionY%noteHeight;
        int startLine = resources.getStartPosition();
        int playLine = resources.getPlayPosition();
        Selected selected = resources.getSelected();
        int maxHeight = trackList.size()*noteHeight-drawPositionY;
        if(maxHeight>getHeight()) maxHeight = getHeight();

        Drawable blueNoteDrawable = getResources().getDrawable(R.drawable.blue_note);
        Drawable grayNoteDrawable = getResources().getDrawable(R.drawable.gray_note);
        Drawable selectStroke = getResources().getDrawable(R.drawable.select_stroke);
        Drawable whiteStroke = getResources().getDrawable(R.drawable.white_stroke);
        Drawable blackStroke = getResources().getDrawable(R.drawable.black_stroke);

        for(int i=0-drawPositionX%(noteWidth/12*noteType);i<=getWidth();i+=noteWidth/12*noteType){
            canvas.drawLine(i,0,i,maxHeight,noteLinePaint);
        }
        for(int i=0-gapY;i<=maxHeight;i+=noteHeight){
            canvas.drawLine(0,i,getWidth(),i,noteLinePaint);
        }

        for(int i=drawPosY;i<(drawPositionY+maxHeight)/noteHeight+((drawPositionY+maxHeight)%noteHeight==0?0:1);i++){
            if(i>=trackList.size()) break;
            Track track = trackList.get(i);
            Drawable noteDrawable = track.getInstType()==-1?grayNoteDrawable:blueNoteDrawable;
            int top = (i-drawPosY)*noteHeight+noteHeight/2-rectHeight/2-gapY;
            int bottom = top + rectHeight;
            if(selected!=null&&i==selected.getY()&&selected.getSelectedNoteList().size()==0){
                int left = (selected.getX()-drawPosX)*(noteWidth/12) - gapX;
                int right = left + selected.getSize()*(noteWidth/12);
                if(left<getWidth()&&right>=0){
                    left = Math.max(left, -10);
                    right = Math.min(right, getWidth()+10);
                    whiteStroke.setBounds(left, top, right, bottom);
                    selectStroke.setBounds(left, top, right, bottom);
                    whiteStroke.draw(canvas);
                    selectStroke.draw(canvas);
                }
            }
            for(int j=drawPosX;j<=(drawPositionX+getWidth())/(noteWidth/12);){
                Note note = track.getNote(j);
                if(note!=null){
                    int left = (note.getStartIndex()-drawPosX)*(noteWidth/12) - gapX;
                    int right = left + note.getSize()*(noteWidth/12);
                    left = Math.max(left, -10);
                    right = Math.min(right, getWidth()+10);
                    noteDrawable.setBounds(left, top + (rectHeight-rectHeight*note.getVolume()/100), right, bottom);
                    blackStroke.setBounds(left, top, right, bottom);
                    noteDrawable.draw(canvas);
                    blackStroke.draw(canvas);
                    if(selected!=null&&i==selected.getY()&&selected.getSelectedNoteList().contains(note)) {
                        whiteStroke.setBounds(left, top, right, bottom);
                        selectStroke.setBounds(left, top, right, bottom);
                        whiteStroke.draw(canvas);
                        selectStroke.draw(canvas);
                    }
                    /*
                    if(track.getInstType()!=-1) {
                        int pitchStrIndex = note.getPitch() % 12;
                        int octave = note.getPitch() / 12;
                        if (size > 1) {
                            fontPaint.setTextSize(rectHeight*0.5f);
                            canvas.drawText(StrArray.PITCH[pitchStrIndex] + " " + octave, (note.getStartIndex()-drawPosX + size) * noteWidth - size*noteWidth/2 - gapX, noteHeight / 2 + fontPaint.getTextSize() / 2 - 5 + (i-drawPosY)*noteHeight - gapY, fontPaint);
                        } else {
                            fontPaint.setTextSize(rectHeight*0.35f);
                            canvas.drawText(StrArray.PITCH[pitchStrIndex], (note.getStartIndex()-drawPosX + size) * noteWidth - size*noteWidth/2 - gapX, noteHeight / 2 - 1 + (i-drawPosY)*noteHeight - gapY, fontPaint);
                            canvas.drawText(String.valueOf(octave), (note.getStartIndex()-drawPosX + size) * noteWidth - size*noteWidth/2 - gapX, noteHeight / 2 + fontPaint.getTextSize() - 1 + (i-drawPosY)*noteHeight - gapY, fontPaint);
                        }
                    }
                    */
                    j=note.getStartIndex()+note.getSize();
                }else{
                    j++;
                }
            }
        }

        if(startLine*(noteWidth/12)>=drawPositionX&&startLine*(noteWidth/12)<=drawPositionX + getWidth()) {
            canvas.drawLine(startLine*(noteWidth/12)-drawPositionX, 0, startLine*(noteWidth/12)-drawPositionX, maxHeight, startLinePaint);
        }

        if(playLine>=0&&playLine*(noteWidth/12)>=drawPositionX&&playLine*(noteWidth/12)<=drawPositionX + getWidth()){
            canvas.drawLine(playLine*(noteWidth/12)-drawPositionX, 0, playLine*(noteWidth/12)-drawPositionX, maxHeight, playLinePaint);
        }
    }

    public void init(){
        noteLinePaint.setColor(Color.parseColor("#cccccc"));
        noteLinePaint.setStrokeWidth(2);

        fontPaint.setAntiAlias(true);
        fontPaint.setColor(Color.BLACK);
        fontPaint.setTextAlign(Paint.Align.CENTER);

        startLinePaint.setColor(Color.parseColor("#55acee"));
        startLinePaint.setStrokeWidth(4f);
        startLinePaint.setAlpha(180);

        playLinePaint.setColor(Color.RED);
        playLinePaint.setStrokeWidth(4f);
        playLinePaint.setAlpha(180);
    }
}
