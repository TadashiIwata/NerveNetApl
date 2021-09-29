package jp.co.nassua.nervenet.groupchatmain;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import jp.co.nassua.nervenet.groupchatmain.ChatCommon;

public class ChatNotify  extends BroadcastReceiver {
    
    private static ChatCommon chatCommon;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle extras = intent.getExtras();
        String cmd;

        if (chatCommon == null) {
            chatCommon = new ChatCommon();
        }
        if (action.equalsIgnoreCase(ContentChat.ACT_NOTIFY)) {
            // Load完了通知
            if (extras != null) {
                cmd = extras.getString(ContentChat.EXTRA_EVENT);
                if (cmd.equalsIgnoreCase(ContentChat.EVENT_LOAD_COMPLETE)) {
                    chatCommon.refreshview();
                }
            }
        } else if (action.equalsIgnoreCase(ContentChat.ACT_REQUEST)) {
            // Load画面更新要求
            if (extras != null) {
                cmd = extras.getString(ContentChat.EXTRA_EVENT);
                if (cmd.equalsIgnoreCase(ContentChat.EVENT_REQUEST_REFRESH_VIEW)) {
                    chatCommon.refreshview();
                }
            }
        }
    }
}
