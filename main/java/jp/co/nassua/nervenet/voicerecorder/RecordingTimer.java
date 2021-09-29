package jp.co.nassua.nervenet.voicerecorder;

import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;

import java.util.Timer;
import java.util.TimerTask;

import jp.co.nassua.nervenet.voicemessage.VoiceMessageCommon;

/**
 * Created by I.Tadshi on 2016/08/05.
 */
public class RecordingTimer extends CountDownTimer {
    public RecordingTimer(long recMaxtime, long recInterval) {
        super(recMaxtime, recInterval);
    }

    @Override
    public void onFinish() {
        Intent intent;
        // タイムアウト、録音終了指示を発行する。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Android 8.0以降
            intent = new Intent(VoiceMessage.actMyself, RecorderRcvNotify.class);
            intent.setAction(ConstantRecorder.ACT_REQUEST);
            intent.putExtra(ConstantRecorder.EXTRA_EVENT, ConstantRecorder.EVENT_STOP);
            VoiceMessage.actMyself.sendBroadcast(intent);
        } else {
            // Android 7.1.1以前
            intent = new Intent(ConstantRecorder.ACT_REQUEST);
            intent.putExtra(ConstantRecorder.EXTRA_EVENT, ConstantRecorder.EVENT_STOP);
            VoiceMessage.actMyself.sendBroadcast(intent);
        }
    }

    @Override
    public void onTick(long recUntilFinished) {
        // 1秒ごとに呼ばれるがなにもしない。
    }

}
