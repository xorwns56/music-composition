package com.xorwns56.music_composition_app;


import java.util.ArrayList;
import java.util.List;

public class Resources {
    private int drawPositionX;
    private int drawPositionY;
    private int startPosition;
    private int playPosition;
    private int selectedTrack;
    private Selected selected;
    private List<Track> trackList;
    private int tempoOver;
    private int tempoUnder;
    private int bpm;

    Resources(MainActivity mainActivity){
        setDrawPositionX(0);
        setDrawPositionY(0);
        setStartPosition(0);
        setPlayPosition(-1);
        setSelectedTrack(-1);
        setSelected(null);
        setTrackList(new ArrayList<Track>());
        setTempoOver(4);
        setTempoUnder(4);
        setBpm(120);
        mainActivity.noteLineImageView.setTag(R.id.resources,this);
        mainActivity.noteTableImageView.setTag(R.id.resources,this);
        mainActivity.instrumentImageView.setTag(R.id.resources,this);
    }

    public int getDrawPositionX() {
        return drawPositionX;
    }
    public void setDrawPositionX(int drawPositionX) {
        this.drawPositionX = drawPositionX;
    }
    public int getDrawPositionY() {
        return drawPositionY;
    }
    public void setDrawPositionY(int drawPositionY) {
        this.drawPositionY = drawPositionY;
    }
    public int getStartPosition() {
        return startPosition;
    }
    public void setStartPosition(int startPosition) {
        this.startPosition = startPosition;
    }
    public int getPlayPosition() {
        return playPosition;
    }
    public void setPlayPosition(int playPosition) {
        this.playPosition = playPosition;
    }
    public int getSelectedTrack() {
        return selectedTrack;
    }
    public void setSelectedTrack(int selectedTrack) {
        this.selectedTrack = selectedTrack;
    }
    public Selected getSelected() {
        return selected;
    }
    public void setSelected(Selected selected) {
        this.selected = selected;
    }
    public List<Track> getTrackList() {
        return trackList;
    }
    public void setTrackList(List<Track> trackList) {
        this.trackList = trackList;
    }
    public int getTempoOver() {
        return tempoOver;
    }
    public void setTempoOver(int tempoOver) {
        this.tempoOver = tempoOver;
    }
    public int getTempoUnder() {
        return tempoUnder;
    }
    public void setTempoUnder(int tempoUnder) {
        this.tempoUnder = tempoUnder;
    }
    public int getBpm() {
        return bpm;
    }
    public void setBpm(int bpm) {
        this.bpm = bpm;
    }
}
