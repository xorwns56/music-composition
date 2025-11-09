package com.xorwns56.music_composition_app;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import static java.lang.Thread.sleep;


/**
 * Created by xorwns56 on 2018-04-17.
 */

class MediaManager {
    static final int MIDI_PREVIEW_PLAYER = 0;
    static final int MIDI_PLAYER = 1;

    private MediaPlayer[] mediaPlayers = new MediaPlayer[2];
    private String dirPath;
    private boolean isRecording = false;
    private AudioRecord record = null;

    MediaManager(String dirPath){
        this.dirPath = dirPath;
        for(int i=0;i< mediaPlayers.length;i++) mediaPlayers[i] = null;
    }

    void playPreview(int instType, List<Note> noteList, int tempoOver, int tempoUnder, int bpm) throws IOException{
        if(noteList.size()==0) return;
        String previewPath = dirPath + "Preview.mid";
        Track track = new Track(instType);
        for(Note note : noteList) track.addNoteBlock(new Note(note.getStartIndex()-noteList.get(0).getStartIndex(),note.getSize(),note.getPitch(),note.getVolume()));
        List<Track> tracks = new ArrayList<>();
        tracks.add(track);
        if(isPlaying(MIDI_PREVIEW_PLAYER)) stopAudio(MIDI_PREVIEW_PLAYER);
        MidiFile.writeFile(previewPath,tracks,0,tempoOver,tempoUnder,bpm);
        playAudio(MIDI_PREVIEW_PLAYER, previewPath);
    }

    void playMidi(List<Track> tracks, int startLine, int tempoOver, int tempoUnder, int bpm) throws IOException{
        String songPath = dirPath + "Song.mid";
        if(isPlaying(MIDI_PREVIEW_PLAYER)) stopAudio(MIDI_PREVIEW_PLAYER);
        if(isPlaying(MIDI_PLAYER)) stopAudio(MIDI_PLAYER);
        MidiFile.writeFile(songPath,tracks,startLine,tempoOver,tempoUnder,bpm);
        playAudio(MIDI_PLAYER, songPath);
    }

