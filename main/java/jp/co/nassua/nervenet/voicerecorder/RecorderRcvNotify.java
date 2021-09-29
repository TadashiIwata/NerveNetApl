package jp.co.nassua.nervenet.voicerecorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import jp.co.nassua.nervenet.voicemessage.ActMain;
import jp.co.nassua.nervenet.voicemessage.VoiceMessageCommon;

/**
 * Created by I.Tadshi on 2016/08/01.
 */
public class RecorderRcvNotify extends BroadcastReceiver {
    private static boolean recExec = false;
    private static boolean reqStop = false;
    private static boolean reqRead = false;
    private static boolean recTimerOn = false;
    private static long recTime = 0;
    private static VoiceRecorder voiceRecorder;
    private static RecordingTimer recordingTimer = null;
    private static VoiceMessageCommon voiceMessageCommon;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle extras = intent.getExtras();
        String cmd;
        if (voiceRecorder == null) {
            voiceRecorder = new VoiceRecorder();
        }
        if (voiceMessageCommon == null) {
            voiceMessageCommon = new VoiceMessageCommon();
        }

        // カウントダウンタイマー初期設定。
        if (recordingTimer == null) {
            recordingTimer = new RecordingTimer(ActMain.actMyself.MaxRecTime * 1000, 1000);
            recTime = ActMain.actMyself.MaxRecTime;
        }
        // カウントダウンタイマー値更新
        if (recTime != ActMain.actMyself.MaxRecTime) {
            recTime = ActMain.actMyself.MaxRecTime;
        }

        if (action.equalsIgnoreCase(ConstantRecorder.ACT_NOTIFY)) {
            // 録音通知
            if (extras != null) {
                cmd = extras.getString(ConstantRecorder.EXTRA_EVENT);
                if (cmd.equalsIgnoreCase(ConstantRecorder.EVENT_STOP)) {
                    if (recExec) {
                        // 録音停止通知
                        recExec = false;
                        VoiceMessage.actMyself.vmn.SendMessageNotify(VoiceMessageNotify.STAT_SEND_COMPLETE);
                    }
                } else if (cmd.equalsIgnoreCase(ConstantRecorder.EVENT_SEND)) {
                    // 送信中メッセージ表示
                    VoiceMessage.actMyself.vmn.SendMessageNotify(VoiceMessageNotify.STAT_SEND_EXEC);
                } else if (cmd.equalsIgnoreCase(ConstantRecorder.EVENT_SEND_END)) {
                    VoiceMessage.actMyself.EnableRecButton();
                }
            }
        } else if (action.equalsIgnoreCase(ConstantRecorder.ACT_REQUEST)) {
            // 録音操作
            if (extras != null) {
                cmd = extras.getString(ConstantRecorder.EXTRA_EVENT);
                if (cmd.equalsIgnoreCase(ConstantRecorder.EVENT_START)) {
                    if (!recExec) {
                        recExec = true;
                        reqStop = false;
                        reqRead = false;
                        recTimerOn = true;
                        // 録音開始
                        voiceRecorder.startRecording(context);
                        // カウントダウンタイマー開始
                        recordingTimer = new RecordingTimer(recTime * 1000, 1000);
                        recordingTimer.start();
                    }
                } else if (cmd.equalsIgnoreCase(ConstantRecorder.EVENT_STOP)) {
                    if (!reqStop) {
                        reqStop = true;
                        if (recTimerOn) {
                            // カウントダウンタイマー停止
                            recordingTimer.cancel();
                            recTimerOn = false;
                        }
                        // 録音終了
                        voiceRecorder.stopRecording(context, true);
                    }
                } else if (cmd.equalsIgnoreCase(ConstantRecorder.EVENT_CANCEL)) {
                    if (recTimerOn) {
                        // カウントダウンタイマー停止
                        recordingTimer.cancel();
                        recTimerOn = false;
                    }
                    if (recExec) {
                        // 録音キャンセル。
                        voiceRecorder.stopRecording(context, false);
                    }
                    recExec = false;
                    reqStop = false;
                    reqRead = false;
                /* MP3の場合この下の処理は不要 */
                } else if (cmd.equalsIgnoreCase(ConstantRecorder.EVENT_READ)) {
                    if (!reqRead) {
                        reqRead = true;
                        voiceRecorder.readRequest();
                    }
                /* ここまで */
                } else if (cmd.equalsIgnoreCase(ConstantRecorder.EVENT_SHOW)) {
                    VoiceMessage.actMyself.showSendComplete();
                } else if (cmd.equalsIgnoreCase(ConstantRecorder.EVENT_BSCHECK)) {
                    //  基地局との接続を確認する。
                    if (voiceMessageCommon.checkNodeStatus(context)) {
                        // 接続されていれば送信を行う。
                        voiceRecorder.SendWaitMessages(context);
                    } else {
                        // 送信待ちタイマー再設定
                        if (voiceMessageCommon.getSendWaitRecordIdsCount() > 0){
                            voiceRecorder.StartSendWaitTimer();
                        } else {
                            voiceRecorder.CancelSendWaitTimer();
                        }
                    }
                }
            }
        }
    }
}
