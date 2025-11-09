package com.xorwns56.music_composition_app;

class Setting {
    private int noteType;
    private int noteWidth;
    private int noteHeight;
    private String filename;
    private boolean isSaved;
    private String clipBoard;

    Setting(MainActivity mainActivity){
        setNoteType(12);
        setNoteWidth(72);
        setNoteHeight(180);
        setFilename("");
        setSaved(false);
        setClipBoard("");
        mainActivity.noteLineImageView.setTag(R.id.setting,this);
        mainActivity.noteTableImageView.setTag(R.id.setting,this);
        mainActivity.instrumentImageView.setTag(R.id.setting,this);
    }

    int getNoteType() { return noteType; }
    void setNoteType(int noteType) { this.noteType = noteType; }
    int getNoteWidth() { return noteWidth; }
    void setNoteWidth(int noteWidth) { this.noteWidth = noteWidth; }
    int getNoteHeight() { return noteHeight; }
    void setNoteHeight(int noteHeight) { this.noteHeight = noteHeight; }
    String getFilename() { return filename; }
    void setFilename(String filename) { this.filename = filename; }
    boolean isSaved() { return isSaved; }
    void setSaved(boolean saved) { isSaved = saved; }
    String getClipBoard() { return clipBoard; }
    void setClipBoard(String clipBoard) { this.clipBoard = clipBoard; }
}
