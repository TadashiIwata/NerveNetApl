package jp.co.nassua.nervenet.voicerecorder;

/**
 * Created by I.Tadshi on 2016/07/11.
 */
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.lang.String;
import java.util.TimeZone;


public class WaveFile extends VoiceRecorder {
    SimpleDateFormat recSdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    public static VoiceMessageSubFunctions voiceMessageSubFunctions;

    private final int SAMPLING_RATE = 44100;
    private final int FILESIZE_SEEK = 4;
    private final int DATASIZE_SEEK = 40;
    private RandomAccessFile raf;
    private File recFile;
    private String fileName = "test.wav";
    private byte[] RIFF = {'R', 'I', 'F', 'F'};
    private int fileSize = 36;
    private byte[] WAVE = {'W', 'A', 'V', 'E'};
    private byte[] fmt = {'f', 'm', 't', ' '};
    private int fmtSize = 16;
    private byte[] fmtID = {1, 0};
    private short chCount = 1;
    private int sampleRate = SAMPLING_RATE;
    private int bytePerSec = SAMPLING_RATE * 2;
    private short blockSize = 2;
    private short bitPerSample = 16;
    private byte[] data = {'d', 'a', 't', 'a'};
    private int dataSize = 0;

    public WaveFile() {
        recSdf.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
    }

    public void createFile() {
        // 録音用ファイル名を生成
        Date recDate = new Date();
        fileName = "Vm" + recSdf.format(recDate).toString();
        VoiceRecorder.recFileName = fileName;
        fileName = Environment.getExternalStorageDirectory() + "/" + fileName + ".wav";
        // Emulatorでは Permission Errorで動作不可。
        //fileName = Environment.getExternalStorageDirectory() + "/Download/" + recSdf.format(recDate).toString() + ".wav";

        recFile = new File(fileName);
        if (recFile.exists()) {
            recFile.delete();
        }
        try {
            recFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            raf = new RandomAccessFile(recFile, "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        voiceMessageSubFunctions = new VoiceMessageSubFunctions();
        voiceMessageSubFunctions.setPcmFileName(fileName);
        try {
            raf.seek(0);
            raf.write(RIFF);
            raf.write(littleEndianInteger(fileSize));
            raf.write(WAVE);
            raf.write(fmt);
            raf.write(littleEndianInteger(fmtSize));
            raf.write(fmtID);
            raf.write(littleEndianShort(chCount));
            raf.write(littleEndianInteger(sampleRate));
            raf.write(littleEndianInteger(bytePerSec));
            raf.write(littleEndianShort(blockSize));
            raf.write(littleEndianShort(bitPerSample));
            raf.write(data);
            raf.write(littleEndianInteger(dataSize));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] littleEndianInteger(int i) {
        byte[] buffer = new byte[4];
        buffer[0] = (byte) i;
        buffer[1] = (byte) (i >> 8);
        buffer[2] = (byte) (i >> 16);
        buffer[3] = (byte) (i >> 24);
        return buffer;
    }

    public void addBigEndianData(short[] shortData) {
        try {
            raf.seek(raf.length());
            raf.write(littleEndianShorts(shortData));
        } catch (IOException e) {
            e.printStackTrace();
        }
        updateFileSize();
        updateDataSize();
    }

    private void updateFileSize() {
        fileSize = (int) (recFile.length() - 8);
        byte[] fileSizeBytes = littleEndianInteger(fileSize);
        try {
            raf.seek(FILESIZE_SEEK);
            raf.write(fileSizeBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateDataSize() {

        dataSize = (int) (recFile.length() - 44);
        byte[] dataSizeBytes = littleEndianInteger(dataSize);
        try {
            raf.seek(DATASIZE_SEEK);
            raf.write(dataSizeBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] littleEndianShort(short s) {
        byte[] buffer = new byte[2];
        buffer[0] = (byte) s;
        buffer[1] = (byte) (s >> 8);
        return buffer;
    }

    private byte[] littleEndianShorts(short[] s) {
        byte[] buffer = new byte[s.length * 2];
        int i;
        for (i = 0; i < s.length; i++) {
            buffer[2 * i] = (byte) s[i];
            buffer[2 * i + 1] = (byte) (s[i] >> 8);
        }

        return buffer;
    }

    public void close() {
        try {
            raf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
