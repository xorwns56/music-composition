package com.xorwns56.music_composition_app;

/**
 * Created by xorwns56 on 2018-04-15.
 */

public class Note {
    private int startIndex;
    private int size;
    private int volume;
    private int pitch;

    Note(int startIndex, int size, int pitch, int volume){
        setStartIndex(startIndex);
        setSize(size);
        setPitch(pitch);
        setVolume(volume);
    }

    int getStartIndex() { return startIndex; }
    void setStartIndex(int startIndex) { this.startIndex = startIndex; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    int getVolume() {
        return volume;
    }
    void setVolume(int volume) {
        this.volume = volume;
    }
    int getPitch() {
        return pitch;
    }
    void setPitch(int pitch) {
        this.pitch = pitch;
    }
}

