package jp.co.nassua.nervenet.vmphonedepend;

/**
 * Created by I.Tadshi on 2016/07/26.
 */
public class UserProfilePermission {
    private static boolean profileRead = false;
    private static boolean android6 = false;
    private static boolean storageWrite = false;
    private static boolean inputMic = false;
    private static boolean camera = false;
    private static boolean vibrate = false;

    public void setReadProfileStatus(boolean flag) {
        profileRead = flag;
    }

    public boolean getReadProfileStatus() {
        return profileRead;

    }
    public void setStorageWriteStatus(boolean flag) {
        storageWrite = flag;
    }

    public boolean getStorageWriteStatus() {
        return storageWrite;
    }

    public void setInputMicStatus(boolean flag) {
        inputMic = flag;
    }

    public boolean getInputMicStatus() {
        return inputMic;
    }

    public void setAndroid6Status(boolean flag){
        android6 = flag;
    }

    public boolean getAndroid6Status() {
        return android6;
    }

    public void setCameraStatus(boolean flag) {
        camera = flag;
    }
     public boolean getCameraStatus() {
        return camera;
     }

     public void setVibrateStatus(boolean flag) {
        vibrate = flag;
     }
     public boolean getVibrateStatus() {
        return vibrate;
     }
}
