package com.xorwns56.music_composition_app;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

public class MainActivity extends AppCompatActivity {
    static final int REQUEST_CODE_SELECT_INST = 101;
    static final int REQUEST_CODE_SELECT_UNIT = 102;
    static final int REQUEST_CODE_EDIT_BPM = 103;
    static final int REQUEST_CODE_LOAD_PROJECT = 104;

    Animation translateUpAnim;
    Animation fadeInAnim;
    LinearLayout slide;
    RelativeLayout main;
    RadioGroup drumGroup;
    RadioGroup pitchGroup;
    CustomVolumeBar volumeBar;
    CustomOctaveBar octaveBar;
    NoteTableImageView noteTableImageView;
    NoteLineImageView noteLineImageView;
    InstrumentImageView instrumentImageView;
    MediaManager mediaManager;
    String dirPath;
    String projectDirPath;
    Resources resources;
    Setting setting;
    private InterstitialAd mInterstitialAd;
    private UnifiedNativeAd nativeAd;
    private boolean noteTableImageViewIsDragging = false;
    private boolean isActivityCreated = false;

    @Override
    public void onBackPressed() {
        if(!isActivityCreated) {
            mInterstitialAd.setAdListener(null);
            super.onBackPressed();
            return;
        }
        if(slide.getVisibility()==View.VISIBLE) slideCancel();
        else {
            final View view = View.inflate(this, R.layout.exit_dialog,null);
            AdLoader.Builder builder = new AdLoader.Builder(this, getString(R.string.native_ad_id));
            builder.forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                @Override
                public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                    if (nativeAd != null) nativeAd.destroy();
                    nativeAd = unifiedNativeAd;
                    FrameLayout frameLayout = view.findViewById(R.id.fl_adplaceholder);
                    UnifiedNativeAdView adView = (UnifiedNativeAdView) getLayoutInflater().inflate(R.layout.ad_unified, null);
                    populateUnifiedNativeAdView(unifiedNativeAd, adView);
                    frameLayout.removeAllViews();
                    frameLayout.addView(adView);
                }
            });
            VideoOptions videoOptions = new VideoOptions.Builder().setStartMuted(true).build();
            NativeAdOptions adOptions = new NativeAdOptions.Builder().setVideoOptions(videoOptions).build();
            builder.withNativeAdOptions(adOptions);
            builder.build().loadAd(new AdRequest.Builder().build());
            final AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setView(view)
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            if (nativeAd != null) nativeAd.destroy();
                        }
                    }).show();
            view.findViewById(R.id.exitBtn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mediaManager.stopAudio(MediaManager.MIDI_PREVIEW_PLAYER);
                    mediaManager.stopAudio(MediaManager.MIDI_PLAYER);
                    if (nativeAd!=null) nativeAd.destroy();
                    MainActivity.super.onBackPressed();
                }
            });
            view.findViewById(R.id.cancelBtn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertDialog.cancel();
                }
            });
            if(alertDialog.getWindow()!=null)
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void populateUnifiedNativeAdView(UnifiedNativeAd nativeAd, UnifiedNativeAdView adView) {
        MediaView mediaView = adView.findViewById(R.id.ad_media);
        adView.setMediaView(mediaView);
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to check before trying to display them.
        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }
        adView.setNativeAd(nativeAd);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==0){
            if(grantResults[0]!=PackageManager.PERMISSION_GRANTED) Toast.makeText(this,getString(R.string.permission_message), Toast.LENGTH_SHORT).show();
            else{
                ((ImageButton) findViewById(R.id.playBtn)).setImageResource(R.drawable.pause_btn);
                resources.setDrawPositionX(0);
                resources.setPlayPosition(0);
                noteLineImageView.invalidate();
                noteTableImageView.invalidate();
                Thread thread = new Thread(new PlayRunnable(true));
                try {
                    mediaManager.playMidi(resources.getTrackList(), 0, resources.getTempoOver(), resources.getTempoUnder(), resources.getBpm());
                } catch (Exception e) {
                }
                thread.start();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        main = findViewById(R.id.main);
        slide = findViewById(R.id.slide);
        fadeInAnim = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fadeInAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                main.setVisibility(View.VISIBLE);
            }
            @Override
            public void onAnimationEnd(Animation animation) {}
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        translateUpAnim = AnimationUtils.loadAnimation(this, R.anim.translate_up);
        translateUpAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                slide.setVisibility(View.VISIBLE);
            }
            @Override
            public void onAnimationEnd(Animation animation) {}
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        dirPath = getFilesDir().getAbsolutePath() + File.separator;
        projectDirPath = dirPath + "Project" + File.separator;
        if(new File(projectDirPath).mkdirs()) System.out.println("Project Folder is Created");

        mediaManager = new MediaManager(dirPath);

        instrumentImageView = (InstrumentImageView) findViewById(R.id.instrumentImageView);
        noteTableImageView = (NoteTableImageView) findViewById(R.id.noteTableImageView);
        noteLineImageView = (NoteLineImageView) findViewById(R.id.noteLineImageView);

        resources = new Resources(this);
        setting = new Setting(this);

        if(loadSetting()==-1||loadFile(dirPath+"tmp.txt")==-1){
            finish();
            return;
        }

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_ad_id));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                if(mInterstitialAd.isLoaded()) mInterstitialAd.show();
                isActivityCreated = true;
            }
            @Override
            public void onAdFailedToLoad(int errorCode) {
                main.startAnimation(fadeInAnim);
                isActivityCreated = true;
            }
            @Override
            public void onAdClosed() { main.setVisibility(View.VISIBLE); }
        });

        setUnitDrawable(setting.getNoteType());
        ((TextView)findViewById(R.id.tempoOverText)).setText(String.valueOf(resources.getTempoOver()));
        ((TextView)findViewById(R.id.tempoUnderText)).setText(String.valueOf(resources.getTempoUnder()));
        ((TextView)findViewById(R.id.bpmText)).setText(String.valueOf(resources.getBpm()));

        noteLineImageView.invalidate();
        noteTableImageView.invalidate();
        instrumentImageView.invalidate();

        drumGroup = (RadioGroup) findViewById(R.id.drumGroup);
        pitchGroup = (RadioGroup) findViewById(R.id.pitchGroup);
        volumeBar = (CustomVolumeBar) findViewById(R.id.volumeBar);
        octaveBar = (CustomOctaveBar) findViewById(R.id.octaveBar);

        int maxRowItemCount = 3;
        LinearLayout.LayoutParams autoWidth = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT,1f);
        LinearLayout.LayoutParams radioGroupLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,100);
        for(int i=0;i<StrArray.DRUM.length;i+=maxRowItemCount){
            LinearLayout radioGroup = new LinearLayout(this);
            radioGroup.setLayoutParams(radioGroupLp);
            radioGroup.setWeightSum(maxRowItemCount);
            for(int j=i;j<i+maxRowItemCount;j++) {
                RadioButton radioButton = new RadioButton(this);
                radioButton.setLayoutParams(autoWidth);
                radioButton.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                radioButton.setButtonDrawable(new StateListDrawable());
                radioButton.setBackgroundResource(R.drawable.customrb);
                radioButton.setTextColor(ContextCompat.getColorStateList(getApplicationContext(),R.color.selected_text_color));
                radioButton.setText(StrArray.DRUM[j]);
                radioButton.setTextSize(10);
                radioButton.setTag(j+35);
                radioButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        radioBtnAction(v);
                    }
                });
                radioGroup.addView(radioButton);
                if(j==StrArray.DRUM.length-1) break;
            }
            drumGroup.addView(radioGroup);
        }
        radioBtnCheck(drumGroup.findViewWithTag(35));
        radioBtnCheck(pitchGroup.findViewWithTag("0"));

        volumeBar.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if(action==event.ACTION_DOWN||action==event.ACTION_MOVE||action==event.ACTION_UP){
                    Selected selected = resources.getSelected();
                    if(selected == null) return false;
                    volumeBar.setProgress((int)(volumeBar.getMax()*event.getX()/volumeBar.getWidth()));
                    if(action==event.ACTION_UP) {
                        for (Note note : selected.getSelectedNoteList()){
                            note.setVolume(volumeBar.getProgress());
                            if(setting.isSaved()) setting.setSaved(false);
                        }
                        try {
                            mediaManager.playPreview(resources.getTrackList().get(selected.getY()).getInstType(), selected.getSelectedNoteList(), resources.getTempoOver(), resources.getTempoUnder(), resources.getBpm());
                        } catch (IOException e) {}
                        noteTableImageView.invalidate();
                    }
                    return true;
                }
                return false;
            }
        });

        octaveBar.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if(action==event.ACTION_DOWN||action==event.ACTION_MOVE||action==event.ACTION_UP){
                    Selected selected = resources.getSelected();
                    if(selected == null) return false;
                    octaveBar.setProgress((int)((octaveBar.getMax()+1)*event.getX()/octaveBar.getWidth()));
                    if(action==event.ACTION_UP){
                        for(Note note : selected.getSelectedNoteList()){
                            note.setPitch((octaveBar.getProgress()+1)*12+Integer.parseInt(((RadioButton)pitchGroup.getTag()).getTag().toString()));
                            if(setting.isSaved()) setting.setSaved(false);
                        }
                        try {
                            mediaManager.playPreview(resources.getTrackList().get(selected.getY()).getInstType(), selected.getSelectedNoteList(), resources.getTempoOver(), resources.getTempoUnder(), resources.getBpm());
                        } catch (IOException e) {}
                    }
                    return true;
                }
                return false;
            }
        });

        noteTableImageView.setOnTouchListener(new View.OnTouchListener() {
            private float initX, initY;
            private Handler handler = new Handler();
            private boolean isLongPress;
            private Runnable longPress = new Runnable() {
                @Override
                public void run() {
                    Selected selected = resources.getSelected();
                    if(selected==null) return;
                    List<Note> selectedNoteList = selected.getSelectedNoteList();
                    if(selectedNoteList.size()>0) return;
                    isLongPress = true;
                    noteTableImageViewIsDragging = false;
                    if(setting.getClipBoard().isEmpty()){
                        Toast.makeText(MainActivity.this, getString(R.string.no_clipboard_message) , Toast.LENGTH_SHORT).show();
                        slideCancel();
                        return;
                    }
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
                    dialogBuilder.setMessage(getString(R.string.paste_message))
                        .setNegativeButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Selected selected = resources.getSelected();
                                    List<Note> selectedNoteList = selected.getSelectedNoteList();
                                    Track track = resources.getTrackList().get(selected.getY());
                                    String clipboard[] = setting.getClipBoard().split("#");
                                    String data[] = clipboard[0].split(",");
                                    int startIndex = selected.getX()+Integer.parseInt(data[0]);
                                    int size = Integer.parseInt(data[1]);
                                    int pitch = Integer.parseInt(data[2]);
                                    if(track.getInstType()<0&&!(35<=pitch&&pitch<=81)) pitch = 35;
                                    int volume = Integer.parseInt(data[3]);
                                    for(int i=startIndex;i<startIndex+size;i++){
                                        if(track.getNote(i)!=null){
                                            Toast.makeText(MainActivity.this, getString(R.string.paste_fail_message) , Toast.LENGTH_SHORT).show();
                                            slideCancel();
                                            return;
                                        }
                                    }
                                    Note firstNote = track.addNoteBlock(new Note(startIndex,size,pitch,volume));
                                    selectedNoteList.add(firstNote);;
                                    Note nextNote = track.getNoteBySparseArrayIndex(track.getSparseArrayStartIndexByNote(firstNote)+firstNote.getSize());
                                    for(int i=1;i<clipboard.length;i++){
                                        data = clipboard[i].split(",");
                                        startIndex = selected.getX()+Integer.parseInt(data[0]);
                                        size = Integer.parseInt(data[1]);
                                        if(nextNote!=null&&startIndex+size>nextNote.getStartIndex()){
                                            Toast.makeText(MainActivity.this, getString(R.string.paste_fail_message) , Toast.LENGTH_SHORT).show();
                                            break;
                                        }
                                        pitch = Integer.parseInt(data[2]);
                                        if(track.getInstType()<0&&!(35<=pitch&&pitch<=81)) pitch = 35;
                                        volume = Integer.parseInt(data[3]);
                                        selectedNoteList.add(track.addNoteBlock(new Note(startIndex,size,pitch,volume)));
                                    }
                                    if(setting.isSaved()) setting.setSaved(false);
                                    slideUp();
                                }
                        }).setPositiveButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) { slideCancel(); }
                        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                                public void onCancel(DialogInterface dialog) { slideCancel(); }
                        }).show();
                }
            };
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                int noteWidth = setting.getNoteWidth();
                int noteHeight = setting.getNoteHeight();
                int noteType = setting.getNoteType();
                int drawPositionX = resources.getDrawPositionX();
                int drawPositionY = resources.getDrawPositionY();
                int x = (int)((event.getX() + drawPositionX)/(noteWidth/12));
                int y = (int)((event.getY() + drawPositionY)/noteHeight);
                List<Track> trackList = resources.getTrackList();
                if(action==MotionEvent.ACTION_DOWN){
                    slideCancel();
                    if(!mediaManager.isPlaying(MediaManager.MIDI_PLAYER)&&y<trackList.size()&&resources.getSelectedTrack()==-1){
                        noteTableImageViewIsDragging = true;
                        isLongPress = false;
                        initX = event.getX();
                        initY = event.getY();
                        handler.postDelayed(longPress,ViewConfiguration.getLongPressTimeout());
                        Note note = trackList.get(y).getNote(x);
                        Selected selected;
                        if(note!=null){
                            selected = new Selected(note.getStartIndex(),y,0);
                            selected.getSelectedNoteList().add(note);
                        }else{
                            int min = x/noteType*noteType;
                            for(int i=x-1;i>=min;i--){
                                if(trackList.get(y).getNote(i)!=null){
                                    min = i+1;
                                    break;
                                }
                            }
                            selected = new Selected(min,y,0);
                        }
                        resources.setSelected(selected);
                    }
                }else if(action==MotionEvent.ACTION_MOVE&&!isLongPress){
                    Selected selected = resources.getSelected();
                    if (selected==null || Math.abs(initY - event.getY()) > 10 || Math.abs(initX - event.getX()) > 10) handler.removeCallbacks(longPress);
                    if (selected != null) {
                        Track track = trackList.get(selected.getY());
                        Note note = track.getNote(selected.getX());
                        int oldX = selected.getX() + selected.getSize();
                        int newX = note != null ? x + (x < 0 ? 0 : 1) : x / noteType * noteType + (x < 0 ? 0 : noteType);
                        if (newX < selected.getX()) newX = selected.getX();
                        if (oldX > newX) {
                            int size = selected.getSize() - (oldX - newX);
                            List<Note> selectedNoteList = selected.getSelectedNoteList();
                            for (int i = selectedNoteList.size() - 1; i > 0; i--) {
                                Note lastNote = selectedNoteList.get(i);
                                if (lastNote.getStartIndex() >= newX)
                                    selectedNoteList.remove(i);
                                else break;
                            }
                            selected.setSize(size);
                        } else if (oldX < newX) {
                            int size = selected.getSize() + (newX - oldX);
                            for (int i = oldX; i < newX; i++) {
                                Note noteCheck = track.getNote(i);
                                if (noteCheck != null) {
                                    if (note == null) {
                                        size = selected.getSize() + (i - oldX);
                                        break;
                                    } else if (i == noteCheck.getStartIndex()) {
                                        if (note != noteCheck)
                                            selected.getSelectedNoteList().add(noteCheck);
                                        try {
                                            List<Note> noteList = new ArrayList<>();
                                            noteList.add(noteCheck);
                                            mediaManager.playPreview(track.getInstType(), noteList, resources.getTempoOver(), resources.getTempoUnder(), resources.getBpm());
                                        } catch (IOException e) {
                                        }
                                    }
                                }
                            }
                            selected.setSize(size);
                        }
                    }
                }else if(action==MotionEvent.ACTION_UP&&!isLongPress) {
                    handler.removeCallbacks(longPress);
                    noteTableImageViewIsDragging = false;
                    Selected selected = resources.getSelected();
                    if(selected!=null&&selected.getSize()!=0&&selected.getSelectedNoteList().size()==0) {
                        Track track = resources.getTrackList().get(selected.getY());
                        int pitch = track.getInstType() >= 0 ? (octaveBar.getProgress()+1) * 12 + Integer.parseInt(((RadioButton) pitchGroup.getTag()).getTag().toString()) : (int) ((RadioButton) drumGroup.getTag()).getTag();
                        selected.getSelectedNoteList().add(track.addNoteBlock(new Note(selected.getX(), selected.getSize(), pitch, volumeBar.getProgress())));
                        if (setting.isSaved()) setting.setSaved(false);
                        try {
                            mediaManager.playPreview(track.getInstType(), selected.getSelectedNoteList(),resources.getTempoOver(),resources.getTempoUnder(), resources.getBpm());
                        } catch (IOException e) {}
                    }
                    slideUp();
                }
                noteLineImageView.invalidate();
                noteTableImageView.invalidate();
                return action==MotionEvent.ACTION_DOWN||action==MotionEvent.ACTION_MOVE||action==MotionEvent.ACTION_UP;
            }
        });

        noteLineImageView.setOnTouchListener(new View.OnTouchListener() {
            private long startClickTime;
            private float dX;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if(action==MotionEvent.ACTION_DOWN){
                    startClickTime = System.currentTimeMillis();
                    dX = event.getX();
                }else{
                    if(!mediaManager.isPlaying(MediaManager.MIDI_PLAYER)&&!noteTableImageViewIsDragging&&System.currentTimeMillis()-startClickTime<ViewConfiguration.getTapTimeout()){
                        if(action==MotionEvent.ACTION_UP){
                            int noteType = setting.getNoteType();
                            resources.setStartPosition((int)((dX + resources.getDrawPositionX())/(setting.getNoteWidth()/12))/noteType*noteType);
                        }
                    }else {
                        if (action == MotionEvent.ACTION_MOVE) {
                            int newDrawPositionX = Math.round(resources.getDrawPositionX() + dX - event.getX());
                            resources.setDrawPositionX(newDrawPositionX < 0 ? 0 : newDrawPositionX);
                            dX = event.getX();
                        }
                    }
                }
                noteLineImageView.invalidate();
                noteTableImageView.invalidate();
                return action==MotionEvent.ACTION_DOWN||action==MotionEvent.ACTION_MOVE||action==MotionEvent.ACTION_UP;
            }
        });

        instrumentImageView.setOnTouchListener(new View.OnTouchListener() {
            private long startClickTime;
            private float dY, initY;
            private int selectedTrack;
            private boolean isDialogOpened = false;
            private Handler handler = new Handler();
            private boolean isLongPress;
            private Runnable longPress = new Runnable() {
                @Override
                public void run() {
                    slideCancel();
                    resources.setSelectedTrack(selectedTrack);
                    instrumentImageView.invalidate();
                    isLongPress = true;
                }
            };
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if(!isDialogOpened) {
                    List<Track> trackList = resources.getTrackList();
                    if (action== MotionEvent.ACTION_DOWN) {
                        startClickTime = System.currentTimeMillis();
                        dY = initY = event.getY();
                        selectedTrack = (int) ((initY + resources.getDrawPositionY()) / setting.getNoteHeight());
                        if (selectedTrack >= trackList.size()) selectedTrack = -1;
                        isLongPress = false;
                        if (!mediaManager.isPlaying(MediaManager.MIDI_PLAYER)&&!noteTableImageViewIsDragging && selectedTrack >= 0) handler.postDelayed(longPress, ViewConfiguration.getLongPressTimeout());
                    } else {
                        if (!mediaManager.isPlaying(MediaManager.MIDI_PLAYER)&&!noteTableImageViewIsDragging && System.currentTimeMillis() - startClickTime < ViewConfiguration.getTapTimeout()) {
                            if (action == MotionEvent.ACTION_UP) {
                                slideCancel();
                                if (selectedTrack >= 0) {
                                    handler.removeCallbacks(longPress);
                                    isDialogOpened = true;
                                    resources.setSelectedTrack(selectedTrack);
                                    instrumentImageView.invalidate();
                                    final CharSequence[] dialogItems;
                                    if (trackList.get(selectedTrack).getInstType() >= 0)
                                        dialogItems = new CharSequence[]{getString(R.string.delete), getString(R.string.edit)};
                                    else
                                        dialogItems = new CharSequence[]{getString(R.string.delete)};
                                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
                                    dialogBuilder.setItems(dialogItems, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int item) {
                                            if (item == 0) {
                                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                                builder.setTitle(getString(R.string.warning)).setMessage(getString(R.string.ask_delete_message))
                                                        .setNegativeButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int id) {
                                                                List<Track> trackList = resources.getTrackList();
                                                                int noteHeight = setting.getNoteHeight();

                                                                trackList.remove(selectedTrack);
                                                                int drawPositionY = resources.getDrawPositionY();
                                                                int maxDrawPositionY = trackList.size() * noteHeight - noteTableImageView.getHeight();
                                                                if (maxDrawPositionY < 0)
                                                                    maxDrawPositionY = 0;
                                                                if (drawPositionY > maxDrawPositionY)
                                                                    resources.setDrawPositionY(maxDrawPositionY);

                                                                resources.setSelectedTrack(-1);
                                                                if (setting.isSaved())
                                                                    setting.setSaved(false);
                                                                noteTableImageView.invalidate();
                                                                instrumentImageView.invalidate();
                                                            }
                                                        }).setPositiveButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        resources.setSelectedTrack(-1);
                                                        instrumentImageView.invalidate();
                                                    }
                                                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                                                    @Override
                                                    public void onCancel(DialogInterface dialog) {
                                                        resources.setSelectedTrack(-1);
                                                        instrumentImageView.invalidate();
                                                    }
                                                }).show();
                                            } else if (item == 1) {
                                                selectInstrumentActivity(selectedTrack);
                                            }
                                            isDialogOpened = false;
                                        }
                                    }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                                        @Override
                                        public void onCancel(DialogInterface dialog) {
                                            resources.setSelectedTrack(-1);
                                            instrumentImageView.invalidate();
                                            isDialogOpened = false;
                                        }
                                    }).show();
                                }
                            }
                        } else {
                            if (action == MotionEvent.ACTION_MOVE) {
                                int drawPositionY = resources.getDrawPositionY();
                                int noteHeight = setting.getNoteHeight();
                                if (isLongPress) {
                                    int y = (int) ((event.getY() + drawPositionY) / noteHeight);
                                    if (y >= trackList.size()) y = trackList.size() - 1;
                                    if (y < 0) y = 0;
                                    if (selectedTrack != y) {
                                        trackList.add(y, trackList.remove(selectedTrack));
                                        selectedTrack = y;
                                        resources.setSelectedTrack(selectedTrack);
                                        if (setting.isSaved()) setting.setSaved(false);
                                        instrumentImageView.invalidate();
                                        noteTableImageView.invalidate();
                                    }
                                } else {
                                    if (Math.abs(initY - event.getY()) > 10) handler.removeCallbacks(longPress);
                                    int newDrawPositionY = Math.round(drawPositionY + dY - event.getY());
                                    if (newDrawPositionY > trackList.size() * noteHeight - noteTableImageView.getHeight())
                                        newDrawPositionY = trackList.size() * noteHeight - noteTableImageView.getHeight();
                                    if (newDrawPositionY < 0) newDrawPositionY = 0;
                                    resources.setDrawPositionY(newDrawPositionY);
                                    dY = event.getY();
                                    instrumentImageView.invalidate();
                                    noteTableImageView.invalidate();
                                }
                            } else if (action == MotionEvent.ACTION_UP) {
                                handler.removeCallbacks(longPress);
                                if (isLongPress) {
                                    resources.setSelectedTrack(-1);
                                    instrumentImageView.invalidate();
                                }
                            }
                        }
                    }
                }
                return action==MotionEvent.ACTION_DOWN||action==MotionEvent.ACTION_MOVE||action==MotionEvent.ACTION_UP;
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(saveFile(dirPath+"tmp.txt")) saveSetting();
    }

    public void saveSetting(){
        File file = new File(dirPath+"setting.txt");
        FileWriter fw = null;
        BufferedWriter bufwr = null;
        try {
            fw = new FileWriter(file);
            bufwr = new BufferedWriter(fw);
            bufwr.write(
                        setting.getNoteType()+"/"
                        +setting.getNoteWidth()+"/"
                        +setting.getNoteHeight()+"/"
                        +setting.getFilename()+"/"
                        +String.valueOf(setting.isSaved())+"/"
                        +(setting.getClipBoard().isEmpty()?"empty":setting.getClipBoard())
            );
        }catch(Exception e){
        }finally{
            if (bufwr != null) try {bufwr.close();}catch(Exception e){}
            if (fw != null) try {fw.close();}catch(Exception e){}
        }
    }

    public int loadSetting(){
        File file = new File(dirPath+"setting.txt");
        FileReader fr = null;
        BufferedReader bufrd = null;
        if(!file.exists()) return 0;
        try {
            fr = new FileReader(file);
            bufrd = new BufferedReader(fr);
            String s = bufrd.readLine();
            String[] settingArray = s.split("/");
            setting.setNoteType(Integer.parseInt(settingArray[0]));
            setting.setNoteWidth(Integer.parseInt(settingArray[1]));
            setting.setNoteHeight(Integer.parseInt(settingArray[2]));
            setting.setFilename(settingArray[3]);
            setting.setSaved(Boolean.valueOf(settingArray[4]));
            setting.setClipBoard(settingArray[5].equals("empty")?"":settingArray[5]);
        } catch (Exception e) {
            return -1;
        }finally {
            if (bufrd != null) try {bufrd.close();}catch(Exception e){}
            if (fr != null) try {fr.close();}catch(Exception e){}
        }
        return 1;
    }

    public boolean saveFile(String filePath){
        File file = new File(filePath);
        FileWriter fw = null;
        BufferedWriter bufwr = null;
        try {
            fw = new FileWriter(file);
            bufwr = new BufferedWriter(fw);
            bufwr.write(
                    resources.getTempoOver()+"/"
                        +resources.getTempoUnder()+"/"
                        +resources.getBpm()
            );
            bufwr.newLine();
            List<Track> trackList = resources.getTrackList();
            for(int i=0;i<trackList.size();i++){
                Track track = trackList.get(i);
                bufwr.write("#"+track.getInstType());
                bufwr.newLine();
                int j = 0;
                Note note;
                while((note=track.getNoteBySparseArrayIndex(j))!=null){
                    bufwr.write(note.getStartIndex()+"/"+note.getSize()+"/"+note.getPitch()+"/"+note.getVolume());
                    bufwr.newLine();
                    j+=note.getSize();
                }
            }
        }catch(Exception e){
            return false;
        }finally{
            if (bufwr != null) try {bufwr.close();}catch(Exception e){}
            if (fw != null) try {fw.close();}catch(Exception e){}
        }
        return true;
    }

    public int loadFile(String filePath){
        File file = new File(filePath);
        FileReader fr = null;
        BufferedReader bufrd = null;
        if(!file.exists()) return 0;
        try {
            fr = new FileReader(file);
            bufrd = new BufferedReader(fr);
            String s = bufrd.readLine();
            String res[] = s.split("/");
            resources = new Resources(this);
            resources.setTempoOver(Integer.parseInt(res[0]));
            resources.setTempoUnder(Integer.parseInt(res[1]));
            resources.setBpm(Integer.parseInt(res[2]));

            int trackIdx = -1;
            resources.setTrackList(new ArrayList<Track>());
            List<Track> trackList = resources.getTrackList();
            while((s = bufrd.readLine()) != null){
                if(s.startsWith("#")) {
                    trackIdx++;
                    trackList.add(new Track(Integer.parseInt(s.replace("#",""))));
                }else if(!s.equals("")){
                    String[] trackData = s.split("/");
                    int startIdx = Integer.parseInt(trackData[0]);
                    int size = Integer.parseInt(trackData[1]);
                    int pitch = Integer.parseInt(trackData[2]);
                    int volume = Integer.parseInt(trackData[3]);
                    trackList.get(trackIdx).addNoteBlock(new Note(startIdx,size, pitch, volume));
                }
            }
        } catch (Exception e) {
            Toast.makeText(this,getString(R.string.load_fail_message), Toast.LENGTH_SHORT).show();
            return -1;
        }finally {
            if (bufrd != null) try {bufrd.close();}catch(Exception e){}
            if (fr != null) try {fr.close();}catch(Exception e){}
        }
        return 1;
    }

    public void trackAddBtnClicked(View view){ selectInstrumentActivity(-1); }

    private void selectInstrumentActivity(int selectedTrack){
        List<Track> trackList = resources.getTrackList();
        if(trackList.size()>=99) Toast.makeText(this,getString(R.string.add_track_fail_message), Toast.LENGTH_SHORT).show();
        ArrayList<Integer> usedInstList = new ArrayList<>();
        boolean isMore = false;
        for(int i=0;i<trackList.size();i++){
            int instType = trackList.get(i).getInstType();
            if(!usedInstList.contains(instType)&&instType>=0) usedInstList.add(instType);
            if(!isMore&&selectedTrack!=-1&&i!=selectedTrack&&instType==trackList.get(selectedTrack).getInstType()) isMore = true; //악기가 이거 말고도 더있음.
        }
        Intent intent = new Intent(this,SelectInstrumentActivity.class);
        intent.putIntegerArrayListExtra("usedInstList", usedInstList);
        intent.putExtra("selectedTrack", selectedTrack);
        intent.putExtra("isMore", isMore);  //selectedTrack이 -1이 아니면서, isMore이 false 라면 , 15까지 봐줌 true면 15에서 안봐줌
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivityForResult(intent, REQUEST_CODE_SELECT_INST);
    }

    public void projectBtnClicked(View view){
        if(noteTableImageViewIsDragging) return;
        final CharSequence[] projectDialogItems = new CharSequence[]{getString(R.string.new_project), getString(R.string.load), getString(R.string.save), getString(R.string.save_as)};
        AlertDialog.Builder projectDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        projectDialogBuilder.setItems(projectDialogItems, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, final int item){
                if(item==0||item==1){   //새 프로젝트, 불러오기 선택했을 경우
                    if(!setting.isSaved()&&(!setting.getFilename().isEmpty()||resources.getTrackList().size()>0)) {
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
                        dialogBuilder.setTitle(projectDialogItems[item]).setMessage(getString(R.string.unsaved_project_delete_message))
                                .setNegativeButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        if(item==0){
                                            main.setVisibility(View.INVISIBLE);
                                            resources = new Resources(MainActivity.this);
                                            setting.setFilename("");
                                            noteLineImageView.invalidate();
                                            noteTableImageView.invalidate();
                                            instrumentImageView.invalidate();
                                            ((TextView)findViewById(R.id.tempoOverText)).setText(String.valueOf(resources.getTempoOver()));
                                            ((TextView)findViewById(R.id.tempoUnderText)).setText(String.valueOf(resources.getTempoUnder()));
                                            ((TextView)findViewById(R.id.bpmText)).setText(String.valueOf(resources.getBpm()));
                                            main.startAnimation(fadeInAnim);
                                        }else{
                                            Intent intent = new Intent(MainActivity.this, LoadProjectActivity.class);
                                            intent.putExtra("filename", setting.getFilename());
                                            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                            startActivityForResult(intent, REQUEST_CODE_LOAD_PROJECT);
                                        }
                                    }
                                }).setPositiveButton(getString(R.string.cancel), null).show();
                    }else{
                        if(item==0){
                            main.setVisibility(View.INVISIBLE);
                            resources = new Resources(MainActivity.this);
                            setting.setFilename("");
                            noteLineImageView.invalidate();
                            noteTableImageView.invalidate();
                            instrumentImageView.invalidate();
                            ((TextView)findViewById(R.id.tempoOverText)).setText(String.valueOf(resources.getTempoOver()));
                            ((TextView)findViewById(R.id.tempoUnderText)).setText(String.valueOf(resources.getTempoUnder()));
                            ((TextView)findViewById(R.id.bpmText)).setText(String.valueOf(resources.getBpm()));
                            main.startAnimation(fadeInAnim);
                        }else{
                            Intent intent = new Intent(MainActivity.this, LoadProjectActivity.class);
                            intent.putExtra("filename", setting.getFilename());
                            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivityForResult(intent, REQUEST_CODE_LOAD_PROJECT);
                        }
                    }
                }else{ //저장, 다른이름으로 저장 선택할 경우
                    if(item==3||setting.getFilename().isEmpty()){
                        saveAs();
                    }else {
                        if(saveFile(projectDirPath + setting.getFilename() + ".txt")){
                            Toast.makeText(MainActivity.this, getString(R.string.save_project_message, setting.getFilename()) , Toast.LENGTH_SHORT).show();
                            setting.setSaved(true);
                        }
                    }
                }
            }
        }).show();
    }

    private void saveAs(){
        final EditText editText = new EditText(this);
        AlertDialog.Builder filenameBuilder = new AlertDialog.Builder(this);
        filenameBuilder.setTitle(getString(R.string.project));
        filenameBuilder.setView(editText);
        filenameBuilder.setNegativeButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which){
                try {
                    String text = editText.getText().toString().trim().replaceAll("/", "");
                    if(text.isEmpty()) throw new Exception();
                    final File file = new File(projectDirPath + text +".txt");
                    if(file.createNewFile()){
                        if(saveFile(file.getAbsolutePath())){
                            String filename = file.getName();
                            setting.setFilename(filename.substring(0,filename.lastIndexOf(".")));
                            setting.setSaved(true);
                            Toast.makeText(MainActivity.this, getString(R.string.save_project_message, setting.getFilename()) , Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
                        dialogBuilder.setTitle(getString(R.string.warning)).setMessage(getString(R.string.exist_project_delete_message,file.getName().substring(0,file.getName().lastIndexOf("."))))
                                .setNegativeButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        if(saveFile(file.getAbsolutePath())){
                                            String filename = file.getName();
                                            setting.setFilename(filename.substring(0,filename.lastIndexOf(".")));
                                            setting.setSaved(true);
                                            Toast.makeText(MainActivity.this, getString(R.string.save_project_message, setting.getFilename()) , Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }).setPositiveButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        saveAs();
                                    }
                        }).show();
                    }
                }catch(Exception e){
                    Toast.makeText(MainActivity.this, getString(R.string.project_name_error_message) , Toast.LENGTH_SHORT).show();
                    saveAs();
                }
            }
        }).setPositiveButton(getString(R.string.cancel), null).show();
    }

    public void shareBtnClicked(View view){
        if(noteTableImageViewIsDragging) return;
        final CharSequence[] Items = new CharSequence[]{getString(R.string.share),getString(R.string.share_after_record)};
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setItems(Items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if(item==0){
                    try {
                        shareFile(mediaManager.getFile(resources.getTrackList(), resources.getTempoOver(), resources.getTempoUnder(), resources.getBpm()));
                    }catch(Exception e){}
                }else{
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) // API 23 이상 일경우..
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO},0);
                    else{ // API 23 미만 일경우..
                        ((ImageButton) findViewById(R.id.playBtn)).setImageResource(R.drawable.pause_btn);
                        resources.setDrawPositionX(0);
                        resources.setPlayPosition(0);
                        noteLineImageView.invalidate();
                        noteTableImageView.invalidate();
                        Thread thread = new Thread(new PlayRunnable(true));
                        try {
                            mediaManager.playMidi(resources.getTrackList(), 0, resources.getTempoOver(), resources.getTempoUnder(), resources.getBpm());
                        } catch (Exception e) {
                        }
                        thread.start();
                    }
                }
            }
        }).show();
    }

    private void shareFile(File file){
        if (file.exists()) {
            Uri uri;
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) // API 24 이상 일경우..
            {
                uri = FileProvider.getUriForFile(this,this.getPackageName() + ".fileprovider", file);
            }
            else // API 24 미만 일경우..
            {
                uri = Uri.fromFile(file);
            }
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("audio/*");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(Intent.createChooser(intent, getString(R.string.share)));
        }
    }

    public void settingBtnClicked(View view){
        if(noteTableImageViewIsDragging) return;
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, R.style.TransparentDialog);
        View dialogView = this.getLayoutInflater().inflate(R.layout.activity_edit_setting,null);
        dialogView.findViewById(R.id.noteWidthLeft).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newNoteWidth = setting.getNoteWidth() - 24;
                if(newNoteWidth<24) return;
                setting.setNoteWidth(newNoteWidth);
                noteLineImageView.invalidate();
                noteTableImageView.invalidate();
            }
        });
        dialogView.findViewById(R.id.noteWidthRight).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newNoteWidth = setting.getNoteWidth() + 24;
                if(newNoteWidth>240) return;
                setting.setNoteWidth(newNoteWidth);
                noteLineImageView.invalidate();
                noteTableImageView.invalidate();
            }
        });
        dialogView.findViewById(R.id.noteHeightLeft).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newNoteHeight = setting.getNoteHeight() - 30;
                if(newNoteHeight<90) return;
                setting.setNoteHeight(newNoteHeight);

                int maxDrawPositionY = resources.getTrackList().size()*setting.getNoteHeight()-noteTableImageView.getHeight();
                if(maxDrawPositionY<0) maxDrawPositionY = 0;
                if(resources.getDrawPositionY()>maxDrawPositionY) resources.setDrawPositionY(maxDrawPositionY);

                instrumentImageView.invalidate();
                noteTableImageView.invalidate();
            }
        });
        dialogView.findViewById(R.id.noteHeightRight).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newNoteHeight = setting.getNoteHeight() + 30;
                if(newNoteHeight>300) return;
                setting.setNoteHeight(newNoteHeight);
                instrumentImageView.invalidate();
                noteTableImageView.invalidate();
            }
        });
        dialogBuilder.setView(dialogView);
        dialogBuilder.show();
    }

    public void unitImageBtnClicked(View view){
        Intent intent = new Intent(this,SelectUnitActivity.class);
        intent.putExtra("unit",String.valueOf(setting.getNoteType()));
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivityForResult(intent, REQUEST_CODE_SELECT_UNIT);
    }

    public void tempoOverTextClicked(View view){
        final CharSequence[] tempoOverItems = new CharSequence[]{"2","3","4","5","6","7","8","9","10","11","12"};
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setItems(tempoOverItems, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                int tempoOver = Integer.parseInt(tempoOverItems[item].toString());
                if(resources.getTempoOver()!=tempoOver) {
                    resources.setTempoOver(tempoOver);
                    ((TextView) findViewById(R.id.tempoOverText)).setText(String.valueOf(tempoOver));
                    if(setting.isSaved()) setting.setSaved(false);
                    noteLineImageView.invalidate();
                }
            }
        }).show();
    }

    public void tempoUnderTextClicked(View view){
        final CharSequence[] tempoUnderItems = new CharSequence[]{"4","8"};
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setItems(tempoUnderItems, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                int tempoUnder = Integer.parseInt(tempoUnderItems[item].toString());
                if(resources.getTempoUnder()!=tempoUnder) {
                    resources.setTempoUnder(tempoUnder);
                    ((TextView) findViewById(R.id.tempoUnderText)).setText(String.valueOf(tempoUnder));
                    if(setting.isSaved()) setting.setSaved(false);
                    noteLineImageView.invalidate();
                }
            }
        }).show();
    }

    public void bpmTextClicked(View view){
        Intent intent = new Intent(this,EditBpmActivity.class);
        intent.putExtra("bpm", String.valueOf(resources.getBpm()));
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivityForResult(intent, REQUEST_CODE_EDIT_BPM);
    }

    public void moveHomeBtnClicked(View view){
        resources.setDrawPositionX(0);  //startPosition으로 옮겨야할지 고민
        noteLineImageView.invalidate();
        noteTableImageView.invalidate();
    }

    public void moveLeftBtnClicked(View view){
        int subCycle = 8/resources.getTempoUnder()*setting.getNoteWidth();
        int cycle = subCycle*resources.getTempoOver();
        int drawPositionX = resources.getDrawPositionX();
        int newDrawPositionX = drawPositionX/cycle*cycle-cycle*(drawPositionX%cycle==0?1:0);
        if(newDrawPositionX<0) newDrawPositionX = 0;
        resources.setDrawPositionX(newDrawPositionX);
        noteLineImageView.invalidate();
        noteTableImageView.invalidate();
    }

    public void moveRightBtnClicked(View view){
        int subCycle = 8/resources.getTempoUnder()*setting.getNoteWidth();
        int cycle = subCycle*resources.getTempoOver();
        int drawPositionX = resources.getDrawPositionX();
        int newDrawPositionX = drawPositionX/cycle*cycle+cycle;
        resources.setDrawPositionX(newDrawPositionX);
        noteLineImageView.invalidate();
        noteTableImageView.invalidate();
    }

    public void moveEndBtnClicked(View view){
        int end = 0;
        List<Track> trackList = resources.getTrackList();
        for(int i=0;i<trackList.size();i++){
            if(end<trackList.get(i).getEnd()) end = trackList.get(i).getEnd();
        }
        int newDrawPositionX = end*(setting.getNoteWidth()/12)-noteTableImageView.getWidth();
        if(newDrawPositionX<0) newDrawPositionX = 0;
        resources.setDrawPositionX(newDrawPositionX);
        noteLineImageView.invalidate();
        noteTableImageView.invalidate();
    }

    public void sizeLeftBtnClicked(View view) {
        Selected selected = resources.getSelected();
        if (selected == null) return;
        int noteType = setting.getNoteType();
        Track track = resources.getTrackList().get(selected.getY());
        for (Note note : selected.getSelectedNoteList()) {
            int before = note.getStartIndex() + note.getSize();
            int after = before % noteType == 0 ? before - noteType : before / noteType * noteType;
            if(after<=note.getStartIndex()) return;
        }
        for (Note note : selected.getSelectedNoteList()) {
            int before = note.getStartIndex() + note.getSize();
            int after = before % noteType == 0 ? before - noteType : before / noteType * noteType;
            track.changeNoteBlock(note.getStartIndex(),after-before,0);
            if(setting.isSaved()) setting.setSaved(false);
        }
        noteLineImageView.invalidate();
        noteTableImageView.invalidate();
    }

    public void sizeRightBtnClicked(View view){
        Selected selected = resources.getSelected();
        if (selected == null) return;
        int noteType = setting.getNoteType();
        List<Note> selectedNoteList = selected.getSelectedNoteList();
        Track track = resources.getTrackList().get(selected.getY());
        for (Note note : selectedNoteList) {
            Note nextNote = track.getNoteBySparseArrayIndex(track.getSparseArrayStartIndexByNote(note)+note.getSize());
            int before = note.getStartIndex() + note.getSize();
            int after = before/noteType*noteType + noteType;
            if(nextNote!=null&&after>nextNote.getStartIndex()) return;
        }
        for (Note note : selectedNoteList) {
            int before = note.getStartIndex() + note.getSize();
            int after = before/noteType*noteType + noteType;
            track.changeNoteBlock(note.getStartIndex(),after-before,0);
            if(setting.isSaved()) setting.setSaved(false);
        }
        noteLineImageView.invalidate();
        noteTableImageView.invalidate();
    }

    public void posLeftBtnClicked(View view){
        Selected selected = resources.getSelected();
        if (selected == null) return;
        int noteType = setting.getNoteType();
        Track track = resources.getTrackList().get(selected.getY());
        List<Note> selectedNoteList = selected.getSelectedNoteList();
        int before = selectedNoteList.get(0).getStartIndex();
        for(int i=before-noteType;i<before;i++){
            if(i<0) return;
            Note note = track.getNote(i);
            if(note!=null) return;
        }
        for (Note note : selectedNoteList) {
            track.changeNoteBlock(note.getStartIndex(),0,-noteType);
            if(setting.isSaved()) setting.setSaved(false);
        }
        noteLineImageView.invalidate();
        noteTableImageView.invalidate();
    }

    public void posRightBtnClicked(View view){
        Selected selected = resources.getSelected();
        if (selected == null) return;
        int noteType = setting.getNoteType();
        Track track = resources.getTrackList().get(selected.getY());
        List<Note> selectedNoteList = selected.getSelectedNoteList();
        int before = selectedNoteList.get(selectedNoteList.size()-1).getStartIndex() + selectedNoteList.get(selectedNoteList.size()-1).getSize();
        for(int i=before;i<before+noteType;i++){
            Note note = track.getNote(i);
            if(note!=null) return;
        }
        for (int i=selectedNoteList.size()-1;i>=0;i--) {
            track.changeNoteBlock(selectedNoteList.get(i).getStartIndex(),0,noteType);
            if(setting.isSaved()) setting.setSaved(false);
        }
        noteLineImageView.invalidate();
        noteTableImageView.invalidate();
    }

    public void radioBtnAction(View view){
        radioBtnCheck(view);
        Selected selected = resources.getSelected();
        if(selected==null) return;
        List<Note> selectedNoteList = selected.getSelectedNoteList();
        Track track = resources.getTrackList().get(selected.getY());
        int instType = track.getInstType();
        int pitch = instType>=0?(octaveBar.getProgress()+1)*12+Integer.parseInt(((RadioButton)pitchGroup.getTag()).getTag().toString()):(int)((RadioButton)drumGroup.getTag()).getTag();
        for(Note note : selectedNoteList) {
            note.setPitch(pitch);
            if (setting.isSaved()) setting.setSaved(false);
        }
        try {
            mediaManager.playPreview(instType, selectedNoteList, resources.getTempoOver(), resources.getTempoUnder(), resources.getBpm());
        }catch(IOException e){}
        noteTableImageView.invalidate();//노트의 음계가 노트에 표시되지않는다면 갱신할 필요가 없음
    }

    private void radioBtnCheck(View view){
        RadioButton radioButton = (RadioButton)view;
        RadioGroup radioGroup = (RadioGroup)view.getParent().getParent();
        if(radioGroup.getTag()!=null) ((RadioButton)radioGroup.getTag()).setChecked(false);
        radioButton.setChecked(true);
        radioGroup.setTag(radioButton);
    }

    public void slideCopy(View view){
        Selected selected = resources.getSelected();
        if (selected == null) return;
        List<Note> selectedNoteList = selected.getSelectedNoteList();
        if(selectedNoteList.size()==0) return;
        StringBuilder clipboard = new StringBuilder();
        int start = selectedNoteList.get(0).getStartIndex();
        for(int i=0;i<selectedNoteList.size();i++){
            Note note = selectedNoteList.get(i);
            clipboard.append(note.getStartIndex()-start);
            clipboard.append(",");
            clipboard.append(note.getSize());
            clipboard.append(",");
            clipboard.append(note.getPitch());
            clipboard.append(",");
            clipboard.append(note.getVolume());
            if(i!=selectedNoteList.size()-1) clipboard.append("#");
        }
        setting.setClipBoard(clipboard.toString());
        Toast.makeText(this, getString(R.string.copy_message) , Toast.LENGTH_SHORT).show();
    }

    private void slideUp(){
        Selected selected = resources.getSelected();
        if(selected==null) return;
        List<Note> selectedNoteList = selected.getSelectedNoteList();
        if(selected.getSize()==0&&selectedNoteList.size()==0) return;
        int instType  = resources.getTrackList().get(selected.getY()).getInstType();
        ((ScrollView)drumGroup.getParent()).setVisibility(instType<0?View.VISIBLE:View.GONE);
        ((LinearLayout)pitchGroup.getParent()).setVisibility(instType>=0?View.VISIBLE:View.GONE);
        if(instType>=0) {
            findViewById(R.id.relative).setVisibility(selectedNoteList.size()>1?View.VISIBLE:View.GONE);
            pitchGroup.setVisibility(selectedNoteList.size()<=1?View.VISIBLE:View.GONE);
            octaveBar.setVisibility(selectedNoteList.size()<=1?View.VISIBLE:View.GONE);
        }
        if(selectedNoteList.size()==1){
            Note note = selectedNoteList.get(0);
            if(instType>=0){
                int pitch = note.getPitch();
                radioBtnCheck(pitchGroup.findViewWithTag(String.valueOf(pitch%12)));
                octaveBar.setProgress(pitch/12-1);
            }else{
                radioBtnCheck(drumGroup.findViewWithTag(note.getPitch()));
                ((ScrollView)drumGroup.getParent()).scrollTo(0, (int)((LinearLayout)((RadioButton)drumGroup.getTag()).getParent()).getY());
            }
            volumeBar.setProgress(note.getVolume());
        }
        noteLineImageView.invalidate();
        noteTableImageView.invalidate();
        slide.startAnimation(translateUpAnim);
    }

    public void slideRemove(View view){
        Selected selected = resources.getSelected();
        if(selected!=null){
            Track track = resources.getTrackList().get(selected.getY());
            for(Note note : selected.getSelectedNoteList()){
                track.removeNoteBlock(note.getStartIndex(), note.getSize());
                if(setting.isSaved()) setting.setSaved(false);
            }
        }
        slideCancel();
    }

    public void slideCancel(){
        mediaManager.stopAudio(MediaManager.MIDI_PREVIEW_PLAYER);
        resources.setSelected(null);
        noteLineImageView.invalidate();
        noteTableImageView.invalidate();
        if(slide.getVisibility()==View.VISIBLE) slide.setVisibility(View.INVISIBLE);
    }

    class PlayRunnable implements Runnable{
        boolean useRecording;
        PlayRunnable(boolean useRecording){
            this.useRecording = useRecording;
        }
        @Override
        public void run() {
            if(useRecording) mediaManager.startRecording();
            long startTime = System.currentTimeMillis();
            int end = 0;
            List<Track> trackList = resources.getTrackList();
            for(int i=0;i<trackList.size();i++){
                if(end<trackList.get(i).getEnd()) end = trackList.get(i).getEnd();
            }
            int startPosition = resources.getStartPosition();
            int bpm = resources.getBpm();
            double duration = (end-startPosition)*2500.0/bpm;   //(size * 60000) / (bpm * 24) 밀리세컨드
            while (mediaManager.isPlaying(MediaManager.MIDI_PLAYER)) {
                double percentage = (System.currentTimeMillis()-startTime)/duration;
                if(percentage>1) percentage = 1;
                resources.setPlayPosition((int)(startPosition+(end-startPosition)*percentage));
                int noteWidth = setting.getNoteWidth();
                int currentPlayPosition = resources.getPlayPosition();
                if(currentPlayPosition*(noteWidth/12)>resources.getDrawPositionX()+noteLineImageView.getWidth()){
                    resources.setDrawPositionX(currentPlayPosition*(noteWidth/12));
                }
                noteLineImageView.invalidate();
                noteTableImageView.invalidate();
            }
            ((ImageButton) findViewById(R.id.playBtn)).setImageResource(R.drawable.play_btn);
            resources.setDrawPositionX(startPosition*(setting.getNoteWidth()/12));
            resources.setPlayPosition(-1);
            noteLineImageView.invalidate();
            noteTableImageView.invalidate();
            if (useRecording) shareFile(mediaManager.stopRecording());
        }

    }

    public void playBtnClicked(View view){
        if(mediaManager.isPlaying(MediaManager.MIDI_PLAYER)){
            mediaManager.stopAudio(MediaManager.MIDI_PLAYER);
        }else {
            ((ImageButton) findViewById(R.id.playBtn)).setImageResource(R.drawable.pause_btn);
            int startPosition = resources.getStartPosition();
            int noteWidth = setting.getNoteWidth();
            resources.setDrawPositionX(startPosition*(noteWidth/12));
            resources.setPlayPosition(startPosition);
            noteLineImageView.invalidate();
            noteTableImageView.invalidate();
            Thread thread = new Thread(new PlayRunnable(false));
            try {
                mediaManager.playMidi(resources.getTrackList(), startPosition, resources.getTempoOver(), resources.getTempoUnder(), resources.getBpm());
            } catch (Exception e) {
            }
            thread.start();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        resources.setSelectedTrack(-1);
        instrumentImageView.invalidate();
        if(resultCode!=RESULT_OK) return;
        Bundle bundle = data.getExtras();
        if(bundle==null) return;
        if(requestCode==REQUEST_CODE_SELECT_INST) {
            int instCode = bundle.getInt("instCode");
            int selectedTrack = bundle.getInt("selectedTrack");
            List<Track> trackList = resources.getTrackList();
            if(selectedTrack==-1){
                Track track = new Track(instCode);
                trackList.add(track);
                int maxDrawPositionY = trackList.size()*setting.getNoteHeight()-noteTableImageView.getHeight();
                if(maxDrawPositionY<0) maxDrawPositionY = 0;
                resources.setDrawPositionY(maxDrawPositionY);
                if(setting.isSaved()) setting.setSaved(false);
            }else if(trackList.get(selectedTrack).getInstType()!=instCode){
                trackList.get(selectedTrack).setInstType(instCode);
                if(setting.isSaved()) setting.setSaved(false);
            }
            noteTableImageView.invalidate();
            instrumentImageView.invalidate();
        }else if(requestCode==REQUEST_CODE_SELECT_UNIT) {
            int unit = bundle.getInt("unit");
            if(setting.getNoteType()!=unit) {
                setting.setNoteType(unit);
                setUnitDrawable(unit);
                noteLineImageView.invalidate();
                noteTableImageView.invalidate();
            }
        }else if(requestCode==REQUEST_CODE_EDIT_BPM) {
            int bpm = bundle.getInt("bpm");
            if(resources.getBpm()!=bpm) {
                resources.setBpm(bpm);
                ((TextView) findViewById(R.id.bpmText)).setText(String.valueOf(bpm));
                if(setting.isSaved()) setting.setSaved(false);
            }
        }else if(requestCode==REQUEST_CODE_LOAD_PROJECT){
            main.setVisibility(View.INVISIBLE);
            String filename = bundle.getString("filename", "");
            if(filename.isEmpty()||loadFile(projectDirPath + filename + ".txt")<1) return;
            setting.setFilename(filename);
            setting.setSaved(true);
            noteLineImageView.invalidate();
            noteTableImageView.invalidate();
            instrumentImageView.invalidate();
            ((TextView)findViewById(R.id.tempoOverText)).setText(String.valueOf(resources.getTempoOver()));
            ((TextView)findViewById(R.id.tempoUnderText)).setText(String.valueOf(resources.getTempoUnder()));
            ((TextView)findViewById(R.id.bpmText)).setText(String.valueOf(resources.getBpm()));
            main.startAnimation(fadeInAnim);
        }
    }

    private void setUnitDrawable(int unit){
        ImageButton unitImageBtn = findViewById(R.id.unitImageBtn);
        switch(unit){
            case 12:
                unitImageBtn.setImageResource(R.drawable.note_8th);
                break;
            case 6:
                unitImageBtn.setImageResource(R.drawable.note_16th);
                break;
            case 3:
                unitImageBtn.setImageResource(R.drawable.note_32nd);
                break;
            case 4:
                unitImageBtn.setImageResource(R.drawable.note_8th3);
                break;
            case 2:
                unitImageBtn.setImageResource(R.drawable.note_16th3);
                break;
            default:
                break;
        }
    }

    public void relativePitch(View view){
        Selected selected = resources.getSelected();
        if(selected==null) return;
        int relative = Integer.parseInt(view.getTag().toString());
        for(Note note : selected.getSelectedNoteList()){
            int after = note.getPitch() + relative;
            if(after>=120 || after < 12) return;
        }
        for(Note note : selected.getSelectedNoteList()){
            note.setPitch(note.getPitch() + relative);
            if(setting.isSaved()) setting.setSaved(false);
        }
        try {
            mediaManager.playPreview(resources.getTrackList().get(selected.getY()).getInstType(), selected.getSelectedNoteList(), resources.getTempoOver(), resources.getTempoUnder(), resources.getBpm());
        } catch (IOException e) {}
        //noteTableImageView.invalidate();
    }
}
