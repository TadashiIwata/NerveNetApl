package jp.co.nassua.nervenet.voicerecorder;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;

/**
 * Created by I.Tadshi on 2018/05/17.
 */

public class SendWaitTimer extends CountDownTimer {
    public SendWaitTimer(long recMaxtime, long recInterval) {
        super(recMaxtime, recInterval);
    }

    static VoiceMessage voiceMessage;

    @Override
    public void onFinish() {
        Context context;
        // タイムアウト、録音終了指示を発行する。
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Android 8.0以降
            intent = new Intent(VoiceMessage.actMyself, RecorderRcvNotify.class);
            intent.setAction(ConstantRecorder.ACT_REQUEST);
            intent.putExtra(ConstantRecorder.EXTRA_EVENT, ConstantRecorder.EVENT_BSCHECK);
            VoiceMessage.actMyself.sendBroadcast(intent);
        } else {
            // Android 7.1.1以前
            intent = new Intent(ConstantRecorder.ACT_REQUEST);
            intent.putExtra(ConstantRecorder.EXTRA_EVENT, ConstantRecorder.EVENT_BSCHECK);
            VoiceMessage.actMyself.sendBroadcast(intent);
        }
    }

    @Override
    public void onTick(long recUntilFinished) {
        // 1秒ごとに呼ばれるがなにもしない。
    }

}
