package com.xorwns56.music_composition_app;
import java.io.*;
import java.util.*;

/**
 * Created by xorwns56 on 2018-04-15.
 */

public class MidiFile {
    private static final byte header1[] = new byte[] {
            0x4d, 0x54, 0x68, 0x64, 0x00, 0x00, 0x00, 0x06,
			/*
			 * FORMAT HEADER 순서대로 77(M), 84(T), 104(h), 100(d), 0, 0, 0, 6
			 */
            0x00, 0x01
			/*
			 * MIDI표현 방식(0,1,2) 중 1을 채택
			 */
    };

    private static final byte header2[] = new byte[] {
            // header1과 header2의 사이에는 유동적으로 트랙의 개수가 들어가야하므로 둘을 나누었다.
            0x00, 0x18 // 24(0x00, 0x18)로 수정 - BPM 120(0x00, 0x78), 곡의 BPM을 수정하려 할 때 이곳은 건드리지 않는다. 대신 템포헤더 부분의 beats per microsecond를 바꿔줄 것이다
    };

    private static final byte trackHeader[] = new byte[] {
            0x4d, 0x54, 0x72, 0x6B
			/*
			 * TRACK HEADER 순서대로 77(M), 84(T), 114(r), 107(k)
			 */
    };

    private static byte[] tempoHeader(int bpm){
        byte[] beatsPerMicroSec = base256((int)Math.round(60000000 / (double)bpm), 3);
       /*
        beats per microsecond
        4분음표 하나 당 마이크로초를 뜻하여 이 부분을 수정하면 bpm(beats per minute)을 조절할 수 있다.
        beats per micro second = 60000000 / beats per minute
        */
        return new byte[]{0x00, (byte)0xFF, 0x51, 0x03,beatsPerMicroSec[0], beatsPerMicroSec[1], beatsPerMicroSec[2]};
    }
    private static byte[] timeHeader(int tempoOver, int tempoUnder){
        return new byte[]{
                0x00, (byte) 0xFF, 0x58, 0x04,
                /*
			    * 박자 HEADER 0, 255, 88(박자 설정 명령), 4
			    */
                (byte) tempoOver, // 박자의 분자
                log2(tempoUnder), // 분모 2^n ex) 2일 경우 quarter(4), 3일 경우 eighth(8)
                0x18, // 	메트로놈 틱 당 미디 클록의 수 (사용되지 않음)
                0x08 // 	24미디 클록 당 32분음표의 수 ( 8이 표준)
        };
    }

    private static byte log2(int x) {
        return (byte)(Math.log(x)/Math.log(2));
    }

    private static final byte keyHeader[] = new byte[] {
            0x00, (byte)0xFF, 0x59, 0x02,
			/*
			 * 음계 HEADER 0, 255, 89(음계 설정 명령), 2
			 */
            0x00, 0x00 // 0, 0 C장조 major - 기본
    };

    private static final byte footer[] = new byte[] {
            0x00, (byte)0xFF, 0x2F, 0x00
			/*
			 * TRACK 종료 footer 0, 255, 47(트랙의 끝을 알림), 0
			 */
    };

