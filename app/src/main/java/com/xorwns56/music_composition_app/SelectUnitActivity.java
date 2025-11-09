package com.xorwns56.music_composition_app;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class SelectUnitActivity extends AppCompatActivity {

    LinearLayout unitActivityLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_unit);
        Intent intent = getIntent();
        String unit = intent.getStringExtra("unit");
        if(unit==null) finish();
        unitActivityLayout = findViewById(R.id.unitActivityLayout);
        unitActivityLayout.setTag(unitActivityLayout.findViewWithTag(unit));
        ((ImageButton)unitActivityLayout.getTag()).setBackgroundResource(R.drawable.cyan_stroke);
    }

    public void selectUnit(View view){
        ((ImageButton)unitActivityLayout.getTag()).setBackground(null);
        unitActivityLayout.setTag(view);
        view.setBackgroundResource(R.drawable.cyan_stroke);

        Intent intent = new Intent();
        intent.putExtra("unit", Integer.parseInt(String.valueOf(view.getTag())));
        setResult(RESULT_OK,intent);
        finish();
    }

    public void finishSelectUnitActivity(View view){
        finish();
    }
}
