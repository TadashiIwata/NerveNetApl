package jp.co.nassua.nervenet.playmessage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import jp.co.nassua.nervenet.playmessage.ConstantPlayMessage;
import jp.co.nassua.nervenet.playmessage.PlayMessageCommon;

/**
 * Created by I.Tadshi on 2016/08/17.
 */
public class PlayMessageNotify extends BroadcastReceiver {
    private static PlayMessageService pMService;
    private static PlayMessageCommon pmc;
    private static String playStat = ConstantPlayMessage.EVENT_INIT;
    private static boolean playflag = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle extras = intent.getExtras();
        String cmd;

        if (pMService == null) {
            pMService = new PlayMessageService();
        }
        if (pmc == null) {
            pmc = new PlayMessageCommon();
        }
        if (action.equalsIgnoreCase(ConstantPlayMessage.ACT_NOTIFY)) {
            if (extras != null) {
                cmd = extras.getString(ConstantPlayMessage.EXTRA_EVENT);
                if (cmd.equalsIgnoreCase(ConstantPlayMessage.EVENT_STOP)) {
                    Log.i("nassus", "PlayMessageNotify Status：Message playback end.");
                    playStat = ConstantPlayMessage.STAT_IDLE;
                    pmc.setPlayStatus(playStat);
                } else if (cmd.equalsIgnoreCase(ConstantPlayMessage.EVENT_PLAY)) {
                    if (!playflag) {
                        pmc.setPlayStatus(playStat);
                        pMService.showPlayToast(context);
                    }
                } else if (cmd.equalsIgnoreCase(ConstantPlayMessage.EVENT_INIT)) {
                    if (playStat.equalsIgnoreCase(ConstantPlayMessage.STATE_INIT)) {
                        playStat = ConstantPlayMessage.STAT_IDLE;
                    }
                } else if (cmd.equalsIgnoreCase(ConstantPlayMessage.EVENT_EXEC)) {
                    pmc.PlayMessageServiceStartNotification(context);
                }
            }
        } else if (action.equalsIgnoreCase(ConstantPlayMessage.ACT_REQUEST)) {
            if (extras != null) {
                cmd = extras.getString(ConstantPlayMessage.EXTRA_EVENT);
                if (cmd.equalsIgnoreCase(ConstantPlayMessage.EVENT_START)) {
                    if (playStat.equalsIgnoreCase(ConstantPlayMessage.STAT_IDLE)) {
                        if (pMService.MessageEntry(context)) {
                            Log.i("nassus", "PlayMessageNotify Status：Message playback start.");
                            playStat = ConstantPlayMessage.STAT_PLAY;
                            pMService.AutoPlay();
                        } else {
                            Log.i("nassus", "PlayMessageNotify Status：Message entry failed.");
                        }
                    } else if (playStat.equalsIgnoreCase(ConstantPlayMessage.STAT_PLAY)) {
                        if (pMService.MessageEntry(context)) {
                            Log.i("nassus", "PlayMessageNotify Status：Message is next entry.");
                        } else {
                            Log.i("nassus", "PlayMessageNotify Status：Message is next entry failed.");
                        }
                    }
                } else if (cmd.equalsIgnoreCase(ConstantPlayMessage.TIMER_START)) {
                    // 再生監視タイマー開始
                    pmc.PlaybackTimerStart();
                } else if (cmd.equalsIgnoreCase(ConstantPlayMessage.TIMER_STOP)) {
                    // 再生監視タイマー停止
                    pmc.PlaybackTimerStop();
                }
            }
        }
    }
}