    public synchronized static void writeFile(String filename, List<Track> tracks, int startLine, int tempoOver, int tempoUnder, int bpm) throws IOException { // 악기 별 노트정보가 쓰여진 트랙을 받아와서 파일을 작성한다.

        FileOutputStream fos = new FileOutputStream(filename);

        fos.write(header1);

        int numberOfTrack = tracks.size() + 1; // 매개변수로 받아온 연주트랙 + 기본정보가 포함된 트랙 1개
        fos.write(base256(numberOfTrack, 2)); // 트랙의 개수 - 2자리의 256진수로 변환
        fos.write(header2);
        fos.write(trackHeader);

        byte[] tempoHeader = tempoHeader(bpm);
        byte[] timeHeader = timeHeader(tempoOver, tempoUnder);

        int headerSize = tempoHeader.length + keyHeader.length + timeHeader.length + footer.length;
        fos.write(base256(headerSize, 4)); // 기본 트랙의 길이를 4자리의 256진수로 변환
        fos.write(tempoHeader);
        fos.write(keyHeader);
        fos.write(timeHeader);
        fos.write(footer);

        // 기본 트랙 종료

        // 연주 트랙 시작

        List<Integer> usedInstruments = new ArrayList<>();
        List<byte[]> trackEvents;

        for (int i = 0; i < tracks.size(); i++) {
            fos.write(trackHeader); // 트랙 헤더를 파일에 씀
            trackEvents = new ArrayList<>();

            int instType = tracks.get(i).getInstType();
            int instChannel;
            boolean IsDrum = instType < 0;
            if(IsDrum) { //드럼일 경우
                instChannel = 9; //MIDI에서 드럼은 10채널(9) 고정이다.
                instType = 0; //드럼은 instType가 아닌 Note의 pitch의 값으로 악기를 구분하기 때문에 그냥 0으로 둔다
            }else{
                instChannel = usedInstruments.indexOf(instType);
                if(instChannel==-1) { //처음 쓰이는 악기
                    instChannel = usedInstruments.size() + (usedInstruments.size() >= 9 ? 1:0);
                    usedInstruments.add(instType);
                }else instChannel = instChannel + (instChannel >= 9 ? 1:0); //한번 이상 쓰였던 악기이므로 기존의 채널에 할당한다
            }

            trackEvents.add(instDefine(instChannel,instType)); // 트랙의 악기코드 정보,악기채널 정보를 기록

            Track track = tracks.get(i); // 트랙의 노트리스트를 받아옴

            boolean isFirstNote = true;

            int j = 0;
            Note prevNote = null;
            Note note;
            while((note=track.getNoteBySparseArrayIndex(j))!=null){
                if(note.getStartIndex()<startLine){
                    j += note.getSize();
                    continue;
                }
                int delay = note.getStartIndex() - (prevNote==null?startLine:(prevNote.getStartIndex()+prevNote.getSize()));
                int volume = 127*note.getVolume()/100;
                int pitch = note.getPitch();
                if(isFirstNote){
                    trackEvents.add(noteOn(instChannel,delay,pitch, volume));
                    isFirstNote = false;
                }else{
                    trackEvents.add(noteOn(delay,pitch, volume));
                }
                trackEvents.add(noteOff(note.getSize(), pitch));

                prevNote = note;
                j+=note.getSize();
            }

            int trackSize = getTrackEventSize(trackEvents) + footer.length; // 트랙 헤더 이후의 연주트랙 길이(트랙의 종료를 알리는 footer의 길이도 포함)를 계산
            fos.write(base256(trackSize, 4)); // 계산한 트랙의 길이를 4자리의 256진수로 변환하여 파일에 씀

            for (int x = 0; x < trackEvents.size(); x++) fos.write(trackEvents.get(x)); // 연주트랙 정보 파일에 씀

            fos.write(footer); //트랙 종료 footer를 파일에 씀
        }

        fos.close();

    }

    private static int getTrackEventSize(List<byte[]> trackEvent) {
        int size = 0;
        for (int i = 0; i < trackEvent.size(); i++)
            size += trackEvent.get(i).length;
        return size;
    }

    private static byte[] base256(int size, int length) { // size 크기의 수를 length 길이의 256진수로 바꾼다.
        byte[] array = new byte[length];
        for (int i = array.length-1; i>=0; i--) array[array.length-1-i] = (byte) ((size>>(8*i)) & 0xff);
        return array;
    }

    private static byte[] noteOn(int instChannel, int delay, int pitch, int volume) { // 트랙에서 처음으로 실행되는 NoteOn이다.
        byte[] data = null;
        for(int i=3;i>=0;i--){
            byte tmp = (byte) ((delay>>(7*i))&0x7f);
            if(data==null&&tmp>0) data = new byte[i+4];
            if(data!=null) data[data.length-(i+4)] = (byte) (tmp + (i!=0?128:0));
        }
        if(data==null) data = new byte[]{0,0,0,0};
        data[data.length-1] = (byte)volume;
        data[data.length-2] = (byte)pitch;
        data[data.length-3] = (byte) (144 + instChannel);
        return data;
    }

    private static byte[] noteOn(int delay, int pitch, int volume) {
        byte[] data = null;
        for(int i=3;i>=0;i--){
            byte tmp = (byte) ((delay>>(7*i))&0x7f);
            if(data==null&&tmp>0) data = new byte[i+3];
            if(data!=null) data[data.length-(i+3)] = (byte) (tmp + (i!=0?128:0));
        }
        if(data==null) data = new byte[]{0,0,0};
        data[data.length-1] = (byte)volume;
        data[data.length-2] = (byte)pitch;
        return data;
    }

    private static byte[] noteOff(int duration, int pitch) {
        byte[] data = null;
        for(int i=3;i>=0;i--){
            byte tmp = (byte) ((duration>>(7*i))&0x7f);
            if(data==null&&tmp>0) data = new byte[i+3];
            if(data!=null) data[data.length-(i+3)] = (byte) (tmp + (i!=0?128:0));
        }
        if(data==null) data = new byte[]{0,0,0};
        data[data.length-1] = 0;
        data[data.length-2] = (byte)pitch;
        return data;
    }

    private static byte[] instDefine(int instChannel, int instCode) {
        byte[] data = new byte[3];
        data[0] = 0;
        data[1] = (byte) (192 + instChannel);
        data[2] = (byte)instCode;
        return data;
    }

}
