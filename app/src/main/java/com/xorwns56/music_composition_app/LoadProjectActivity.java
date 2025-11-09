package com.xorwns56.music_composition_app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.StateListDrawable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.File;

public class LoadProjectActivity extends AppCompatActivity {
    LinearLayout.LayoutParams lp;
    RadioGroup projectSelectPanel;
    String projectPath;
    String filename;
    String newFilename;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_project);
        lp  = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        projectSelectPanel = findViewById(R.id.projectSelectPanel);
        Intent intent = getIntent();
        projectPath = getFilesDir().getAbsolutePath() + File.separator + "Project";
        filename = intent.getStringExtra("filename");
        if(filename==null) filename = "";
        newFilename = "";
        File projectDir = new File(projectPath);
        if(projectDir.exists()&&projectDir.isDirectory()){
            File projects[] = projectDir.listFiles();
            for(File project : projects){
                addRadioButton(project.getName().substring(0,project.getName().lastIndexOf(".")));
            }
        }

    }

    public void deleteBtnClicked(View view){
        if(newFilename.isEmpty()||newFilename.equals(filename)){
            Toast.makeText(this,getString(R.string.editing_project_delete_message),Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(getString(R.string.warning)).setMessage(getString(R.string.delete_project_message, newFilename))
                .setNegativeButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        File project = new File(projectPath + File.separator + newFilename + ".txt");
                        if(project.exists()&&project.delete()){
                            projectSelectPanel.removeView(projectSelectPanel.findViewWithTag(newFilename));
                            newFilename = "";
                            findViewById(R.id.okBtn).setVisibility(View.INVISIBLE);
                            findViewById(R.id.deleteBtn).setVisibility(View.INVISIBLE);
                        }
                    }
                }).setPositiveButton(getString(R.string.cancel), null).show();
    }

    public void okBtnClicked(View view){
        if(newFilename.isEmpty()) return;
        Intent intent = new Intent();
        intent.putExtra("filename",newFilename);
        setResult(RESULT_OK,intent);
        finish();
    }

    public void cancelBtnClicked(View view){
        finish();
    }

    private void addRadioButton(String tag){
        RadioButton radioButton = new RadioButton(this);
        radioButton.setLayoutParams(lp);
        radioButton.setButtonDrawable(new StateListDrawable());
        radioButton.setBackgroundResource(R.drawable.customrb);
        radioButton.setText(tag);
        radioButton.setTag(tag);
        radioButton.setTextColor(getResources().getColorStateList(R.color.selected_text_color));
        radioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newFilename = (String)v.getTag();
                findViewById(R.id.okBtn).setVisibility(View.VISIBLE);
                findViewById(R.id.deleteBtn).setVisibility(View.VISIBLE);
            }
        });
        radioButton.setPadding(25,10,0,10);
        projectSelectPanel.addView(radioButton);
    }
}
