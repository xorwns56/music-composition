package com.xorwns56.music_composition_app;

import android.util.SparseArray;

/**
 * Created by xorwns56 on 2018-04-15.
 */

class Track {
    private int instType;
    private SparseArray<Note> sparseArray;

    Track(int instType) {
        setInstType(instType);
        sparseArray = new SparseArray<>();
    }

    int getInstType() { return instType; }

    void setInstType(int instType) { this.instType = instType; }

    Note getNote(int index){
        return sparseArray.get(index);
    }

    Note addNoteBlock(Note note){
        for(int i=note.getStartIndex();i<note.getStartIndex()+note.getSize();i++) sparseArray.append(i,note);
        return note;
    }

    void changeNoteBlock(int startIndex, int extend, int movement){
        if(extend==0&&movement==0) return;
        Note note = sparseArray.get(startIndex);
        int oldSize = note.getSize();
        note.setStartIndex(startIndex+movement);
        note.setSize(oldSize+extend);
        if(note.getStartIndex()<startIndex){
            for(int i=note.getStartIndex();i<startIndex&&i<note.getStartIndex()+note.getSize();i++){
                sparseArray.append(i,note);
            }
        }else{
            for(int i=startIndex;i<note.getStartIndex()&&i<startIndex+oldSize;i++){
                sparseArray.remove(i);
            }
        }

        if(note.getStartIndex()+note.getSize()<startIndex+oldSize){
            for(int i=note.getStartIndex()+note.getSize()>startIndex?note.getStartIndex()+note.getSize():startIndex;i<startIndex+oldSize;i++){
                sparseArray.remove(i);
            }
        }else{
            for(int i=startIndex+oldSize>note.getStartIndex()?startIndex+oldSize:note.getStartIndex();i<note.getStartIndex()+note.getSize();i++){
                sparseArray.append(i,note);
            }
        }
    }

    void removeNoteBlock(int startIndex, int size){
        for(int i=startIndex;i<startIndex+size;i++) {
            sparseArray.remove(i);
        }
    }

    Note getNoteBySparseArrayIndex(int index){
        if(index<0||index>=sparseArray.size()) return null;
        return sparseArray.get(sparseArray.keyAt(index));
    }

    int getSparseArrayStartIndexByNote(Note note){
        return sparseArray.indexOfKey(note.getStartIndex());
    }

    int getEnd(){
        if(sparseArray.size()==0) return 0;
        else return sparseArray.keyAt(sparseArray.size()-1)+1;
    }
}


