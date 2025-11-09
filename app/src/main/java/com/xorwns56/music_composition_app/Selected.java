package com.xorwns56.music_composition_app;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xorwns56 on 2018-05-27.
 */

public class Selected {
    private int x;
    private int y;
    private int size;
    private List<Note> selectedNoteList;

    Selected(int x,int y,int size){
        this.x  = x;
        this.y = y;
        this.size  = size;
        this.selectedNoteList = new ArrayList<>();
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size){
        this.size = size;
    }

    public List<Note> getSelectedNoteList() { return selectedNoteList; }
}

