package com.xorwns56.music_composition_app;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;

public class EditBpmActivity extends AppCompatActivity {
    Animation fadeOutAnim;
    TextView bpmRangeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_bpm);
        Intent intent = getIntent();
        String bpm = intent.getStringExtra("bpm");
        ((EditText)findViewById(R.id.editBpmText)).setText(bpm);
        fadeOutAnim = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        fadeOutAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                bpmRangeText.setVisibility(View.VISIBLE);
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                bpmRangeText.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        bpmRangeText = (TextView)findViewById(R.id.bpmRangeText);
    }

    public void okBtnClicked(View view){
        int bpm;
        try {
            bpm = Integer.parseInt(((EditText) findViewById(R.id.editBpmText)).getText().toString());
            if(bpm<40||bpm>360){
                ((TextView)findViewById(R.id.bpmRangeText)).startAnimation(fadeOutAnim);
                return;
            }
        }catch(Exception e){
            ((TextView)findViewById(R.id.bpmRangeText)).startAnimation(fadeOutAnim);
            return;
        }
        Intent intent = new Intent();
        intent.putExtra("bpm", bpm);
        setResult(RESULT_OK,intent);
        finish();
    }

    public void cancelBtnClicked(View view){
        finish();
    }
}
