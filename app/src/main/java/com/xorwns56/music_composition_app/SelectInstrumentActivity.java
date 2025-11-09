package com.xorwns56.music_composition_app;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.StateListDrawable;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SelectInstrumentActivity extends AppCompatActivity {
    MediaManager mediaManager;
    RadioGroup instSelectPanel;
    Animation blinkAnim;
    TextView messagePanel;
    int selectedTrack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_instrument);

        mediaManager = new MediaManager(getFilesDir().getAbsolutePath() + File.separator);

        Intent intent = getIntent();
        ArrayList<Integer> usedInstList = intent.getIntegerArrayListExtra("usedInstList");
        selectedTrack = intent.getIntExtra("selectedTrack", -1);
        boolean isMore = intent.getBooleanExtra("isMore", false);
        instSelectPanel = findViewById(R.id.instSelectPanel);

        if(usedInstList.size()>0){
            addTextView(getString(R.string.used_inst_list));
            for(int j=usedInstList.size()-1;j>=0;j--) addRadioButton(usedInstList.get(j), true);
        }
        int idx = -1;
        for(int i=0;i<StrArray.INST_GROUP.length;i++){
            addTextView(StrArray.INST_GROUP_NAME[i]);
            for (int j = 0; j < StrArray.INST_GROUP[i].length; j++) {
                if((idx<0&&selectedTrack!=-1)||(idx>=0&&usedInstList.size()>=15&&usedInstList.indexOf(idx)==-1&&(selectedTrack==-1||isMore))) addRadioButton(idx, false);
                else addRadioButton(idx, true);
                idx++;
            }
        }
        ((RadioButton)instSelectPanel.findViewWithTag(usedInstList.size()>0?usedInstList.get(usedInstList.size()-1):-1)).setChecked(true);

        blinkAnim = AnimationUtils.loadAnimation(this, R.anim.blink);
        messagePanel = findViewById(R.id.messagePanel);
        messagePanel.setText(selectedTrack==-1?getString(R.string.choose_inst_add):getString(R.string.choose_inst_edit));
        messagePanel.startAnimation(blinkAnim);
    }

    public void okBtnClicked(View view){
        Intent intent = new Intent();
        intent.putExtra("instCode", (int)findViewById(instSelectPanel.getCheckedRadioButtonId()).getTag());
        intent.putExtra("selectedTrack", selectedTrack);
        setResult(RESULT_OK,intent);
        finish();
    }

    public void cancelBtnClicked(View view){
        finish();
    }

    private void addRadioButton(int tag, boolean isClickable){
        RadioButton radioButton = new RadioButton(this);
        radioButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        radioButton.setButtonDrawable(new StateListDrawable());
        radioButton.setBackgroundResource(R.drawable.customrb);
        radioButton.setText(getString(R.string.inst_text, tag+1, StrArray.INST_GROUP[tag<0?0:tag/8+1][tag<0?0:tag%8]));
        if(isClickable) {
            radioButton.setTag(tag);
            radioButton.setTextColor(ContextCompat.getColorStateList(getApplicationContext(),R.color.selected_text_color));
            radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int instCode = (int) v.getTag();
                    if (instCode <= -1) return;
                    try {
                        List<Note> noteList = new ArrayList<Note>();
                        noteList.add(new Note(0, 48,60,100));
                        mediaManager.playPreview(instCode, noteList,4, 4, 120);
                    } catch (IOException e) {
                    }
                }
            });
        }else{
            radioButton.setTextColor(Color.RED);
            radioButton.setAlpha(0.4f);
            radioButton.setClickable(false);
        }
        radioButton.setPadding(25,10,0,10);
        instSelectPanel.addView(radioButton);
    }

    private void addTextView(String text){
        TextView textView = new TextView(new ContextThemeWrapper(this, R.style.ButtonText),null,0);
        textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        textView.setText(text);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView.setPadding(0,20,0,20);
        textView.setBackground(null);
        instSelectPanel.addView(textView);
    }
}