    private void playAudio(final int type, String filePath) throws IOException{
        mediaPlayers[type] = new MediaPlayer();
        mediaPlayers[type].setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mediaPlayers[type].reset();
                mediaPlayers[type].release();
                mediaPlayers[type] = null;
            }
        });
        mediaPlayers[type].setDataSource(filePath);
        mediaPlayers[type].prepare();
        mediaPlayers[type].start();
    }

    File getFile(List<Track> tracks, int tempoOver, int tempoUnder, int bpm) throws IOException{
        String songPath = dirPath + "Song.mid";
        MidiFile.writeFile(songPath,tracks,0,tempoOver,tempoUnder,bpm);
        return new File(songPath);
    }

    void stopAudio(int type){
        if(mediaPlayers[type]==null) return;
        mediaPlayers[type].stop();
        mediaPlayers[type].reset();
        mediaPlayers[type].release();
        mediaPlayers[type] = null;
    }

    boolean isPlaying(int type){
        return mediaPlayers[type]!=null&&mediaPlayers[type].isPlaying();
    }

    /*
    public void playVoice(int volume, String fileName, int voiceBpm, int bpm)
    {
        File file = new File(dirPath+"Tmp"+File.separator+fileName);
        int shortSizeInBytes = Short.SIZE / Byte.SIZE;
        int bufferSizeInBytes = (int) (file.length() / shortSizeInBytes);
        short[] audioData = new short[bufferSizeInBytes];
        try
        {
            InputStream inputStream = new FileInputStream(file);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            DataInputStream dataInputStream = new DataInputStream(bufferedInputStream);

            int i = 0;
            while (dataInputStream.available() > 0) audioData[i++] = dataInputStream.readShort();
            dataInputStream.close();
            AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes, AudioTrack.MODE_STREAM);
            audioTrack.setVolume((float) (1 - (Math.log(100 - volume) / Math.log(100))));

            audioTrack.setPlaybackParams(audioTrack.getPlaybackParams().setSpeed(bpm/(float)voiceBpm));
            audioTrack.play();
            audioTrack.write(audioData, 0, bufferSizeInBytes);
        }catch (IOException e){}
    }
    */

    void startRecording() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                recording();
            }
        }).start();
    }

    File stopRecording() {
        isRecording = false;
        while(record!=null){
            try {
                sleep(500);
            }catch(Exception e){
            }
        }
        RandomAccessFile accessWave = null;
        File file = new File(dirPath + "Song.wav");
        long fileLength = file.length();
        try {
            accessWave = new RandomAccessFile(file, "rw"); // 읽기-쓰기 모드로 인스턴스 생성
            accessWave.seek(4); // 4바이트 지점으로 가서
            fileLength -= 8;
            accessWave.write(new byte[]{
                    (byte) (fileLength & 0xff),
                    (byte) ((fileLength >> 8) & 0xff),
                    (byte) ((fileLength >> 16) & 0xff),
                    (byte) ((fileLength >> 24) & 0xff)
            },0,4);
            accessWave.seek(40); // 40바이트 지점으로 가서
            fileLength -= 36;
            accessWave.write(new byte[]{
                    (byte) (fileLength & 0xff),
                    (byte) ((fileLength >> 8) & 0xff),
                    (byte) ((fileLength >> 16) & 0xff),
                    (byte) ((fileLength >> 24) & 0xff)
            },0,4);
        } catch (IOException e) {
        } finally {
            if (accessWave != null) {
                try {
                    accessWave.close();
                } catch (IOException ex) {}
            }
        }
        return file;
    }

    private void recording() {
        isRecording = true;
        final int sampleRate = 44100;
        final int channel = AudioFormat.CHANNEL_IN_MONO;
        final int encoding = AudioFormat.ENCODING_PCM_16BIT;
        try
        {
            OutputStream outputStream = new FileOutputStream(dirPath + "Song.wav");
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
            DataOutputStream dataOutputStream = new DataOutputStream(bufferedOutputStream);
            int minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channel, encoding);
            short[] buffer = new short[minBufferSize];
            dataOutputStream.write(new byte[]{
                    'R', 'I', 'F', 'F', // Chunk ID
                    0, 0, 0, 0, // Chunk Size (나중에 업데이트 될것)
                    'W', 'A', 'V', 'E', // Format
                    'f', 'm', 't', ' ', //Chunk ID
                    16, 0, 0, 0, // Chunk Size = 16 고정
                    1, 0, // AudioFormat = 1 고정
                    1, 0, // Num of Channels = MONO(1) 고정
                    (byte)(sampleRate & 0xff), (byte)((sampleRate >> 8) & 0xff), (byte)((sampleRate >> 16) & 0xff), (byte)((sampleRate >> 24) & 0xff), // SampleRate
                    (byte)(sampleRate*2 & 0xff), (byte)((sampleRate*2 >> 8) & 0xff), (byte)((sampleRate*2 >> 16) & 0xff), (byte)((sampleRate*2 >> 24) & 0xff), // Byte Rate
                    2, 0, // Block Align
                    16, 0, // Bits Per Sample
                    'd', 'a', 't', 'a', // Chunk ID
                    0, 0, 0, 0 //PCM Data Size (나중에 업데이트)
            });
            record = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, sampleRate, channel, encoding, minBufferSize);
            record.startRecording();
            while (isRecording)
            {
                int numberOfShort = record.read(buffer, 0, minBufferSize);
                for (int i = 0; i < numberOfShort; i++){
                    dataOutputStream.writeByte(buffer[i] & 0xff);
                    dataOutputStream.writeByte((buffer[i]>>8) & 0xff);
                }
            }
            record.stop();
            dataOutputStream.close();
        }catch (IOException e) {
        }finally {
            record.release();
            record = null;
        }
    }

}
