package jp.co.nassua.nervenet.voicerecorder;

import android.content.Intent;
import android.provider.ContactsContract;

// Voice Message録音 Activity間　API
//
// Copyright (C) 2016 Nassua Solutions Corp.
// Iwata Tadashi <iwata@nassua.co.jp>
//
public class VoiceMessageSubFunctions {
    private static String PhoneNumber;
    private static String PcmFileName;
    private static String UriBoart;
    private static int RecMaxTime;
    private static boolean LockVolKey;
    private static int VolKeyCnt;
    private static boolean VolKeyUp;

    public void setUriBoart(String uriBoart) {
        UriBoart = uriBoart;
    }

    public String getUriBoart() {
        return UriBoart;
    }

    public void setPhoneNumber(String number) {
        PhoneNumber = number;
    }

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public void setPcmFileName(String filename) {
        PcmFileName = filename;
    }

    public String getPcmFileName() {
        return PcmFileName;
    }

    public void setRecTimeValue(int recmaxtime) {
        RecMaxTime = recmaxtime;
    }

    public int getRecTimeValue() {
        return RecMaxTime;
    }

    public void LockVolumeKey() {
        LockVolKey = true;
    }

    public void UnLockVolumeKey() {
        LockVolKey = false;
    }

    public boolean getVolumeKeyStatus() {
        return LockVolKey;
    }

    public void VolumeKeyCountDownStart() {
        VolKeyCnt = 2;
    }

    public void VolumeKeyCountDown() {
        if (VolKeyCnt > 0) {
            VolKeyCnt--;
        }
    }

    public int getVolumeKeyCount() {
        return VolKeyCnt;
    }

    public void initVolumeKeyCount() {
        VolKeyCnt = 0;
    }

    public void initVolumeKeyUp() {
        VolKeyUp = false;
    }
    public void setVolumeKeyUp() {
        VolKeyUp = true;
    }

    public boolean getVolumeKeyUpStatus() {
        return VolKeyUp;
    }

}
